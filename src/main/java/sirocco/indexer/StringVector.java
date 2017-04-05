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
import sirocco.indexer.DerivationStep;
import sirocco.indexer.IGenericVector;
import sirocco.indexer.util.LangUtils;
import CS2JNet.System.StringSupport;

import java.util.HashMap;

public class StringVector extends HashMap<String,String> implements IGenericVector
{
    public static String DefaultValue = "";
    public static String FlagEnding = "[flag]";
    public HashMap<String,Float> Flags;
    private HashMap<String,CSList<DerivationStep>> derivationSteps;
    public HashMap<String,CSList<DerivationStep>> getDerivationSteps() throws Exception {
        return derivationSteps;
    }

    private CSList<DerivationStep> dimDerivationSteps(String dimension) throws Exception {
        CSList<DerivationStep> dimlist = getDerivationSteps().get(dimension);
        if (dimlist == null)
        {
            dimlist = new CSList<DerivationStep>();
            getDerivationSteps().put(dimension, dimlist);
        }
         
        return dimlist;
    }

    public StringVector()  {
        super();
        derivationSteps = new HashMap<String,CSList<DerivationStep>>();
        Flags = new HashMap<String,Float>();
    }

    public void init(String[] keys, String[] fields) throws Exception {
        for (int i = 0;i < keys.length;i++)
        {
            String value = (StringSupport.isNullOrEmpty(fields[i])) ? DefaultValue : fields[i];
            if (keys[i].endsWith(FlagEnding))
            {
                String flagdim = keys[i].substring(0, keys[i].length() - FlagEnding.length());
                this.Flags.put(flagdim, Float.parseFloat(value));
            }
            else
                this.put(keys[i], value); 
        }
    }

    public String[] getDimensions() throws Exception {
        String[] dimensions = this.keySet().toArray(new String[this.keySet().size()]);
        return dimensions;
    }

    public String toCSV() throws Exception {
        String retvalue = null;
        if (this.values().size() > 0)
        {
            String[] values = this.values().toArray(new String[this.values().size()]).clone(); //TODO: do we need to clone?
            retvalue = "\"";
            retvalue += LangUtils.printStringList(new CSList<String>(values),"\",\"");
            retvalue += "\"";
        }
         
        return retvalue;
    }

}


