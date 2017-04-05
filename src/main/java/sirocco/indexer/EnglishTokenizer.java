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

import CS2JNet.JavaSupport.Collections.Generic.LCC.CollectionSupport;
import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import CS2JNet.System.StringSupport;
import CS2JNet.System.Text.RegularExpressions.Group;
import CS2JNet.System.Text.RegularExpressions.GroupCollection;
import CS2JNet.System.Text.RegularExpressions.Match;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;
import sirocco.indexer.FloatVector;
import sirocco.indexer.dictionaries.KeywordDictionary;
import sirocco.indexer.dictionaries.en.EnglishDictionaries;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

/**
* An advanced tokenizer capable of emoticons, abbreviations and regex pattern
*/
public class EnglishTokenizer implements Tokenizer
{
    public static class TokenNode   
    {
        public String Text;
        public boolean IsFinal;
        public TokenNode(String text, boolean isFinal)  {
            Text = text;
            IsFinal = isFinal;
        }

        public String toString() {
        	return Text;
        }
    
    }

    private EnglishDictionaries mDicts;
    private TokenizerME maxentTokenizer;
    private KeywordDictionary keyworddict;
    private static int KeywordDictKeyLength = 2;
    // . and , are often used in numbers and abbreviations
    // we use entropy and accept the fact that sometimes
    // cases like <word>,<word> won't be split properly
    // Cc - Other, control : specify individually [‚] […] [•]
    // Pc - Punctuation, connector : Do nothing, it connects the token
    // Pd - Punctuation, dash : ?? convert to standard dash
    // Pe - Punctuation, close : Close brackets: hard tokenizer: included as category
    // Ps - Punctuation, open : Open brackets: hard tokenizer: included as category
    // Pf - Punctuation, final quote : hard tokenizer: examples: [’] [”]: included as category; http://www.fileformat.info/info/unicode/category/Pf/list.htm
    // Pi - Punctuation, initial quote : hard tokenizer : examples: [‘] [“] [‟]: included as category; http://www.fileformat.info/info/unicode/category/Pi/list.htm
    // Po - Punctuation, other: some characters Hard ('"'), some Soft, some none, so use char[]
    // Sm - Symbol, math : [|]
    private static char[] HardTokenizerChars = new char[]{ ':', '\t', '"', '|', ';', '…', '•'};
    private static byte[] HardTokenizerUnicodeCategories = new byte[]{ Character.START_PUNCTUATION /* Ps */, Character.END_PUNCTUATION /*Pe*/,  Character.INITIAL_QUOTE_PUNCTUATION,  Character.FINAL_QUOTE_PUNCTUATION };
    private static char[] SoftTokenizers = new char[]{ ',', '.', '!', '?', '/' };
    private static char[] SoftTokenizersPlus = new char[]{ ',', '.', '!', '?', '/','\'' };
    private static String EmoticonKeywordType = "emoticon";
    private static String InterjectionKeywordType = "interjection";
    private static String AbbreviationKeywordType = "abbreviation";
    private static String ReplaceDuringTokenizeDim = "replace-during-tokenize";
    private static String RequiresClosingTokenizerDim = "requires-closing-tokenizer";
    public EnglishTokenizer(InputStream maxentmodelstream, EnglishDictionaries dicts) throws Exception {
        mDicts = dicts;
        
        TokenizerModel tokenModel = new TokenizerModel(maxentmodelstream);
        maxentTokenizer = new TokenizerME(tokenModel);
        //sso 8/26: can't be set anymore. this is part of the model now
        //maxentTokenizer.setAlphaNumericOptimization(true);
        keyworddict = new KeywordDictionary(KeywordDictKeyLength);
        int emoticoncount = mDicts.Emoticons.getWords().keySet().size();
        int abbrevcount = mDicts.Abbreviations.getWords().keySet().size();
        int interjcount = mDicts.Interjections.getWords().keySet().size();
        String[] words = null;
        for (Entry<String,FloatVector> kvp : mDicts.Emoticons.getWords().entrySet())
        {
            Float rctvalue = kvp.getValue().Flags.get(RequiresClosingTokenizerDim);
            boolean requiresSeparatorEnclosure = (rctvalue == FloatVector.InitialValue);
            keyworddict.addKeyword(kvp.getKey(),requiresSeparatorEnclosure,EmoticonKeywordType,false,false,false);
        }
        /*addAllLower*/
        /*addFirstCaps*/
        /*addAllCaps*/
        words = new String[abbrevcount];
        mDicts.Abbreviations.getWords().keySet().toArray(words);
        /*requiresSeparatorEnclosure*/
        keyworddict.addRange(words,true,AbbreviationKeywordType,true,false,true);
        /*addAllLower*/
        /*addFirstCaps*/
        /*addAllCaps*/
        words = null;
        for (Entry<String,FloatVector> kvp : mDicts.Interjections.getWords().entrySet())
        {
            Float rdtvalue = kvp.getValue().Flags.get(ReplaceDuringTokenizeDim);
            if (rdtvalue == FloatVector.InitialValue)
                /*requiresSeparatorEnclosure*/
                keyworddict.addKeyword(kvp.getKey(),true,InterjectionKeywordType,true,true,false);
             
        }
    }

