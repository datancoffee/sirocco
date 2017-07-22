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

import CS2JNet.System.Collections.LCC.CSList;
import CS2JNet.System.StringSupport;
import sirocco.annotators.BriefLogTextAnnotator;
import sirocco.indexer.Language;
import sirocco.indexer.SentimentDimension;
import sirocco.indexer.SentimentValenceHelper;
import sirocco.indexer.IndexingConsts.SentimentValence;
import sirocco.model.ContentIndex;
import sirocco.model.LabelledText;
import sirocco.model.TextTag;

import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;

public class LogUtils   
{
    private static Object logFileLock = new Object();
    public static void logNLP(String text) throws Exception {
        synchronized (logFileLock)
        {
            {
                PrintWriter sr = new PrintWriter(".\\nlp.txt", "UTF-8");
                sr.print(text);
                sr.close();
            }
        }
    }

    public static void printIndex(long taskID, ContentIndex contentindex, StringBuilder sb) throws Exception {
        sb.append(("----------- TASK [" + taskID + "] --------------") + System.getProperty("line.separator"));
        if (StringSupport.equals(contentindex.Language, Language.English))
        {
            printStats(taskID,contentindex,sb);
            sb.append(System.lineSeparator());
            printAttributes(taskID,contentindex,sb);
            sb.append(System.lineSeparator());
            printTags(taskID,contentindex.TopTags,sb);
            sb.append(System.lineSeparator());
            printTopSentiments(taskID,contentindex,sb);
            sb.append(System.lineSeparator());
            printLinks(taskID,contentindex.getLinks(),sb);
            sb.append(System.lineSeparator());
            printString(taskID,contentindex.getFullAnnotatedText(true),"FULL ANNOTATED TEXT",sb);
            sb.append(System.lineSeparator());
            printString(taskID,contentindex.OriginalText,"FULL ORIGINAL TEXT",sb);
        }
        else
        {
            printStats(taskID,contentindex,sb);
            sb.append(System.lineSeparator());
            printAttributes(taskID,contentindex,sb);
            sb.append(System.lineSeparator());
            printTags(taskID,contentindex.TopTags,sb);
            sb.append(System.lineSeparator());
            printTopSentiments(taskID,contentindex,sb);
            sb.append(System.lineSeparator());
            printString(taskID,contentindex.OriginalText,"FULL ORIGINAL TEXT",sb);
        } 
    }

    public static void printTopSentiments(long taskID, ContentIndex contentindex, StringBuilder sb) throws Exception {
        sb.append(("TOP SENTIMENTS Begin:") + System.getProperty("line.separator"));
        if (contentindex.SelectedSentiments == null)
            return ;
         
        BriefLogTextAnnotator annotator = new BriefLogTextAnnotator();
        for (int i = 0;i < contentindex.SelectedSentiments.size();i++)
        {
            LabelledText ltext = contentindex.SelectedSentiments.get(i);
            sb.append("Sentiment {" + i + "} Tags: ");
            sb.append(LangUtils.printStringList(ltext.ContainedEntities,", "));
            sb.append(System.lineSeparator());
            sb.append(("Sentiment {" + i + "} Dominant Valence: " + SentimentValenceHelper.valenceLabel(ltext.AggregateSentiment.dominantValence())) + System.getProperty("line.separator"));
            sb.append(("Sentiment {" + i + "} Total Sentiment Score: " + ltext.AggregateSentimentScore) + System.getProperty("line.separator"));
            sb.append("Sentiment {" + i + "} Annotated Text: ");
            String woLabels = annotator.annotate(ltext.LabelledPositions,ltext.Text);
            sb.append(woLabels);
            sb.append(System.lineSeparator());
            sb.append("Sentiment {" + i + "} Serialized Representation: ");
            String data = ltext.LabelledPositions.stringSerialize();
            sb.append(data);
            sb.append(System.lineSeparator());
        }
    }

