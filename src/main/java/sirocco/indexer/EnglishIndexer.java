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

import CS2JNet.JavaSupport.Collections.Generic.LCC.CollectionSupport;
import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import CS2JNet.System.DoubleSupport;
import CS2JNet.System.StringSupport;
import net.sf.extjwnl.dictionary.morph.Util;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import opennlp.tools.util.Span;
import sirocco.config.ConfigurationManager;
import sirocco.indexer.EnglishTokenizer;
import sirocco.indexer.FloatVector;
import sirocco.indexer.IndexerLabel;
import sirocco.indexer.LanguageSpecificIndexer;
import sirocco.indexer.NounChunkType;
import sirocco.indexer.IndexingConsts.ParseDepth;
import sirocco.indexer.dictionaries.GenericDictionary;
import sirocco.indexer.dictionaries.en.EnglishDictionaries;
import sirocco.indexer.util.LangUtils;
import sirocco.model.ContentIndex;
import sirocco.model.EntityLabelledSpan;
import sirocco.model.EntityScoreComparer;
import sirocco.model.EntityStats;
import sirocco.model.IdiomOccurrence;
import sirocco.model.LabelledPositionsV2;
import sirocco.model.LabelledSentence;
import sirocco.model.LabelledSpan;
import sirocco.model.LabelledText;
import sirocco.model.LabelledTextRelevanceComparer;
import sirocco.model.ParagraphIndex;
import sirocco.model.SentenceFlags;
import sirocco.model.SpanFlags;
import sirocco.model.TextReference;
import sirocco.model.TextStats;
import sirocco.model.TextTag;
import opennlp.tools.chunker.Chunker;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.postag.TagDictionary;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

public class EnglishIndexer  extends LanguageSpecificIndexer 
{
    private String mModelPath;
    private int mBeamSize;
    private EnglishDictionaries mDicts;
    private SentenceDetectorME mSentenceDetector;
    /**
     * Old class: OpenNLP.Tools.Tokenize.EnglishMaximumEntropyTokenizer
     */
    private Tokenizer mTokenizer;
    private POSTagger mPosTagger;
    private TagDictionary mTagDictionary;
    private Chunker mChunker;
    private Parser mParser;
    
    //private OpenNLP.Tools.NameFind.EnglishNameFinder mNameFinder;
    //private OpenNLP.Tools.Lang.English.TreebankLinker mCoreferenceFinder;
    public EnglishIndexer() throws Exception {
        mDicts = new EnglishDictionaries();
        mBeamSize = ConfigurationManager.getConfiguration().getInt("BeamSize");
        InputStream modelStream = null;

        modelStream = getClass().getResourceAsStream("/opennlp15model-sa/en-sent.bin");
		SentenceModel model = new SentenceModel(modelStream);
		mSentenceDetector = new SentenceDetectorME(model);
		modelStream.close();

		modelStream = getClass().getResourceAsStream("/opennlp15model-sa/en-token.bin");
		mTokenizer = new EnglishTokenizer(modelStream, mDicts);
		modelStream.close();
		
		// The parser model is about 15x the size of chunking model. 
		// Keep this in mind when using Deep Parsing.
		modelStream = getClass().getResourceAsStream("/opennlp15model-sa/en-pos-maxent.bin");
		//POSModel posModel = POSTaggerUtils.createPOSModel(modelStream);
		POSModel posModel = new POSModel(modelStream);
		mTagDictionary = posModel.getTagDictionary();
		mPosTagger = new POSTaggerME(posModel);
		modelStream.close();

		modelStream = getClass().getResourceAsStream("/opennlp15model-sa/en-chunker.bin");
		ChunkerModel chunkerModel = new ChunkerModel(modelStream);
		mChunker = new ChunkerME(chunkerModel);
		modelStream.close();
		
		modelStream = getClass().getResourceAsStream("/opennlp15model-sa/en-parser-chunking.bin");
		ParserModel parserModel = new ParserModel(modelStream);
		mParser = ParserFactory.create(parserModel);
		modelStream.close();
		
    }
    
    private static CSList<String> tlADJPParentTypes = new CSList<String>(new String[]{ "NP", "S", "SBAR", "TOP" });
    private static CSList<String> tlEmotionVerbsParentTypes = new CSList<String>(new String[]{ "VP", "S", "SBAR", "TOP" });
    private static CSList<String> tlGoodObjectTypesWithNP = new CSList<String>(new String[]{ "NP", "NN", "NNS", "NNP", "NNPS", "JJ", "JJR", "JJS", "CD" });
    private static CSList<String> tlGoodObjectTypes = new CSList<String>(new String[]{ "NN", "NNS", "NNP", "NNPS", "JJ", "JJR", "JJS", "CD" });
    private static CSList<String> tlNotAGoodSoleObject = new CSList<String>(new String[]{ "CD" });
    private static CSList<String> tlPhraseBreakerTypes = new CSList<String>(new String[]{ "DT", "WDT", "CC", ",", ":" });
    // IN will be ignored in breaking, but will become part of label, and excluded from key
    private static CSList<String> tlGoodNPConnectingPrepositions = new CSList<String>(new String[]{ "of/IN", "for/IN" });
    private static CSList<String> tlGoodProperNounChunkers = new CSList<String>(new String[]{ "and/CC", "of/IN", "for/IN" });
    private static CSList<String> tlSalutations = new CSList<String>(new String[]{ "Dear" });
    private static CSList<String> tlSubChunkBreakerTypes = new CSList<String>(new String[]{ "JJ", "JJR", "JJS", "CD", "POS", "IN" });
    private static CSList<String> tlManualNPFixTypes = new CSList<String>(new String[]{ "NN", "NNS", "JJ", "JJR", "JJS", "NNP", "NNPS" });
    private static CSList<String> tlPOSToSkipWhenFlattening = new CSList<String>(new String[]{ "DT", "POS", "CC" });
    private static CSList<String> tlADJP = new CSList<String>(new String[]{ "ADJP" });
    private static CSList<String> tlCardinal = new CSList<String>(new String[]{ "CD" });
    private static CSList<String> tlAllNouns = new CSList<String>(new String[]{ "NN", "NNS", "NNP", "NNPS" });
    private static CSList<String> tlRegularNouns = new CSList<String>(new String[]{ "NN", "NNS" });
    private static CSList<String> tlProperNouns = new CSList<String>(new String[]{ "NNP", "NNPS" });
    private static CSList<String> tlModalVerbs = new CSList<String>(new String[]{ "MD" });
    private static CSList<String> tlVerbs = new CSList<String>(new String[]{ "VB", "VBD", "VBG", "VBN", "VBP", "VBZ" });
    private static CSList<String> tlAdverbs = new CSList<String>(new String[]{ "RB", "RBR", "RBS" });
    private static CSList<String> tlAdjectives = new CSList<String>(new String[]{ "JJ", "JJR", "JJS" });
    private static CSList<String> tlAdjectivesAndVBN = new CSList<String>(new String[]{ "JJ", "JJR", "JJS", "VBN" });
    private static CSList<String> tlAdjectivesJJR_JJS = new CSList<String>(new String[]{ "JJR", "JJS" });
    private static CSList<String> tlDeepAccumulationBreakParents = new CSList<String>(new String[]{ "VP", "NP", "SINV", "S", "SBAR", "INTJ", "FRAG" });
    private static CSList<String> tlShallowAccumulationBreakParents = new CSList<String>(new String[]{ "VP", "NP", "S", "ADVP", "ADJP" });
    private static CSList<String> tlOrphographyTypes = new CSList<String>(new String[]{ ",", ".", ":" });
    private static CSList<String> tlQuestionSentenceTop = new CSList<String>(new String[]{ "SQ", "SBARQ" });
    private static CSList<String> tlSentenceTop = new CSList<String>(new String[]{ "S", "SBAR", "SINV" });
    private static CSList<String> tlOrdinalSuffixes = new CSList<String>(new String[]{ "ST", "ND", "RD", "TH" });

    // Chunker chunk tag prefixes (B- and I-) are different from Parser chunk tag prefixes (S- and C-)
    private static String CHUNKER_CHUNK_START = "B-";
    private static String CHUNKER_CHUNK_CONT = "I-";
    private static String CHUNKER_CHUNK_OTHER = "O";
    
    
    
    public static int HighValueMinimumLength = 9;
    
    public void index(ContentIndex contentindex) throws Exception {
        contentindex.ActionTimestamps.put("Index:start", Calendar.getInstance().getTime());
        split(contentindex);
        chunk(contentindex);
        findIdioms(contentindex);
        getSentiment(contentindex);
        findGoodEntities(contentindex);
        buildEntitySentimentContext(contentindex);
        calculateEntityScore(contentindex);
        selectTopTags(contentindex);
        buildLabelledSentences(contentindex);
        chunkLabelledSentences(contentindex);
        selectTopSentiments(contentindex);
        contentindex.ActionTimestamps.put("Index:stop", Calendar.getInstance().getTime());
    }

    public Boolean isGoodAsTopic(String tag) throws Exception {
        String[] tokens = tokenizeSentence(tag);
        String[] postags = posTagTokens(tokens);
        CSList<Parse> phrase = new CSList<Parse>();
        for (int idx = 0;idx < tokens.length;idx++)
        {
            Parse parse = new Parse(tokens[idx], new Span(0,tokens[idx].length() - 1), postags[idx], 1.0, 1);
            phrase.add(parse);
        }
        Boolean goodAsTopic;
        Boolean goodAsTag;
        RefSupport<Boolean> refVar0 = new RefSupport<Boolean>();
        RefSupport<Boolean> refVar1 = new RefSupport<Boolean>();
        isHighValueObject(phrase, refVar0, refVar1);
        goodAsTag = refVar0.getValue();
        goodAsTopic = refVar1.getValue();
        return goodAsTopic;
    }

    public void split(ContentIndex contentindex) throws Exception {
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
        {
            contentindex.ParagraphIndexes[i].OriginalSentences = splitIntoSentences(contentindex.ParagraphIndexes[i].OriginalText);
            contentindex.ParagraphIndexes[i].SentenceCount = contentindex.ParagraphIndexes[i].OriginalSentences.length;
        }
    }

