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

package sirocco.model;

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;
import sirocco.indexer.util.LangUtils;

import java.util.HashMap;

public class SentenceFlags   
{
    public TextStats SentenceStats = new TextStats();
    public TextStats ParagraphStats = null;
    //this is a just a reference
    public HashMap<String,SpanFlags> SpanFlags = new HashMap<String,SpanFlags>();
    public CSList<Span> Quotes = new CSList<Span>();
    // this list is built in Chunk() in order of tokens do it is sorted
    public SpanFlags getSpanFlags(Span span) {
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));

        if (flags==null)
        {
            flags = new SpanFlags();
            SpanFlags.put(LangUtils.spanKey(span),flags);
        }
        return flags;
    }

    public void addIdiomOccurence(CSList<Parse> parses, String idiomkey) throws Exception{
        if ((parses == null) || (parses.size() == 0))
            return ;
         
        Parse parentOfAll = parses.get(0).getParent();
        for (int j = 1;j < parses.size();j++)
        {
            if (parentOfAll == null)
                break;
             
            parentOfAll = parentOfAll.getCommonParent(parses.get(j));
        }
        if (parentOfAll == null)
            return ;
         
        IdiomOccurrence occurrence = new IdiomOccurrence(parses,idiomkey);
        for (Parse parse : parses)
            getSpanFlags(parse.getSpan()).IdiomOccurrence = occurrence;
        getSpanFlags(parentOfAll.getSpan()).IncludedIdioms.Add(occurrence);
    }

    public boolean isIdiom(Span span){
        
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return false;
        return (flags.IdiomOccurrence != null);
        
    }

    public boolean isSentiment(Span span){
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return false;
         
        return flags.IsSentiment;
    }

    public boolean isAllCaps(Span span) {
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return false;
        return flags.IsAllCaps;
    }

    public boolean isFirstCap(Span span) {
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return false;
        return flags.IsFirstCap;
    }

    public boolean isLink(Span span) {
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return false;
         
        return flags.IsLink;
    }

    public boolean isHashtag(Span span) {
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return false;
        return flags.IsHashtag;
    }

    public boolean isIdiomParent(Span span) {
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return false;
        return flags.IncludedIdioms.size() > 0;
    }

    public boolean isInQuotes(Span span) {
        boolean foundprioropenquote = false;
        for (int i = 0;i < Quotes.size();i++)
        {
            if (Quotes.get(i).getStart() < span.getStart())
                foundprioropenquote = !foundprioropenquote;
            else
                break; 
        }
        return foundprioropenquote;
    }

    public String getOriginalText(Span span) {
        SpanFlags flags = SpanFlags.get(LangUtils.spanKey(span));
        if (flags==null)
            return null;
        return flags.OriginalText;
    }

}