    public static void printStats(long taskID, ContentIndex contentindex, StringBuilder sb) throws Exception {
        sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("TEXT STATS Length [{0}] "),contentindex.OriginalText.length()));
        if (contentindex.ParagraphIndexes != null)
            sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("Paragraphs [{0}] "),contentindex.ParagraphIndexes.length));
         
        if (contentindex.ContentEntityStats != null)
            sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("Unique Entities [{0}] "),contentindex.ContentEntityStats.size()));
         
        printOperationTime(contentindex.ActionTimestamps,"Index",sb);
        printOperationTime(contentindex.ActionTimestamps,"Chunk",sb);
        printOperationTime(contentindex.ActionTimestamps,"Parse",sb);
    }

    public static void printAttributes(long taskID, ContentIndex contentindex, StringBuilder sb) throws Exception {
    	printString(taskID,  contentindex.Url,"URL",sb);
    	printLong(taskID,  contentindex.PublicationTime,"PublicationTime",sb);
    	printString(taskID,  contentindex.Title,"Title",sb);
    	printString(taskID,  contentindex.Author,"Author",sb);
    	printString(taskID,  contentindex.DocumentCollectionId,"DocumentCollectionId",sb);
    	printString(taskID,  contentindex.CollectionItemId,"CollectionItemId",sb);
    	printString(taskID,  contentindex.ParentUrl,"ParentUrl",sb);
    	printLong(taskID,  contentindex.ParentPubTime,"ParentPubTime",sb);
    }

    private static void printOperationTime(HashMap<String,Date> timestamps, String operation, StringBuilder sb) throws Exception {
        Date start = timestamps.get(operation + ":start");
        Date stop = timestamps.get(operation + ":stop");
        if (stop!=null && start!=null)
        {
            long duration = stop.getTime() - start.getTime();
            sb.append(String.format(StringSupport.CSFmtStrToJFmtStr(operation + " [{0}] ms "),duration));
        }
         
    }

    private static String goodAsTopicLabel(Boolean goodAsTopic) throws Exception {
        if (goodAsTopic == null)
            return "Undetermined Topic";
        else if (goodAsTopic)
            return "Good Topic";
        else
            return "Bad Topic";  
    }

    public static void printTags(long taskID, TextTag[] tags, StringBuilder sb) throws Exception {
        sb.append(("TAGS Begin:") + System.getProperty("line.separator"));
        if (tags == null)
            return ;
         
        for (TextTag tag : tags)
            sb.append(String.format("%.1f \"%s\" [%s]\r\n",tag.getWeight(),tag.getWord(),goodAsTopicLabel(tag.getGoodAsTopic())));
    }

    public static void printLinks(long taskID, CSList<String> links, StringBuilder sb) throws Exception {
        if ((links == null) || (links.size() == 0))
            return ;
         
        sb.append(("LINKS Begin:") + System.getProperty("line.separator"));
        for (String link : links)
            sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("{0}\r\n"),link));
    }

    public static void printString(long taskID, String text, StringBuilder sb) throws Exception {
        if (!StringSupport.isNullOrEmpty(text))
            sb.append((text) + System.getProperty("line.separator"));
         
    }

    public static void printString(long taskID, String text, String label, StringBuilder sb) throws Exception {
        sb.append((label + " Begin:") + System.getProperty("line.separator"));
        printString(taskID, text, sb);
    }

    public static void printLong(long taskID, Long number, String label, StringBuilder sb) throws Exception {
    	String text = (number == null) ? null : number.toString();
        printString(taskID, text, label, sb);
    }

    public static void logSummary(String text) throws Exception {
        PrintWriter sr = new PrintWriter(".\\summary.txt", "UTF-8");
        sr.print(text);
        sr.close();
    }

    public static void generateSQL(ContentIndex contentindex, StringBuilder sb) throws Exception {
        sb.append(System.lineSeparator());
        sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("insert into Cues (ID, URL) values (<ID>,<URL>);\r\n")));
        sb.append(System.lineSeparator());
        sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("insert into WebResources (ID, URL) values (<ID>,<URL>);\r\n")));
        sb.append(System.lineSeparator());
        for (TextTag tag : contentindex.TopTags)
        {
            sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("insert into WebResourceTags (ID, WebResourceID, Tag, DicTagID) " + "values (<ID> , <WebResourceID>, '{0}', <DicTagID>);\r\n"),tag.getWord()));
        }
        sb.append(System.lineSeparator());
        for (int i = 0;i < contentindex.SelectedSentiments.size();i++)
        {
            LabelledText ltext = contentindex.SelectedSentiments.get(i);
            String valText = ltext.Text;
            String valLabelledPositions = ltext.LabelledPositions.stringSerialize();
            /* test code
                            PositionsDictionary deserialized = PositionsDictionary.StringDeserialize(valLabelledPositions);
                            LogTextAnnotator annotator = new LogTextAnnotator();
                            string annotated = annotator.Annotate(deserialized, valText, true);
                            sb.AppendLine(annotated);
                            */
            int valStAcceptance = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Acceptance);
            int valStAnger = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Anger);
            int valStAnticipation = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Anticipation);
            int valStDisgust = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Disgust);
            int valStFear = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Fear);
            int valStGuilt = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Guilt);
            int valStInterest = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Interest);
            int valStJoy = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Joy);
            int valStSadness = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Sadness);
            int valStShame = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Shame);
            int valStSurprise = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Surprise);
            int valStPositive = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Positive);
            int valStNegative = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Negative);
            int valStSentiment = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.GeneralSentiment);
            int valStProfane = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Profane);
            int valStUnsafe = LangUtils.getIntValue(ltext.AggregateSentiment,SentimentDimension.Unsafe);
            int valSentimentTotalScore = (int)ltext.AggregateSentimentScore;
            SentimentValence valDominantValence = ltext.AggregateSentiment.dominantValence();
            sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("insert into Sentiments ( " + "ID, WebResourceID, " + "Text, LabelledPositions, " + "StAcceptance, StAnger, StAnticipation, StDisgust, " + "StFear, StGuilt, StInterest, StJoy, " + "StSadness, StShame, StSurprise, StPositive, " + "StNegative, StSentiment, StProfane, StUnsafe, " + "SentimentTotalScore, DominantValence, Created) " + "values (<ID>, <WebResourceID>, " + "'{0}', '{1}', " + "{2}, {3}, {4}, {5}, " + "{6}, {7}, {8}, {9}, " + "{10}, {11}, {12}, {13}, " + "{14}, {15}, " + "{16}, {17}, <Created> );\r\n"),valText,valLabelledPositions,valStAcceptance,valStAnger,valStAnticipation,valStDisgust,valStFear,valStGuilt,valStInterest,valStJoy,valStSadness,valStShame,valStSurprise,valStPositive,valStNegative,valStSentiment,valStProfane,valStUnsafe,valSentimentTotalScore,valDominantValence));
            for (String entity : ltext.ContainedEntities)
            {
                sb.append(String.format(StringSupport.CSFmtStrToJFmtStr("insert into SentimentTags (ID, SentimentID, Tag, DicTagID) " + "values (<ID> , <SentimentID>, '{0}', <DicTagID>);\r\n"),entity));
            }
        }
        sb.append(System.lineSeparator());
    }

}


