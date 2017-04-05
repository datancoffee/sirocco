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
import sirocco.indexer.IndexingConsts.SentimentValence;

public class SentimentValenceList   
{
    public static CSList<SentimentValence> Opinion = new CSList<SentimentValence>(new SentimentValence[]{ SentimentValence.Ambiguous, SentimentValence.General });
    public static CSList<SentimentValence> Positive = new CSList<SentimentValence>(new SentimentValence[]{ SentimentValence.Positive });
    public static CSList<SentimentValence> Negative = new CSList<SentimentValence>(new SentimentValence[]{ SentimentValence.Negative });
    public static CSList<SentimentValence> All = new CSList<SentimentValence>(new SentimentValence[]{ SentimentValence.Ambiguous, SentimentValence.General, SentimentValence.Positive, SentimentValence.Negative });
}


