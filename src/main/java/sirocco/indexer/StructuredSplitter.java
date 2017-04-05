/*******************************************************************************
 * 	Copyright 2008 and onwards Sergei Sokolenko, Alexey Shevchuk, 
 * 	Sergey Shevchook, and Roman Khnykin.
 *
 * 	This product includes software developed at 
 * 	Cuesense 2008-2011 (http://www.cuesense.com/).
 *
 * 	This product includes software developed by
 * 	Sergei Sokolenko (@datancoffee) 2008-2017.
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 		http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 * 	Author(s):
 * 	Sergei Sokolenko (@datancoffee)
 *******************************************************************************/

package sirocco.indexer;

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import CS2JNet.System.StringSplitOptions;
import CS2JNet.System.StringSupport;
import sirocco.indexer.StructuredSplitter;
import sirocco.indexer.dictionaries.LangDictionary;
import sirocco.model.DocParagraph;
import sirocco.model.DocSentence;
import sirocco.model.Document;

import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

public class StructuredSplitter   
{
    private static CSList<Character> SentenceSeparatorsList = new CSList<Character>(LangDictionary.getInstance().getSentenceSeparatorCharacters());
    public static int MaxWordsInEntity = 3;
    // maximum number of words in a capitalized entity
    public static float MaxCapitalizedRatio = 0.66F;
    // 66% of entities (word phrases) can be capitalized to be still considered a content sentence
    //public static int GoodParagraphWindowSize = 10; //
    public static int LengthAvgWindowSize = 5;
    // +/-
    public static float SpikeThreshold = 3.0F;
    public static int MinDistanceToFirstBad = 7;
    // this is driven by our Plain Text algorithm that puts navigational parts on separate lines
    public static int WhiteSpaceMaxCnt = 4;
    public static float SkipInitialRatio = 0.1F;
    public static float InitialGoodParagraphsThreashold = 0.2F;
    
    public static void uncapitalizeShortMessages(String[] paragraphs) throws Exception {
        if (paragraphs.length > 1)
            return ;
         
        // Verify whether this is a short message by looking at sentence separators.
        int firstIdx = StringUtils.indexOfAny(paragraphs[0], LangDictionary.getInstance().getSentenceSeparators());
        
        if ((firstIdx < 0) || (firstIdx == paragraphs[0].length()))
        {
            float firstCapRatio = 0.0F;
            float allCapsRatio = 0.0F;
            RefSupport<Float> refVar0 = new RefSupport<Float>();
            RefSupport<Float> refVar1 = new RefSupport<Float>();
            ratioOfCapitalizedWords(paragraphs[0],refVar0,refVar1);
            allCapsRatio = refVar0.getValue();
            firstCapRatio = refVar1.getValue();
            
            if (Math.max(allCapsRatio,firstCapRatio) > MaxCapitalizedRatio)
                paragraphs[0] = lowercaseCapitalizedWords(paragraphs[0],1);
             
        }
         
    }

