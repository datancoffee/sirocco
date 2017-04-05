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

package sirocco.indexer;

import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.IndexingConsts.SentimentValence;
import CS2JNet.System.StringSupport;

public class SentimentValenceHelper   
{
    public static float DominantValenceThreshold = 0.5F;
    // Positive or negative valence must be > 50% to be considered definitive
    public static CSList<String> PositiveDimensions = new CSList<String>(new String[]{ "acceptance", "anticipation", "interest", "joy", "surprise", "positive" });
    public static CSList<String> NegativeDimensions = new CSList<String>(new String[]{ "anger", "disgust", "fear", "guilt", "sadness", "shame", "negative" });
    public static CSList<String> GeneralDimensions = new CSList<String>(new String[]{ SentimentDimension.GeneralSentiment });
    public static CSList<String> AmbiguousDimensions = new CSList<String>(new String[]{ SentimentDimension.Ambiguous, SentimentDimension.Profane, SentimentDimension.Unsafe });
    
    public static String valenceLabel(SentimentValence valence) throws Exception {
        if (valence == SentimentValence.Positive)
            return "positive";
        else if (valence == SentimentValence.Negative)
            return "negative";
        else if (valence == SentimentValence.Ambiguous)
            return "ambiguous";
        else if (valence == SentimentValence.General)
            return "general";
        else if (valence == SentimentValence.Zero)
            return "zero";
        else
            return "general";     
    }

    public static SentimentValence valenceOfLabel(String label) throws Exception {
        if (StringSupport.equals(label, "positive"))
            return SentimentValence.Positive;
        else if (StringSupport.equals(label, "negative"))
            return SentimentValence.Negative;
        else if (StringSupport.equals(label, "ambiguous"))
            return SentimentValence.Ambiguous;
        else if (StringSupport.equals(label, "general"))
            return SentimentValence.General;
        else if (StringSupport.equals(label, "zero"))
            return SentimentValence.Zero;
        else
            return SentimentValence.Unknown;     
    }

}


