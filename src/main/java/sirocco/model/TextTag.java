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


public class TextTag   
{
    private String word;
    private float weight;
    private Boolean goodAsTopic;
    
    public TextTag(String word, float weight, Boolean goodAsTopic) {
        this.word = word;
        this.weight = weight;
        this.goodAsTopic = goodAsTopic;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String value) {
        word = value;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float value) {
        weight = value;
    }

    public Boolean getGoodAsTopic() {
        return goodAsTopic;
    }

    public void setGoodAsTopic(Boolean value)  {
        goodAsTopic = value;
    }

}


