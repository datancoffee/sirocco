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

package sirocco.indexer.dictionaries.en;


import sirocco.config.ConfigurationManager;
import sirocco.indexer.dictionaries.EmoticonDictionary;

import java.io.InputStream;

public class EnglishDictionaries   
{
    public BaseFormsDictionary BaseForms;
    public BaseformOverrideDictionary BaseformOverrides;
    public IdiomDictionary Idioms;
    public PrefixDictionary Prefixes;
    public AbbreviationDictionary Abbreviations;
    public EmoticonDictionary Emoticons;
    public EmotionDictionary Emotions;
    public InterjectionDictionary Interjections;
    public QualityDictionary Qualities;
    public SubstitutionDictionary Substitutions;
    public DegreeAdverbDictionary DegreeAdverbs;
    public ModifierDictionary Modifiers;
    public NegatorDictionary Negators;
    public PatternDictionary Patterns;
    public EnglishDictionaries() throws Exception {
    	BaseformOverrides = new BaseformOverrideDictionary(getClass().getResourceAsStream("/csdict/bfoverrides-en.csv"));
    	BaseForms = new BaseFormsDictionary(BaseformOverrides);
        String[] idiomsources = new String[]{ "idioms-en", "profanity-idioms-en", "custom-idioms-en" };
        InputStream[] idiomdictionarystreams = new InputStream[]{ getClass().getResourceAsStream("/csdict/idioms-en.csv"), getClass().getResourceAsStream("/csdict/profanity-idioms-en.csv"), getClass().getResourceAsStream("/csdict/custom-idioms-en.csv") };
        Idioms = new IdiomDictionary(idiomsources,idiomdictionarystreams);
        Prefixes = new PrefixDictionary(getClass().getResourceAsStream("/csdict/prefixes-en.csv"));
        Abbreviations = new AbbreviationDictionary(getClass().getResourceAsStream("/csdict/abbreviations-en.csv"));
        Emoticons = new EmoticonDictionary(getClass().getResourceAsStream("/csdict/emoticons.csv"));
        Emotions = new EmotionDictionary(getClass().getResourceAsStream("/csdict/emotions-en.csv"));
        Interjections = new InterjectionDictionary(getClass().getResourceAsStream("/csdict/interjections-en.csv"));
        Qualities = new QualityDictionary(getClass().getResourceAsStream("/csdict/qualities-en.csv"));
        Substitutions = new SubstitutionDictionary(getClass().getResourceAsStream("/csdict/substitutions-en.csv"));
        DegreeAdverbs = new DegreeAdverbDictionary(getClass().getResourceAsStream("/csdict/degree-adv-en.csv"));
        Modifiers = new ModifierDictionary(getClass().getResourceAsStream("/csdict/modifiers-en.csv"));
        Negators = new NegatorDictionary(getClass().getResourceAsStream("/csdict/negators-en.csv"));
        Patterns = new PatternDictionary(getClass().getResourceAsStream("/csdict/patterns.csv"));
    }

}