    public void chunk(ContentIndex contentindex) throws Exception {
        contentindex.ActionTimestamps.put("Chunk:start", Calendar.getInstance().getTime());
        contentindex.ContentParseDepth = IndexingConsts.ParseDepth.SHALLOW;;
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
        {
            ParagraphIndex pindex = contentindex.ParagraphIndexes[i];
            pindex.SentenceParses = new Parse[pindex.SentenceCount];
            pindex.SentenceFlags = new SentenceFlags[pindex.SentenceCount];
            pindex.ParagraphStats = new TextStats();
            for (int j = 0;j < pindex.SentenceCount;j++)
            {
                pindex.SentenceFlags[j] = new SentenceFlags();
                //replace chars that NLP can;t understand
                String normalizedSentence = normalizeSentence(pindex.OriginalSentences[j]);
                String[] tokens = tokenizeSentence(normalizedSentence);
                // calculate caps and number of entities stats
                pindex.SentenceFlags[j].SentenceStats.calculateSentenceStats(tokens);
                pindex.ParagraphStats.addStats(pindex.SentenceFlags[j].SentenceStats);
                pindex.SentenceFlags[j].ParagraphStats = pindex.ParagraphStats;
                // lower case some tokens to make them work with NLP
                String sentence = null;
                Span[] spans = null;
                String[] fixedtokens = null;
                RefSupport<Span[]> refVar2 = new RefSupport<Span[]>();
                RefSupport<String[]> refVar3 = new RefSupport<String[]>();
                RefSupport<String> refVar4 = new RefSupport<String>();
                fixTokens(tokens,pindex.SentenceFlags[j],refVar2,refVar3,refVar4);
                spans = refVar2.getValue();
                fixedtokens = refVar3.getValue();
                sentence = refVar4.getValue();
                // determine parts of speech
                String[] tags = posTagTokens(fixedtokens);
                fixTags(tokens,tags);
                // chunk words into groups
                String[] chunks = chunkSentence(fixedtokens,tags);
                // fix some combinations
                fixChunks(fixedtokens,tags,chunks);
                pindex.SentenceParses[j] = createParseFromChunks(sentence,fixedtokens,spans,tags,chunks);
            }
        }
        contentindex.ActionTimestamps.put("Chunk:stop", Calendar.getInstance().getTime());
    }

    
    /**
     * Method for Deep Parsing
     * Not currently used because Shallow Parsing (the chunk method) has a much better performance with 
     * acceptable quality loss
     * 
     * @param contentindex
     * @throws Exception
     */
    public void parse(ContentIndex contentindex) throws Exception {
        contentindex.ActionTimestamps.put("Parse:start", Calendar.getInstance().getTime());
        contentindex.ContentParseDepth = IndexingConsts.ParseDepth.DEEP;
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
        {
            ParagraphIndex pindex = contentindex.ParagraphIndexes[i];
            pindex.SentenceParses = new Parse[pindex.SentenceCount];
            for (int j = 0;j < pindex.SentenceCount;j++)
            {
                String normalizedSentence = normalizeSentence(pindex.OriginalSentences[j]);
                pindex.SentenceParses[j] = parseSentence(normalizedSentence);
            }
        }
        contentindex.ActionTimestamps.put("Parse:stop", Calendar.getInstance().getTime());
    }

    public void findIdioms(ContentIndex contentindex) throws Exception {
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
        {
            ParagraphIndex pindex = contentindex.ParagraphIndexes[i];
            for (int j = 0;j < pindex.SentenceCount;j++)
            {
                findIdiomsInSentence(pindex.SentenceParses[j],pindex.SentenceFlags[j]);
            }
        }
    }

    public void getSentiment(ContentIndex contentindex) throws Exception {
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
        {
            ParagraphIndex pindex = contentindex.ParagraphIndexes[i];
            pindex.SentenceSentiments = new FloatVector[pindex.SentenceCount];
            pindex.IndexedSentences = new String[pindex.SentenceCount];
            pindex.SpanMap = (HashMap<String,Span>[]) new HashMap[pindex.SentenceCount];
            for (int j = 0;j < pindex.SentenceCount;j++)
            {
                // if we do chunking instead of parsing, then use Shallow Accumulation Breaks
                RefSupport<FloatVector> refVar5 = new RefSupport<FloatVector>();
                getSentimentVector(pindex.SentenceParses[j],
                		pindex.SentenceFlags[j],contentindex.ContentParseDepth,refVar5);
                pindex.SentenceSentiments[j] = refVar5.getValue();
                RefSupport<String> refVar6 = new RefSupport<String>();
                RefSupport<HashMap<String,Span>> refVar7 = new RefSupport<HashMap<String,Span>>();
                makeIndexedSentence(pindex.SentenceParses[j],
                		pindex.SentenceFlags[j],pindex.SentenceSentiments[j],refVar6,refVar7);
                pindex.IndexedSentences[j] = refVar6.getValue();
                pindex.SpanMap[j] = refVar7.getValue();
            }
        }
    }

    private void findGoodEntities(ContentIndex contentindex) throws Exception {
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
        {
            ParagraphIndex pindex = contentindex.ParagraphIndexes[i];
            for (int j = 0;j < pindex.SentenceCount;j++)
            {
                Parse parse = pindex.SentenceParses[j];
                CSList<CSList<Parse>> goodEntitiesInParse = new CSList<CSList<Parse>>();
                findGoodEntitiesInParses(new CSList<Parse>(parse.getChildren()),pindex.SentenceFlags[j],parse.getType(),goodEntitiesInParse);
                for (CSList<Parse> phrase : goodEntitiesInParse)
                {
                    String phrasestring = goodEntityToKeyString(phrase);
                    Boolean goodAsTag;
                    Boolean goodAsTopic;
                    RefSupport<Boolean> outGoodAsTag = new RefSupport<Boolean>();
                    RefSupport<Boolean> outGoodAsTopic = new RefSupport<Boolean>();
                    isHighValueObject(phrase, outGoodAsTag, outGoodAsTopic);
                    goodAsTag = outGoodAsTag.getValue();
                    goodAsTopic = outGoodAsTopic.getValue();
                    int newStart = pindex.SpanMap[j].get(LangUtils.spanKey(phrase.get(0).getSpan())).getStart();
                    int newEnd = pindex.SpanMap[j].get(LangUtils.spanKey(phrase.get(phrase.size() - 1).getSpan())).getEnd();
                    TextReference tref = new TextReference(i,j,new Span(newStart,newEnd));
                    contentindex.addEntityReference(phrasestring, tref, goodAsTopic, goodAsTag);
                }
                for (Entry<String,SpanFlags> kvp : pindex.SentenceFlags[j].SpanFlags.entrySet())
                {
                    // add hashtags
                    if (kvp.getValue().IsHashtag)
                    {
                    	String spanKey = kvp.getKey();
                        int newStart = pindex.SpanMap[j].get(spanKey).getStart();
                        int newEnd = pindex.SpanMap[j].get(spanKey).getEnd();
                        TextReference tref = new TextReference(i,j,new Span(newStart,newEnd));
                        String intensitytoken = parse.getText().substring(newStart, newEnd);
                        String originaltext = FloatVector.getDimensionValueFromIntensityToken(intensitytoken,FloatVector.OriginalTextDimension);
                        contentindex.addEntityReference(originaltext,tref,null,true); // a Hashtag is a good tag, but an unknown quality topic
                    }
                     
                }
            }
        }
    }

    private void buildEntitySentimentContext(ContentIndex contentindex) throws Exception {
        CSList<String> processedSentences = new CSList<String>();
        for (Entry<String,EntityStats> kvp : contentindex.ContentEntityStats.entrySet())
        {
            for (TextReference tref : kvp.getValue().References)
            {
                String parsenkey = ContentIndex.parSenKey(tref.ParagraphNum,tref.SentenceNum);
                if (!processedSentences.contains(parsenkey))
                {
                    FloatVector sentencesentiment = contentindex.ParagraphIndexes[tref.ParagraphNum].SentenceSentiments[tref.SentenceNum];
                    kvp.getValue().AggregateSentiment.accumulate(sentencesentiment,false);
                    /*addDerivationSteps*/
                    processedSentences.add(parsenkey);
                }
                 
            }
            processedSentences.clear();
        }
    }

    private void calculateEntityScore(ContentIndex contentindex) throws Exception {
        for (Entry<String,EntityStats> kvp : contentindex.ContentEntityStats.entrySet())
        {
            int numref = kvp.getValue().References.size();
            float sentscore = kvp.getValue().AggregateSentiment.sumAllIntensities();
            int length = kvp.getValue().Entity.length();
            kvp.getValue().Score = 1.0F;
            // 1 reference yields factor of 1, 2 references - 1.7, 3 - 2.1
            kvp.getValue().Score *= (float)(1 + Math.log(numref));
            // attention: we add 1 to original score
            // 5 sentiment score - factor 1.4, 50  - 2.8, 250 - 4.3
            if (sentscore != FloatVector.DefaultValue)
                kvp.getValue().Score *= (float)(1 + Math.log(1 + sentscore / FloatVector.InitialValue));
             
            // length of 3 - 0.5, 5 - 0.75, 9 - 1, 15 - 1.5, 25 - 2.0
            if (length >= HighValueMinimumLength)
                kvp.getValue().Score *= (float)(1 + Math.log((float)length / (float)HighValueMinimumLength));
            else
                kvp.getValue().Score *= (float)(1 + Math.log10((float)length / (float)HighValueMinimumLength)); 
        }
    }

    /**
    * Selects top N tags from ContentEntityStats, ignoring already included tags
    */
    private void selectTopTags(ContentIndex contentindex) throws Exception {
    	EntityStats[] statsArray = contentindex.ContentEntityStats.values().toArray(new EntityStats[contentindex.ContentEntityStats.values().size()]);
    	CSList<EntityStats> sorted = new CSList<EntityStats>(statsArray);
        Collections.sort(sorted, new EntityScoreComparer());
        
        contentindex.SortedEntityStats = sorted;
        int tagsnum = (sorted.size() < ContentIndex.MaxTopTags) ? sorted.size() : ContentIndex.MaxTopTags;
        CSList<TextTag> toplist = new CSList<TextTag>();
        int i = 0;
        while ((i < tagsnum) && (i < sorted.size()))
        {
            int idx = sorted.size() - i - 1;
            
            // Don't include tags that are contained as substrings in tags that were already included
            // Example: exclude "fox" if "red fox" is already included
            boolean isAlreadyIncluded = false;
            for (TextTag tag : toplist)
            {
                if (tag.getWord().contains(sorted.get(idx).Entity))
                {
                    isAlreadyIncluded = true;
                    break;
                }
                 
            }
            if (!isAlreadyIncluded)
                toplist.add(new TextTag(sorted.get(idx).Entity,sorted.get(idx).Score,sorted.get(idx).GoodAsTopic));
             
            i++;
        }
        contentindex.TopTags = toplist.toArray(new TextTag[toplist.size()]);
    }

