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

import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;
import sirocco.indexer.FloatVector;
import java.util.HashMap;

public class ParagraphIndex   
{
    public String OriginalText;
    public int SentenceCount;
    public String[] OriginalSentences;
    //public ArrayList[] SentenceChunks;
    public Parse[] SentenceParses;
    public SentenceFlags[] SentenceFlags;
    // flags like is a parse part of an idiom, or a parent of an idiom occurence
    public String[] IndexedSentences;
    public HashMap<String,Span>[] SpanMap;
    // Map btw Spans in SentenceParses and IndexedSentences. The key in the map is the toString representation of the span
    public FloatVector[] SentenceSentiments;
    public TextStats ParagraphStats;
}


