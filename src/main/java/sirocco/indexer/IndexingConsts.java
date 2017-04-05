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


public class IndexingConsts
{
	public enum IndexingType
	{
	    TEXT (0),
	    WEBPAGE (1);
	
	    private final int val;
	
	    IndexingType(int v) {
	        this.val = v;
	    }   	
	
	}
	
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
	
	public enum ContentType
    {
        // WARNING! Do not remove unused values
        UNKNOWN (0),
        SHORTTEXT (1), // Short messages like Twitter or SMS, usually less than 140 chars
        ARTICLE (2); // long articles, usually longer than 140 chars.
    	
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
	
}