    private void buildLabelledSentences(ContentIndex contentindex) throws Exception {
        // build a map [par,sent] -> Entity References
        contentindex.LabelledSentences = new HashMap<String,LabelledSentence>();
        for (int i = 0;i < contentindex.TopTags.length;i++)
        {
            String entity = contentindex.TopTags[i].getWord();
            EntityStats estats = contentindex.ContentEntityStats.get(entity);
            for (TextReference tref : estats.References)
            {
                String parsenkey = ContentIndex.parSenKey(tref.ParagraphNum,tref.SentenceNum);
                LabelledSentence lsentence = contentindex.LabelledSentences.get(parsenkey);
                
                if (lsentence == null)
                {
                    lsentence = new LabelledSentence();
                    contentindex.LabelledSentences.put(parsenkey, lsentence);
                }
                 
                EntityLabelledSpan lspan = new EntityLabelledSpan(tref.Span.getStart(),tref.Span.getEnd(),IndexerLabel.EntityLabel,entity);
                lsentence.ParSenKey = parsenkey;
                lsentence.LabelledPositions.addEntityLabelledSpan(lspan);
                lsentence.addContainedEntity(entity,i);
            }
        }
        for (int parnum = 0;parnum < contentindex.ParagraphIndexes.length;parnum++)
        {
            for (int sennum = 0;sennum < contentindex.ParagraphIndexes[parnum].SentenceCount;sennum++)
            {
                // add sentiment to above map
                FloatVector sentiment = contentindex.ParagraphIndexes[parnum].SentenceSentiments[sennum];
                if ((sentiment == null) || !sentiment.hasIntensities())
                    continue;
                 
                String parsenkey = ContentIndex.parSenKey(parnum,sennum);
                LabelledSentence lsentence = contentindex.LabelledSentences.get(parsenkey);
                if (lsentence == null)
                {
                    lsentence = new LabelledSentence();
                    contentindex.LabelledSentences.put(parsenkey, lsentence);
                }
                 
                lsentence.ParSenKey = parsenkey;
                lsentence.LabelledPositions.addLabelledSpans(sentiment.getDerivationSpans());
                lsentence.TotalSentimentScore = sentiment.sumAllIntensities();
            }
        }
    }

    private void chunkLabelledSentences(ContentIndex contentindex) throws Exception {
    	
    	if (contentindex.CueType == IndexingConsts.ContentType.UNKNOWN || contentindex.CueType != IndexingConsts.ContentType.SHORTTEXT)
            chunkByCompatibility(contentindex);
        else
            chunkInOne(contentindex); 
    }

    private void chunkInOne(ContentIndex contentindex) throws Exception {
        LabelledText singleChunk = new LabelledText();
        for (int i = 0;i < contentindex.ParagraphIndexes.length;i++)
        {
            ParagraphIndex pindex = contentindex.ParagraphIndexes[i];
            for (int j = 0;j < pindex.SentenceCount;j++)
            {
                String parsenkey = ContentIndex.parSenKey(i,j);
                LabelledSentence lsentence = contentindex.LabelledSentences.get(parsenkey);
                if (lsentence == null)
                {
                    // this must be a sentence without entities or sentiment. still, add it to the big chunk
                    lsentence = new LabelledSentence();
                    lsentence.ParSenKey = parsenkey;
                }
                 
                FloatVector sentiment = contentindex.ParagraphIndexes[i].SentenceSentiments[j];
                singleChunk.addSentence(lsentence,sentiment);
            }
        }
        singleChunk.AggregateSentimentScore = singleChunk.AggregateSentiment.sumAllIntensities();
        contentindex.ChunkedSentences = new CSList<LabelledText>(new LabelledText[]{ singleChunk });
    }

    private void chunkByCompatibility(ContentIndex contentindex) throws Exception {
        CSList<LabelledText> chunks = new CSList<LabelledText>();
        CSList<String> keyssorted = new CSList<String>(CollectionSupport.mk(contentindex.LabelledSentences.keySet()));
        Collections.sort(keyssorted);
        int blockparnum = 0;
        int prevsennum = 0;
        int blockentities = 0;
        LabelledText block = new LabelledText();
        for (String parsenkey : keyssorted)
        {
            int curparnum = 0;
            int cursennum = 0;
            RefSupport<Integer> refVar13 = new RefSupport<Integer>();
            RefSupport<Integer> refVar14 = new RefSupport<Integer>();
            ContentIndex.splitParSenKey(parsenkey,refVar13,refVar14);
            curparnum = refVar13.getValue();
            cursennum = refVar14.getValue();
            int sententities = contentindex.ParagraphIndexes[curparnum].SentenceFlags[cursennum].SentenceStats.NumAllEntities;
            // Check if we need to package accumulated sentences already
            // Target number of entities per quote is 30 (~200 characters)
            if ((blockparnum != curparnum) || (cursennum - prevsennum >= 2) || (blockentities > 30) || ((blockentities > 15) && (blockentities + sententities > 35)))
            {
                if (block.ParSenKeys.size() > 0)
                {
                    block.AggregateSentimentScore = block.AggregateSentiment.sumAllIntensities();
                    chunks.add(block);
                    block = new LabelledText();
                    blockparnum = curparnum;
                    prevsennum = -1;
                    blockentities = 0;
                }
                 
            }
             
            LabelledSentence lsentence = contentindex.LabelledSentences.get(parsenkey);
            FloatVector sentiment = contentindex.ParagraphIndexes[curparnum].SentenceSentiments[cursennum];
            if ((sentiment == null) || !sentiment.hasIntensities())
            {
                // this must be an entity sentence
                block.addSentence(lsentence,null);
                blockentities += sententities;
                prevsennum = cursennum;
            }
            else
            {
                if (block.AggregateSentiment.hasCompatibleValence(sentiment))
                {
                    block.addSentence(lsentence,sentiment);
                    blockentities += sententities;
                    prevsennum = cursennum;
                }
                else
                {
                    if (block.ParSenKeys.size() >= 1)
                    {
                        block.AggregateSentimentScore = block.AggregateSentiment.sumAllIntensities();
                        chunks.add(block);
                        block = new LabelledText();
                        blockentities = 0;
                    }
                     
                    block.addSentence(lsentence,sentiment);
                    prevsennum = cursennum;
                    blockentities += sententities;
                } 
            } 
        }
        if ((block.ParSenKeys.size() >= 1))
        {
            block.AggregateSentimentScore = block.AggregateSentiment.sumAllIntensities();
            chunks.add(block);
        }
         
        contentindex.ChunkedSentences = chunks;
    }

    private void selectTopSentiments(ContentIndex contentindex) throws Exception {
        CSList<LabelledText> sorted = new CSList<LabelledText>(contentindex.ChunkedSentences);
        Collections.sort(sorted, new LabelledTextRelevanceComparer());
        int sentimentnum = (sorted.size() < ContentIndex.MaxTopSentiments) ? sorted.size() : ContentIndex.MaxTopSentiments;
        CSList<LabelledText> toplist = new CSList<LabelledText>();
        for (int i = 0;i < sentimentnum;i++)
        {
            int idx = sorted.size() - i - 1;
            buildText(contentindex,sorted.get(idx));
            toplist.add(sorted.get(idx));
        }
        contentindex.TopSentiments = toplist;
    }

    public void fixTokens(String[] rawtokens, SentenceFlags flags, RefSupport<Span[]> spans, RefSupport<String[]> fixedtokens, RefSupport<String> sentence) throws Exception {
        fixedtokens.setValue(null);
        sentence.setValue(null);
        StringBuilder sb = new StringBuilder();
        CSList<String> fixedtokenlist = new CSList<String>();
        spans.setValue(new Span[rawtokens.length]);
        Integer start = 0;
        for (Integer i = 0;i < rawtokens.length;i++)
        {
            String rawtoken = rawtokens[i];
            String nexttoken = (i < rawtokens.length - 1) ? rawtokens[i + 1] : null;
            Boolean isAllCaps = false, isFirstCap = false, isQuote = false, isLink = false, isHashtag = false;
            RefSupport<Boolean> refVar15 = new RefSupport<Boolean>();
            RefSupport<Boolean> refVar16 = new RefSupport<Boolean>();
            RefSupport<Boolean> refVar17 = new RefSupport<Boolean>();
            RefSupport<Boolean> refVar18 = new RefSupport<Boolean>();
            RefSupport<Boolean> refVar19 = new RefSupport<Boolean>();
            String fixedtoken = fixToken(rawtoken,i,nexttoken,refVar15,refVar16,refVar17,refVar18,refVar19);
            isAllCaps = refVar15.getValue();
            isFirstCap = refVar16.getValue();
            isQuote = refVar17.getValue();
            isLink = refVar18.getValue();
            isHashtag = refVar19.getValue();
            fixedtokenlist.add(fixedtoken);
            sb.append(fixedtoken).append(" ");
            spans.getValue()[i] = new Span(start,start + fixedtoken.length());
            if (!StringSupport.equals(fixedtoken, rawtoken))
                flags.getSpanFlags(spans.getValue()[i]).OriginalText = rawtoken;
             
            if (isLink)
                flags.getSpanFlags(spans.getValue()[i]).IsLink = true;
            else if (isHashtag)
                flags.getSpanFlags(spans.getValue()[i]).IsHashtag = true;
              
            if (isAllCaps)
                flags.getSpanFlags(spans.getValue()[i]).IsAllCaps = true;
            else if (isFirstCap)
                flags.getSpanFlags(spans.getValue()[i]).IsFirstCap = true;
            else if (isQuote)
                flags.Quotes.add(spans.getValue()[i]);
               
            start += fixedtoken.length() + 1;
        }
        if (sb.length() == 0)
            return ;
         
        fixedtokens.setValue((fixedtokenlist.toArray(new String[fixedtokenlist.size()])));
        sentence.setValue(sb.substring(0, (0)+(sb.length() - 1)).toString());
    }

