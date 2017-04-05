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

import org.apache.avro.reflect.Nullable;

import sirocco.indexer.IndexingConsts;
import sirocco.model.TextTag;
import sirocco.util.HashUtils;
import sirocco.util.IdConverterUtils;
import sirocco.util.URLUtils;

/** 
 * WebResource corresponds to the GDELT GKG items of SourceCollectionIdentifier type 1. 
 * It represents an article or a shorttext available on the web and accessible through an URL. 
 * If, in the future, we will add support for books or other documents, that are addressible 
 * through other means, we will add another entity.
 *
 */

public class Document   
{
	public String documentHash; 
	public Long publicationTime; // When the resource was published. If unable to determine from metadata, ProcessingTime will be used. 
	public Integer publicationDateId; // Integer in the format YYYYMMDD. Also, partition key.
	public Long processingTime; // When we became aware and processed the webresource. Typically only useful if we can't determine 
	public Integer processingDateId; // Integer in the format YYYYMMDD.
	@Nullable public String documentCollectionId; 
	@Nullable public String collectionItemId; 
	@Nullable public String title; // domain of URL
	public IndexingConsts.ContentType type; // Unknown (0), Shorttext (1) (includes tweets, comments), Article (2) (includes blog posts, news site articles). Only important to distinguish, because people tend to express opinions differently when they have little space.
	@Nullable public String language; // String (EN for english, UN for unknown)
	public IndexingConsts.ParseDepth contentParseDepth; // Unknown (0), Shallow (1), Deep (4); 
	public Integer contentLength; // Length of text in characters.
	@Nullable public String author; // Person who claims authorship of the resource, if we can determine it
	@Nullable public String text;
	@Nullable public DocumentTag[] tags; // Tags of the Web Resource

    public Document() {}
    
	public void initialize(String documentHash, Long publicationTime, Integer publicationDateId, Long processingTime, Integer processingDateId, 
			String documentCollectionId, String collectionItemId,
			String title, IndexingConsts.ContentType type,
			String language, IndexingConsts.ParseDepth contentParseDepth, Integer contentLength, 
			String author, String text,
			DocumentTag[] tags) {
		this.documentHash = documentHash;
		this.publicationTime = publicationTime;
		this.publicationDateId = publicationDateId;
		this.processingTime = processingTime;
		this.processingDateId = processingDateId;
		this.documentCollectionId = documentCollectionId;
		this.collectionItemId = collectionItemId;
		this.title = title;
		this.type = type;
		this.language = language;
		this.contentParseDepth = contentParseDepth;
		this.contentLength = contentLength;
		this.author = author;
		this.text = text;
		this.tags = tags;
	}    
    
	public void initialize(Long publicationTime, Long processingTime, 
			String documentCollectionId, String collectionItemId,
			String title, String author, String text,
			IndexingConsts.ContentType contentType, IndexingConsts.ParseDepth contentParseDepth, String language,
			TextTag[] topTags) {

		// Create tags for Web Resource
		DocumentTag[] dTags = new DocumentTag[topTags.length];
		for (int i = 0; i < topTags.length; i++) {
			dTags[i] = new DocumentTag();
			dTags[i].initialize(topTags[i].getWord(), topTags[i].getWeight(), topTags[i].getGoodAsTopic());
		}

		// Determine content length
		Integer contentLength = text.length();

		// Determine publication time and Date Ids
		if (publicationTime == null)
			publicationTime = processingTime;

		Integer publicationDateId = IdConverterUtils.getDateIdFromTimestamp(publicationTime);
		Integer processingDateId = IdConverterUtils.getDateIdFromTimestamp(processingTime);

		// Determine the ID of Document
		String documentHash = HashUtils.getSHA1HashBase64(publicationTime + text);

		this.initialize(documentHash, publicationTime, publicationDateId, processingTime, processingDateId,
				documentCollectionId, collectionItemId,
				title, contentType, language, contentParseDepth, contentLength, author, text, dTags);

	}    
    
}