    /*addAllLower*/
    /*addFirstCaps*/
    /*addAllCaps*/
    public String[] tokenize(String input) {
        CSList<String> finaltokens = new CSList<String>();
        CSList<TokenNode> ruleTokens = ruleSplit(input);
        for (TokenNode token : ruleTokens)
        {
            if (FloatVector.isIntensityToken(token.Text))
                finaltokens.add(token.Text);
            else
            {
                String s = token.Text;
                int firstSoftTokenizer = StringUtils.indexOfAny(s,SoftTokenizersPlus);
                if (firstSoftTokenizer < 0)
                    // if no soft tokenizers
                    finaltokens.add(s);
                else if (firstSoftTokenizer == s.length() - 1)
                {
                    // if soft tokenizer at end
                    String[] maxentsplit = maxentTokenizer.tokenize(s);
                    if (maxentsplit.length == 1)
                        finaltokens.add(maxentsplit[0]);
                    else if (maxentsplit.length == 2)
                    {
                        if (maxentsplit[1].length() == 1)
                            finaltokens.addAll(new CSList<String>(maxentsplit));
                        else
                            finaltokens.add(s); 
                    }
                    else
                    {
                        finaltokens.add(s.substring(0, s.length() - 1));
                        finaltokens.add(s.substring(s.length() - 1, s.length()));
                    }  
                }
                else
                {
                    String[] maxentsplit = maxentTokenizer.tokenize(s);
                    finaltokens.addAll(new CSList<String>(maxentsplit));
                }  
            } 
        }
        return finaltokens.toArray(new String[finaltokens.size()]);
    }

    public Span[] tokenizePositions(String input) throws Exception {
        return null;
    }

    // don't call this method directly, use Tokenize
    private CSList<TokenNode> ruleSplit(String input)  {
        CSList<TokenNode> tokens = new CSList<TokenNode>();
        tokens.add(new TokenNode(input,false));
        ruleSplitByPatterns(tokens);
        ruleSplitByOtherRules(tokens);
        return tokens;
    }

    private void ruleSplitByPatterns(CSList<TokenNode> input)  {
        for (Entry<String,FloatVector> kvp : mDicts.Patterns.getWords().entrySet())
        {
            ruleSplitByPattern(input,kvp.getKey());
        }
    }

    private void ruleSplitByPattern(CSList<TokenNode> input, String patternkey)  {
        CSList<Group> groups = new CSList<Group>();
        Pattern pattern = mDicts.Patterns.Regexes.get(patternkey);
        FloatVector patternvector = mDicts.Patterns.getWords().get(patternkey);
        int i = 0;
        while (i < input.size())
        {
            if (input.get(i).IsFinal)
            {
                i++;
                continue;
            }
             
            for (Match m : Match.mkMatches(pattern, input.get(i).Text))
            {
                if (m.getSuccess())
                    groups.add((Group)GroupCollection.mk(m).get(0));
                 
            }
            if (groups.size() > 0)
            {
                CSList<TokenNode> newTokens = new CSList<TokenNode>();
                int curPos = 0;
                for (int j = 0;j < groups.size();j++)
                {
                    // left of match
                    if (groups.get(j).getIndex() > curPos)
                    {
                        TokenNode token = new TokenNode(input.get(i).Text.substring(curPos, groups.get(j).getIndex()),false);
                        newTokens.add(token);
                    }
                     
                    // the actual match
                    // String rawtoken = input.get(i).Text.substring(groups.get(j).getIndex(), groups.get(j).getValue().length()).Trim();
                    String rawtoken = groups.get(j).getValue().trim();
                    String encodedtoken = encodeToken(patternvector,rawtoken);
                    newTokens.add(new TokenNode(encodedtoken,true));
                    curPos = groups.get(j).getIndex() + groups.get(j).getValue().length();
                }
                groups.clear();
                // remainder of text
                if (curPos < input.get(i).Text.length())
                {
                    TokenNode token = new TokenNode(input.get(i).Text.substring(curPos, input.get(i).Text.length()),false);
                    newTokens.add(token);
                }
                 
                input.remove(i);
                input.addAll(i, newTokens);
                i += newTokens.size();
            }
            else
                i++; 
        }
    }

    private String encodeToken(FloatVector patternvector, String text)  {
        String replacement = patternvector.toIntensityToken();
        replacement += FloatVector.dimensionToken(FloatVector.PosOverrideDimension,"UH");
        replacement += FloatVector.dimensionToken(FloatVector.OriginalTextDimension,text);
        return replacement;
    }

