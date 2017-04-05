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

package sirocco.annotators;

import CS2JNet.System.Collections.LCC.CSList;
import sirocco.model.ILabelledPositions;
import sirocco.model.LabelledPosition;
import sirocco.model.PositionsDictionary;

import java.util.Map.Entry;

public abstract class GenericTextAnnotator   
{
    public abstract String startTag(CSList<Character> labels) throws Exception ;

    public abstract String endTag(CSList<Character> labels) throws Exception ;

    public String annotate(ILabelledPositions lpositions, String text) throws Exception {
        return annotate(lpositions,text,null,null);
    }

    // annotate only the specified entities and emotions
    public String annotate(ILabelledPositions lpositions, String text, CSList<String> entityFilter, CSList<Character> labelFilter) throws Exception {
        StringBuilder sb = new StringBuilder();
        int tokenstart = 0;
        CSList<String> lowerEntityFilter = null;
        if (entityFilter != null)
        {
            lowerEntityFilter = new CSList<String>();
            for (String entity : entityFilter)
                lowerEntityFilter.add(entity.toLowerCase());
        }
         
        if (lpositions != null)
        {
            PositionsDictionary pdict = lpositions.getPositionsDictionary(lowerEntityFilter,labelFilter);
            for (Entry<Integer,LabelledPosition> kvp : pdict.entrySet())
            {
                if (kvp.getValue().IsSingleSpan)
                {
                    if (kvp.getKey() - 1 >= tokenstart)
                        sb.append(text.substring(tokenstart, (tokenstart) + ((kvp.getKey() - 1) - (tokenstart)+1)));
                     
                    sb.append(startTag(kvp.getValue().StartLabels));
                    sb.append(text.substring(kvp.getKey(), (kvp.getKey()) + (1)));
                    sb.append(endTag(kvp.getValue().EndLabels));
                    tokenstart = kvp.getKey() + 1;
                }
                else if (kvp.getValue().IsStart && !kvp.getValue().IsEnd)
                {
                    if (kvp.getKey() - 1 >= tokenstart)
                        sb.append(text.substring(tokenstart, (tokenstart) + ((kvp.getKey() - 1) - (tokenstart)+1)));
                     
                    sb.append(startTag(kvp.getValue().StartLabels));
                    tokenstart = kvp.getKey();
                }
                else if (!kvp.getValue().IsStart && kvp.getValue().IsEnd)
                {
                    if (kvp.getKey() >= tokenstart)
                        sb.append(text.substring(tokenstart, tokenstart + (kvp.getKey())-tokenstart + 1));
                     
                    sb.append(endTag(kvp.getValue().EndLabels));
                    tokenstart = kvp.getKey() + 1;
                }
                else if (kvp.getValue().IsStart && kvp.getValue().IsEnd)
                {
                    throw new Exception("TextAnnotator.Annotate(): position can't be Start and End unless Single Character Span");
                }
                    
            }
        }
         
        if ((tokenstart) <= (text.length() - 1))
            sb.append(text.substring(tokenstart));
         
        return sb.toString();
    }

}


