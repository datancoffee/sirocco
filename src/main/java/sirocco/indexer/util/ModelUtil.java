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

public class ModelUtil {

	public ModelUtil() {
	}

/*	
	
	public static void convertContentIndex(ContentIndex index)
	{
        // Create sentiments
        List<Sentiment> sentimentsList = new List<Sentiment>();
        if (contentindex.TopSentiments != null) {
            foreach (LabelledText text in contentindex.TopSentiments) {
                Sentiment sent = CueUtils.CreateSentimentFromLabelledText(text);
                sentimentsList.Add(sent);
            }
        }

		
	}
	
	public static Sentiment CreateSentimentFromLabelledText(LabelledText text) {
	            Sentiment sent = new Sentiment();
	            sent.Created = DateTime.UtcNow;

	            sent.Text = text.Text;
	            sent.LabelledPositions = (text.LabelledPositions != null) ? text.LabelledPositions.StringSerialize() : "";
	            sent.StAcceptance = GetShortValue(text.AggregateSentiment, SentimentDimension.Acceptance);
	            sent.StAnger = GetShortValue(text.AggregateSentiment, SentimentDimension.Anger);
	            sent.StAnticipation = GetShortValue(text.AggregateSentiment, SentimentDimension.Anticipation);
	            sent.StDisgust = GetShortValue(text.AggregateSentiment, SentimentDimension.Disgust);
	            sent.StFear = GetShortValue(text.AggregateSentiment, SentimentDimension.Fear);
	            sent.StGuilt = GetShortValue(text.AggregateSentiment, SentimentDimension.Guilt);
	            sent.StInterest = GetShortValue(text.AggregateSentiment, SentimentDimension.Interest);
	            sent.StJoy = GetShortValue(text.AggregateSentiment, SentimentDimension.Joy);
	            sent.StSadness = GetShortValue(text.AggregateSentiment, SentimentDimension.Sadness);
	            sent.StShame = GetShortValue(text.AggregateSentiment, SentimentDimension.Shame);
	            sent.StSurprise = GetShortValue(text.AggregateSentiment, SentimentDimension.Surprise);
	            sent.StPositive = GetShortValue(text.AggregateSentiment, SentimentDimension.Positive);
	            sent.StNegative = GetShortValue(text.AggregateSentiment, SentimentDimension.Negative);
	            sent.StSentiment = GetShortValue(text.AggregateSentiment, SentimentDimension.GeneralSentiment);
	            sent.StProfane = GetShortValue(text.AggregateSentiment, SentimentDimension.Profane);
	            sent.StUnsafe = GetShortValue(text.AggregateSentiment, SentimentDimension.Unsafe);
	            sent.SentimentTotalScore = (int)text.AggregateSentimentScore;
	            sent.DominantValence = (byte)text.AggregateSentiment.DominantValence();

	            if ((text.ContainedEntities != null) && (text.ContainedEntities.Count > 0))
	            {
	                List<SentimentTag> sentTags = new List<SentimentTag>();
	                foreach (string textTag in text.ContainedEntities)
	                {
	                    string tag = textTag;
	                    if (tag == null) continue;
	                    tag = tag.Trim();
	                    if (tag.Length == 0) continue;
	                    sentTags.Add(new SentimentTag(tag, long.MinValue));
	                }
	                sent.Tags = sentTags.ToArray();
	            }
	            else
	                sent.Tags = new SentimentTag[0];

	            return sent;
	        }

	public static WebResource CreateWebResource(TextTag[] textTags, long initialCueID, string url, string title) {
	            WebResource webResource = new WebResource();
	            webResource.InitialCueID = initialCueID;
	            webResource.URL = url;
	            webResource.Title = title;
	            if ((textTags != null) && (textTags.Length > 0))
	            {
	                List<WebResourceTag> webTags = new List<WebResourceTag>();
	                foreach (TextTag textTag in textTags)
	                {
	                    string tag = textTag.Word;
	                    if (tag == null) continue;
	                    tag = tag.Trim();
	                    if (tag.Length == 0) continue;
	                    webTags.Add(new WebResourceTag(tag, long.MinValue, textTag.GoodAsTopic));
	                }
	                webResource.Tags = webTags.ToArray();
	            }
	            else
	                webResource.Tags = new WebResourceTag[0];
	            
	            return webResource;
	        }

	private static short GetShortValue(FloatVector vector, string dimension) {
	            float value = 0;
	            vector.TryGetValue(dimension, out value);
	            if (value > byte.MaxValue) value = byte.MaxValue;
	            return (short)value;
	        }

	private static void MakeCuePlainTextDescription(CueBase rc) {
	            if (string.IsNullOrEmpty(rc.Description))
	                return;
	            if (rc.CueSource != CueConsts.CueSources.Blog)
	                return;
	            try {
	                string plainText = HtmlProcessorPool.Instance.GetPlainText(rc.Description);
	                rc.Description = plainText;
	            }
	            catch {
	                //WriteLog("HtmlProcessorPool.Instance.GetPlainText error: {0}", e.Message);
	            }
	        }

	public static RemoteCue[] PostProcessRemoteCue(RemoteCue rc) {
		return PostProcessRemoteCue(rc, true);
	}

	public static RemoteCue[] PostProcessRemoteCue(RemoteCue rc, bool allowBookmarks) {
		RemoteCue[] res = new RemoteCue[] { rc };
		MakeCuePlainTextDescription(rc);
		return res;
	}

}

*/

}
