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

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.EnglishTokenizer;
import CS2JNet.System.StringSupport;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

public class KeywordDictionary   
{
    private HashMap<String,TreeMap<String, KeywordNode>> buckets = new HashMap<String,TreeMap<String, KeywordNode>>();
    private int BucketKeyLength;
    private static ReverseStringComparer reverseComparer = new ReverseStringComparer();
    CSList<String> errors = new CSList<String>();
    public KeywordDictionary(int bucketkeylength) throws Exception {
        BucketKeyLength = bucketkeylength;
    }

    public void addKeyword(String word, boolean requiresSeparatorEnclosure, String source) throws Exception {
        if (word.length() < BucketKeyLength)
        {
            errors.add(word + "\nError:\n" + "Length of keyword must be >= " + BucketKeyLength);
            return ;
        }
         
        String bucketkey = word.substring(0, (0) + (BucketKeyLength));
        String restofword = word.substring(BucketKeyLength);
        TreeMap<String, KeywordNode> bucket = getBucket(bucketkey);
        if (bucket.containsKey(restofword))
        {
            errors.add(word + "\nError:\n" + "Attempting to insert a duplicate keyword");
            return ;
        }
         
        bucket.put(restofword,new KeywordNode(word,requiresSeparatorEnclosure,source));
    }

    public void addKeyword(String word, boolean requiresSeparatorEnclosure, String source, boolean addAllLower, boolean addFirstCaps, boolean addAllCaps) throws Exception {
        if (addAllLower)
            if (!StringSupport.equals(word.toLowerCase(), word))
                addKeyword(word.toLowerCase(),requiresSeparatorEnclosure,source);
             
         
        if (addAllCaps)
            if (!StringSupport.equals(word.toUpperCase(), word))
                addKeyword(word.toUpperCase(),requiresSeparatorEnclosure,source);
             
         
        if (addFirstCaps)
        {
            CSList<Character> seps = new CSList<Character>(new Character[]{ ' ', '-' });
            String firstwordCaps = "";
            String allwordsCaps = "";
            for (int i = 0;i < word.length();i++)
            {
                if (i == 0)
                {
                    firstwordCaps += Character.toUpperCase(word.charAt(0));
                    allwordsCaps += Character.toUpperCase(word.charAt(0));
                }
                else if (seps.contains(word.charAt(i - 1)))
                {
                    firstwordCaps += word.charAt(i);
                    allwordsCaps += Character.toUpperCase(word.charAt(i));
                }
                else
                {
                    firstwordCaps += word.charAt(i);
                    allwordsCaps += word.charAt(i);
                }  
            }
            if (!StringSupport.equals(allwordsCaps, word))
                addKeyword(allwordsCaps,requiresSeparatorEnclosure,source);
             
            if ((!StringSupport.equals(firstwordCaps, word)) && (!StringSupport.equals(firstwordCaps, allwordsCaps)))
                addKeyword(firstwordCaps,requiresSeparatorEnclosure,source);
             
        }
         
        addKeyword(word,requiresSeparatorEnclosure,source);
    }

    public void addRange(String[] words, boolean requiresSeparatorEnclosure, String source, boolean addAllLower, boolean addFirstCaps, boolean addAllCaps) throws Exception {
        for (String word : words)
            addKeyword(word,requiresSeparatorEnclosure,source,addAllLower,addFirstCaps,addAllCaps);
    }

    public void findLongestKeywordAtPosition(String text, int startPosition, RefSupport<String> keyword, RefSupport<String> source)  {
        keyword.setValue(null);
        source.setValue(null);
        if (startPosition + BucketKeyLength > text.length())
            return ;
         
        String bucketkey = text.substring(startPosition, (startPosition) + (BucketKeyLength));
        String restoftext = text.substring(startPosition + BucketKeyLength);
        TreeMap<String, KeywordNode> bucket = getBucket(bucketkey);
        for (Entry<String,KeywordNode> kvp : bucket.entrySet())
        {
            Integer endOfKeyword = startPosition + BucketKeyLength + kvp.getKey().length() - 1;
            if (endOfKeyword < text.length())
            {
                if (restoftext.startsWith(kvp.getKey()))
                {
                    if (kvp.getValue().RequiresSeparatorEnclosure)
                    {
                        if (EnglishTokenizer.isTokenSeparated(text,endOfKeyword))
                        {
                            source.setValue(kvp.getValue().Source);
                            keyword.setValue(kvp.getValue().Text);
                            break;
                        }
                         
                    }
                    else
                    {
                        source.setValue(kvp.getValue().Source);
                        keyword.setValue(kvp.getValue().Text);
                        break;
                    } 
                }
                 
            }
             
        }
    }

    private TreeMap<String, KeywordNode> getBucket(String bucketkey)  {
        TreeMap<String, KeywordNode> bucket = buckets.get(bucketkey);
        if (bucket == null)
        {
            bucket = new TreeMap<String, KeywordNode>(reverseComparer);
            buckets.put(bucketkey, bucket);
        }
         
        return bucket;
    }

}