    public String fixToken(String token, int sentenceposition, String nexttoken, RefSupport<Boolean> isAllCaps, RefSupport<Boolean> isFirstCap, RefSupport<Boolean> isQuote, RefSupport<Boolean> isLink, RefSupport<Boolean> isHashtag) throws Exception {
        
    	isAllCaps.setValue(false);
        isFirstCap.setValue(false);
        isQuote.setValue(false);
        isLink.setValue(false);
        isHashtag.setValue(false);
        
        if (token.length() == 1)
        {
            int tokencat = Character.getType(token.charAt(0));
            
            switch (tokencat)
            {
                case Character.START_PUNCTUATION:
                    return "-LRB-";
                case Character.END_PUNCTUATION:
                    return "-RRB-";
                case Character.INITIAL_QUOTE_PUNCTUATION:
                    isQuote.setValue(true);
                    return "\"";
                case Character.FINAL_QUOTE_PUNCTUATION:
                	isQuote.setValue(true);
                    return "\"";
                case Character.DASH_PUNCTUATION:
                    return "-";
            }
                 
            if (token.equals("\""))
            {
                isQuote.setValue(true);
                return "\"";
            }
             
        }
         
        if (FloatVector.isIntensityToken(token))
        {
            FloatVector parsevector = new FloatVector();
            parsevector.initFromIntensityToken(token);
            Float value; 
            
            value = parsevector.get(FloatVector.IsHashTagDimension);
            if (value!=null)
                isHashtag.setValue((value == FloatVector.InitialValue));
            else
                isHashtag.setValue(false); 
            
            value = parsevector.get(FloatVector.IsLinkDimension);
            if (value!=null)
                isLink.setValue((value == FloatVector.InitialValue));
            else
                isLink.setValue(false); 
            return token;
        }
         
        // short tokens are a mixed bag. There are 42 lowercase 2-character words,
        // and about 10 of them have overlaps in meaning
        // AX, ET, DE, ON, OR, US, OH, EN,
        // CA/ca(md) - bad, LA/la(dt) - bad, IT/it(PRP) - bad, AM/am(vb) - bad
        // if (token.Length <= 2) return token;
        // if this exact token has POS tags, return
        if (mTagDictionary.getTags(token) != null)
            return token;
         
        String lower = token.toLowerCase();
        if (mTagDictionary.getTags(lower) == null)
            return token;
         
        // if we found tags for the lower-cased version of the word, then
        // we better use the lower-case version
        if (StringSupport.equals(token, token.toUpperCase()))
        {
            isAllCaps.setValue(true);
            return lower;
        }
         
        if ((sentenceposition == 0) && Character.isUpperCase(token.charAt(0)))
        {
            if ((nexttoken != null) && Character.isUpperCase(nexttoken.charAt(0)))
                return token;
            else
            {
                isFirstCap.setValue(true);
                return lower;
            } 
        }
         
        return token;
    }

    public void fixTags(String[] tokens, String[] tags) throws Exception {
        int idx = 0;
        while (idx < tokens.length)
        {
            if (tokens[idx].length() == 1)
            {
                char c = tokens[idx].charAt(0);
                if (!Character.isLetterOrDigit(c) && !LangUtils.isCharPunctuation(c))
                    tags[idx] = "SYM";
                 
            }
             
            // correct adjectives that are part of NNP phrases
            if (Character.isUpperCase(tokens[idx].charAt(0)))
            {
                if (tlAdjectives.contains(tags[idx]))
                {
                    // correct first adj
                    if (idx == 0)
                    {
                        if (tlSalutations.contains(tokens[idx]))
                            tags[idx] = "UH";
                        else if (tokens.length >= 2 && tlProperNouns.contains(tags[1]))
                            tags[idx] = "NNP";
                          
                    }
                    else if (idx <= tokens.length - 1)
                        tags[idx] = "NNP";
                      
                }
                else if (tlRegularNouns.contains(tags[idx]))
                {
                    if (idx > 0)
                    {
                        tags[idx] = (StringSupport.equals(tags[idx], "NN")) ? "NNP" : "NNPS";
                    }
                     
                }
                  
            }
             
            idx++;
        }
    }

    public void fixChunks(String[] tokens, String[] tags, String[] chunks) throws Exception {
        int idx = 0;
        while (idx < chunks.length)
        {
            // fix case when NP and POS are divided into two different chunks
            if (chunks[idx].startsWith(CHUNKER_CHUNK_START))
            {
                // if the chunk is started by a POS, then add it to previous chunk
                if ((StringSupport.equals(tags[idx], "POS")) && (idx > 0) && (!StringSupport.equals(chunks[idx - 1], CHUNKER_CHUNK_OTHER)))
                {
                    chunks[idx] = CHUNKER_CHUNK_CONT + chunks[idx - 1].substring(2);
                }
                else if (StringSupport.equals(chunks[idx].substring(2), "INTJ"))
                {
                    tags[idx] = "UH";
                }
                else if (StringSupport.equals(chunks[idx].substring(2), "ADVP"))
                {
                    // look ahead for Verbs
                    if ((idx + 1 < chunks.length) && (tlVerbs.contains(tags[idx + 1])))
                    {
                        if (StringSupport.equals(chunks[idx + 1], CHUNKER_CHUNK_OTHER))
                        {
                            chunks[idx] = CHUNKER_CHUNK_START + "VP";
                            chunks[idx + 1] = CHUNKER_CHUNK_CONT + "VP";
                        }
                         
                    }
                    else // look backwards for started verb phrases
                    if ((idx > 0) && (chunks[idx - 1].length() > 2) && (StringSupport.equals(chunks[idx - 1].substring(2), "VP")))
                    {
                        chunks[idx] = CHUNKER_CHUNK_CONT + "VP";
                    }
                    else // look ahead for the start of another another ADVP
                    if ((idx + 1 < chunks.length) && ((StringSupport.equals(chunks[idx + 1], CHUNKER_CHUNK_START + "ADVP"))))
                    {
                        chunks[idx + 1] = CHUNKER_CHUNK_CONT + "ADVP";
                    }
                       
                }
                   
            }
            else if (StringSupport.equals(chunks[idx], CHUNKER_CHUNK_OTHER))
            {
                String plabel = tokens[idx] + '/' + tags[idx];
                if (((StringSupport.equals(tags[idx], "JJ")) || (StringSupport.equals(tags[idx], "VBN"))) && (idx > 0) && (StringSupport.equals(tags[idx - 1], "RB")))
                {
                    if ((StringSupport.equals(chunks[idx - 1], CHUNKER_CHUNK_START + "ADVP")) || (StringSupport.equals(chunks[idx - 1], CHUNKER_CHUNK_OTHER)))
                    {
                        chunks[idx - 1] = CHUNKER_CHUNK_START + "ADJP";
                        chunks[idx] = CHUNKER_CHUNK_CONT + "ADJP";
                    }
                     
                }
                else if (tlManualNPFixTypes.contains(tags[idx]))
                {
                    chunks[idx] = CHUNKER_CHUNK_START + "NP";
                    int idx2 = idx;
                    boolean phrasebreak = false;
                    while (!phrasebreak && (idx2 < (chunks.length - 1)))
                    {
                        idx2++;
                        if ((StringSupport.equals(chunks[idx2], CHUNKER_CHUNK_OTHER)) && tlManualNPFixTypes.contains(tags[idx2]))
                        {
                            chunks[idx2] = CHUNKER_CHUNK_CONT + "NP";
                        }
                        else
                            phrasebreak = true; 
                    }
                    if (phrasebreak)
                        idx = idx2 - 1;
                    else
                        idx = idx2; 
                }
                else if (tlVerbs.contains(tags[idx]))
                {
                    chunks[idx] = CHUNKER_CHUNK_START + "VP";
                }
                else if (tlGoodNPConnectingPrepositions.contains(plabel))
                {
                    if (((idx > 0) && (chunks[idx - 1].length() > 2) && (StringSupport.equals(chunks[idx - 1].substring(2), "NP"))) && ((idx < chunks.length - 1) && (chunks[idx + 1].length() > 2) && (StringSupport.equals(chunks[idx + 1].substring(2), "NP"))) && (tlProperNouns.contains(tags[idx - 1]) && tlProperNouns.contains(tags[idx + 1])))
                    {
                        chunks[idx] = CHUNKER_CHUNK_CONT + "NP";
                        chunks[idx + 1] = CHUNKER_CHUNK_CONT + "NP";
                    }
                     
                }
                    
            }
              
            idx++;
        }
    }

    public Parse createParseFromChunks(String text, String[] tokens, Span[] tokenspans, String[] tags, String[] chunks) throws Exception {
        Parse topParse = new Parse(text, new Span(0,text.length()), AbstractBottomUpParser.TOP_NODE, 1.0, 1);
        Parse sentenceParse = new Parse(text, new Span(0,text.length()), "S", 1.0, 1);
        topParse.insert(sentenceParse);
        CSList<Parse> chunkParses = new CSList<Parse>();
        int start = 0, chunkstart = 0;
        String chunktype = null;
        for (int currentChunk = 0, chunkCount = chunks.length;currentChunk < chunkCount;currentChunk++)
        {
            if (currentChunk > 0 && !chunks[currentChunk].startsWith(CHUNKER_CHUNK_CONT) && !chunks[currentChunk - 1].equals(CHUNKER_CHUNK_OTHER))
                // this indicates end of a chunk, so create a chunk with accumulated parses
                createChunkParse(text,sentenceParse,chunkParses,start,chunkstart,chunktype);
             
            Parse tokenParse = new Parse(text, tokenspans[currentChunk], AbstractBottomUpParser.TOK_NODE, 1.0, 1);
            Parse posParse = new Parse(text, tokenspans[currentChunk], tags[currentChunk], 1.0, 1);
            posParse.insert(tokenParse);
            if (chunks[currentChunk].equals(CHUNKER_CHUNK_OTHER))
            {
                sentenceParse.insert(posParse);
            }
            else if (chunks[currentChunk].startsWith(CHUNKER_CHUNK_START))
            {
                chunktype = chunks[currentChunk].substring(2);
                chunkstart = start;
                chunkParses.add(posParse);
            }
            else if (chunks[currentChunk].startsWith(CHUNKER_CHUNK_CONT))
                chunkParses.add(posParse);
               
            start += tokens[currentChunk].length() + 1;
        }
        if ((!StringSupport.equals(chunks[chunks.length - 1], CHUNKER_CHUNK_OTHER)) && chunkParses.size() > 0)
            createChunkParse(text,sentenceParse,chunkParses,start,chunkstart,chunktype);
         
        return topParse;
    }

