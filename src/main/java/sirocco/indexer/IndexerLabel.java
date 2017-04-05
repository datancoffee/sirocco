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
import CS2JNet.System.StringSupport;

public class IndexerLabel   
{
    public static Character EntityLabel = 'e';
    public static Character GeneralSentimentLabel = 'g';
    public static Character AmbiguousLabel = 'm';
    public static Character PositiveLabel = 'p';
    public static Character NegativeLabel = 'n';
    public static Character AcceptanceLabel = 'c';
    public static Character AnticipationLabel = 't';
    public static Character InterestLabel = 'i';
    public static Character JoyLabel = 'j';
    public static Character SurpriseLabel = 'u';
    public static Character AngerLabel = 'a';
    public static Character DisgustLabel = 'd';
    public static Character FearLabel = 'f';
    public static Character GuiltLabel = 'l';
    public static Character SadnessLabel = 's';
    public static Character ShameLabel = 'h';
    public static Character ProfaneLabel = 'r';
    public static Character UnsafeLabel = 'x';
    public static Character UnknownLabel = '?';
    public static CSList<Character> EntityLabelList = new CSList<Character>(new Character[]{ EntityLabel });
    public static CSList<Character> SentimentLabelList = new CSList<Character>(new Character[]{ GeneralSentimentLabel, AmbiguousLabel, PositiveLabel, NegativeLabel, AcceptanceLabel, AnticipationLabel, InterestLabel, JoyLabel, SurpriseLabel, AngerLabel, DisgustLabel, FearLabel, GuiltLabel, SadnessLabel, ShameLabel, ProfaneLabel, UnsafeLabel });
    public static CSList<Character> EntityAndSentimentLabelList = new CSList<Character>(new Character[]{ EntityLabel, GeneralSentimentLabel, AmbiguousLabel, PositiveLabel, NegativeLabel, AcceptanceLabel, AnticipationLabel, InterestLabel, JoyLabel, SurpriseLabel, AngerLabel, DisgustLabel, FearLabel, GuiltLabel, SadnessLabel, ShameLabel, ProfaneLabel, UnsafeLabel });
    
    public static Character labelOfSentiment(String dimension)  {
        char label = UnknownLabel;
        if (StringSupport.equals(dimension, SentimentDimension.Acceptance))
            label = AcceptanceLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Disgust))
            label = DisgustLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Anticipation))
            label = AnticipationLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Surprise))
            label = SurpriseLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Joy))
            label = JoyLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Sadness))
            label = SadnessLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Positive))
            label = PositiveLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Negative))
            label = NegativeLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Fear))
            label = FearLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Anger))
            label = AngerLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Guilt))
            label = GuiltLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Shame))
            label = ShameLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Interest))
            label = InterestLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.GeneralSentiment))
            label = GeneralSentimentLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Ambiguous))
            label = AmbiguousLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Profane))
            label = ProfaneLabel;
        else if (StringSupport.equals(dimension, SentimentDimension.Unsafe))
            label = UnsafeLabel;
                         
        return label;
    }

}


