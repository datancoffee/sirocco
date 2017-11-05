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

package sirocco.model;

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import CS2JNet.System.StringSupport;
import opennlp.tools.parser.Parse;
import sirocco.annotators.BriefLogTextAnnotator;
import sirocco.annotators.ExtendedLogTextAnnotator;
import sirocco.indexer.FloatVector;
import sirocco.indexer.IndexingConsts;
import sirocco.indexer.util.LangUtils;
import sirocco.model.summary.ContentIndexSummary;

import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;


public class ContentIndex   
{
	/****************/
	
    public final static int MaxTopSentiments = 4;
    
    public final static int MaxTopTags = 7;
    /**
     * Measured in characters
     */
    public final static int ShortSummaryTargetLength = 150;

    /**
     * Padding for ParSen keys. Enough padding for 999 paragraphs, 999 sentences texts.
     */
    public final static int ParSenPadding = 3;

    /****************/
    /* Inputs into indexing */
    
    /**
     * OriginalText (required): text passed for indexing
     */
    public String OriginalText;
    /**
     * IndexingType (required): Full Index or just top sentiments
     */
    public IndexingConsts.IndexingType IndexingType;
    /**
     * ContentType (required): Is it shorttext or article, is it plain text extract of a web page, or is it a regular text
     */    
    public IndexingConsts.ContentType ContentType;
    /**
     * ProcessingTime (required)
     */    
    public Long ProcessingTime; 

    public String Url;
    
    public Long PublicationTime;
    
    public String Title;
    
    public String Author;
    
    public String DocumentCollectionId;
    
    public String CollectionItemId;
    
    public String ParentUrl;
    
    public Long ParentPubTime;
    
    /****************/
    /* Intermediate results of indexing */
    public String Language;
    public IndexingConsts.ParseDepth ContentParseDepth = IndexingConsts.ParseDepth.SHALLOW;
    public ParagraphIndex[] ParagraphIndexes;
    public HashMap<String,EntityStats> ContentEntityStats = new HashMap<String,EntityStats>();
    public CSList<EntityStats> SortedEntityStats;
    public HashMap<String,LabelledSentence> LabelledSentences;
    public CSList<LabelledText> ChunkedSentences;
    
    /****************/
    /* Final results of indexing */
    public TextTag[] TopTags;
    public CSList<LabelledText> SelectedSentiments;
    public Boolean IsIndexingSuccessful = false;
    public String IndexingErrors;
    
    /**
     * Temporary performance stats.
     */
    public HashMap<String,Date> ActionTimestamps = new HashMap<String,Date>();
    
    public ContentIndex() {}
    
    public ContentIndex(String content, IndexingConsts.IndexingType indexingType, 
    		IndexingConsts.ContentType cueType, Long processingTime)  {
        this(content, indexingType, cueType,processingTime, null,null,null,null, null, null, null, null);
    }
    
    public ContentIndex(String content, IndexingConsts.IndexingType indexingType, 
    		IndexingConsts.ContentType cueType, Long processingTime,
    	    String url, Long publicationTime,  String title, String author, 
    	    String documentCollectionId, String collectionItemId,
    	    String parentUrl, Long parentPubTime)  {
        this.OriginalText = content;
        this.IndexingType = indexingType;
        this.ContentType = cueType;
        this.ProcessingTime = processingTime; 
        this.Url = url;
        this.PublicationTime = publicationTime;
        this.Title = title;
        this.Author = author;
        this.DocumentCollectionId = documentCollectionId;
        this.CollectionItemId = collectionItemId;
        this.ParentUrl = parentUrl;
        this.ParentPubTime = parentPubTime;
    }

    /**
     * Should be called on the final content index, after the .index operation has been executed.
     * @return The final content index with just the important text stats
     */
    public ContentIndexSummary getContentIndexSummary()
    {
    	ContentIndexSummary summary = new ContentIndexSummary();
    	summary.initialize(this.Url, this.PublicationTime, this.ProcessingTime, 
    		this.DocumentCollectionId, this.CollectionItemId,
    		this.Title, this.Author, 
    		this.OriginalText, this.ContentType, this.ContentParseDepth, this.Language, this.TopTags, 
    		this.SelectedSentiments, this.ParentUrl, this.ParentPubTime);

    	return summary;
    }
    
    public void initializeParagraphs(String[] paragraphs) throws Exception {
        ParagraphIndexes = new ParagraphIndex[paragraphs.length];
        for (int i = 0;i < paragraphs.length;i++)
        {
            ParagraphIndexes[i] = new ParagraphIndex();
            ParagraphIndexes[i].OriginalText = paragraphs[i];
        }
    }

    public void addEntityReference(String entity, TextReference tref, Boolean goodAsTopic, Boolean goodAsTag) throws Exception {
        EntityStats estats = this.ContentEntityStats.get(entity);
        if (estats == null)
            ContentEntityStats.put(entity, new EntityStats(entity));
         
        ContentEntityStats.get(entity).References.add(tref);
        ContentEntityStats.get(entity).GoodAsTopic = goodAsTopic;
        ContentEntityStats.get(entity).GoodAsTag = goodAsTag;
    }

