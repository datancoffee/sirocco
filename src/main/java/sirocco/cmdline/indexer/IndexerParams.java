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

package sirocco.cmdline.indexer;

import opennlp.tools.cmdline.ArgumentParser.OptionalParameter;
import opennlp.tools.cmdline.ArgumentParser.ParameterDescription;
import opennlp.tools.cmdline.params.EncodingParameter;

import java.io.File;

/**
 * Params for Dictionary tools.
 *
 * Note: Do not use this class, internal use only!
 */
interface IndexerParams extends EncodingParameter {

  @ParameterDescription(valueName = "inputFile", description = "Name of file with input document to be indexed.")
  File getInputFile();

  @ParameterDescription(valueName = "outputFile", description = "Name of file with the output sentiment index.")
  File getOutputFile();
  
  @ParameterDescription(valueName = "indexingType", description = "Indexing type: TOPSENTIMENTS, FULLINDEX, TEXTENCODING.")
  String getIndexingType();

  @ParameterDescription(valueName = "parsingType", description = "Parsing type: DEEP, SHALLOW, DEPENDENCY.")
  @OptionalParameter (defaultValue = "SHALLOW")
  String getParsingType();
  
  @ParameterDescription(valueName = "recordDelimiters", description = "(Optional, Default=30) Record delimiter characters dividing records in the file. If multiple characters are used, separate them by a comma, e.g. 13,10 stands for <CR><LF>. Use delimiter 30 (=<RS>) to prevent regular new line delimiters splitting text files.")
  @OptionalParameter (defaultValue = "30")
  String getRecordDelimiters();

  @ParameterDescription(valueName = "readAsCSV", description ="(Optional, Default=false) Should records be processed as CSV")
  @OptionalParameter (defaultValue = "false")
  Boolean getReadAsCSV();

  @ParameterDescription(valueName = "textColumnIdx", description ="(Optional) CSV inputs: Zero-based index of the Text column in input file")
  @OptionalParameter (defaultValue = "")
  String getTextColumnIdx();
	
  @ParameterDescription(valueName = "collectionItemIdIdx", description ="(Optional) CSV inputs: Zero-based index of the Collection Item ID column - unique identifier - in input file")
  @OptionalParameter (defaultValue = "")
  String getCollectionItemIdIdx();
  

}
