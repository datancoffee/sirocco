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

package sirocco.indexer.util;

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import CS2JNet.System.StringSupport;
import CS2JNet.System.Text.EncodingSupport;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;
import sirocco.indexer.DerivationStep;
import sirocco.indexer.FloatVector;
import sirocco.indexer.Language;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;


public class LangUtils   
{
    public static String determineDominantLanguage(String[] paragraphs) throws Exception {
        int latinCount = 0;
        int foreignCount = 0;
        int neutralCount = 0;
        int spaceCount = 0;

        for (int i = 0;i < paragraphs.length;i++)
        {
            String data = paragraphs[i];
            for (int j = 0;j < data.length();j++)
            {
                char c = data.charAt(j);
                /*
                    ..-31 control
                    32 space
                    33-64 neutral: visible chars
                    65-90 latin: uppercase letters
                    91-96 neutral: visible chars
                    97-122 latin: lowercase letters
                    123-126 neutral: visible chars
                    127 (del) neutral
                    128-165 foreign only
                    166-254 neutral: various symbols (math, graphic)
                    255+ unicode
                */
                // quick and dirty implementation of language identification
                // algorithm. When we support more languages in the Parser,
                // implement N-gram language identification (TextCat)
                if (c < 32)
                    neutralCount++;
                else if (c == 32)
                    spaceCount++;
                else if ((c >= 33) && (c <= 64))
                    neutralCount++;
                else if ((c >= 65) && (c <= 90))
                    latinCount++;
                else if ((c >= 91) && (c <= 96))
                    neutralCount++;
                else if ((c >= 97) && (c <= 122))
                    latinCount++;
                else if ((c >= 123) && (c <= 127))
                    neutralCount++;
                else if ((c >= 128) && (c <= 165))
                    foreignCount++;
                else if ((c >= 166) && (c <= 254))
                    neutralCount++;
                else if (Character.isLetter(c))
                    foreignCount++;
                else
                    neutralCount++;         
            }
        }
        if ( ((float)(neutralCount / (float)(latinCount + neutralCount + foreignCount)) > 0.50F) && (neutralCount > 50) ) 
        	// too many of neutral chars will cause extreme delays in processing. Also, this is not a real text blurb
        	//"Too many invalid characters. Latin chars "+latinCount+", Non-latin chars "+foreignCount+", Neutral chars "+neutralCount);
        	return Language.UnindexabeText;
        else if ((float)(foreignCount / (float)(latinCount + neutralCount + foreignCount)) > 0.1F)
            return Language.Undetermined;
        else
            return Language.English; 
    }

    public static int getIntValue(FloatVector vector, String dimension)  {
        Float value = vector.get(dimension);
        if (value == null)
        	return 0;
        else
        	return Math.round(value);
    }

    public static String printStringList(CSList<String> list, String separator) throws Exception {
        String output = "";
        for (String item : list)
            if (StringSupport.equals(output, ""))
                output = item;
            else
                output += separator + item; 
        return output;
    }

    public static String printCharList(CSList<Character> list, String separator) throws Exception {
        String output = "";
        for (char item : list)
            if (StringSupport.equals(output, ""))
                output += item;
            else
                output += separator + item; 
        return output;
    }

    public static String printDerivationStepList(CSList<DerivationStep> list, String separator) throws Exception {
        String output = "";
        for (DerivationStep item : list)
            if (StringSupport.equals(output, ""))
                output = item.toString();
            else
                output += separator + item.toString(); 
        return output;
    }

    public static String printParseList(CSList<Parse> list, String separator) throws Exception {
        String output = "";
        for (Parse item : list)
        {
            String token = item.toString();
            if (StringSupport.equals(output, ""))
                output = token;
            else
                output += separator + token; 
        }
        return output;
    }

    /*
    public static String printParsePennbankTree(Parse parse) throws Exception {
		StringBuffer buff = new StringBuffer();
		buff.append("(");
		try{
			parse.show();
			buff.append(parse.getNodeType());
		}catch(Exception e){
			System.err.println("Caught NPE");
		}
		if(pathTree.getLeaf()){ //pathTree.getChildren().size() == 1 && pathTree.getChildren(0).getLeaf()){
			buff.append(" ");
			buff.append(pathTree.getNodeValue());
//			buff.append(")");
		}else{
			for(int i = 0; i < pathTree.getChildren().size(); i++){
				buff.append(" ");
				buff.append(tree2str(pathTree.getChildren(i)));
			}
		}
		buff.append(")");
		return buff.toString();
    }
    */
    
    public static String printPOSTags(CSList<Parse> list, String separator) throws Exception {
        String output = "";
        for (Parse item : list)
        {
            String token = item.toString();
            String tag = item.getType();
            if (StringSupport.equals(output, ""))
                output = token + "/" + tag;
            else
                output += separator + token + "/" + tag; 
        }
        return output;
    }

    public static String restoreText(String text) throws Exception {
        String res = text;
        res = res.replace("-LRB-", "(");
        res = res.replace("-RRB-", ")");
        res = res.replace("-LCB-", "{");
        res = res.replace("-RCB-", "}");
        return res;
    }

    public static boolean isBrackets(String token, RefSupport<String> bracket) throws Exception {
        Boolean isbracket = false;
        bracket.setValue(null);
        if (StringSupport.equals(token, "-LRB-"))
        {
            bracket.setValue("(");
            isbracket = true;
        }
        else if (StringSupport.equals(token, "-RRB-"))
        {
            bracket.setValue(")");
            isbracket = true;
        }
        else if (StringSupport.equals(token, "-LCB-"))
        {
            bracket.setValue("{");
            isbracket = true;
        }
        else if (StringSupport.equals(token, "-RCB-"))
        {
            bracket.setValue("}");
            isbracket = true;
        }
            
        return isbracket;
    }

