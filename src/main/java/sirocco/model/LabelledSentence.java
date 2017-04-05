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

import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.FloatVector;
import sirocco.model.LabelledPositionsV2;

public class LabelledSentence   
{
    public String ParSenKey;
    public int ContainedEntityTopRank = Integer.MAX_VALUE;
    // values: MaxValue = no Entities, 1 - top entity, 7 - last top entity
    public float TotalSentimentScore = FloatVector.DefaultValue;
    public LabelledPositionsV2 LabelledPositions = new LabelledPositionsV2();
    public CSList<String> ContainedEntities = new CSList<String>();
    public void addContainedEntity(String entity, int rank) throws Exception {
        if (!ContainedEntities.contains(entity))
        {
            ContainedEntities.add(entity);
            if (ContainedEntityTopRank > rank)
                ContainedEntityTopRank = rank;
             
        }
         
    }

    public PositionsDictionary getPositions() throws Exception {
        return LabelledPositions.getPositionsDictionary(null,null);
    }

}


