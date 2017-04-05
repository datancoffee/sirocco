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


import sirocco.config.ConfigurationManager;
import sirocco.indexer.EnglishIndexer;
import sirocco.indexer.EnglishIndexerPool;
import sirocco.indexer.ObjectPool;
import sirocco.model.ContentIndex;

public class EnglishIndexerPool extends ObjectPool<EnglishIndexer> 
{
    private static int DefaultPoolSize = 1;
    private static int DefaultValidationInterval = 1000;
    
    private static EnglishIndexerPool instance = null;
    public static EnglishIndexerPool getInstance() throws Exception {
        if (instance == null)
            instance = new EnglishIndexerPool();
         
        return instance;
    }

    public EnglishIndexerPool() throws Exception {
        super();
        int minIdle;
        int maxIdle;
        int validationInterval;
        try
        {
            minIdle = ConfigurationManager.getConfiguration().getInt("EnglishIndexerMinIdlePoolSize");
            maxIdle = ConfigurationManager.getConfiguration().getInt("EnglishIndexerMaxIdlePoolSize");
            validationInterval = ConfigurationManager.getConfiguration().getInt("EnglishIndexerValidationInterval");
        }
        catch (Exception ex)
        {
            minIdle = DefaultPoolSize;
            maxIdle = DefaultPoolSize;
            validationInterval = DefaultValidationInterval;
        }

        super.initialize(minIdle,maxIdle,validationInterval);
    }

    protected EnglishIndexer createObject() throws Exception {
        return new EnglishIndexer();
    }

    public void index(ContentIndex contentindex) throws Exception {
        EnglishIndexer i = null;
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

    public Boolean isGoodAsTopic(String tag) throws Exception {
        EnglishIndexer i = null;
        Boolean res = false;
        try
        {
            i = borrowObject();
            res = i.isGoodAsTopic(tag);
        }
        finally
        {
            if (i != null)
                returnObject(i);
             
        }
        return res;
    }

}


