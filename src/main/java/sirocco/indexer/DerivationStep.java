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
import opennlp.tools.util.Span;
import sirocco.indexer.DerivationStep;


public class DerivationStep   
{
    public String Action;
    public Span DerivationSpan;
    public Boolean IsRemapped = false; // temporary state during remapSteps() operation
    public static final String AccumulateAction = "accumulate";
    //public static string MultiplyAction = "multiply";
    public static final String NegateAndMultiplyAction = "negate-multiply";
    //public static string NeutralizeAction = "neutralize";
    public DerivationStep(String action, Span span)  {
        Action = action;
        DerivationSpan = span;
    }

    public String toString() {
    	return "action=" + Action + ",span=" + DerivationSpan.toString();
    }

    public static CSList<DerivationStep> createList(String action, CSList<Span> spans)  {
        CSList<DerivationStep> steps = new CSList<DerivationStep>();
        for (Span span : spans)
            steps.add(new DerivationStep(action,span));
        return steps;
    }

}


