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

package sirocco.indexer.dictionaries;

import java.util.HashMap;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

public class StemDictionary   
{
    private static StemDictionary instance = null;
    private SnowballStemmer stemmer = null;
    private HashMap<String,String> stemDict = null;
    public static StemDictionary getInstance() throws Exception {
        if (instance == null)
            instance = new StemDictionary();
         
        return instance;
    }

    private StemDictionary() throws Exception {
        stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        stemDict = new HashMap<String,String>();
    }

    public String stem(String word) throws Exception {
        String result = stemDict.get(word);
        if (result == null)
        {
            result = stemmer.stem(word).toString();
            stemDict.put(word, result);
        }
        return result;
    }

}