    private void ruleSplitByOtherRules(CSList<TokenNode> input)  {
        int i = 0;
        while (i < input.size())
        {
            if (input.get(i).IsFinal)
            {
                i++;
                continue;
            }
             
            CSList<String> newStringTokens = ruleSplitByOtherRules(input.get(i).Text);
            if (newStringTokens.size() > 0)
            {
                CSList<TokenNode> newTokens = new CSList<TokenNode>();
                for (String token : newStringTokens)
                    newTokens.add(new TokenNode(token,false));
                input.remove(i);
                input.addAll(i, newTokens);
                i += newTokens.size();
            }
            else
                i++; 
        }
    }

    private CSList<String> ruleSplitByOtherRules(String input)  {
        CSList<String> tokens = new CSList<String>();
        //gather up potential tokens
        int tokenStart = -1;
        boolean isInToken = false;
        
        int endPosition = input.length();
        int currentChar = 0;
        while (currentChar < endPosition)
        {
            if (Character.isWhitespace(input.charAt(currentChar)))
            {
                if (isInToken)
                {
                    tokens.add(input.substring(tokenStart, currentChar));
                    isInToken = false;
                    
                    tokenStart = -1;
                    currentChar++;
                }
                else
                    currentChar++; 
            }
            else
            {
                if (!isInToken)
                {
                    String keyword = null;
                    String source = null;
                    RefSupport<String> ref1 = new RefSupport<String>();
                    RefSupport<String> ref2 = new RefSupport<String>();
                    keyworddict.findLongestKeywordAtPosition(input,currentChar,ref1,ref2);
                    keyword = ref1.getValue();
                    source = ref2.getValue();
                    if (keyword != null)
                    {
                        // add transformation of keyword to @vector
                        String replace = getReplacementToken(keyword,source);
                        tokens.add(replace);
                        currentChar += keyword.length();
                        continue;
                    }
                    else
                    {
                        isInToken = true;
                        tokenStart = currentChar;
                    } 
                }
                 
                if (isHardTokenizer(input.charAt(currentChar)))
                {
                    if (isInToken)
                    {
                        if (currentChar > tokenStart)
                        {
                            tokens.add(input.substring(tokenStart, currentChar));
                            isInToken = false;
                            tokenStart = -1;
                        }
                        else
                        {
                            tokens.add(input.substring(currentChar, (currentChar) + (1)));
                            currentChar++;
                            isInToken = false;
                            tokenStart = -1;
                        } 
                    }
                    else
                    {
                        tokens.add(input.substring(currentChar, (currentChar) + (1)));
                        currentChar++;
                    } 
                }
                else
                    currentChar++; 
            } 
        }
        if (isInToken)
        {
            tokens.add(input.substring(tokenStart, endPosition));
        }
         
        return tokens;
    }

    private static boolean isHardTokenizer(char c)  {
    	int charType = Character.getType(c);
        return Arrays.asList(HardTokenizerUnicodeCategories).contains(charType) || Arrays.asList(HardTokenizerChars).contains(c);
    }

    private static boolean isSoftTokenizer(char c)  {
        return Arrays.asList(SoftTokenizers).contains(c);
    }

    private String getReplacementToken(String keyword, String source)  {
        String replacement = null;
        // emoticons are checked against their exact spelling, for Interjections and
        // Abbreviations, the dictionaries contain only their lower-case variants
        if (StringSupport.equals(source, EmoticonKeywordType))
        {
            replacement = mDicts.Emoticons.getWords().get(keyword).toIntensityToken();
            replacement += FloatVector.dimensionToken(FloatVector.PosOverrideDimension,"UH");
            replacement += FloatVector.dimensionToken(FloatVector.OriginalTextDimension,keyword);
        }
        else if (StringSupport.equals(source, InterjectionKeywordType))
        {
            String lower = keyword.toLowerCase();
            replacement = mDicts.Interjections.getWords().get(lower).toIntensityToken();
            replacement += FloatVector.dimensionToken(FloatVector.PosOverrideDimension,"UH");
            // Original text has the original spelling, not the lower cased one
            replacement += FloatVector.dimensionToken(FloatVector.OriginalTextDimension,keyword);
        }
        else if (StringSupport.equals(source, AbbreviationKeywordType))
        {
            String lower = keyword.toLowerCase();
            replacement = mDicts.Abbreviations.getWords().get(lower).toIntensityToken();
            replacement += FloatVector.dimensionToken(FloatVector.PosOverrideDimension,"UH");
            // Original text has the original spelling, not the lower cased one
            replacement += FloatVector.dimensionToken(FloatVector.OriginalTextDimension,keyword);
        }
           
        return replacement;
    }

    public static boolean isTokenSeparated(String input, int position)  {
        if (position == (input.length() - 1))
            return true;
         
        char nextchar = input.charAt(position + 1);
        if (Character.isWhitespace(nextchar))
            return true;
         
        if (isHardTokenizer(nextchar))
            return true;
         
        if (isSoftTokenizer(nextchar))
            return true;
         
        return false;
    }

	public Span[] tokenizePos(String s) {
		// TODO Auto-generated method stub
		return null;
	}

}


