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

import java.net.URI;
import java.net.URISyntaxException;

public class URLUtils {


	
	/**
	 * Will take a url such as http://www.stackoverflow.com and return stackoverflow.com.
	 * Will strip www from the name, but otherwise leave other sub-domains.
	 * 
	 * @param url
	 * @return
	 */
	
	public static String getDomainName(String url) {
	    String result = null;
		try {
	    	URI uri = new URI(url);
	    	String domain = uri.getHost();
	    	result = domain.startsWith("www.") ? domain.substring(4) : domain;
	    } catch (URISyntaxException e) {
	    	result = getHost(url);
		}
		return result;
	}	
	
	/**
	 * Will take a url such as http://www.stackoverflow.com and return www.stackoverflow.com
	 * 
	 * @param url
	 * @return
	 */
	public static String getHost(String url){
	    if(url == null || url.length() == 0)
	        return "";

	    int doubleslash = url.indexOf("//");
	    if(doubleslash == -1)
	        doubleslash = 0;
	    else
	        doubleslash += 2;

	    int end = url.indexOf('/', doubleslash);
	    end = end >= 0 ? end : url.length();

	    int port = url.indexOf(':', doubleslash);
	    end = (port > 0 && port < end) ? port : end;

	    return url.substring(doubleslash, end);
	}


	/**  Based on : http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/2.3.3_r1/android/webkit/CookieManager.java#CookieManager.getBaseDomain%28java.lang.String%29
	 * Get the base domain for a given host or url. E.g. mail.google.com will return google.com
	 * @param host 
	 * @return 
	 */
	public static String getBaseDomain(String url) {
	    String host = getHost(url);

	    int startIndex = 0;
	    int nextIndex = host.indexOf('.');
	    int lastIndex = host.lastIndexOf('.');
	    while (nextIndex < lastIndex) {
	        startIndex = nextIndex + 1;
	        nextIndex = host.indexOf('.', startIndex);
	    }
	    if (startIndex > 0) {
	        return host.substring(startIndex);
	    } else {
	        return host;
	    }
	}
	
	
}