    public static String[] splitIntoParagraphs(String text, IndexingConsts.IndexingType indexingType) throws Exception {
        // split in paragraphs first
        String[] delims = new String[]{ "\r\n", "\n" };
        String[] origParagraphs = StringSupport.Split(text, delims, StringSplitOptions.None);
        boolean[] isGoodAsBeginning = new boolean[origParagraphs.length];
        boolean[] isGoodAsInside = new boolean[origParagraphs.length];
        for (int idx = 0;idx < origParagraphs.length;idx++)
        {
            // check every paragraph if it looks like good text
            isGoodAsBeginning[idx] = false;
            isGoodAsInside[idx] = false;
            String paragraph = StringSupport.Trim(origParagraphs[idx]);
            if (StringSupport.isNullOrEmpty(paragraph))
                continue;
             
            if (indexingType == IndexingConsts.IndexingType.TEXT)
            {
                isGoodAsBeginning[idx] = true;
                isGoodAsInside[idx] = true;
            }
            else
            {
                RefSupport<Boolean> refVar2 = new RefSupport<Boolean>();
                RefSupport<Boolean> refVar3 = new RefSupport<Boolean>();
                isGoodParagraph(paragraph,refVar2,refVar3);
                isGoodAsBeginning[idx] = refVar2.getValue();
                isGoodAsInside[idx] = refVar3.getValue();
            } 
        }
        CSList<String> resParagraphs = new CSList<String>();
        if (indexingType == IndexingConsts.IndexingType.TEXT)
        {
            for (int idx = 0;idx < origParagraphs.length;idx++)
                if (isGoodAsInside[idx])
                    resParagraphs.add(origParagraphs[idx]);
                 
        }
        else
        {
            // very often, at the beginning of the page there are a few paragraphs that
            // looks like good, but are still just navogation
            // calculate text density
            int[] lengthOfGood = new int[origParagraphs.length];
            for (int idx = 0;idx < origParagraphs.length;idx++)
                lengthOfGood[idx] = (isGoodAsInside[idx]) ? origParagraphs[idx].length() : 0;
            float[] lengthAvg = new float[origParagraphs.length];
            // calculate the seed avg length for idx = 0
            int idxFrom = 0;
            int idxTo = (LengthAvgWindowSize < origParagraphs.length - 1) ? LengthAvgWindowSize : origParagraphs.length - 1;
            for (int idx = idxFrom;idx <= idxTo;idx++)
                lengthAvg[0] = lengthAvg[0] + lengthOfGood[idx];
            lengthAvg[0] = lengthAvg[0] / (idxTo - idxFrom + 1);
            for (int idx = 1;idx < origParagraphs.length;idx++)
            {
                // calculate the avg length for rest
                lengthAvg[idx] = 0;
                int prevIdxFrom = (idx >= LengthAvgWindowSize + 1) ? (idx - LengthAvgWindowSize - 1) : 0;
                int prevIdxTo = (idx <= origParagraphs.length - LengthAvgWindowSize - 1) ? idx - 1 + LengthAvgWindowSize : origParagraphs.length - 1;
                int prevWindowSize = prevIdxTo - prevIdxFrom + 1;
                int newWindowSize = prevWindowSize;
                float tmp = (lengthAvg[idx - 1] * (float)prevWindowSize);
                if (prevIdxFrom > 0)
                {
                    tmp -= lengthOfGood[prevIdxFrom - 1];
                    newWindowSize--;
                }
                else if ((prevIdxFrom == 0) && (idx == LengthAvgWindowSize + 1))
                {
                    newWindowSize--;
                }
                  
                if (prevIdxTo < origParagraphs.length - 1)
                {
                    tmp += lengthOfGood[prevIdxTo + 1];
                    newWindowSize++;
                }
                 
                lengthAvg[idx] = tmp / newWindowSize;
            }
            // calculate average text density in good as beginning
            int numGoodAsBeginning = 0;
            float avgDensity = 0.0F;
            for (int idx = 0;idx < origParagraphs.length;idx++)
            {
                if (isGoodAsBeginning[idx])
                {
                    numGoodAsBeginning++;
                    avgDensity += lengthAvg[idx];
                }
                 
            }
            avgDensity /= numGoodAsBeginning;
            // declassify all goodAsBeginning that have less than average text density
            boolean bFirstGoodFound = false;
            for (int idx = 0;(idx < origParagraphs.length) && (!bFirstGoodFound);idx++)
            {
                if (isGoodAsBeginning[idx])
                {
                    if (lengthAvg[idx] < avgDensity)
                        isGoodAsBeginning[idx] = false;
                    else
                        bFirstGoodFound = true; 
                }
                 
            }
            // calculate distance to next good "As Beginning"
            int[] distanceToNextGood = new int[origParagraphs.length];
            int idxOfNextGood = Integer.MIN_VALUE;
            for (int idx = 0;idx < origParagraphs.length;idx++)
            {
                if ((idx > 0) && (idx < idxOfNextGood))
                {
                    distanceToNextGood[idx] = distanceToNextGood[idx - 1] - 1;
                    continue;
                }
                 
                idxOfNextGood = Integer.MIN_VALUE;
                distanceToNextGood[idx] = Integer.MAX_VALUE;
                int idx2 = idx + 1;
                while ((idx2 < origParagraphs.length) && (idxOfNextGood == Integer.MIN_VALUE))
                {
                    // calculate distance to next good "As Beginning"
                    if (isGoodAsBeginning[idx2])
                    {
                        distanceToNextGood[idx] = idx2 - idx;
                        idxOfNextGood = idx2;
                    }
                    else
                        idx2++; 
                }
            }
            int startGoodBlock = Integer.MIN_VALUE;
            for (int idx = 1;(idx < origParagraphs.length) && (startGoodBlock == Integer.MIN_VALUE);idx++)
            {
                if (distanceToNextGood[idx] >= distanceToNextGood[idx - 1])
                    startGoodBlock = idx;
                 
            }
            if (startGoodBlock == Integer.MIN_VALUE)
                startGoodBlock = 0;
             
            int endGoodBlock = Integer.MAX_VALUE;
            float rollingAvgInBlock = (float)distanceToNextGood[startGoodBlock];
            for (int idx = startGoodBlock + 1;(idx < origParagraphs.length) && (endGoodBlock == Integer.MAX_VALUE);idx++)
            {
                if ((distanceToNextGood[idx] >= MinDistanceToFirstBad) && ((float)distanceToNextGood[idx]) >= (rollingAvgInBlock * SpikeThreshold))
                    endGoodBlock = idx;
                 
                rollingAvgInBlock = ((rollingAvgInBlock * (idx - startGoodBlock)) + distanceToNextGood[idx]) / (idx - startGoodBlock + 1);
            }
            if (endGoodBlock == Integer.MAX_VALUE)
                endGoodBlock = origParagraphs.length - 1;
             
            for (int idx = startGoodBlock;idx <= endGoodBlock;idx++)
                if (isGoodAsInside[idx])
                    resParagraphs.add(origParagraphs[idx]);
                 
        } 
        return resParagraphs.toArray(new String[]{});
    }


