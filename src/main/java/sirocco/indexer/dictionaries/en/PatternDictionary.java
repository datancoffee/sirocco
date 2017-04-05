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

import sirocco.indexer.FloatVector;
import sirocco.indexer.FloatVectorFactory;
import sirocco.indexer.dictionaries.GenericDictionary;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class PatternDictionary  extends GenericDictionary<FloatVector> 
{
    public HashMap<String,Pattern> Regexes;
    public PatternDictionary(InputStream dictionarystream) throws Exception {
        super(dictionarystream, new FloatVectorFactory());
        Regexes = new HashMap<String,Pattern>();
        for (Entry<String,FloatVector> kvp : this.getWords().entrySet())
        {
            Float rovalue = kvp.getValue().Flags.get(FloatVector.RegexOptionDimension);
            if (rovalue == null)
            	rovalue = 0.0F;
            int regexOptions = (int)(rovalue / FloatVector.InitialValue);
            Pattern regex = Pattern.compile(kvp.getKey(), regexOptions);
            Regexes.put(kvp.getKey(), regex);
        }
    }

}