    private void createChunkParse(String text, Parse rootParse, CSList<Parse> chunkParses, int start, int chunkstart, String chunktype) throws Exception {
        Span chunkSpan = new Span(chunkstart,start - 1);
        Parse chunkParse = new Parse(text, chunkSpan, chunktype, 1.0, 1);
        for (Parse parse : chunkParses)
            chunkParse.insert(parse);
        chunkParses.clear();
        rootParse.insert(chunkParse);
    }

    public void findIdiomsInSentence(Parse parse, SentenceFlags parseflags) throws Exception {
        Parse[] posnodes = parse.getTagNodes();
        String posedSentence = "";
        HashMap<Integer,Integer> startmarkers = new HashMap<Integer,Integer>();
        for (int i = 0;i < posnodes.length;i++)
        {
            // position in posedSentence -> index in posnodes
            String searchtoken = null;
            if (StringSupport.equals(posnodes[i].getType(), "PRP"))
            {
                searchtoken = "SBJ";
            }
            else if (StringSupport.equals(posnodes[i].getType(), "PRP$"))
            {
                searchtoken = "SBJ$";
            }
            else
            {
                String lemma = getLemma(posnodes[i]);
                String type = posnodes[i].getType();
                if (!tlProperNouns.contains(type))
                    lemma = lemma.toLowerCase();
                 
                if (tlVerbs.contains(type) || tlModalVerbs.contains(type))
                {
                    String shortpos = LangUtils.parseTypeToShortPOS(type);
                    String basetype = LangUtils.baseTypeOfParseType(type);
                    String bestbaseform = mDicts.BaseForms.bestBaseForm(lemma,shortpos);
                    searchtoken = bestbaseform + "/" + basetype;
                }
                else
                    searchtoken = lemma + "/" + type; 
            }  
            if (searchtoken == null)
                continue;
             
            if (StringSupport.equals(posedSentence, ""))
            {
                startmarkers.put(0, i);
                posedSentence = searchtoken;
            }
            else
            {
                startmarkers.put(posedSentence.length() + 1, i);
                posedSentence += " " + searchtoken;
            } 
        }
        int processedToParseIdx = -1;
        for (Entry<Integer,Integer> kvp : startmarkers.entrySet())
        {
            // kvp.Key is position in sentence, and kvp.Value is index in parse list
            if (kvp.getValue() <= processedToParseIdx)
                continue;
             
            String idiom = null;
            String source = null;
            int position = kvp.getKey();
            RefSupport<String> refVar22 = new RefSupport<String>();
            RefSupport<String> refVar23 = new RefSupport<String>();
            mDicts.Idioms.KeyFastIndex.findLongestKeywordAtPosition(posedSentence,position,refVar22,refVar23);
            idiom = refVar22.getValue();
            source = refVar23.getValue();
            if (idiom != null)
            {
                int startidx = kvp.getValue();
                int endidx = startmarkers.get(position + idiom.length() + 1) - 1;
                CSList<Parse> parselist = new CSList<Parse>();
                for (int j = startidx;j <= endidx;j++)
                    parselist.add(posnodes[j]);
                parseflags.addIdiomOccurence(parselist,idiom);
                processedToParseIdx = endidx;
            }
             
        }
    }

    private void buildText(ContentIndex contentindex, LabelledText ltext) throws Exception {
        LabelledPositionsV2 textpositions = new LabelledPositionsV2();
        for (String parsenkey : ltext.ParSenKeys)
        {
            LabelledSentence lsentence = contentindex.LabelledSentences.get(parsenkey);
            String sentence = contentindex.indexedSentenceByParSenKey(parsenkey);
            if (!StringSupport.isNullOrEmpty(ltext.Text))
                ltext.Text += " ";
            else if (ltext.Text == null)
                ltext.Text = "";
              
            int offset = ltext.Text.length();
            ltext.Text += sentence;
            if (lsentence != null)
                textpositions.addPositions(lsentence.LabelledPositions,offset);
             
        }
        ltext.LabelledPositions = textpositions;
    }

    private String goodEntityToKeyString(CSList<Parse> phrase) throws Exception {
        String output = "";
        String separator = " ";
        boolean keepOrigForm = false;
        if (StringSupport.equals(phrase.get(0).getType(), "CD"))
            keepOrigForm = true;
         
        for (Parse item : phrase)
        {
            String lemma = item.toString();
            String[] tokens = StringSupport.Split(lemma, '-');
            if (tokens.length == 2)
            {
                lemma = getLemmaKey(item.getType(),tokens[1],keepOrigForm);
                lemma = tokens[0] + '-' + lemma;
            }
            else if (tokens.length == 1)
            {
                lemma = getLemmaKey(item.getType(),tokens[0],keepOrigForm);
            }
              
            if (StringSupport.equals(output, ""))
                output = lemma;
            else
                output += separator + lemma; 
        }
        return output;
    }

    private String getLemmaKey(String parsetype, String lemma, boolean keepOrigForm) throws Exception {
        String result = lemma;
        if (keepOrigForm)
            return result;
         
        // for plural nouns, use singular
        if (StringSupport.equals(parsetype, "NNS"))
            result = mDicts.BaseForms.bestBaseForm(result,"noun");
         
        // for non-proper nouns and JJs, lower case them
        if ((!StringSupport.equals(parsetype, "NNP")) && (!StringSupport.equals(parsetype, "NNPS")))
            result = result.toLowerCase();
         
        return result;
    }

    private void makeIndexedSentence(Parse parsedSentence, SentenceFlags sentenceflags, FloatVector sentenceSentiment, RefSupport<String> indexedSentence, RefSupport<HashMap<String,Span>> spanmap) throws Exception {
        indexedSentence.setValue(null);
        StringBuilder sb = new StringBuilder();
        Parse[] posnodes = parsedSentence.getTagNodes();
        String[] newtokens = new String[posnodes.length];
        spanmap.setValue(new HashMap<String,Span>());
        for (Integer i = 0;i < posnodes.length;i++)
        {
            String nodetext = posnodes[i].toString();
            String originaltext = null;
            if (FloatVector.isIntensityToken(nodetext))
                originaltext = FloatVector.getDimensionValueFromIntensityToken(nodetext,FloatVector.OriginalTextDimension);
            else if ((originaltext = sentenceflags.getOriginalText(posnodes[i].getSpan())) == null)
                originaltext = nodetext;
              
            // originaltext will be set in IsBrackets
            Boolean addWhitespace = addWhiteSpace(originaltext,posnodes[i].getType());
            if ((sb.length() > 0) && addWhitespace)
                sb.append(' ');
             
            Integer newSpanStart = sb.length();
            sb.append(originaltext);
            Span newSpan = new Span(newSpanStart,sb.length() - 1);
            spanmap.getValue().put(LangUtils.spanKey(posnodes[i].getSpan()), newSpan);
        }
        indexedSentence.setValue(sb.toString());
        if (sentenceSentiment != null)
            sentenceSentiment.remapSpans(spanmap.getValue());
         
    }

    private Boolean addWhiteSpace(String lemma, String parsetype) throws Exception {
        if (StringSupport.equals(parsetype, "POS"))
            return false;
        else if (StringSupport.equals(parsetype, "VBZ") && StringSupport.equals(lemma, "'s"))
            return false;
        else if (StringSupport.equals(parsetype, "RB") && StringSupport.equals(lemma, "n't"))
            return false;
        else if (StringSupport.equals(parsetype, "VBP") && (StringSupport.equals(lemma, "'ve") || StringSupport.equals(lemma, "'m") || StringSupport.equals(lemma, "'re")))
            return false;
        else if (StringSupport.equals(parsetype, "MD") && (StringSupport.equals(lemma, "'ll") || StringSupport.equals(lemma, "'d")))
            return false;
        else if (tlOrphographyTypes.contains(parsetype))
            return false;
        else
            return true;      
    }

    private void getSentimentVector(Parse parse, SentenceFlags sentenceflags, ParseDepth parsedepth, RefSupport<FloatVector> parsevector) throws Exception {
        parsevector.setValue(null);
        if ((parse == null) || (StringSupport.equals(parse.getType(), "INC")) || (StringSupport.equals(parse.getType(), AbstractBottomUpParser.TOK_NODE)))
        {
            return ;
        }
         
        // check for TK: should not happen, as the recursion stops at POS, but be cautious
        // check for null: sometimes the parse tree has null children
        // check for not complete: depending on beam size, sentence sometimes does not get parsed correctly
        Parse curparse = (!StringSupport.equals(parse.getType(), AbstractBottomUpParser.TOP_NODE)) ? parse : parse.getChildren()[0];
        if (curparse.isPosTag())
        {
            RefSupport<FloatVector> refVar25 = new RefSupport<FloatVector>();
            getSentimentVectorFromPOSParse(curparse,sentenceflags,refVar25);
            parsevector.setValue(refVar25.getValue());
        }
        else
        {
            RefSupport<FloatVector> refVar26 = new RefSupport<FloatVector>();
            accumulateVectors(curparse,sentenceflags,new CSList<Parse>(curparse.getChildren()),null,parsedepth,refVar26);
            parsevector.setValue(refVar26.getValue());
            if (parsevector.getValue() != null)
            {
                if (sentenceflags.isInQuotes(curparse.getSpan()))
                    parsevector.getValue().moveAllToAmbiguousSentiment();
                 
                Boolean isQSTop = tlQuestionSentenceTop.contains(curparse.getType());
                Boolean isSTop = tlSentenceTop.contains(curparse.getType());
                if (isQSTop || isTerminatedByQuestion(curparse))
                {
                    parsevector.getValue().moveAllToAmbiguousSentiment();
                }
                else if (isSTop && parsevector.getValue().containsKey(FloatVector.SlyIronicSarcasticDimension))
                {
                    parsevector.getValue().moveAllToAmbiguousSentiment();
                }
                  
                if (isQSTop || isSTop)
                {
                    CSList<LabelledSpan> lspans = parsevector.getValue().getDerivationSpans();
                    for (LabelledSpan lspan : lspans)
                        sentenceflags.getSpanFlags((Span)lspan).IsSentiment = true;
                }
                 
            }
             
        } 
    }

