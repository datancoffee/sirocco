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

package sirocco.model;


import CS2JNet.System.Collections.LCC.CSList;
import opennlp.tools.util.Span;
import sirocco.indexer.FieldSerializer;
import sirocco.model.PositionsDictionary;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map.Entry;
import java.util.TreeMap;


public class PositionsDictionary  extends TreeMap<Integer, LabelledPosition> 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6956693018998936682L;

	public PositionsDictionary copyWithFilter(CSList<Character> keepLabelFilter) throws Exception {
        if (keepLabelFilter == null)
            return this;
         
        PositionsDictionary result = new PositionsDictionary();
        for (Entry<Integer,LabelledPosition> kvp : this.entrySet())
        {
            CSList<Character> startfilteredlist = new CSList<Character>();
            CSList<Character> endfilteredlist = new CSList<Character>();
            for (char c : keepLabelFilter)
            {
                if (kvp.getValue().StartLabels.contains(c))
                    startfilteredlist.add(c);
                 
                if (kvp.getValue().EndLabels.contains(c))
                    endfilteredlist.add(c);
                 
            }
             
            result.put(kvp.getKey(),new LabelledPosition());
            result.get(kvp.getKey()).IsSingleSpan = kvp.getValue().IsSingleSpan;
            result.get(kvp.getKey()).IsStart = kvp.getValue().IsStart && (startfilteredlist != null);
            result.get(kvp.getKey()).IsEnd = kvp.getValue().IsEnd && (endfilteredlist != null);
            result.get(kvp.getKey()).StartLabels = startfilteredlist;
            result.get(kvp.getKey()).EndLabels = endfilteredlist;
        }
        return result;
    }

    public void addSpan(Span span, CSList<Character> labels) throws Exception {
        if (span.getStart() == span.getEnd())
        {
            LabelledPosition lpos = this.get(span.getStart());
            if (lpos == null)
            {
                lpos = new LabelledPosition();
                this.put(span.getStart(),lpos);
                lpos.IsSingleSpan = true;
            }
            else
                lpos.IsSingleSpan = false; 
            lpos.IsStart = true;
            lpos.IsEnd = true;
            LabelledPosition.addNewLabels(lpos.StartLabels,labels);
            LabelledPosition.addNewLabels(lpos.EndLabels,labels);
        }
        else
        {
            addStart(span.getStart(),labels);
            addEnd(span.getEnd(),labels);
        } 
    }

    private void addStart(int pos, CSList<Character> labels) throws Exception {
        LabelledPosition lpos = this.get(pos);
        if (lpos == null)
        {
            lpos = new LabelledPosition();
            this.put(pos,lpos);
        }
        else
            lpos.IsSingleSpan = false; 
        lpos.IsStart = true;
        LabelledPosition.addNewLabels(lpos.StartLabels,labels);
    }

    private void addEnd(int pos, CSList<Character> labels) throws Exception {
        LabelledPosition lpos = this.get(pos);
        if (lpos == null)
        {
            lpos = new LabelledPosition();
            this.put(pos,lpos);
        }
        else
            lpos.IsSingleSpan = false; 
        lpos.IsEnd = true;
        LabelledPosition.addNewLabels(lpos.EndLabels,labels);
    }

    public void addPositions(PositionsDictionary positions, int offset) throws Exception {
        for (Entry<Integer,LabelledPosition> kvp : positions.entrySet())
        {
            this.put(offset + kvp.getKey(),kvp.getValue());
        }
    }

    public String stringSerialize() throws Exception {
        String data;
        StringWriter wr = new StringWriter();
        FieldSerializer.writeInt32(this.size(), wr);
        for (Entry<Integer,LabelledPosition> kvp : this.entrySet())
        {
            FieldSerializer.writeInt32(kvp.getKey(),wr);
            FieldSerializer.writeBool(kvp.getValue().IsSingleSpan,wr);
            FieldSerializer.writeBool(kvp.getValue().IsStart,wr);
            FieldSerializer.writeBool(kvp.getValue().IsEnd,wr);
            FieldSerializer.writeInt32(kvp.getValue().StartLabels.size(),wr);
            for (char label : kvp.getValue().StartLabels)
                wr.append(label);
            FieldSerializer.writeInt32(kvp.getValue().EndLabels.size(),wr);
            for (char label : kvp.getValue().EndLabels)
                wr.append(label);
        }
        data = wr.toString();
        return data;
    }

    public static PositionsDictionary stringDeserialize(String data) throws Exception {
        PositionsDictionary res = new PositionsDictionary();
        StringReader rd = new StringReader(data);
        int count = FieldSerializer.readInt32(rd);
        for (int i = count;i > 0;i--)
        {
            int key = FieldSerializer.readInt32(rd);
            LabelledPosition lposition = new LabelledPosition();
            lposition.IsSingleSpan = FieldSerializer.readBool(rd);
            lposition.IsStart = FieldSerializer.readBool(rd);
            lposition.IsEnd = FieldSerializer.readBool(rd);
            int startcount = FieldSerializer.readInt32(rd);
            lposition.StartLabels = new CSList<Character>(startcount);
            for (int j = startcount;j > 0;j--)
            {
                char label = (char)rd.read();
                lposition.StartLabels.add(label);
            }
            int endcount = FieldSerializer.readInt32(rd);
            lposition.EndLabels = new CSList<Character>(endcount);
            for (int j = endcount;j > 0;j--)
            {
                char label = (char)rd.read();
                lposition.EndLabels.add(label);
            }
            res.put(key,lposition);
        }
        return res;
    }

}


