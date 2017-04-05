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

package sirocco.util;

import CS2JNet.System.IO.FileNotFoundException;
import CS2JNet.System.StringSupport;
import CS2JNet.System.Text.StringBuilderSupport;

public class ExceptionUtils   
{
    public static String getFullErrorText(Exception ex) throws Exception {
        StringBuilder res = new StringBuilder();
        res.append("Exception: ");
        if (ex instanceof FileNotFoundException)
            // sso 8/29: The CS2JNet implementation does not expose the file name, so disabling this for now
        	// res.append(String.format(StringSupport.CSFmtStrToJFmtStr("File not found: {0}"),((FileNotFoundException)ex).FileName));
    		res.append(String.format(StringSupport.CSFmtStrToJFmtStr("File not found: {0}"),"Unknown File Name"));
        else
            res.append(ex.getMessage()); 
        res.append("\nException Type: ");
        res.append(ex.getClass().getName());
        res.append("\n=== StackTrace ===\n");
        res.append(ex.getStackTrace().toString());
        Exception e = ((Exception)ex.getCause());
        while (e != null)
        {
            res.append("\nInner Exception: ");
            res.append(e.getMessage());
            res.append("\nInner Exception Type: ");
            res.append(e.getClass().getName());
            res.append("\n=== Inner Exception StackTrace ===\n");
            res.append(e.getStackTrace().toString());
            e = ((Exception)e.getCause());
        }
        return res.toString();
    }

    public static String getErrorText(Exception ex) throws Exception {
        StringBuilder res = new StringBuilder();
        Exception e = ex;
        while (e != null)
        {
            if (res.length() > 0)
                res.append(System.lineSeparator());;
             
            res.append(e.getMessage());
            e = ((Exception)e.getCause());
        }
        return res.toString();
    }

}


