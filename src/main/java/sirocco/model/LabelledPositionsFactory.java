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

package sirocco.model;

import sirocco.model.LabelledPositionsV2;

public class LabelledPositionsFactory   
{
    public static ILabelledPositions create(String serializedPositions) throws Exception {
        if (serializedPositions.charAt(0) == 'V')
        {
            int versionend = serializedPositions.indexOf('|');
            int version = Integer.valueOf(serializedPositions.substring(1, versionend));
            if (version == 2)
                return new LabelledPositionsV2(serializedPositions.substring(versionend + 1));
            else
                throw new Exception("LabelledPositionsFactory.Create(): unsupported version " + version); 
        }
        else
        	throw new Exception("LabelledPositionsFactory.Create(): missing version number"); 
    }

    // create Latest version
    public static ILabelledPositions create() throws Exception {
        return new LabelledPositionsV2();
    }

}


