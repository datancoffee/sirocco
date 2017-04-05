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

import sirocco.config.ConfigurationManager;
import sirocco.indexer.NonEnglishIndexer;
import sirocco.indexer.NonEnglishIndexerPool;
import sirocco.indexer.ObjectPool;
import sirocco.model.ContentIndex;

public class NonEnglishIndexerPool extends ObjectPool<NonEnglishIndexer> 
{
    private static int DefaultPoolSize = 1;
    private static int DefaultValidationInterval = 1000;

    private static NonEnglishIndexerPool instance = null;
    public static NonEnglishIndexerPool getInstance() throws Exception {
        if (instance == null)
            instance = new NonEnglishIndexerPool();
         
        return instance;
    }

    public NonEnglishIndexerPool() throws Exception {
        super();
        int minIdle;
        int maxIdle;
        int validationInterval;
        try
        {
            minIdle = ConfigurationManager.getConfiguration().getInt("NonEnglishIndexerMinIdlePoolSize");
            maxIdle = ConfigurationManager.getConfiguration().getInt("NonEnglishIndexerMaxIdlePoolSize");
            validationInterval = ConfigurationManager.getConfiguration().getInt("NonEnglishIndexerValidationInterval");
        }
        catch (Exception ex)
        {
            minIdle = DefaultPoolSize;
            maxIdle = DefaultPoolSize;
            validationInterval = DefaultValidationInterval;
        }

        super.initialize(minIdle,maxIdle,validationInterval);
    }

    protected NonEnglishIndexer createObject() throws Exception {
        return new NonEnglishIndexer();
    }

    public void index(ContentIndex contentindex) throws Exception {
        NonEnglishIndexer i = null;
        try
        {
            i = borrowObject();
            i.index(contentindex);
        }
        finally
        {
            if (i != null)
                returnObject(i);
             
        }
    }

}