    private void accumulateVectors(Parse parent, SentenceFlags sentenceflags, CSList<Parse> parses, CSList<Parse> parsesToIgnore, ParseDepth parsedepth, RefSupport<FloatVector> listvector) throws Exception {
        listvector.setValue(new FloatVector());
        FloatVector afternegationvector = new FloatVector();
        Integer negationstart = Integer.MAX_VALUE;
        CSList<Parse> plainParses = new CSList<Parse>();
        for (Parse parse : parses)
        {
            // accumulate all chunks
            if ((parsesToIgnore != null) && parsesToIgnore.contains(parse))
                continue;
             
            FloatVector parsevector;
            RefSupport<FloatVector> refVar27 = new RefSupport<FloatVector>();
            getSentimentVector(parse,sentenceflags,parsedepth,refVar27);
            parsevector = refVar27.getValue();
            if (parsevector == null)
            {
                plainParses.add(parse);
            }
            else if ((parsedepth == IndexingConsts.ParseDepth.SHALLOW) && (tlShallowAccumulationBreakParents.contains(parent.getType())))
            {
                Float negationscore = parsevector.get(FloatVector.NegationDimension);
                if (negationstart == Integer.MAX_VALUE && negationscore != null)
                    negationstart = parse.getSpan().getStart();
                 
                if (parse.getSpan().getStart() >= negationstart)
                    afternegationvector.accumulate(parsevector);
                else
                    listvector.getValue().accumulate(parsevector); 
            }
            else
                listvector.getValue().accumulate(parsevector);  
        }
        // special processing for ADJP and ADVP, where certain modifiers
        // make the entire chunk a sentiment
        if (StringSupport.equals(parent.getType(), "ADVP") || StringSupport.equals(parent.getType(), "ADJP"))
        {
            Float modifierscore = listvector.getValue().get(FloatVector.ScoreDimension);
            Float sumallsentiment = listvector.getValue().sumAllSentimentIntensities() + listvector.getValue().sumOfIntensities(SentimentDimension.DimensionsRequiringModifier);
            if ((modifierscore != null) && (sumallsentiment != null))
            {
                for (Parse parse : plainParses)
                {
                    FloatVector parsevector = new FloatVector(parse.getSpan(), SentimentDimension.GeneralSentiment);
                    listvector.getValue().accumulate(parsevector);
                }
            }
             
        }
         
        // add all sentiment idioms of this parent
        if (sentenceflags.isIdiomParent(parent.getSpan()))
        {
            CSList<IdiomOccurrence> idiomoccurences = sentenceflags.SpanFlags.get(LangUtils.spanKey(parent.getSpan())).IncludedIdioms;
            for (IdiomOccurrence idiomoccurence : idiomoccurences)
            {
                FloatVector idiomvector = mDicts.Idioms.words(idiomoccurence.IdiomKey);
                // check: if entity idiom, don't add idiom to sentiment
                if (idiomvector.get(FloatVector.EntityDimension) == FloatVector.InitialValue)
                    continue;
                 
                CSList<Span> spans = parsesToSpans(idiomoccurence.Parses);
                if (spans.get(0).getStart() > negationstart)
                    afternegationvector.accumulate(idiomvector,spans,true);
                else
                    listvector.getValue().accumulate(idiomvector,spans,true); 
            }
        }
         
        if ((parsedepth == IndexingConsts.ParseDepth.DEEP) && (tlDeepAccumulationBreakParents.contains(parent.getType())))
        {
            listvector.getValue().applyNegationAndMultiplication();
        }
        else if ((parsedepth == IndexingConsts.ParseDepth.SHALLOW) && (tlShallowAccumulationBreakParents.contains(parent.getType())))
        {
            afternegationvector.applyNegationAndMultiplication();
            listvector.getValue().applyNegationAndMultiplication();
            listvector.getValue().accumulate(afternegationvector);
        }
          
        if (tlSentenceTop.contains(parent.getType()) || tlQuestionSentenceTop.contains(parent.getType()))
        {
            listvector.getValue().removeUnusedCombinationParts();
        }
         
        if (!listvector.getValue().hasIntensities())
            listvector.setValue(null);
         
    }

    private CSList<Span> parsesToSpans(CSList<Parse> parses) throws Exception {
        if (parses == null)
            return null;
         
        CSList<Span> spans = new CSList<Span>();
        for (Parse parse : parses)
            spans.Add(parse.getSpan());
        return spans;
    }

    private void getSentimentVectorFromPOSParse(Parse parse, SentenceFlags sentenceflags, RefSupport<FloatVector> parsevector) throws Exception {
        RefSupport<FloatVector> refVar30 = new RefSupport<FloatVector>();
        getSentimentVectorFromPOSParse(parse,sentenceflags,null,refVar30);
        parsevector.setValue(refVar30.getValue());
    }

    private void getSentimentVectorFromPOSParse(Parse parse, SentenceFlags sentenceflags, String posoverride, RefSupport<FloatVector> parsevector) throws Exception {
        parsevector.setValue(null);
        if (sentenceflags.isIdiom(parse.getSpan()) || sentenceflags.isLink(parse.getSpan()) || sentenceflags.isHashtag(parse.getSpan()))
            return ;
        else if (tlProperNouns.contains(parse.getType()))
            return ;
          
		// ignore proper nouns during sentiment extraction
		String token = getLemma(parse);
		String pos = (posoverride != null) ? posoverride : LangUtils.parseTypeToShortPOS(parse.getType());
		Span span = parse.getSpan();
		FloatVector outvector = null;
		RefSupport<FloatVector> refOutvector = new RefSupport<FloatVector>();

		if (FloatVector.isIntensityToken(token)) {
			parsevector.setValue(new FloatVector());
			FloatVector emotvector = new FloatVector();
			emotvector.initFromIntensityToken(token);
			parsevector.getValue().accumulate(emotvector, span, true);
		} else if (token.equals("!")) {
			parsevector.setValue(new FloatVector(span, SentimentDimension.GeneralSentiment));
			FloatVector modifierScore = mDicts.Modifiers.getWords().get("exclamation");
			parsevector.getValue().accumulate(modifierScore);
			parsevector.getValue().applyNegationAndMultiplication();
		} else if (isNegator(parse, refOutvector)) {
			outvector = refOutvector.getValue();
			parsevector.setValue(new FloatVector());
			parsevector.getValue().accumulate(outvector, span, true);
		} else if (isPOSModifier(parse, refOutvector)) {
			outvector = refOutvector.getValue();
			parsevector.setValue(new FloatVector());
			parsevector.getValue().accumulate(outvector, span, true);
		} else if (pos.equals("i")) {
			String lower = token.toLowerCase();

			outvector = mDicts.Interjections.getWords().get(lower);

			if ((outvector != null) && (outvector.hasIntensities())) {
				parsevector.setValue(new FloatVector());
				parsevector.getValue().accumulate(outvector, span, true);
			}

		} else {
			parsevector.setValue(getEmotionOrQualityVector(token, pos, span));
		}

        if (sentenceflags.isAllCaps(span) && sentenceflags.ParagraphStats.CapitalizationStandsOut)
        {
            FloatVector allcaps = new FloatVector(span,SentimentDimension.GeneralSentiment);
            FloatVector modifierScore = mDicts.Modifiers.getWords().get("allcaps");
            allcaps.accumulate(modifierScore);
            allcaps.applyNegationAndMultiplication();
            if (parsevector.getValue() == null)
                parsevector.setValue(allcaps);
            else
                parsevector.getValue().accumulate(allcaps); 
        }
         
    }

    private FloatVector getEmotionOrQualityVector(String token, String pos, Span span) throws Exception {
        FloatVector parsevector;
        FloatVector emotvector = getEmotionVector(token,pos);
        FloatVector qualvector = getQualityVector(token,pos);
        if ((emotvector != null) && (qualvector != null))
            throw new Exception("GetEmotionOrQualityVector: duplicate emotion and quality entries for " + token + "/" + pos);
         
        if (emotvector != null)
        {
            if (span != null)
            {
                parsevector = new FloatVector();
                parsevector.accumulate(emotvector,span,true);
            }
            else
                parsevector = emotvector; 
        }
        else if (qualvector != null)
        {
            if (span != null)
            {
                parsevector = new FloatVector();
                parsevector.accumulate(qualvector,span,true);
            }
            else
                parsevector = qualvector; 
        }
        else
            parsevector = null;  
        return parsevector;
    }

    private boolean isPOSModifier(Parse parse, RefSupport<FloatVector> modifier) throws Exception {
        modifier.setValue(null);
        if (!tlAdverbs.contains(parse.getType()) && !tlAdjectivesJJR_JJS.contains(parse.getType()))
            return false;
         
        String lower = getLemma(parse).toLowerCase();
        String pos = LangUtils.parseTypeToShortPOS(parse.getType());
        String label = lower + '/' + pos;
        if (tlAdverbs.contains(parse.getType()))
        {
            modifier.setValue(mDicts.DegreeAdverbs.getWords().get(label));
        }
        else if (tlAdjectivesJJR_JJS.contains(parse.getType()))
        {
            FloatVector baseformvector = getEmotionOrQualityVector(lower,pos,null);
            FloatVector scorevector = mDicts.Modifiers.getWords().get(parse.getType());
            if (baseformvector != null)
            {
            	// sso 12/16/2016 fix
            	FloatVector bfcopy = new FloatVector();
            	bfcopy.accumulate(baseformvector);
                bfcopy.accumulate(scorevector);
                bfcopy.applyNegationAndMultiplication();
                modifier.setValue(bfcopy);
            }
            else
                modifier.setValue(scorevector); 
        }
          
        return (modifier.getValue() != null);
    }

    private Boolean isNegator(Parse parse, RefSupport<FloatVector> negator) throws Exception {
        negator.setValue(null);
        if ((!parse.getType().equals("RB")) && (!parse.getType().equals("DT")) && (!parse.getType().equals("MD")))
            return false;
         
        String parselabel = getParseLabel(parse);
        String lower = parselabel.toLowerCase();
        FloatVector outvalue = mDicts.Negators.getWords().get(lower);
        negator.setValue(outvalue);
        return (negator.getValue() != null);
    }

    private Boolean isTerminatedByQuestion(Parse parse) throws Exception {
        if (parse.getChildCount() == 0)
            return false;
         
        return (StringSupport.equals(getLemma(parse.getChildren()[parse.getChildCount() - 1]), "?"));
    }

    private FloatVector getEmotionVector(String lemma, String pos) throws Exception {
        return getVector(mDicts.Emotions,lemma,pos);
    }

    private FloatVector getQualityVector(String lemma, String pos) throws Exception {
        return getVector(mDicts.Qualities,lemma,pos);
    }