    //good paragraph will have sentences, and no excessive white spaces
    private static void isGoodParagraph(String paragraph, RefSupport<Boolean> isGoodAsBeginning, RefSupport<Boolean> isGoodAsInside) throws Exception {
        Boolean bFoundSep = false;
        Boolean bSepAtEnd = false;
        Boolean bFoundLongWhiteSpaceBlock = false;
        Integer whiteSpaceCnt = 0;
        for (Integer i = 0;i < paragraph.length();i++)
        {
            if (SentenceSeparatorsList.contains(paragraph.charAt(i)))
            {
                whiteSpaceCnt = 0;
                if (i != (paragraph.length() - 1))
                {
                    Character c = paragraph.charAt(i + 1);
                    int cat = Character.getType(c);
                    Boolean isWS = Character.isWhitespace(c);
                    if (isWS || (cat == Character.FINAL_QUOTE_PUNCTUATION) || (cat == Character.END_PUNCTUATION))
                    {
                        bFoundSep = true;
                        bSepAtEnd = false;
                    }
                     
                }
                else
                {
                    // don;t overwrite the bSepAtEnd flag
                    if (!bFoundSep)
                    {
                        bFoundSep = true;
                        bSepAtEnd = true;
                    }
                     
                } 
            }
            else if (Character.isWhitespace(paragraph.charAt(i)))
            {
                whiteSpaceCnt++;
                if (whiteSpaceCnt >= WhiteSpaceMaxCnt)
                    bFoundLongWhiteSpaceBlock = true;
                 
            }
            else
                whiteSpaceCnt = 0;  
        }
        isGoodAsBeginning.setValue((bFoundSep && !bSepAtEnd && !bFoundLongWhiteSpaceBlock));
        isGoodAsInside.setValue((bFoundSep && !bFoundLongWhiteSpaceBlock));
    }


