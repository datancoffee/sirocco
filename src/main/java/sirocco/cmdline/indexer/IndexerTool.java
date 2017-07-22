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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import opennlp.tools.cmdline.BasicCmdLineTool;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.dictionary.Dictionary;
import sirocco.indexer.Indexer;
import sirocco.indexer.IndexingConsts;
import sirocco.indexer.util.LogUtils;
import sirocco.model.ContentIndex;
import sirocco.model.serialization.AvroSerializer;
import sirocco.model.summary.ContentIndexSummary;

public class IndexerTool extends BasicCmdLineTool {

  interface Params extends IndexerParams {
  }

  public String getShortDescription() {
    return "creates a sentiment index of a document";
  }

  public String getHelp() {
    return getBasicHelp(Params.class);
  }

  public void run(String[] args) {
    Params params = validateAndParseParams(args, Params.class);

    File dictInFile = params.getInputFile();
    File dictOutFile = params.getOutputFile();
    Charset encoding = params.getEncoding();
    IndexingConsts.IndexingType indexingType =  IndexingConsts.IndexingType.valueOf(params.getIndexingType());

    CmdLineUtil.checkInputFile("input file of the document to be indexed", dictInFile);
    CmdLineUtil.checkOutputFile("output file with sentiment index", dictOutFile);

    InputStreamReader in = null;
    OutputStream out = null;
    OutputStream outAvro = null;
    
    try {
      in = new InputStreamReader(new FileInputStream(dictInFile), encoding);
      out = new FileOutputStream(dictOutFile);
      outAvro = new FileOutputStream(dictOutFile+".avro");
      
      String inputFileContents = IOUtils.toString(in);
      long now = System.currentTimeMillis();
      ContentIndex contentindex = new ContentIndex(inputFileContents,
    		  indexingType,
              IndexingConsts.ContentType.ARTICLE,
              now);
      
      Indexer.index(contentindex);
          
      StringBuilder sb = new StringBuilder();
      LogUtils.printIndex(1, contentindex, sb);
      String docIndex = sb.toString();
      
      IOUtils.write(docIndex,out,Charset.forName("utf-8"));
      ContentIndexSummary summary = contentindex.getContentIndexSummary();
      AvroSerializer.writeContentIndexSummary(summary, outAvro);

    } catch (IOException e) {
    	throw new TerminateToolException(-1, "IO error while reading documents or writing sentiment index: " + e.getMessage(), e);

    } catch (Exception e) {
    	throw new TerminateToolException(-1, "Error while indexing document: " + e.getMessage(), e);

    } finally {
      try {
        in.close();
        out.close();
        outAvro.close();
      } catch (IOException e) {
        // sorry that this can fail
      }
    }

  }

}