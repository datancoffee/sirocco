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

import sirocco.model.TextStats;

public class TextStats   
{
    public int NumFirstCapitalEntities = 0;
    public int NumAllCapitalEntities = 0;
    public int NumLowerCaseEntities = 0;
    public int NumAllEntities = 0;
    public int NumSentences = 0;
    public float AllCapsRatio = 0.0F;
    public float FirstCapRatio = 0.0F;
    public boolean CapitalizationStandsOut = false;
    public void addStats(TextStats otherStats) throws Exception {
        NumFirstCapitalEntities += otherStats.NumFirstCapitalEntities;
        NumAllCapitalEntities += otherStats.NumAllCapitalEntities;
        NumLowerCaseEntities += otherStats.NumLowerCaseEntities;
        NumAllEntities += otherStats.NumAllEntities;
        NumSentences += otherStats.NumSentences;
        calculateRatios();
    }

    public void calculateSentenceStats(String[] tokens) throws Exception {
        NumSentences = NumFirstCapitalEntities = NumAllCapitalEntities = NumLowerCaseEntities = NumAllEntities = 0;
        AllCapsRatio = FirstCapRatio = 0.0F;
        if ((tokens == null) || (tokens.length == 0))
            return ;
         
        NumSentences = 1;
        for (int idx = 0;idx < tokens.length;idx++)
        {
            if (tokens[idx].length() == 0)
                continue;
             
            if (Character.isUpperCase(tokens[idx].charAt(0)))
            {
                // just check one next character (for performance reasons)
                if (tokens[idx].length() >= 2)
                    if (Character.isUpperCase(tokens[idx].charAt(1)))
                        NumAllCapitalEntities++;
                    else
                        NumFirstCapitalEntities++; 
                else
                    NumFirstCapitalEntities++; 
            }
            else if (Character.isLowerCase(tokens[idx].charAt(0)))
                // check, because a token can be punctuation
                NumLowerCaseEntities++;
              
        }
        NumAllEntities = NumAllCapitalEntities + NumFirstCapitalEntities + NumLowerCaseEntities;
        calculateRatios();
    }

    private void calculateRatios() throws Exception {
        if (NumAllEntities == 0)
        {
            AllCapsRatio = FirstCapRatio = 0.0F;
        }
        else
        {
            AllCapsRatio = ((float)NumAllCapitalEntities / (float)NumAllEntities);
            FirstCapRatio = ((float)NumFirstCapitalEntities / (float)NumAllEntities);
        } 
        CapitalizationStandsOut = determineIfCapitalizationStandsOut();
    }

    private boolean determineIfCapitalizationStandsOut() throws Exception {
        if (NumAllEntities <= 2)
            return false;
        else if ((NumAllEntities >= 3) && (NumAllEntities <= 7))
        {
            if (AllCapsRatio <= 0.5F)
                return true;
            else
                return false; 
        }
        else if ((NumAllEntities >= 8) && (NumAllEntities <= 15))
        {
            if (AllCapsRatio <= 0.4F)
                return true;
            else
                return false; 
        }
        else if (NumAllEntities >= 16)
        {
            if (((NumAllCapitalEntities - 6) / NumAllEntities) <= 0.2F)
                return true;
            else
                return false; 
        }
            
        return false;
    }

}


