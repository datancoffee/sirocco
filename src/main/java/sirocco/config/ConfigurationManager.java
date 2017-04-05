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
package sirocco.config;


import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class ConfigurationManager {

	private static final String configFilePath = "config.properties";
	private static ConfigurationManager instance;
	private static PropertiesConfiguration config;
	
	private ConfigurationManager() throws ConfigurationException{
		FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
			new FileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
				.configure(new Parameters().properties()
				.setFileName(configFilePath)
				.setThrowExceptionOnMissing(true)
				.setListDelimiterHandler(new DefaultListDelimiterHandler(';'))
				.setIncludesAllowed(false));
		config = builder.getConfiguration();
	}

	public static ConfigurationManager getInstance() throws ConfigurationException{
	    if(instance == null){
	        synchronized (ConfigurationManager.class) {
	            if(instance == null){
	                instance = new ConfigurationManager();
	            }
	        }
	    }
	    return instance;
	}
	
	public static Configuration getConfiguration(){
		if (config == null) 
		{
			try {
				ConfigurationManager.getInstance();
				return config;
			} catch (ConfigurationException e) {
				return null;
			}
		}
		else
			return config;
	}
	
	
}
