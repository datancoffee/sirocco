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

import opennlp.tools.cmdline.BasicCmdLineTool;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import sirocco.indexer.Indexer;
import sirocco.indexer.IndexingConsts;
import sirocco.indexer.util.LogUtils;
import sirocco.model.ContentIndex;
import sirocco.model.serialization.AvroSerializer;
import sirocco.model.summary.ContentIndexSummary;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class IndexerTool extends BasicCmdLineTool {

  public static final String DOC_COL_ID_CSV_FILE = "04";
  
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
    IndexingConsts.ParseDepth parsingType =  IndexingConsts.ParseDepth.valueOf(params.getParsingType());
    
    String recordDelimiters = params.getRecordDelimiters();
    Boolean readAsCSV = params.getReadAsCSV();

    Integer textColumnIdx = (params.getTextColumnIdx()!=null) ? Integer.valueOf(params.getTextColumnIdx()) : null;
	Integer collectionItemIdIdx = (params.getCollectionItemIdIdx()!=null) ? Integer.valueOf(params.getCollectionItemIdIdx()) : null;
    
    CmdLineUtil.checkInputFile("input file of the document to be indexed", dictInFile);
    CmdLineUtil.checkOutputFile("output file with sentiment index", dictOutFile);

    InputStreamReader in = null;
    OutputStream out = null;
    OutputStream outAvro = null;
    
    try {
      in = new InputStreamReader(new FileInputStream(dictInFile), encoding);
      out = new FileOutputStream(dictOutFile);
      outAvro = new FileOutputStream(dictOutFile+".avro");
      
      ArrayList<ContentIndex> docList = new ArrayList<ContentIndex>();
      
      if (readAsCSV == false) {
          
    	  String inputFileContents = IOUtils.toString(in);
    	  long now = System.currentTimeMillis();

          ContentIndex contentindex = new ContentIndex(inputFileContents,
        		  indexingType,
                  IndexingConsts.ContentType.ARTICLE,
                  now);
          contentindex.ContentParseDepth = parsingType; //TODO: Move to constructor
          docList.add(contentindex);

      } else {
		
		Iterable<CSVRecord> records = CSVFormat.DEFAULT
			.withFirstRecordAsHeader()
			.parse(in);
		
		for (CSVRecord record : records) {
			
			long now = System.currentTimeMillis();
			
			String text = record.get(textColumnIdx);
			String documentCollectionId = DOC_COL_ID_CSV_FILE;
			String collectionItemId = (collectionItemIdIdx!=null)? record.get(collectionItemIdIdx): null;
			
			ContentIndex contentindex = new ContentIndex(/*content*/ text, /*indexingType*/ indexingType, 
		    		/*cueType*/ IndexingConsts.ContentType.ARTICLE, /*processingTime*/ now,
		    	    /*url*/ null, /*publicationTime*/ null,  /*title*/ null, /*author*/ null, 
		    	    /*documentCollectionId*/ documentCollectionId, /*collectionItemId*/ collectionItemId,
		    	    /*parentUrl*/ null, /*parentPubTime*/ null, /*metaFields*/ null);
			contentindex.ContentParseDepth = parsingType; //TODO: Move to constructor
			docList.add(contentindex);
	          
		}
      } 
	  
      // iterate through all collected docs
      for (ContentIndex ci : docList) {

          Indexer.index(ci);
          
          StringBuilder sb = new StringBuilder();
          LogUtils.printIndex(1, ci, sb);
          String docIndex = sb.toString();
          
          IOUtils.write(docIndex,out,Charset.forName("utf-8"));
          ContentIndexSummary summary = ci.getContentIndexSummary();
          AvroSerializer.writeContentIndexSummary(summary, outAvro);

	  }
      

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