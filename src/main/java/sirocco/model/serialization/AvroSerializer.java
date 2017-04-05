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
package sirocco.model.serialization;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.reflect.ReflectDatumWriter;

import sirocco.model.summary.ContentIndexSummary;

import org.apache.avro.reflect.ReflectDatumReader;

public class AvroSerializer {

	public AvroSerializer() {
	}

	public static void writeContentIndexSummary(ContentIndexSummary index, OutputStream out) throws Exception{

	    Schema schema = ReflectData.get().getSchema(ContentIndexSummary.class);

	    DatumWriter<ContentIndexSummary> writer = new ReflectDatumWriter<ContentIndexSummary>(schema);

	    writer.write(index, EncoderFactory.get().directBinaryEncoder(out, null));
	   
	}

	public static ContentIndexSummary readContentIndexSummary(InputStream in) throws Exception{

	    Schema schema = ReflectData.get().getSchema(ContentIndexSummary.class);

		ReflectDatumReader<ContentIndexSummary> reader = new ReflectDatumReader<ContentIndexSummary>(schema);
		
		ContentIndexSummary index = reader.read(null, DecoderFactory.get().binaryDecoder(in, null));
		
		return index;
	}
	
	
}