    public static String toFirstCap(String text) throws Exception {
        if (text.length() == 0)
            return "";
        else if (text.length() == 1)
            return "" + Character.toUpperCase(text.charAt(0));
        else
            return Character.toUpperCase(text.charAt(0)) + text.substring(1);  
    }

    // based on WordnetDictionary.GetLemmas
    // can convert Parse.Type such as NN, VB, JJR etc
    // as well as WordNet pos abbreviations n,v,a,s,r
    public static String shortPOSToFullPOS(String tag) throws Exception {
        String partOfSpeech = "noun";
        if (tag.equals("n"))
        {
            partOfSpeech = "noun";
        }
        else if (tag.equals("v"))
        {
            partOfSpeech = "verb";
        }
        else if (tag.equals("a") || tag.equals("s"))
        {
            partOfSpeech = "adjective";
        }
        else if (tag.equals("r"))
        {
            partOfSpeech = "adverb";
        }
        else if (tag.equals("e"))
        {
            partOfSpeech = "preposition";
        }
        else if (tag.equals("o"))
        {
            partOfSpeech = "pronoun";
        }
        else if (tag.equals("c"))
        {
            partOfSpeech = "conjunction";
        }
        else if (tag.equals("i"))
        {
            partOfSpeech = "interjection";
        }
        else if (tag.equals("d"))
        {
            partOfSpeech = "determiner";
        }
        else
        {
            partOfSpeech = "noun";
        }         
        return partOfSpeech;
    }

    // based on WordnetDictionary.GetLemmas
    // can convert Parse.Type such as NN, VB, JJR etc
    // as well as WordNet pos abbreviations n,v,a,s,r
    public static String parseTypeToShortPOS(String parsetype) throws Exception {
        String partOfSpeech = "n";
        if (parsetype.startsWith("N"))
        {
            partOfSpeech = "n";
        }
        else // noun
        if (parsetype.startsWith("V") || parsetype.equals("MD"))
        {
            partOfSpeech = "v";
        }
        else // verb
        if (parsetype.startsWith("J"))
        {
            partOfSpeech = "a";
        }
        else // adjective
        if (parsetype.startsWith("RB"))
        {
            partOfSpeech = "r";
        }
        else // adverb
        if (parsetype.equals("IN"))
        {
            partOfSpeech = "e";
        }
        else // preposition
        if (parsetype.startsWith("PRP"))
        {
            partOfSpeech = "o";
        }
        else // pronoun
        if (parsetype.equals("CC"))
        {
            partOfSpeech = "c";
        }
        else // conjunction
        if (parsetype.equals("UH"))
        {
            partOfSpeech = "i";
        }
        else //interjection
        if (parsetype.equals("DT"))
        {
            partOfSpeech = "d";
        }
        else
        {
            //determiner
            partOfSpeech = "n";
        }         
        return partOfSpeech;
    }

    public static String baseTypeOfParseType(String parsetype) throws Exception {
        String basetype;
        if (parsetype.startsWith("N"))
        {
            basetype = "NN";
        }
        else // noun
        if (parsetype.startsWith("V"))
        {
            basetype = "VB";
        }
        else // verb
        if (parsetype.startsWith("J"))
        {
            basetype = "JJ";
        }
        else // adjective
        if (parsetype.startsWith("RB"))
        {
            basetype = "RB";
        }
        else // adverb
        if (parsetype.startsWith("PRP"))
        {
            basetype = "PRP";
        }
        else
        {
            // pronoun
            basetype = parsetype;
        }     
        return basetype;
    }

    public static String base64Encode(String data) throws Exception {
        try
        {
            byte[] encData_byte = new byte[data.length()];
            encData_byte = EncodingSupport.GetEncoder("UTF-8").getBytes(data);
            byte[] encodedData_byte = Base64.encodeBase64(encData_byte);
            return new String(encodedData_byte, StandardCharsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new Exception("Error in base64Encode" + e.getMessage());
        }
    
    }
    
    public static boolean isCharPunctuation(Character ch)
    {
    	int charType = Character.getType(ch);
        return (charType == Character.CONNECTOR_PUNCTUATION ||
        		charType == Character.DASH_PUNCTUATION ||
        		charType == Character.END_PUNCTUATION ||
        		charType == Character.FINAL_QUOTE_PUNCTUATION ||
        		charType == Character.INITIAL_QUOTE_PUNCTUATION ||
        		charType == Character.OTHER_PUNCTUATION ||
        		charType == Character.START_PUNCTUATION);
    }

    public static String spanKey(Span sp)
    {
    	// returns a string looking like "[12..123) <type>"
    	return sp.toString();
    }

    public static int spanStartFromKey(String s)
    {
    	int sqBracketPos = s.indexOf('[');
    	int dotsPos = s.indexOf("..");
    	
    	int result = Integer.parseInt(s.substring(sqBracketPos+1, dotsPos));
    	return result;
    }
    
    public static int spanEndFromKey(String s)
    {
    	// processes a string looking like "[12..123) <type>"
    	int dotsPos = s.indexOf("..");
    	int rndBracketPos = s.indexOf(')');
    	
    	int result = Integer.parseInt(s.substring(dotsPos+2, rndBracketPos));
    	return result;
    }
    
}


