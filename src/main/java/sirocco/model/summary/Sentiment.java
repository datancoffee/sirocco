/*******************************************************************************
 * 	Copyright 2008-2011 Sergei Sokolenko, Alexey Shevchuk, 
 * 	Sergey Shevchook, and Roman Khnykin.
 *
 * 	Copyright 2011 and onwards Sergei Sokolenko (@datancoffee).
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
package sirocco.model.summary;

import org.apache.avro.reflect.Nullable;

import sirocco.indexer.IndexingConsts;
import sirocco.indexer.SentimentDimension;
import sirocco.indexer.util.LangUtils;
import sirocco.model.LabelledText;
import sirocco.model.TextTag;
import sirocco.model.summary.SentimentTag;
import sirocco.util.HashUtils;
import sirocco.annotators.HtmlTextAnnotator;
import sirocco.annotators.ExtendedLogTextAnnotator;


public class Sentiment   
{
	public String sentimentHash; //ID of the Sentiment record, calculated as to_base64(sha1(text + web resource UrlHash)).
	public String documentHash; // ID of the containing document. For WebResources, it's WebResourceHash
	public Long documentTime; // Timestamp of the document creation. Same as PublicationTime in WebResource
	public Integer documentDateId; // Date of the document creation. Also, partition date for this table. Same as PublicationDateId in WebResource
	public String text; // The text of the sentiment.
	@Nullable public String labelledPositions; // Serialized list of positions in text labeled with emotions and entities
	@Nullable public String annotatedText; // text annotated with compact labels of sentiments and entities
	@Nullable public String annotatedHtmlText; // text annotated with html
	
	public Integer sentimentTotalScore;
	public IndexingConsts.SentimentValence dominantValence; // 0: positive, 1:negative, 2:ambiguous, 3:zero, 4: general

    // Sentiments vector
	public Integer stAcceptance; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stAnger; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stAnticipation; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stAmbiguous; // // Score (normalized to 10). True sentiment is difficult to determine due to e..g sarcasm or irony
	public Integer stDisgust; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stFear; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stGuilt; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stInterest; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stJoy; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stSadness; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stShame; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stSurprise; // Score (normalized to 10) for one of Plutchik's sentiment dimensions.
	public Integer stPositive; 
	public Integer stNegative;
	public Integer stSentiment;
	public Integer stProfane; 
	public Integer stUnsafe;

	@Nullable public SentimentTag[] tags; 
	
    public Sentiment() {
    }
    
    public void initialize(String documentHash, Long documentTime, Integer documentDateId, LabelledText lt, TextTag[] wrTags) {

    	this.documentHash = documentHash;
    	this.documentTime = documentTime;
    	this.documentDateId = documentDateId;
    	
    	this.text = lt.Text; 
    	this.labelledPositions = lt.LabelledPositions.stringSerialize();
    	
    	HtmlTextAnnotator htmlAnnotator = new HtmlTextAnnotator();
    	ExtendedLogTextAnnotator textAnnotator = new ExtendedLogTextAnnotator();
    	
    	if (lt.LabelledPositions != null) {
    		try {
				this.annotatedHtmlText = htmlAnnotator.annotate(lt.LabelledPositions, lt.Text);
				this.annotatedText = textAnnotator.annotate(lt.LabelledPositions, lt.Text);
			} catch (Exception e) {
				// TODO: report to LOGS
			}
    	}
    	
    	this.sentimentHash = HashUtils.getSHA1HashBase64(this.documentTime + this.text); 
    	
    	this.sentimentTotalScore = Math.round(lt.AggregateSentimentScore);
    	this.dominantValence = lt.AggregateSentiment.dominantValence();

        this.stAcceptance = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Acceptance);
        this.stAnger = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Anger);
        this.stAnticipation = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Anticipation);
        this.stAmbiguous = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Ambiguous);
        this.stDisgust = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Disgust);
        this.stFear = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Fear);
        this.stGuilt = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Guilt);
        this.stInterest = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Interest);
        this.stJoy = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Joy);
        this.stSadness = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Sadness);
        this.stShame = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Shame);
        this.stSurprise = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Surprise);
        this.stPositive = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Positive);
        this.stNegative = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Negative);
        this.stSentiment = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.GeneralSentiment);
        this.stProfane = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Profane);
        this.stUnsafe = LangUtils.getIntValue(lt.AggregateSentiment,SentimentDimension.Unsafe);
        
        this.tags = new SentimentTag[lt.ContainedEntities.size()];
        
        for (int i = 0; i < lt.ContainedEntities.size(); i++)
        {
        	String entity = lt.ContainedEntities.get(i);
        	
            for (int j = 0; j < wrTags.length; j++) {
            	if (wrTags[j].getWord().equals(entity)) {
    				this.tags[i] = new SentimentTag();
    				this.tags[i].initialize(entity, wrTags[j].getGoodAsTopic());
    				break;
            	}
            }
        }
    	
    }
    

}


