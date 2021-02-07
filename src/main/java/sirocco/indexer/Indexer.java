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

import CS2JNet.System.StringSupport;
import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.EnglishIndexerPool;
import sirocco.indexer.Language;
import sirocco.indexer.NonEnglishIndexerPool;
import sirocco.indexer.util.LangUtils;
import sirocco.model.ContentIndex;
import sirocco.model.LabelledText;
import sirocco.model.TextTag;

public class Indexer   
{
    /**
    * Generates NLP info
    */
    public static void index(ContentIndex contentindex) throws Exception {
    	
    	String validationMessage = contentindex.validateInputFields();
		if (validationMessage != null) {
			contentindex.IndexingErrors = validationMessage;
        	contentindex.IsIndexingSuccessful = false;
        	return;
		}
		
		if (contentindex.OriginalText == null || contentindex.OriginalText.isEmpty() || contentindex.OriginalText.trim().isEmpty() ) {
			// No need to run through full indexing. In the previous step we validated that other important fields are available
			contentindex.IsIndexingSuccessful = true;
			contentindex.populateResultsWithMinValues();
			return;
		}
    	
        // need to split into clean paragraphs before determining language
        String[] paragraphs = StructuredSplitter.splitIntoParagraphs(contentindex.OriginalText,contentindex.ContentType);
        if (paragraphs.length == 0) 
            return ;
         
        contentindex.initializeParagraphs(paragraphs);
        contentindex.Language = LangUtils.determineDominantLanguage(paragraphs);
        if (contentindex.Language.equals(Language.English))
            EnglishIndexerPool.getInstance().index(contentindex);
        else if (contentindex.Language.equals(Language.Undetermined))
            NonEnglishIndexerPool.getInstance().index(contentindex); 
        else {
        	contentindex.IndexingErrors = "Unindexable Text. Too many invalid characters";
        	contentindex.IsIndexingSuccessful = false;
        }
    }

    /**
    * Generates NLP info
    */
    public static Boolean isGoodAsTopic(String tag) throws Exception {
        return EnglishIndexerPool.getInstance().isGoodAsTopic(tag);
    }

}


