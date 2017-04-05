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

package sirocco.indexer.dictionaries.en;

import CS2JNet.JavaSupport.Collections.Generic.LCC.CollectionSupport;
import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.Generic.KeyCollectionSupport;
import sirocco.indexer.FloatVector;
import sirocco.indexer.dictionaries.KeywordDictionary;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class IdiomDictionary   
{
    public KeywordDictionary KeyFastIndex;
    public HashMap<String,IdiomSubDictionary> SubDictionaries;
    
    public IdiomDictionary(String[] sources, InputStream[] dictionarystreams) throws Exception {
        KeyFastIndex = new KeywordDictionary(6);
        SubDictionaries = new HashMap<String,IdiomSubDictionary>();
        for (int i = 0;i < sources.length;i++)
        {
            String source = sources[i];
            IdiomSubDictionary subdict = new IdiomSubDictionary(dictionarystreams[i]);
            SubDictionaries.put(source, subdict);
            Set<String> keySet = subdict.getWords().keySet();
            String[] keys = new String[keySet.size()];
            keySet.toArray(keys);
            KeyFastIndex.addRange(keys,true,source,false,false,false);
        }
    }

    public FloatVector words(String key) throws Exception {
        FloatVector res = null;
        for (Entry<String,IdiomSubDictionary> kvp : SubDictionaries.entrySet())
        {
        	res = kvp.getValue().getWords().get(key);
        	if (res!=null)
        		return res;
        }
        return res;
    }

}