    private FloatVector getVector(GenericDictionary<FloatVector> dict, String lemma, String pos) throws Exception {
        FloatVector vector = dict.getWords().get(lemma + '/' + pos);
        if (vector != null)
            return vector;
         
        // check base forms.
        String lowercaseLemma = lemma.toLowerCase();

        String[] tokens = Util.split(lowercaseLemma);
        if (tokens.length > 2)
            return null;
        else if (tokens.length == 2)
        {
            FloatVector prefixvector = mDicts.Prefixes.getWords().get(tokens[0]);
            if (prefixvector == null)
                return null;
             
            String bestbaseform = mDicts.BaseForms.bestBaseForm(tokens[1],pos);
            vector = dict.getWords().get(bestbaseform + '/' + pos);
            if (vector == null)
                return null;
             
            vector.accumulate(prefixvector);
            vector.applyNegationAndMultiplication();
            return vector;
        }
        else if (tokens.length == 1)
        {
            String bestbaseform = mDicts.BaseForms.bestBaseForm(tokens[0],pos);
            vector = dict.getWords().get(bestbaseform + '/' + pos);
            return vector;
        }
        else
            return null;   
    }

    private static String getParseLabel(Parse parse) throws Exception {
        return getLemma(parse) + '/' + LangUtils.parseTypeToShortPOS(parse.getType());
    }

    private static String getLemma(Parse parse) throws Exception {
    	int start = parse.getSpan().getStart();
    	int end = parse.getSpan().getEnd();
    	String lemma = parse.getText().substring(start,end);
        return lemma;
    }

    private static String getParseLabelPennStyle(Parse parse) throws Exception {
        return getLemma(parse) + '/' + parse.getType();
    }

    private void findGoodEntitiesInParses(CSList<Parse> parses, SentenceFlags sentenceflags, String parentType, CSList<CSList<Parse>> goodEntities) throws Exception {
        CSList<Parse> runningPhrase = new CSList<Parse>();
        for (Parse parse : parses)
        {
            if (parse.isPosTag())
            {
                if (sentenceflags.isIdiom(parse.getSpan()))
                    continue;
                 
                // don't start the running phrase with a DT or similar phrase breakers
                // this check is necessary so that the next check works as intended
                if ((runningPhrase.size() == 0) && !tlGoodObjectTypes.contains(parse.getType()))
                    continue;
                 
                // don't start the running phrase with Sentiment adjectives
                if ((runningPhrase.size() == 0) && (tlAdjectives.contains(parse.getType())) && sentenceflags.isSentiment(parse.getSpan()))
                    continue;
                 
                if (FloatVector.isIntensityToken(getLemma(parse)))
                    continue;
                 
                if (!StringSupport.equals(parentType, "NP"))
                    continue;
                 
                runningPhrase.add(parse);
            }
            else
            {
                if (runningPhrase.size() >= 1)
                {
                    CSList<CSList<Parse>> foundchunks = null;
                    RefSupport<CSList<CSList<Parse>>> refVar41 = new RefSupport<CSList<CSList<Parse>>>();
                    findNounPhraseChunks(runningPhrase,refVar41);
                    foundchunks = refVar41.getValue();
                    goodEntities.addAll(foundchunks);
                    runningPhrase.clear();
                }
                 
                // children
                CSList<CSList<Parse>> childrenGoodEntities = new CSList<CSList<Parse>>();
                CSList<Parse> children = new CSList<Parse>(parse.getChildren());
                findGoodEntitiesInParses(children,sentenceflags,parse.getType(),childrenGoodEntities);
                goodEntities.addAll(childrenGoodEntities);
                //idioms
                findIdiomEntities(parse,sentenceflags,goodEntities);
            } 
        }
        if (runningPhrase.size() > 0)
        {
            CSList<CSList<Parse>> foundchunks = null;
            RefSupport<CSList<CSList<Parse>>> refVar42 = new RefSupport<CSList<CSList<Parse>>>();
            findNounPhraseChunks(runningPhrase,refVar42);
            foundchunks = refVar42.getValue();
            goodEntities.addAll(foundchunks);
        }
         
    }

    private void findNounPhraseChunks(CSList<Parse> parses, RefSupport<CSList<CSList<Parse>>> foundchunks) throws Exception {
        foundchunks.setValue(new CSList<CSList<Parse>>());
        CSList<CSList<Parse>> foundlongchunks = new CSList<CSList<Parse>>();
        NounChunkType nounChunkType = NounChunkType.Undetermined;
        Boolean ignoreChangeOfPhrase = false;
        CSList<Parse> chunk = new CSList<Parse>();
        for (Integer i = 0;i < parses.size();i++)
        {
            Parse parse = parses.get(i);
            String plabel = getParseLabelPennStyle(parse);
            if (tlGoodProperNounChunkers.contains(plabel))
            {
                if ((i > 0) && (i < parses.size() - 1) && tlProperNouns.contains(parses.get(i - 1).getType()) && tlProperNouns.contains(parses.get(i + 1).getType()))
                {
                    chunk.add(parse);
                }
                else if (chunk.size() > 0)
                {
                    if (isHighValueObject(chunk))
                    {
                        CSList<Parse> newlist = new CSList<Parse>(chunk);
                        foundlongchunks.add(newlist);
                    }
                     
                    chunk.clear();
                }
                  
            }
            else if (tlPhraseBreakerTypes.contains(parse.getType()))
            {
                if (chunk.size() > 0)
                {
                    if (isHighValueObject(chunk))
                    {
                        CSList<Parse> newlist = new CSList<Parse>(chunk);
                        foundlongchunks.add(newlist);
                    }
                     
                    chunk.clear();
                }
                 
            }
            else if (StringSupport.equals(parse.getType(), "POS"))
            {
                ignoreChangeOfPhrase = true;
            }
            else if (StringSupport.equals(parse.getType(), "CD"))
            {
                chunk.add(parse);
            }
            else if (tlProperNouns.contains(parse.getType()) || tlRegularNouns.contains(parse.getType()) || tlAdjectives.contains(parse.getType()))
            {
                Boolean isLastParse = (i == (parses.size() - 1));
                RefSupport<NounChunkType> refVar43 = new RefSupport<NounChunkType>(nounChunkType);
                RefSupport<Boolean> refVar44 = new RefSupport<Boolean>(ignoreChangeOfPhrase);
                attachParseToChunk(parse,chunk,foundlongchunks,isLastParse,refVar43,refVar44);
                nounChunkType = refVar43.getValue();
                ignoreChangeOfPhrase = refVar44.getValue();
            }
                 
        }
        if (chunk.size() > 0)
        {
            if (isHighValueObject(chunk))
            {
                CSList<Parse> newlist = new CSList<Parse>(chunk);
                foundlongchunks.add(newlist);
            }
             
        }
         
        for (CSList<Parse> longchunk : foundlongchunks)
        {
            // find smaller sub chunks that correspond to more strict conditions
            // we do that to calculate subchunk stats in larger blogs
            foundchunks.getValue().add(longchunk);
            findSubChunks(longchunk,foundchunks.getValue());
        }
    }

    private void attachParseToChunk(Parse parse, CSList<Parse> chunk, CSList<CSList<Parse>> foundchunks, boolean isLastParse, RefSupport<NounChunkType> nounChunkType, RefSupport<Boolean> ignoreChangeOfPhrase) throws Exception {
        Boolean justAdd;
        if (nounChunkType.getValue() == NounChunkType.Undetermined)
        {
            if (tlProperNouns.contains(parse.getType()))
                nounChunkType.setValue(NounChunkType.ProperNoun);
            else if (tlRegularNouns.contains(parse.getType()))
                nounChunkType.setValue(NounChunkType.RegularNoun);
              
            justAdd = true;
        }
        else
        {
            if (tlProperNouns.contains(parse.getType()))
            {
                justAdd = (nounChunkType.getValue() == NounChunkType.ProperNoun);
                nounChunkType.setValue(NounChunkType.ProperNoun);
            }
            else if (tlRegularNouns.contains(parse.getType()))
            {
                justAdd = (nounChunkType.getValue() == NounChunkType.RegularNoun);
                nounChunkType.setValue(NounChunkType.RegularNoun);
            }
            else
            {
                // e.g. NNP NNP JJ NN: break at JJ
                justAdd = false;
                nounChunkType.setValue(NounChunkType.Undetermined);
            }  
        } 
        // order of conditions is important, so be careful!
        if (chunk.size() == 0)
            chunk.add(parse);
        else if (justAdd)
            chunk.add(parse);
        else if (ignoreChangeOfPhrase.getValue())
        {
            chunk.add(parse);
            ignoreChangeOfPhrase.setValue(false);
        }
        else if (!tlProperNouns.contains(parse.getType()) && isLastParse)
            chunk.add(parse);
        else
        {
            if (isHighValueObject(chunk))
            {
                CSList<Parse> newlist = new CSList<Parse>(chunk);
                foundchunks.add(newlist);
            }
             
            chunk.clear();
            chunk.add(parse);
        }    
    }

    private void findSubChunks(CSList<Parse> longchunk, CSList<CSList<Parse>> foundchunks) throws Exception {
        boolean inProperNounPhrase = false;
        CSList<Parse> subchunk = new CSList<Parse>();
        for (int i = 0;i < longchunk.size();i++)
        {
            Parse parse = longchunk.get(i);
            if (tlSubChunkBreakerTypes.contains(parse.getType()))
            {
                if (subchunk.size() > 0)
                {
                    if (isHighValueObject(subchunk) && (subchunk.size() < longchunk.size()))
                    {
                        CSList<Parse> newlist = new CSList<Parse>(subchunk);
                        foundchunks.add(newlist);
                    }
                     
                    subchunk.clear();
                }
                 
            }
            else if (tlProperNouns.contains(parse.getType()) || tlRegularNouns.contains(parse.getType()))
            {
                RefSupport<Boolean> refVar45 = new RefSupport<Boolean>(inProperNounPhrase);
                attachParseToSubChunk(parse,subchunk,foundchunks,refVar45);
                inProperNounPhrase = refVar45.getValue();
            }
              
        }
        if (subchunk.size() > 0)
        {
            if (isHighValueObject(subchunk) && (subchunk.size() < longchunk.size()))
            {
                CSList<Parse> newlist = new CSList<Parse>(subchunk);
                foundchunks.add(newlist);
            }
             
        }
         
    }

