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

public class WebResource   
{
	public String webResourceHash; //The ID of the web resource, calculated as to_base64(sha1(Url + PublicationTime))
	@Nullable public String url; // Full URL of the resource, preferably normalized and stripped of unnecessary query parameters
	public Long publicationTime; // When the resource was published. If unable to determine from metadata, ProcessingTime will be used. 
	public Integer publicationDateId; // Integer in the format YYYYMMDD. Also, partition key.
	public Long processingTime; // When we became aware and processed the webresource. Typically only useful if we can't determine 
	public Integer processingDateId; // Integer in the format YYYYMMDD.
	@Nullable public String documentHash; 
	@Nullable public String documentCollectionId; 
	@Nullable public String collectionItemId; 
	@Nullable public String title; // domain of URL
	@Nullable public String domain; // domain of URL
	@Nullable public String author; // Person who claims authorship of the resource, if we can determine it


    public WebResource() {}
    
	public void initialize(String webResourceHash, String url, 
			Long publicationTime, Integer publicationDateId, Long processingTime, Integer processingDateId, 
			String documentHash, String documentCollectionId, String collectionItemId,
			String title, String domain, String author) {
		this.webResourceHash = webResourceHash;
		this.url = url;
		this.publicationTime = publicationTime;
		this.publicationDateId = publicationDateId;
		this.processingTime = processingTime;
		this.processingDateId = processingDateId;
		this.documentHash = documentHash; 
		this.documentCollectionId = documentCollectionId; 
		this.collectionItemId = collectionItemId; 
		this.title = title;
		this.domain = domain;
		this.author = author;

	}    
    
	public void initialize(String url, 
			Long publicationTime, Long processingTime, 
			String documentHash, String documentCollectionId, String collectionItemId,
			String title, String author) {

		// Determine publication time and Date Ids
		if (publicationTime == null)
			publicationTime = processingTime;

		Integer publicationDateId = IdConverterUtils.getDateIdFromTimestamp(publicationTime);
		Integer processingDateId = IdConverterUtils.getDateIdFromTimestamp(processingTime);

		// Determine the ID of the Web Resource
		String webResourceHash = HashUtils.getSHA1HashBase64(publicationTime + url);

		// Determine the domain, if the URL is not empty
		String domain = null;
		if (url != null)
			domain = URLUtils.getDomainName(url);

		this.initialize(webResourceHash, url, 
				publicationTime, publicationDateId, processingTime, processingDateId,
				documentHash, documentCollectionId, collectionItemId,
				title, domain, author);

	}    
    
}