    public static void split(String text, boolean ignoreShortParagraphs, boolean ignoreCapitalizedSentences, Document document, RefSupport<String[]> words) throws Exception {
        words.setValue(null);
    }

    public static void splitAndTokenize(String[] paragraphs, IndexingConsts.IndexingType indexingType, RefSupport<Document> document, RefSupport<String[]> words) throws Exception {
        document.setValue(new Document());
        CSList<String> tags = new CSList<String>();
        for (String paragraph : paragraphs)
        {
            String paragraphwrk = StringSupport.Trim(paragraph);
            if (StringSupport.isNullOrEmpty(paragraphwrk))
                continue;
             
            for (Entry<String,String> s : LangDictionary.getInstance().getSentenceNotSeparators().entrySet())
            {
                // remove all . and ! that are explicitely part of accepted abbreviations
                String safeversion = safeVersion(s.getValue());
                paragraphwrk = paragraph.replace(s.getValue(), safeversion);
            }
            // neutralize all dots which are part of brands like del.icio.us or Last.Fm
            paragraphwrk = neutralizeSentenceSeparators(paragraphwrk);
            String[] sentencearray;
            sentencearray = StringSupport.Split(paragraphwrk, LangDictionary.getInstance().getSentenceSeparators(), StringSplitOptions.RemoveEmptyEntries);
            DocParagraph docparagraph = new DocParagraph();
            document.getValue().Paragraphs.add(docparagraph);
            
            for (String sentence : sentencearray)
            {
                String sentencewrk = StringSupport.Trim(sentence);
                if (StringSupport.isNullOrEmpty(sentencewrk))
                    continue;
                 
                words.setValue(StringSupport.Split(sentencewrk, LangDictionary.getInstance().getWordSeparators(), StringSplitOptions.RemoveEmptyEntries));
                CSList<String> sentenceEntities;
                RefSupport<CSList<String>> refVar4 = new RefSupport<CSList<String>>();
                buildSentenceEntities(words.getValue(),refVar4);
                sentenceEntities = refVar4.getValue();
                
                if (sentenceEntities == null)
                    continue;
                 
                // sometimes there is junk in a sentence
                DocSentence docsentence = new DocSentence(sentencewrk,sentenceEntities);
                document.getValue().addSentence(docparagraph,docsentence);
            }
        }
        
        HashMap<String,Integer> countOfSentences = new HashMap<String,Integer>();
        for (DocSentence docsentence : document.getValue().Sentences)
        {
            Integer count = countOfSentences.get(docsentence.OriginalString);
            if (count!=null)
                countOfSentences.put(docsentence.OriginalString, count + 1);
            else
                countOfSentences.put(docsentence.OriginalString, 1); 
        }
        
        Integer idx = 0;
        while (idx < document.getValue().Sentences.size())
        {
            DocSentence docsentence = document.getValue().Sentences.get(idx);
            if (countOfSentences.get(docsentence.OriginalString) > 3)
            {
                docsentence.Paragraph.Sentences.remove(docsentence);
                document.getValue().Sentences.remove(docsentence);
            }
            else
            {
                if (docsentence.Words != null)
                    tags.addAll(docsentence.Words);
                 
                idx++;
            } 
        }
        words.setValue((tags.toArray(new String[tags.size()])));
    }

