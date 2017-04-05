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

package sirocco.indexer.dictionaries;

import java.util.HashMap;

public class LangDictionary   
{
    private static LangDictionary instance = null;
    private static char[] sentenceSeparators = new char[] {'.','?','!'};
    private static Character[] sentenceSeparatorCharacters = new Character[] {'.','?','!'};
    private static char[] wordSeparators = new char[] {':', ',', '\t', '{', '}', '(', ')', '[', ']', '"', '\\', ';'};
    private HashMap<String,String> sentenceNotSeparators = new HashMap<String,String>();
    private HashMap<String,String> ignoreWords = new HashMap<String,String>();
    private HashMap<String,String> commonWords = new HashMap<String,String>();
    
    public static LangDictionary getInstance()  {
        if (instance == null)
            instance = new LangDictionary("Default");
         
        return instance;
    }

    public char[] getSentenceSeparators()  {
        return sentenceSeparators;
    }

    public Character[] getSentenceSeparatorCharacters()  {
        return sentenceSeparatorCharacters;
    }

    public HashMap<String,String> getSentenceNotSeparators()  {
        return sentenceNotSeparators;
    }

    public char[] getWordSeparators()  {
        return wordSeparators;
    }

    public HashMap<String,String> getIgnoreWords()  {
        return ignoreWords;
    }

    public HashMap<String,String> getCommonWords()  {
        return commonWords;
    }

    private LangDictionary(String language)  {
    }

}

