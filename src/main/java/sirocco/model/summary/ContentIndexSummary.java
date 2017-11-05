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
package sirocco.model.summary;



import java.util.Arrays;

import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.IndexingConsts;
import sirocco.model.LabelledText;
import sirocco.model.TextTag;
import sirocco.util.HashUtils;


public class ContentIndexSummary   
{
	public Document doc;
	public WebResource wr;
	public Sentiment[] sentiments;

    public ContentIndexSummary() {}
    
	public void initialize(String url, Long publicationTime, Long processingTime, 
			String documentCollectionId, String collectionItemId,
			String title, String author, String text,
			IndexingConsts.ContentType contentType, IndexingConsts.ParseDepth contentParseDepth, String language,
			TextTag[] topTags, CSList<LabelledText> topSentiments, String parentUrl, Long parentPubTime) {

		// calculate the Parent Web Resource Hash, if available
		String parentWebResourceHash = ((parentUrl != null && parentPubTime != null)) ? 
			HashUtils.getSHA1HashBase64(parentPubTime + parentUrl) : null;
		
		// Create Document
		this.doc = new Document();
		this.doc.initialize(publicationTime, processingTime, 
				documentCollectionId, collectionItemId,
				title, author, text,
				contentType, contentParseDepth, language,
				topTags);

		// Create the Web Resource
		this.wr = new WebResource();
		this.wr.initialize(url, publicationTime, processingTime, 
				this.doc.documentHash, documentCollectionId,  collectionItemId,
				title, author, parentWebResourceHash);
		
		// Adjust the document record
		this.doc.mainWebResourceHash = this.wr.webResourceHash;
		this.doc.parentWebResourceHash = parentWebResourceHash;
		
		// Create Sentiments
		this.sentiments = new Sentiment[topSentiments.size()];
		for (int i = 0; i < topSentiments.size(); i++) {
			this.sentiments[i] = new Sentiment();
			this.sentiments[i].initialize(this.doc.documentHash,this.doc.publicationTime,
					this.doc.publicationDateId, topSentiments.get(i), topTags,
					this.doc.mainWebResourceHash, this.doc.parentWebResourceHash);
		}

	}
	
	public ContentIndexSummary copy(){
		ContentIndexSummary result = new ContentIndexSummary();
		
		// Create Document
		result.doc = new Document();
		
		DocumentTag[] dTags = new DocumentTag[this.doc.tags.length];
		for (int i = 0; i < this.doc.tags.length; i++) {
			dTags[i] = new DocumentTag();
			dTags[i].initialize(this.doc.tags[i].tag, this.doc.tags[i].weight, this.doc.tags[i].goodAsTopic);
		}
		
		result.doc.initialize(this.doc.documentHash, this.doc.publicationTime, this.doc.publicationDateId,this.doc.processingTime, 
			this.doc.processingDateId, this.doc.documentCollectionId, this.doc.collectionItemId,
			this.doc.title, this.doc.type, this.doc.language, this.doc.contentParseDepth, this.doc.contentLength, 
			this.doc.author, this.doc.text, dTags, this.doc.mainWebResourceHash, this.doc.parentWebResourceHash);

		// Create the Web Resource
		result.wr = new WebResource();
		result.wr.initialize(this.wr.webResourceHash, this.wr.url, 
				this.wr.publicationTime, this.wr.publicationDateId, this.wr.processingTime, this.wr.processingDateId, 
				this.wr.documentHash, this.wr.documentCollectionId, this.wr.collectionItemId,
				this.wr.title, this.wr.domain, this.wr.author, this.wr.parentWebResourceHash);
		
		// Create Sentiments
		result.sentiments = new Sentiment[this.sentiments.length];
		for (int i = 0; i < this.sentiments.length; i++) {
			result.sentiments[i] = new Sentiment();
			
			SentimentTag[] sTags = new SentimentTag[this.sentiments[i].tags.length];
			for (int j = 0; j < this.sentiments[i].tags.length; j++) {
				sTags[j] = new SentimentTag();
				sTags[j].initialize(this.sentiments[i].tags[j].tag, this.sentiments[i].tags[j].goodAsTopic);
			}
			
			String[] sigarray = Arrays.copyOf(this.sentiments[i].signals, this.sentiments[i].signals.length);
			
			result.sentiments[i].initialize(this.sentiments[i].sentimentHash, this.sentiments[i].documentHash, this.sentiments[i].documentTime, this.sentiments[i].documentDateId,
				this.sentiments[i].text, this.sentiments[i].labelledPositions, this.sentiments[i].annotatedText, this.sentiments[i].annotatedHtmlText,
				this.sentiments[i].sentimentTotalScore, this.sentiments[i].dominantValence,
				this.sentiments[i].stAcceptance, this.sentiments[i].stAnger, this.sentiments[i].stAnticipation, this.sentiments[i].stAmbiguous, this.sentiments[i].stDisgust,
				this.sentiments[i].stFear, this.sentiments[i].stGuilt, this.sentiments[i].stInterest, this.sentiments[i].stJoy, this.sentiments[i].stSadness, this.sentiments[i].stShame,
				this.sentiments[i].stSurprise, this.sentiments[i].stPositive, this.sentiments[i].stNegative, this.sentiments[i].stSentiment, this.sentiments[i].stProfane, this.sentiments[i].stUnsafe, 
				this.sentiments[i].mainWebResourceHash, this.sentiments[i].parentWebResourceHash,
				sTags, sigarray);
		}
		
		
		return result;
		
	}

}


