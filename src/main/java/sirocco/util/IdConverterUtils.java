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
package sirocco.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.apache.commons.codec.binary.Base64;

public class IdConverterUtils   
{	// TODO: Consider using java.time.Instant instead of java.util.Date
	public static int getDateIdFromTimestamp(long millis)
	{
    	int result;
    	Instant i = Instant.ofEpochMilli(millis);
    	LocalDateTime ldt = LocalDateTime.ofInstant(i, ZoneId.of("UTC") ); //ZoneId.systemDefault()
    	int year = ldt.getYear();
    	int month = ldt.getMonth().getValue();
    	int day = ldt.getDayOfMonth();
    	result = day + (100 * month) + (10000 * year);
    	return result;
	}
	
	/*
    public static String convertIDToUrlID(long id) throws Exception {
        byte[] ba = BitConverter.GetBytes(Math.Abs(id));
        String prefix = (id < 0) ? "*" : null;
        return convertByteArrayToBase64String(ba,prefix);
    }
	*/
	
    public static String convertByteArrayToBase64String(byte[] ba, String prefix)  {
        /*
    	int end = ba.length - 1;
        while ((end > 0) && (ba[end] == 0))
        	end--;
        String s = Base64.encodeBase64URLSafeString(ba, 0, end + 1);
        */
    	
        String s = Base64.encodeBase64URLSafeString(ba);
        
        /* probably not needed because we are using encodeBase64URLSafeString
        s = s.replace('/', '_');
        s = s.replace('+', '-');
        s = StringSupport.TrimEnd(s, new char[]{ '=' });
        */
        if (prefix != null)
            s = prefix + s;
         
        return s;
    }

    /*
    public static long convertUrlIDToID(String id) throws Exception {
        long res;
        boolean boolVar___0 = tryConvertUrlIDToID(id,refVar___0);
        if (boolVar___0)
        {
            RefSupport<Long> refVar___0 = new RefSupport<Long>();
            Long resVar___1 = res;
            res = refVar___0.getValue();
            return resVar___1;
        }
        else
            throw new RuntimeException("Invalid UrlID format"); 
    }

    public static boolean tryConvertUrlIDToID(String strID, RefSupport<Long> id) throws Exception {
        id.setValue(0);
        if (StringSupport.isNullOrEmpty(strID))
            return false;
         
        Long mul = 1;
        String str;
        if (strID.charAt(0) == '*')
        {
            if (strID.length() == 1)
                return false;
             
            str = strID.substring(1);
            mul = -1;
        }
        else
            str = strID; 
        switch(str.length() % 4)
        {
            case 1: 
                return false;
            case 2: 
                str += "==";
                break;
            case 3: 
                str += "=";
                break;
        
        }
        str = str.replace('_', '/');
        str = str.replace('-', '+');
        byte[] ba;
        try
        {
            ba = Base64.decodeBase64(str);
        }
        catch (Exception __dummyCatchVar0)
        {
            return false;
        }

        if (ba.length != 8)
        {
            RefSupport<byte[]> refVar___1 = new RefSupport<byte[]>(ba);
            Array.Resize(refVar___1, 8);
            ba = refVar___1.getValue();
        }
         
        id.setValue(Math.Abs(BitConverter.ToInt64(ba, 0)) * mul);
        return true;
    }
    */

}