    public static void splitParSenKey(String key, RefSupport<Integer> parnum, RefSupport<Integer> sennum) {
        String[] parts = StringSupport.Split(key, '/');
        
        try {
        	parnum.setValue(Integer.parseInt(parts[0]));
        	sennum.setValue(Integer.parseInt(parts[1]));
        } catch (Exception e) {
        	parnum.setValue(null);
        	sennum.setValue(null);
        }
    }

    public static String parSenKey(int parnum, int sennum) throws Exception {
        return StringSupport.PadLeft(Integer.toString(parnum), 3, '0') + "/" + StringSupport.PadLeft(Integer.toString(sennum), 3, '0');
    }

    public String indexedSentenceByParSenKey(String parsenkey) throws Exception {
        int parnum;
        int sennum;
        RefSupport<Integer> refVar___3 = new RefSupport<Integer>();
        RefSupport<Integer> refVar___4 = new RefSupport<Integer>();
        splitParSenKey(parsenkey,refVar___3,refVar___4);
        parnum = refVar___3.getValue();
        sennum = refVar___4.getValue();
        return ParagraphIndexes[parnum].IndexedSentences[sennum];
    }

    // for debug only
    public void getLabelledSentences(CSList<String> parsenkeys, RefSupport<String> allWOLabels, RefSupport<String> allWithLabels) throws Exception {
        StringBuilder sbWOLabels = new StringBuilder();
        StringBuilder sbWithLabels = new StringBuilder();
        BriefLogTextAnnotator annotatorWOLabels = new BriefLogTextAnnotator();
        ExtendedLogTextAnnotator annotatorWithLabels = new ExtendedLogTextAnnotator();
        for (String parsenkey : parsenkeys)
        {
            Integer parnum;
            Integer sennum;
            RefSupport<Integer> refVar___5 = new RefSupport<Integer>();
            RefSupport<Integer> refVar___6 = new RefSupport<Integer>();
            ContentIndex.splitParSenKey(parsenkey,refVar___5,refVar___6);
            parnum = refVar___5.getValue();
            sennum = refVar___6.getValue();
            String sentence = ParagraphIndexes[parnum].IndexedSentences[sennum];
            LabelledSentence lsentence = LabelledSentences.get(parsenkey);
            String woLabels = annotatorWOLabels.annotate(lsentence.LabelledPositions,sentence);
            String withLabels = annotatorWithLabels.annotate(lsentence.LabelledPositions,sentence);
            if (sbWOLabels.length() > 0)
                sbWOLabels.append(" ");
             
            sbWOLabels.append(woLabels);
            if (sbWithLabels.length() > 0)
                sbWithLabels.append(" ");
             
            sbWithLabels.append(withLabels);
        }
        allWOLabels.setValue(sbWOLabels.toString());
        allWithLabels.setValue(sbWithLabels.toString());
    }

    // for debug only
    public String getFullAnnotatedText(boolean withLabels) throws Exception {
        if (ParagraphIndexes == null)
            return null;
         
        StringBuilder sb = new StringBuilder();
        ExtendedLogTextAnnotator annotator = new ExtendedLogTextAnnotator();
        for (int i = 0;i < ParagraphIndexes.length;i++)
        {
            for (int j = 0;j < ParagraphIndexes[i].SentenceCount;j++)
            {
                String parsenkey = parSenKey(i,j);
                String isentence = ParagraphIndexes[i].IndexedSentences[j];
                String sentence = null;
                LabelledSentence lsentence = LabelledSentences.get(parsenkey);
                if (lsentence != null)
                {
                    sentence = annotator.annotate(lsentence.LabelledPositions,isentence);
                }
                else
                    sentence = isentence; 
                sb.append(sentence).append(" ");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public CSList<String> getLinks() throws Exception {
        CSList<String> links = new CSList<String>();
        for (int i = 0;i < this.ParagraphIndexes.length;i++)
        {
            ParagraphIndex pindex = this.ParagraphIndexes[i];
            for (int j = 0;j < pindex.SentenceCount;j++)
            {
                Parse parse = pindex.SentenceParses[j];
                for (Entry<String,SpanFlags> kvp : pindex.SentenceFlags[j].SpanFlags.entrySet())
                {
                    if (kvp.getValue().IsLink)
                    {
                    	String spanKey = kvp.getKey();
                    	int start = LangUtils.spanStartFromKey(spanKey);
                    	int end = LangUtils.spanEndFromKey(spanKey);

                        String intensitytoken = parse.getText().substring(start,end);
                        String link = FloatVector.getDimensionValueFromIntensityToken(intensitytoken,FloatVector.OriginalTextDimension);
                        links.add(link);
                    }
                     
                }
            }
        }
        return links;
    }

}


