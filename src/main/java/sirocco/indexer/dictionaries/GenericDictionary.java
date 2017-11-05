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

package sirocco.indexer.dictionaries;

import CS2JNet.System.Collections.LCC.CSList;
import CS2JNet.System.IO.StreamReader;
import CS2JNet.System.StringSupport;
import CS2JNet.System.Text.EncodingSupport;
import sirocco.indexer.FloatVector;
import sirocco.indexer.IGenericVector;
import sirocco.indexer.IGenericVectorFactory;
import sirocco.indexer.util.LangUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map.Entry;

public class GenericDictionary<TVector extends IGenericVector>  
{
    private String CommaReplacement = "<123comma456>";
    private HashMap<String,TVector> words = new HashMap<String,TVector>();
    private CSList<String> errors = new CSList<String>();
    private IGenericVectorFactory vectorFactory = null;
    
    // words we could not read or add
    public HashMap<String,TVector> getWords()  {
        return words;
    }

    public String dictionaryFile()  {
        return null;
    }

     
    public GenericDictionary(InputStream dictionarystream, IGenericVectorFactory factory)  {
    	vectorFactory = factory;
        loadDictionary(dictionarystream);
    }

    private void loadDictionary(String dictionaryFile)  {
        BufferedReader sr;
		try {
			sr = new BufferedReader(StreamReader.make(new BufferedInputStream(new FileInputStream(dictionaryFile)), new EncodingSupport("utf-8")));
			loadDictionary(sr);
		} catch (FileNotFoundException e) {
			errors.add("FileNotFoundException:\n" + e.getMessage());
		} catch (IOException e) {
			errors.add("IOException:\n" + e.getMessage());
		}
    }

    private void loadDictionary(InputStream stream)  {
        BufferedReader sr;
		try {
			sr = new BufferedReader(StreamReader.make(new BufferedInputStream(stream), new EncodingSupport("utf-8")));
		    loadDictionary(sr);
		} catch (IOException e) {
			errors.add("IOException:\n" + e.getMessage());
		}
    }

    private void loadDictionary(BufferedReader sr)  {
        String line;
        int cnt = 0;
        int numkeyfields = 0;
        int numfields = 0;
        try {
            // first line is the header
			line = sr.readLine();
	        String[] fields = StringSupport.Split(line, ',');
	        CSList<String> fieldnames = new CSList<String>();
	        numfields = fields.length;
	        for (int i = 0;i < numfields;i++)
	        {
	            if (fields[i].contains(FloatVector.KeyEnding))
	                numkeyfields++;
	            else
	                fieldnames.add(fields[i].replace("\"\"", "")); 
	        }
			
	        // continue with data lines
	        while ((line = sr.readLine()) != null)
			{
			    try
			    {
			        processLine(line,fieldnames,numkeyfields);
			    }
			    catch (Exception ex)
			    {
			        errors.add(line + "\nException:\n" + ex.toString());
			    }
			}
		} catch (IOException e) {
			errors.add("\nIOException:\n" + e.getMessage());
			return;
		}
    }

    void processLine(String line, CSList<String> fieldnames, int numkeyfields) throws Exception {
        if (line.length() == 0)
            return ;
         
        String[] fields = null;
        String linewrk = "";
        boolean inQuotes = false;
        for (int i = 0;i < line.length();i++)
        {
            if (line.charAt(i) == '"')
                inQuotes = !inQuotes;
            else if (line.charAt(i) == ',')
            {
                if (inQuotes)
                    linewrk += CommaReplacement;
                else
                    linewrk += ','; 
            }
            else
                linewrk += line.charAt(i);  
        }
        fields = StringSupport.Split(linewrk, ',');
        for (int i = 0;i < fields.length;i++)
        {
            fields[i] = fields[i].replace(CommaReplacement, ","); //TODO: this could be improved. No need to check numeric fields. Only Key Fields.
        }
        String key = "";
        for (int keyfield = 0;keyfield < numkeyfields;keyfield++)
        {
            String keyfieldval;
            if (fields[keyfield].charAt(0) == '"')
                keyfieldval = fields[keyfield].substring(1, (1) + (fields[keyfield].length() - 2));
            else
                keyfieldval = fields[keyfield]; 
            key += ((!StringSupport.equals(key, "")) ? "/" : "") + keyfieldval;
        }
        String[] vectorfields = new String[fields.length - numkeyfields];
        for (int i = 0;i < fields.length - numkeyfields;i++)
        {
            vectorfields[i] = fields[numkeyfields + i];
        }
        TVector v = (TVector) vectorFactory.createNewVector();
        String[] fieldnamesarray = fieldnames.toArray(new String[fieldnames.size()]);
        v.init(fieldnamesarray, vectorfields,key);
        getWords().put(key, v);
    }

    public void saveDictionary(String filename) throws Exception {
        int linecount = 0;
        PrintWriter sr = null;
        for (Entry<String,TVector> kvp : this.getWords().entrySet())
        {
            if (linecount == 0)
            {
                sr = new PrintWriter(filename, "utf-8");
                String header = "key," + LangUtils.printStringList(new CSList<String>(kvp.getValue().getDimensions()),",");
                sr.println(header);
            }
             
            String line = "\"" + kvp.getKey() + "\"," + kvp.getValue().toCSV();
            sr.println(line);
            linecount++;
        }
        if (sr != null)
            sr.close();
         
    }

}


