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
import sirocco.indexer.LanguageSpecificIndexer;
import sirocco.indexer.dictionaries.StemDictionary;
import sirocco.indexer.util.TextUtils;
import sirocco.model.ContentIndex;
import sirocco.model.ContentWeight;
import sirocco.model.DocParagraph;
import sirocco.model.DocSentence;
import sirocco.model.Document;
import sirocco.model.LabelledPositionsV2;
import sirocco.model.LabelledText;
import sirocco.model.TextTag;
import sirocco.model.TextTagWeightComparer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;

public class NonEnglishIndexer  extends LanguageSpecificIndexer 
{
    private static int LengthOfLongSummary = 200;
    // Length in words. The longer the summary, the more chances it will include a rare word, but also that it will include irrelevant sentences. 1000 chars seem to be about right
    private static int LengthOfShortSummary = 25;
    // Length in words.
    private static int FrequencyTagsQuota = 7;
    private static String SpecialEntitityRegex = "(?\'entity\'[#@])(?\'word\'\\w+)";
    private static float DefaultTagWeight = 1.0F;
    private static int TagMinLength = 8;
    //words up to and including this length will be ignored
    public void index(ContentIndex contentindex) throws Exception {

    	String[] paragraphs = new String[contentindex.ParagraphIndexes.length];
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
            paragraphs[i] = contentindex.ParagraphIndexes[i].OriginalText;
        StructuredSplitter.uncapitalizeShortMessages(paragraphs);
        Document document;
        String[] words;
        RefSupport<Document> refVarDocument = new RefSupport<Document>();
        RefSupport<String[]> refVarWords = new RefSupport<String[]>();
        StructuredSplitter.splitAndTokenize(paragraphs,contentindex.IndexingType,refVarDocument,refVarWords);
        document = refVarDocument.getValue();
        words = refVarWords.getValue();
        String shortsummary;
        RefSupport<String> refVarShortsummary = new RefSupport<String>();
        HashMap<String,TextTag> frequencyStats = calculateFrequencyStats(document, words, refVarShortsummary);
        shortsummary = refVarShortsummary.getValue();
        CSList<TextTag> topTags = getTopTags(ContentIndex.MaxTopTags, frequencyStats);
        contentindex.TopTags = topTags.toArray(new TextTag[topTags.size()]);
        LabelledText ltext = new LabelledText();
        ltext.Text = shortsummary;
        ltext.LabelledPositions = new LabelledPositionsV2();
        contentindex.TopSentiments = new CSList<LabelledText>();
        contentindex.TopSentiments.add(ltext);
    }

    private HashMap<String,TextTag> calculateFrequencyStats(Document document, String[] words, RefSupport<String> summary) throws Exception {
        HashMap<String,HashMap<String,TextTag>> wordLists = new HashMap<String,HashMap<String,TextTag>>();
        summary.setValue(null);
        for (String word : words)
        {
            if (isWordToIgnore(word))
                continue;
             
            Float weight = Character.isUpperCase(word.charAt(0)) ? (ContentWeight.CapitalWords * DefaultTagWeight) : DefaultTagWeight;
            addWordToDictionary(wordLists, word, weight);
        }
        HashMap<String,TextTag> frequencyStatistics = sumUpStemmedWords(wordLists);
        gradeDocument(document, frequencyStatistics);
        selectSentencesForLongSummary(document);
        summary.setValue(writeShortSummary(document));
        HashMap<String,TextTag> result = new HashMap<String,TextTag>();
        extractTagsByFrequency(frequencyStatistics, result);
        return detectSpecificNames(result);
    }

    private HashMap<String,TextTag> detectSpecificNames(HashMap<String,TextTag> tags) throws Exception {
        HashMap<String,TextTag> res = new HashMap<String,TextTag>();
        for (Entry<String,TextTag> word : tags.entrySet())
        {
            String val = word.getValue().getWord();
            if (Character.isUpperCase(val.charAt(0)))
            {
                String lowerVal = val.toLowerCase();
                if (tags.containsKey(lowerVal))
                {
                    TextTag t;
                    if (res.containsKey(lowerVal))
                        t = res.get(lowerVal);
                    else
                        t = tags.get(lowerVal); 
                    t.setWeight(t.getWeight() + word.getValue().getWeight());
                    continue;
                }
                 
            }
             
            res.put(word.getValue().getWord(), word.getValue());
        }
        return res;
    }

    private boolean isWordToIgnore(String word) throws Exception {
        if (TextUtils.isNumeric(word))
            return true;
         
        if (word.length() <= TagMinLength)
            return true;
         
        return false;
    }

    private static String writeShortSummary(Document document) throws Exception {
        StringBuilder sb = new StringBuilder();
        int numwords = 0;
        for (DocSentence docsentence : document.Sentences)
        {
            if (docsentence.Selected)
            {
                if (sb.length() == 0)
                    sb.append(docsentence.OriginalString);
                else
                    sb.append(". " + docsentence.OriginalString); 
            }
             
            numwords += docsentence.Words.size();
            if (numwords > LengthOfShortSummary)
                break;
             
        }
        return sb.toString();
    }

