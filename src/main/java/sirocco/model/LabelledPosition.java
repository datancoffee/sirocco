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

package sirocco.model;

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.IndexerLabel;

public class LabelledPosition   
{
    public boolean IsSingleSpan;
    public boolean IsStart;
    public boolean IsEnd;
    public CSList<Character> StartLabels = new CSList<Character>();
    public CSList<Character> EndLabels = new CSList<Character>();
    public static void addNewLabels(CSList<Character> targetlist, CSList<Character> listtoadd) throws Exception {
        for (char c : listtoadd)
        {
            // entity boundaries can overlap (e.g. <<local<<governments>>>>)
            if ((c == IndexerLabel.EntityLabel) || (!targetlist.contains(c)))
                targetlist.add(c);
             
        }
    }

    public static void labelTypes(CSList<Character> labellist, RefSupport<Boolean> hasEntities, RefSupport<Boolean> hasSentiments) throws Exception {
        hasEntities.setValue(false);
        hasSentiments.setValue(false);
        for (Integer i = 0;i < labellist.size();i++)
        {
            if (labellist.get(i) == IndexerLabel.EntityLabel)
            {
                hasEntities.setValue(true);
                if (hasSentiments.getValue())
                    break;
                 
            }
            else
            {
                hasSentiments.setValue(true);
                if (hasEntities.getValue())
                    break;
                 
            } 
        }
    }

    public static void labelTypes(CSList<Character> labellist, CSList<Character> labelfilter, RefSupport<Boolean> hasEntities, RefSupport<Boolean> hasSentiments, RefSupport<CSList<Character>> foundlabels) throws Exception {
        hasEntities.setValue(false);
        hasSentiments.setValue(false);
        if (labelfilter == null)
            foundlabels.setValue(labellist);
        else
            foundlabels.setValue(null); 
        for (Integer i = 0;i < labellist.size();i++)
        {
            if (labellist.get(i) == IndexerLabel.EntityLabel)
                hasEntities.setValue(true);
            else
                hasSentiments.setValue(true); 
            if ((labelfilter != null) && labelfilter.contains(labellist.get(i)))
            {
                if (foundlabels.getValue() == null)
                    foundlabels.setValue(new CSList<Character>());
                 
                foundlabels.getValue().add(labellist.get(i));
            }
             
        }
    }

}


