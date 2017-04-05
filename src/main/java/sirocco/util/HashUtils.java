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

import CS2JNet.JavaSupport.util.LocaleSupport;
import CS2JNet.System.Text.EncodingSupport;
import sirocco.util.IdConverterUtils;

import org.apache.commons.codec.digest.DigestUtils;
 

public class HashUtils   
{
    /**
    * Returns 32 byte string in base 16
    * 
    *  @param value 
    *  @return
    */
    public static String getMD5Hash(String value) {
    	// see http://commons.apache.org/proper/commons-codec/apidocs/org/apache/commons/codec/digest/DigestUtils.html
    	return DigestUtils.md5Hex(value);
    }

    /**
    *  Calculates base64(md5(value)). 
    *  This string is URL and JSON safe but 35% shorter than the simple MD5
    * 
    *  @param value - string to be hashed
    *  @return A 21 byte string in base 64. 
    */
    public static String getMD5HashBase64(String value)  {
        byte[] hash = DigestUtils.md5(value);
        return IdConverterUtils.convertByteArrayToBase64String(hash,null);
    }

    /**
    *  Calculates base64(sha1(value)) 
    *  This string is URL and JSON safe but 35% shorter than the simple SHA1
    * 
    *  @param value - string to be hashed
    *  @return A 21 byte string in base 64. 
    */
    public static String getSHA1HashBase64(String value) {
        byte[] hash = DigestUtils.sha1(value);
        return IdConverterUtils.convertByteArrayToBase64String(hash,null);
    }
    
    
    /**
    * Returns shortest hash that is URL + JSON safe. Checks if Value contains unsafe characters, projects expected length
    * to make UrlEncode safe, and then either returns original value, or UrlEncoded value, or MD5 in Base 64
    * Examples:
    * Austria = Austria
    * red corvette = red%20%corvette
    * A text definitely longer than 21 characters = HJHJ2883hd_YUUYU-YD89
    * 
    * 
    *  @param value 
    *  @return
    */
    public static String getHash(String value) {
        return null;
    }

}