    private static void selectSentencesForLongSummary(Document document) throws Exception {
        int numwords = 0;
        while (numwords < LengthOfLongSummary)
        {
            float maxScore = 0;
            DocSentence maxSentence = null;
            for (int i = 0;i < document.Sentences.size();i++)
            {
                if ((!document.Sentences.get(i).Selected) && (maxScore < document.Sentences.get(i).Score))
                {
                    maxScore = document.Sentences.get(i).Score;
                    maxSentence = document.Sentences.get(i);
                }
                 
                    ;
            }
            if (maxSentence != null)
            {
                numwords += maxSentence.Words.size();
                maxSentence.Selected = true;
            }
            else
                break; 
        }
    }

    private static void gradeDocument(Document document, HashMap<String,TextTag> res) throws Exception {
        for (DocParagraph docparagraph : document.Paragraphs)
        {
            for (DocSentence docsentence : docparagraph.Sentences)
            {
                for (String word : docsentence.Words)
                {
                    TextTag texttag = res.get(word);
                    if (texttag != null)
                    {
                        docsentence.Score += texttag.getWeight();
                    }
                     
                }
                docparagraph.Score += docsentence.Score;
            }
        }
    }

    private void extractTagsByFrequency(HashMap<String,TextTag> frequencyStatistics, HashMap<String,TextTag> result) throws Exception {
        selectTopTags(frequencyStatistics, new TextTagWeightComparer(), FrequencyTagsQuota, result);
    }

    private static void selectTopTags(HashMap<String,TextTag> dictTags, Comparator<TextTag> comparer, int tagsToSelect, HashMap<String,TextTag> result) throws Exception {
        CSList<TextTag> sortedTags = new CSList<TextTag>(dictTags.values().toArray(new TextTag[dictTags.values().size()]));
        Collections.sort(sortedTags,comparer);
        for (int i = 0;(i < tagsToSelect) && (i < sortedTags.size());i++)
        {
            String tag = sortedTags.get(sortedTags.size() - i - 1).getWord();
            float weight = sortedTags.get(sortedTags.size() - i - 1).getWeight();
            Boolean goodAsTopic = sortedTags.get(sortedTags.size() - i - 1).getGoodAsTopic();
            result.put(tag, new TextTag(tag, weight, goodAsTopic));
        }
    }

    public void addWordToDictionary(HashMap<String,HashMap<String,TextTag>> wordLists, String word, float weight) throws Exception {
        boolean useStemmer = false;
        String stemWord = (useStemmer) ? StemDictionary.getInstance().stem(word) : word;
        if (wordLists.containsKey(stemWord))
        {
            if (wordLists.get(stemWord).containsKey(word))
            {
                TextTag t = wordLists.get(stemWord).get(word);
                t.setWeight(t.getWeight() + weight);
            }
            else
            {
                TextTag t = new TextTag(word, weight, true);
                wordLists.get(stemWord).put(word, t);
            } 
        }
        else
        {
            HashMap<String,TextTag> newWord = new HashMap<String,TextTag>();
            newWord.put(word, new TextTag(word, weight, true));
            wordLists.put(stemWord, newWord);
        } 
    }

    public HashMap<String,TextTag> sumUpStemmedWords(HashMap<String,HashMap<String,TextTag>> wordLists) throws Exception {
        HashMap<String,TextTag> res = new HashMap<String,TextTag>();
        for (Entry<String,HashMap<String,TextTag>> tags : wordLists.entrySet())
        {
            TextTag stemTextTag = null;
            float listWeight = 0.0F;
            for (Entry<String,TextTag> word : tags.getValue().entrySet())
            {
                if (word.getValue().getWord().equals(tags.getKey()))
                    stemTextTag = word.getValue();
                else
                {
                    listWeight += word.getValue().getWeight();
                    res.put(word.getKey(), word.getValue());
                } 
            }
            if (stemTextTag != null)
            {
                stemTextTag.setWeight(stemTextTag.getWeight() + listWeight);
                res.put(stemTextTag.getWord(), stemTextTag);
            }
             
        }
        return res;
    }

    public CSList<TextTag> getTopTags(int nTags, HashMap<String,TextTag> tags) throws Exception {
        int resLen = nTags < tags.size() ? nTags : tags.size();
        CSList<TextTag> res = new CSList<TextTag>();
        String tag = null;
        float maxWeight;
        for (int i = 0;i < resLen;i++)
        {
            maxWeight = Float.MIN_VALUE;
            for (TextTag t : tags.values())
            {
                if (t.getWeight() > maxWeight)
                {
                    tag = t.getWord();
                    maxWeight = t.getWeight();
                }
                 
            }
            res.add(new TextTag(tag, maxWeight, true));
            tags.remove(tag);
            if (res.size() > nTags)
                break;
             
        }
        return res;
    }

}


