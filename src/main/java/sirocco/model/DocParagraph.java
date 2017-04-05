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
import sirocco.model.DocSentence;

public class DocParagraph   
{
    public CSList<DocSentence> Sentences;
    public float Score;
    public boolean Selected;
    public DocParagraph() throws Exception {
        Sentences = new CSList<DocSentence>();
        Score = 0.0F;
        Selected = false;
    }

    public void addSentence(DocSentence docsentence) throws Exception {
        Sentences.add(docsentence);
        docsentence.Paragraph = this;
    }

}


