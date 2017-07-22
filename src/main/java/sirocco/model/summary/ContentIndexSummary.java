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

}


