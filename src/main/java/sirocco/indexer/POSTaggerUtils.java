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
package sirocco.indexer;

import java.io.InputStream;
import java.util.Map;

import opennlp.tools.dictionary.Dictionary;
import opennlp.tools.ml.model.MaxentModel;
import opennlp.tools.ml.model.SequenceClassificationModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerFactory;
import opennlp.tools.postag.TagDictionary;

public class POSTaggerUtils {

	public static POSModel createPOSModel(InputStream modelStream) throws Exception{
		
		POSModel posModel = new POSModel(modelStream);
		
		MaxentModel meModel = posModel.getPosModel();
		
		String lang = posModel.getLanguage();
		Map<String, String> manifestInfoEntries = null;
		// TODO: If I chose to go this path, I need to copy the manifestInfoEntries from 
		// the loaded model
		
		Dictionary ngramDictionary = posModel.getNgramDictionary();
		TagDictionary tagDictionary = posModel.getTagDictionary();
		// initialize the Sentiment Analysis aware tag dictionary
		POSTagDictionary posTagDict = new POSTagDictionary(tagDictionary);
		
		POSTaggerFactory factory = new POSTaggerFactory(ngramDictionary,posTagDict);
		
		posModel = new POSModel(lang, meModel, manifestInfoEntries, factory);

		return posModel;

		
	}
	
}
