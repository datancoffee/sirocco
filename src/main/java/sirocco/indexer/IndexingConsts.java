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

import CS2JNet.System.Collections.LCC.CSList;

public class IndexingConsts
{
	
	public enum ParseDepth
	{
		UNKNOWN (0),
	    SHALLOW (1),
	    DEEP (4);
		
	    private final int val;
		
	    ParseDepth(int v) {
	        this.val = v;
	    }   	
	}	

	/**
	 * Instructions what the output of the indexing should be
	 * @author sezok
	 *
	 */
	public enum IndexingType
	{
	    TOPSENTIMENTS (0),
	    FULLINDEX (1);
	
	    private final int val;
	
	    IndexingType(int v) {
	        this.val = v;
	    }   	
	
	}

	/**
	 * Content type can be a predictor of how people write sentences, for example, don't 
	 * use proper grammar.
	 * 
	 * This enum is a combination of length/grammar (shorttext, article) and extraction 
	 * context (from a webpage, where it can be surrounded by "bad" navigational paragraphs 
	 * 
	 * @author sezok
	 *
	 */
	public enum ContentType
    {
        // WARNING! Do not remove unused values
        UNKNOWN (0),
        SHORTTEXT (1), // Short messages like Twitter or SMS, usually less than 140 chars
        ARTICLE (3), // long articles, usually longer than 140 chars.
        ARTICLE_WEBPAGE(4); 
    	
        private final int val;
        ContentType(int v) {
            this.val = v;
        }   	
    }	
   
   public enum SentimentValence
   {
	   Unknown (0),
	   Positive (1),
       Negative (2),
       Ambiguous (3),
       Zero (4), 
       General (5);
   	
   		private final int val;

   		SentimentValence(int v) {
           this.val = v;
   		}  
   }   
   
   public final static CSList<ContentType> lstCleanTextContentTypes = new CSList<ContentType>(new ContentType[]{ ContentType.SHORTTEXT, ContentType.ARTICLE });
   public final static CSList<ContentType> lstWebpageContentTypes = new CSList<ContentType>(new ContentType[]{ ContentType.ARTICLE_WEBPAGE });
   public final static CSList<ContentType> lstArticleContentTypes = new CSList<ContentType>(new ContentType[]{ ContentType.ARTICLE, ContentType.ARTICLE_WEBPAGE });
   
	
}

