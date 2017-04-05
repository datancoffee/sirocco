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

package sirocco.indexer;

import CS2JNet.System.StringSupport;
import sirocco.indexer.EnglishIndexerPool;
import sirocco.indexer.Language;
import sirocco.indexer.NonEnglishIndexerPool;
import sirocco.indexer.util.LangUtils;
import sirocco.model.ContentIndex;

public class Indexer   
{
    /**
    * Generates NLP info
    */
    public static void index(ContentIndex contentindex) throws Exception {
        // need to split into clean paragraphs before determining language
        String[] paragraphs = StructuredSplitter.splitIntoParagraphs(contentindex.OriginalText,contentindex.IndexingType);
        if (paragraphs.length == 0)
            return ;
         
        contentindex.initializeParagraphs(paragraphs);
        contentindex.Language = LangUtils.determineDominantLanguage(paragraphs);
        if (StringSupport.equals(contentindex.Language, Language.English))
            EnglishIndexerPool.getInstance().index(contentindex);
        else
            NonEnglishIndexerPool.getInstance().index(contentindex); 
    }

    /**
    * Generates NLP info
    */
    public static Boolean isGoodAsTopic(String tag) throws Exception {
        return EnglishIndexerPool.getInstance().isGoodAsTopic(tag);
    }

}