    private void attachParseToSubChunk(Parse parse, CSList<Parse> subchunk, CSList<CSList<Parse>> foundchunks, RefSupport<Boolean> inProperNounPhrase) throws Exception {
        Boolean parseIsProperNoun = tlProperNouns.contains(parse.getType());
        Boolean justAdd = (inProperNounPhrase.getValue() == parseIsProperNoun);
        if (subchunk.size() == 0)
        {
            subchunk.add(parse);
            inProperNounPhrase.setValue(parseIsProperNoun);
        }
        else
        {
            if (justAdd)
                subchunk.add(parse);
            else
            {
                if (isHighValueObject(subchunk))
                {
                    CSList<Parse> newlist = new CSList<Parse>(subchunk);
                    foundchunks.add(newlist);
                }
                 
                subchunk.clear();
                subchunk.add(parse);
                inProperNounPhrase.setValue(parseIsProperNoun);
            } 
        } 
    }

    private void findIdiomEntities(Parse parent, SentenceFlags sentenceflags, CSList<CSList<Parse>> idiomEntities) throws Exception {
        if (!sentenceflags.isIdiomParent(parent.getSpan()))
            return ;
         
        CSList<IdiomOccurrence> idiomoccurences = sentenceflags.SpanFlags.get(LangUtils.spanKey(parent.getSpan())).IncludedIdioms;
        for (IdiomOccurrence idiomoccurence : idiomoccurences)
        {
            FloatVector idiomvector = mDicts.Idioms.words(idiomoccurence.IdiomKey);
            // check: if entity idiom, don't add idiom to sentiment
            if (idiomvector.get(FloatVector.EntityDimension) != FloatVector.InitialValue)
                continue;
             
            if (isHighValueObject(idiomoccurence.Parses))
                idiomEntities.add(idiomoccurence.Parses);
             
        }
    }

    private boolean isHighValueObject(CSList<Parse> phrase) throws Exception {
        boolean goodAsTag;
        Boolean goodAsTopic;
        RefSupport<Boolean> refVar46 = new RefSupport<Boolean>();
        RefSupport<Boolean> refVar47 = new RefSupport<Boolean>();
        isHighValueObject(phrase, refVar46, refVar47);
        goodAsTag = refVar46.getValue();
        goodAsTopic = refVar47.getValue();
        return goodAsTag;
    }

    private void isHighValueObject(CSList<Parse> phrase, RefSupport<Boolean> goodAsTag, RefSupport<Boolean> goodAsTopic) throws Exception {
        goodAsTag.setValue(false);
        goodAsTopic.setValue(null);
        // check if long phrase (3 "good" words are enough,
        // but calling function needs to use POS info to remove DT, POS, etc
        if (phrase.size() >= 3)
        {
            isHighValueObject3PlusParses(phrase, goodAsTag, goodAsTopic);
        }
        else if (phrase.size() == 2)
        {
            isHighValueObject2Parses(phrase.get(0), phrase.get(1), goodAsTag, goodAsTopic);
        }
        else if (phrase.size() == 1)
        {
            isHighValueObject1Parse(phrase.get(0), goodAsTag, goodAsTopic);
        }
           
    }

    private void isHighValueObject1Parse(Parse parse, RefSupport<Boolean> goodAsTag, RefSupport<Boolean> goodAsTopic) throws Exception {
        if (parse.getSpan().getStart() == 0 && isTwitterStyleName(parse))
        {
        	goodAsTag.setValue(false);
            goodAsTopic.setValue(false);
            return ;
        }
         
        if (!tlAllNouns.contains(parse.getType()))
        {
        	goodAsTag.setValue(false);
            goodAsTopic.setValue(false);
            return ;
        }
         
        if (isTwitterStyleName(parse))
        {
            goodAsTopic.setValue(false);
            goodAsTag.setValue(true);
        }
        else // good tag only if it does not start the sentence
        if (tlProperNouns.contains(parse.getType()))
        {
            // single word NNPs are good tags and topics
            goodAsTopic.setValue(true);
            goodAsTag.setValue(true);
        }
        else if (parse.getSpan().length() >= HighValueMinimumLength)
        {
            goodAsTopic.setValue(null);
            // need the admin to decide
            goodAsTag.setValue(true);
        }
        else
        {
            // single words are ok as tags if they are long
            goodAsTopic.setValue(null);
            goodAsTag.setValue(false);
        }   
    }

    private void isHighValueObject2Parses(Parse parse1, Parse parse2, RefSupport<Boolean> goodAsTag, RefSupport<Boolean> goodAsTopic) throws Exception {
        if (parse1.getSpan().getStart() == 0 && isTwitterStyleName(parse1))
        {
        	goodAsTag.setValue(false);
            goodAsTopic.setValue(false);
            return ;
        }
         
        Boolean parse2IsNoun = tlAllNouns.contains(parse2.getType());
        if (StringSupport.equals(parse1.getType(), "CD") && parse2IsNoun)
        {
            goodAsTag.setValue(true);
            goodAsTopic.setValue(false);
        }
        else if (StringSupport.equals(parse1.getType(), "JJ") && isOrdinalWithSuffix(getLemma(parse1)) && parse2IsNoun)
        {
            goodAsTag.setValue(true);
            goodAsTopic.setValue(false);
        }
        else if (parse2IsNoun || tlAllNouns.contains(parse1.getType()))
        {
            // one of them is noun
            Integer length = parse1.getSpan().length() + 1 + parse2.getSpan().length();
            goodAsTag.setValue((length >= HighValueMinimumLength));
            goodAsTopic.setValue(goodAsTag.getValue());
        }
        else
        {
            // no nouns found
        	goodAsTag.setValue(false);
            goodAsTopic.setValue(false);
        }   
    }

    private void isHighValueObject3PlusParses(CSList<Parse> phrase, RefSupport<Boolean> goodAsTag, RefSupport<Boolean> goodAsTopic) throws Exception {
        if (phrase.get(0).getSpan().getStart() == 0 && isTwitterStyleName(phrase.get(0)))
        {
        	goodAsTag.setValue(false);
            goodAsTopic.setValue(false);
            return ;
        }
         
        Boolean foundNoun = false;
        for (Parse parse : phrase)
        {
            // check if there is at least one Noun
            if (tlAllNouns.contains(parse.getType()))
            {
                foundNoun = true;
                break;
            }
             
        }
        goodAsTag.setValue(foundNoun);
        goodAsTopic.setValue(foundNoun);
    }

    /**
     * Checks if the Parse starts with an @
     * @param parse
     * @return
     * @throws Exception
     */
    private Boolean isTwitterStyleName(Parse parse) {
        return (parse.getText().charAt(parse.getSpan().getStart()) == '@');
    }

    private boolean isOrdinalWithSuffix(String candidate) throws Exception {
        RefSupport<Double> ref = new RefSupport<Double>();
        if ((candidate.length() >= 3) && 
        		DoubleSupport.tryParse(candidate.substring(0, (0) + (candidate.length() - 2)), ref) && 
        		tlOrdinalSuffixes.contains(candidate.substring(candidate.length() - 2).toUpperCase()))
        	return true;
        else
        	return false;
    }

    private void findParsesOfTypes(CSList<Parse> parses, CSList<String> types, CSList<Parse> foundParses) throws Exception {
        for (Parse parse : parses)
            if (types.contains(parse.getType()))
                foundParses.add(parse);
             
    }

    private void findImmediateChildren(Parse startNode, CSList<String> types, RefSupport<CSList<Parse>> foundChildren) throws Exception {
        Parse[] children = startNode.getChildren();
        foundChildren.setValue(null);
        for (Parse child : children)
        {
            if (types.contains(child.getType()))
            {
                if (foundChildren.getValue() == null)
                    foundChildren.setValue(new CSList<Parse>());
                 
                foundChildren.getValue().add(child);
            }
             
        }
    }

    private void findChildren(Parse startNode, CSList<String> types, CSList<Parse> foundChildren) throws Exception {
        Parse[] children = startNode.getChildren();
        for (Parse child : children)
        {
            if (types.contains(child.getType()))
            {
                foundChildren.add(child);
            }
            else
                findChildren(child,types,foundChildren); 
        }
    }

    private boolean isAllChildrenPOSTags(Parse startNode) throws Exception {
        Parse[] children = startNode.getChildren();
        for (Parse child : children)
            if (!child.isPosTag())
                return false;
             
        return true;
    }

    private String normalizeSentence(String input) throws Exception {
        String res = StringSupport.Trim(input);
        res = res.replace('’', '\''); // right single quote (U+2019)
        res = res.replace('‘', '\''); // left single quote (U+2018)
        res = res.replace('”', '"'); // right double quote (U+201D)
        res = res.replace('“', '"'); // left double quote (U+201C)
        res = res.replace("‐", " - "); // replace hyphen (U+2010) with (U+002D) Hyphen-Minus
        Character last = res.charAt(res.length() - 1);
        if (!LangUtils.isCharPunctuation(last))
            res += " .";
        
        return res;
    }

    
    
    private void findChildrenFromTo(Parse parse, int spanFrom, int spanTo, CSList<Parse> foundChildren) throws Exception {
        Parse[] children = parse.getChildren();
        if (children == null)
            return ;
         
        for (Parse child : children)
        {
            if (child.isPosTag())
            {
                if ((child.getSpan().getStart() >= spanFrom) && (child.getSpan().getEnd() <= spanTo))
                    foundChildren.add(child);
                 
                if (child.getSpan().getEnd() > spanTo)
                    break;
                 
            }
            else
            {
                if (child.getSpan().getStart() <= spanTo)
                    findChildrenFromTo(child, child.getSpan().getStart(), spanTo, foundChildren);
                else
                    break; 
            } 
        }
    }

    public String[] splitIntoSentences(String paragraph) throws Exception {
        return mSentenceDetector.sentDetect(paragraph);
    }

    private String[] tokenizeSentence(String sentence) throws Exception {
        return mTokenizer.tokenize(sentence);
    }

    private String[] posTagTokens(String[] tokens) throws Exception {
        return mPosTagger.tag(tokens);
    }

    private String[] chunkSentence(String[] tokens, String[] tags) {
        return mChunker.chunk( tokens, tags);
    }

    /**
     * Not currently used, because it is useful only for Deep Parsing.
     * 
     * @param sentence
     * @return
     * @throws Exception
     */
    private Parse parseSentence(String sentence) throws Exception {
        //return mParser.parse(sentence);
    	return null;
    }

}


