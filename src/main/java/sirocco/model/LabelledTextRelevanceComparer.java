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

package sirocco.model;

import java.util.Comparator;

import sirocco.model.LabelledText;

// not entirely correct to use the top rank, but the result will be correct
public class LabelledTextRelevanceComparer implements Comparator<LabelledText> 
{
    public int compare(LabelledText x, LabelledText y) {
        if (x.ContainedEntityTopRank != y.ContainedEntityTopRank)
            return x.ContainedEntityTopRank == y.ContainedEntityTopRank ? 0 : 
            	(x.ContainedEntityTopRank < y.ContainedEntityTopRank ? 1 : -1); 
    			// if x entity has higher(=smaller) rank, than x is bigger than y
        else
            return x.AggregateSentimentScore == y.AggregateSentimentScore ? 0 : 
            	(x.AggregateSentimentScore < y.AggregateSentimentScore ? -1 : 1); 
    }

}