    private static void buildSentenceEntities(String[] words, RefSupport<CSList<String>> sentenceEntities) throws Exception {
        if (words.length == 0)
        {
            sentenceEntities.setValue(null);
        }
         
        sentenceEntities.setValue(new CSList<String>());
        CSList<String> entity = new CSList<String>();
        for (Integer i = 0;i < words.length;i++)
        {
            String word = StringSupport.Trim(words[i]);
            if (StringSupport.isNullOrEmpty(word))
                continue;
             
            // flush prev word if current starts with lower case
            if (entity.size() > 0)
            {
                if (!Character.isUpperCase(word.charAt(0)))
                {
                    sentenceEntities.getValue().add(wordsList2Word(entity));
                    entity = new CSList<String>();
                    sentenceEntities.getValue().add(word);
                    continue;
                }
                else
                {
                    if (entity.size() > StructuredSplitter.MaxWordsInEntity)
                    {
                        for (String w : entity)
                            // entity doesn't look like a proper name, flush individual words
                            sentenceEntities.getValue().add(w);
                        entity = new CSList<String>();
                        sentenceEntities.getValue().add(word);
                        continue;
                    }
                     
                    entity.add(word);
                    continue;
                } 
            }
             
            if (Character.isUpperCase(word.charAt(0)))
            {
                // word starts with capital, maybe complex entity
                entity.add(word);
                continue;
            }
             
            sentenceEntities.getValue().add(word);
        }
        // flush last word
        if (entity.size() > 0)
        {
            sentenceEntities.getValue().add(wordsList2Word(entity));
        }
         
    }

    //entity = new List<string>();
    public static void ratioOfCapitalizedWords(String sentence, RefSupport<Float> allCapsRatio, RefSupport<Float> firstCapRatio) throws Exception {
        Integer numFirstCapitalEntities = 0;
        Integer numAllCapitalEntities = 0;
        Integer numLowerCaseEntities = 0;
        Integer idx = 0;
        while ((idx < sentence.length()) && (idx > 0))
        {
            String tailOfsentence = sentence.substring(idx);
        	Integer idxInTail = StringUtils.indexOfAny(tailOfsentence, LangDictionary.getInstance().getWordSeparators());
        	
            if (idxInTail == 0)
            	idx = 0;
            else
            {
            	idx=idx+idxInTail;
                if (Character.isUpperCase(sentence.charAt(idx + 1)))
                {
                    // just check one next character (for performance reasons)
                    if ((idx + 2) < sentence.length())
                        if (Character.isUpperCase(sentence.charAt(idx + 2)))
                            numAllCapitalEntities++;
                        else
                            numFirstCapitalEntities++; 
                    else
                        numFirstCapitalEntities++; 
                }
                else if (Character.isLowerCase(sentence.charAt(idx + 1)))
                	numLowerCaseEntities++;
                  
            }
             
        }
        Integer numAllEntities = numAllCapitalEntities + numFirstCapitalEntities + numLowerCaseEntities;
        if (numAllEntities == 0)
        {
        	firstCapRatio.setValue(0.0F);
            allCapsRatio.setValue(0.0F);
        }
        else
        {
            allCapsRatio.setValue(((float)numAllCapitalEntities / (float)numAllEntities));
            firstCapRatio.setValue(((float)numFirstCapitalEntities / (float)numAllEntities));
        } 
    }

    public static String lowercaseCapitalizedWords(String sentence, int startIdx) throws Exception {
        return sentence.substring(0, (0) + (startIdx)) + sentence.substring(startIdx).toLowerCase();
    }

    public static String safeVersion(String s) throws Exception {
        String safeversion = s;
        for (char c : LangDictionary.getInstance().getSentenceSeparators())
        {
            safeversion = safeversion.replace(String.valueOf(c), "");
        }
        return safeversion;
    }

    public static String neutralizeSentenceSeparators(String data) throws Exception {
        int pos = 0;
        while (pos < data.length())
        {
            int nextpos = data.indexOf('.', pos);
            if ((nextpos == -1) || (nextpos == (data.length() - 1)))
                pos = data.length();
            else
            {
                if (data.charAt(nextpos + 1) != ' ')
                {
                	// replaces: data.Remove(nextpos, 1);
                    data = data.substring(0, nextpos) + data.substring(nextpos+1,data.length());
                    pos = nextpos;
                }
                else
                {
                    pos = nextpos + 1;
                } 
            } 
        }
        return data;
    }

    private static String wordsList2Word(CSList<String> lst) throws Exception {
        String res = "";
        for (int i = 0;i < lst.size();i++)
        {
            if (i > 0)
                res += " ";
             
            res += lst.get(i);
        }
        return res;
    }

}


