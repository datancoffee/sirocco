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

package sirocco.annotators;

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import sirocco.model.LabelledPosition;

public class BriefLogTextAnnotator  extends GenericTextAnnotator 
{
    public static String EntityLabelStart = "{{";
    public static String EntityLabelEnd = "}}";
    public static String SentimentLabelStart = "<<";
    public static String SentimentLabelEnd = ">>";
    public static String MixedLabelStart = "<{";
    public static String MixedLabelEnd = "}>";
    public String startTag(CSList<Character> labels) throws Exception {
        String result = "";
        boolean hasEntities = false;
        boolean hasSentiments = false;
        RefSupport<Boolean> refEntities = new RefSupport<Boolean>();
        RefSupport<Boolean> refSentiments = new RefSupport<Boolean>();
        LabelledPosition.labelTypes(labels,refEntities,refSentiments);
        hasEntities = refEntities.getValue();
        hasSentiments = refSentiments.getValue();
        if (hasEntities && !hasSentiments)
            result += EntityLabelStart;
        else if (!hasEntities && hasSentiments)
            result += SentimentLabelStart;
        else if (hasEntities && hasSentiments)
            result += MixedLabelStart;
           
        return result;
    }

    public String endTag(CSList<Character> labels) throws Exception {
        String result = "";
        boolean hasEntities = false;
        boolean hasSentiments = false;
        RefSupport<Boolean> refEntities = new RefSupport<Boolean>();
        RefSupport<Boolean> refSentiments = new RefSupport<Boolean>();
        LabelledPosition.labelTypes(labels,refEntities,refSentiments);
        hasEntities = refEntities.getValue();
        hasSentiments = refSentiments.getValue();
        if (hasEntities && !hasSentiments)
            result = EntityLabelEnd;
        else if (!hasEntities && hasSentiments)
            result = SentimentLabelEnd;
        else if (hasEntities && hasSentiments)
            result = MixedLabelEnd;
           
        return result;
    }

}


