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
import java.util.HashMap;

public class SentimentDimension   
{
    public static String GeneralSentiment = "sentiment";
    // General (=Neutral) sentiment
    public static String Ambiguous = "ambiguous";
    // is known to have opposing meanings (e.g. killer)
    public static String AmbiguousWhenModified = "ambiguous-when-modified";
    // this dimension is not to be persisted in DB.
    public static String Positive = "positive";
    public static String PositiveWhenModified = "positive-when-modified";
    public static String Negative = "negative";
    public static String Acceptance = "acceptance";
    public static String Anticipation = "anticipation";
    public static String Interest = "interest";
    public static String Joy = "joy";
    public static String Surprise = "surprise";
    public static String Anger = "anger";
    public static String Disgust = "disgust";
    public static String Fear = "fear";
    public static String Guilt = "guilt";
    public static String Sadness = "sadness";
    public static String Shame = "shame";
    public static String Profane = "profane";
    public static String ProfaneWhenCombined = "profane-when-combined";
    public static String Unsafe = "unsafe";
    public static String UnsafeWhenCombined = "unsafe-when-combined";
    public static CSList<String> AllSentimentDimensions = new CSList<String>(new String[]{ GeneralSentiment, Ambiguous, Positive, Negative, Acceptance, Anticipation, Interest, Joy, Surprise, Anger, Disgust, Fear, Guilt, Sadness, Shame, Profane, Unsafe });
    public static CSList<String> DimensionsRequiringModifier = new CSList<String>(new String[]{ AmbiguousWhenModified, PositiveWhenModified });
    public static HashMap<String,CSList<String>> DimensionsRequiringCombinations = new HashMap<String,CSList<String>>();
    public static float CombinationThreshold = 10.0F;
    
    static {
    	DimensionsRequiringCombinations.put(ProfaneWhenCombined, new CSList<String>(new String[]{ ProfaneWhenCombined, Profane }));
    	DimensionsRequiringCombinations.put(UnsafeWhenCombined, new CSList<String>(new String[]{ UnsafeWhenCombined, Unsafe }));
    }

    public static String sentimentOfModified(String modifieddimension)  {
        if (StringSupport.equals(modifieddimension, AmbiguousWhenModified))
            return Ambiguous;
        else if (StringSupport.equals(modifieddimension, PositiveWhenModified))
            return Positive;
        else
            return modifieddimension;  
    }

    public static String sentimentOfCombined(String combineddimension)  {
        if (StringSupport.equals(combineddimension, ProfaneWhenCombined))
            return Profane;
        else if (StringSupport.equals(combineddimension, UnsafeWhenCombined))
            return Unsafe;
        else
            return combineddimension;  
    }

    public static String oppositeOf(String dimension)  {
        String oppositedimension = null;
        if (StringSupport.equals(dimension, Acceptance))
            oppositedimension = Disgust;
        else if (StringSupport.equals(dimension, Disgust))
            oppositedimension = Acceptance;
        else if (StringSupport.equals(dimension, Anticipation))
            oppositedimension = Surprise;
        else if (StringSupport.equals(dimension, Surprise))
            oppositedimension = Anticipation;
        else if (StringSupport.equals(dimension, Joy))
            oppositedimension = Sadness;
        else if (StringSupport.equals(dimension, Sadness))
            oppositedimension = Joy;
        else if (StringSupport.equals(dimension, Positive))
            oppositedimension = Negative;
        else if (StringSupport.equals(dimension, Negative))
            oppositedimension = Positive;
        else if (StringSupport.equals(dimension, Fear))
            oppositedimension = Anger;
        else if (StringSupport.equals(dimension, Anger))
            oppositedimension = Fear;
        else if (StringSupport.equals(dimension, Guilt))
            oppositedimension = Joy;
        else if (StringSupport.equals(dimension, Shame))
            oppositedimension = Joy;
        else if (StringSupport.equals(dimension, Interest))
            oppositedimension = Sadness;
                     
        return oppositedimension;
    }

}


