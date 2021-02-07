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

import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.FloatVector;
import sirocco.model.SentimentReference;

public class EntityStats   
{
    public String Entity;
    public float Score;
    public Boolean GoodAsTopic;
    public Boolean GoodAsTag;
    public CSList<TextReference> References;
    public CSList<SentimentReference> RelatedSentiments;

    public EntityStats(String entity) {
        this.Entity = entity;
        References = new CSList<TextReference>();
        RelatedSentiments = new CSList<SentimentReference>();
    }

    public FloatVector getAggregateSentiment() {
    	FloatVector result = new FloatVector();
    	for (SentimentReference ref: RelatedSentiments)
    		result.accumulate(ref.Sentiment, false);
    	return result;
    };
    
    public void addRelatedSentiment(int paragraphNum, int sentenceNum, FloatVector sentiment){
    	RelatedSentiments.add(new SentimentReference(paragraphNum, sentenceNum, sentiment));
    }
    
}


