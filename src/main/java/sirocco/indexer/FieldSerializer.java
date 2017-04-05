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

package sirocco.indexer;

import CS2JNet.System.Collections.LCC.CSList;
import java.io.StringReader;
import java.io.StringWriter;

public class FieldSerializer   
{
    public static void writeBool(boolean value, StringWriter wr)  {
        wr.write((value) ? 'T' : 'F');
    }

    public static boolean readBool(StringReader rd) throws Exception {
        char c = (char)rd.read();
        return (c == 'T');
    }

    public static void writeInt32(int value, StringWriter wr)  {
        wr.write(Integer.toString(value));
        wr.write('|');
    }

    public static int readInt32(StringReader rd) throws Exception {
        // if there is an error in encoding, let the base functions throw an exception
        CSList<Character> clist = new CSList<Character>();
        int nextchar;
        while (((nextchar = rd.read()) != -1) && ((char)nextchar != '|'))
        {
        	clist.add((char)nextchar);
        }
         	
        // ignore
        String number = clist.toArray().toString();
        return Integer.valueOf(number);
    }

    public static void writeString(String value, StringWriter wr)  {
        value.replace("|", "||");
        wr.write(value);
        wr.write('|');
    }

    public static String readString(StringReader rd) throws Exception {
        // if there is an error in encoding, let the base functions throw an exception
        CSList<Character> clist = new CSList<Character>();
        int nextchar = -1;
        boolean stop = false;
        while (!stop)
        {
            if ((nextchar = rd.read()) == -1)
                stop = true;
            else if ((char)nextchar != '|')
                clist.add((char)nextchar);
            else
            {
                // ignore first | and check whether there is another | following
            	// if the next one is | again, then 
            	rd.mark(1);
                if (((nextchar = rd.read()) != -1) && ((char)nextchar == '|'))
                {
                    // this was an escaped |, and so read it as such
                	clist.add((char)nextchar);
                }
                else
                {
                	rd.reset(); //return the stolen character
                    stop = true;
                }
            }  
        }
         
        return clist.toArray().toString();
    }

}


// ignore