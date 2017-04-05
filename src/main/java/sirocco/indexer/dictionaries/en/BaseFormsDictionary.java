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

package sirocco.indexer.dictionaries.en;

import CS2JNet.System.Collections.LCC.CSList;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.dictionary.MorphologicalProcessor;
import net.sf.extjwnl.dictionary.Dictionary;

import sirocco.config.ConfigurationManager;
import sirocco.indexer.StringVector;
import sirocco.indexer.dictionaries.en.BaseformOverrideDictionary;
import sirocco.indexer.util.LangUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class BaseFormsDictionary   
{
    private BaseformOverrideDictionary BaseformOverrides;
    private Dictionary wnDict = null;
    private HashMap<String,CSList<String>> bfDict = null;
    private CSList<String> supportedPartsOfSpeech = null;
    public BaseFormsDictionary(BaseformOverrideDictionary overrides) throws Exception {
        BaseformOverrides = overrides;
        String propsFile = ConfigurationManager.getConfiguration().getString("WordnetPropertiesFile");
        InputStream stream = getClass().getResourceAsStream(propsFile);
        wnDict = Dictionary.getInstance(stream);
        bfDict = new HashMap<String,CSList<String>>();
        supportedPartsOfSpeech = new CSList<String>(new String[]{"noun","verb","adjective","adverb"}); // JWNL 1.3 and 1.4 only support these POSes 
    }
    
    public String bestBaseForm(String lemma, String pos) throws Exception {
        CSList<String> allbaseforms = baseForms(lemma,pos);
        // sometimes a word will have 2 base forms,
        // e.g. better is JJR of both good and well.
        // data has {data, datum}
        // Try to find the matching base form or take the first one.
        if (allbaseforms.contains(lemma))
            return lemma;
        else
            return allbaseforms.get(0); 
    }

    public CSList<String> baseForms(String lemma, String pos) throws Exception {
        CSList<String> result = null;
        String lowercaseLemma = lemma.toLowerCase();
        String fullpos = null;
        if (pos.length() == 1)
            fullpos = LangUtils.shortPOSToFullPOS(pos);
        else
            fullpos = pos; 
        String key = lowercaseLemma + "/" + fullpos;
        result = bfDict.get(key);
        if (result == null)
        {
            StringVector vector = BaseformOverrides.getWords().get(lowercaseLemma + '/' + pos);
            if (vector!=null)
            {
                result = new CSList<String>(new String[]{ vector.get("baseform") });
            }
            else
            {
                if (supportedPartsOfSpeech.contains(fullpos))
                {
                	POS fullposobj = POS.getPOSForLabel(fullpos);
                    List<String> bf = (List<String>) wnDict.getMorphologicalProcessor().lookupAllBaseForms(fullposobj,lowercaseLemma);
                    if (bf.size() > 0)
                    {
                    	String[] bfa = bf.toArray(new String [bf.size()]);
                        result = new CSList<String>(bfa);
                    }
                    else
                        result = new CSList<String>(new String[]{ lowercaseLemma }); 
                }
                else
                {
                    result = new CSList<String>(new String[]{ lowercaseLemma });
                } 
            } 
            bfDict.put(key, result);
        }
         
        return result;
    }

}


