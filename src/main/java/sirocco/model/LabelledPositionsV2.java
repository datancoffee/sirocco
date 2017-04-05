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

import CS2JNet.JavaSupport.language.RefSupport;
import CS2JNet.System.Collections.LCC.CSList;
import sirocco.indexer.FieldSerializer;
import sirocco.indexer.IndexerLabel;
import sirocco.model.LabelledPosition;
import sirocco.model.LabelledPositionsV2;
import sirocco.model.LabelledSpan;

import java.io.StringReader;
import java.io.StringWriter;

public class LabelledPositionsV2   implements ILabelledPositions
{
    public CSList<EntityLabelledSpan> EntitySpans;
    public CSList<LabelledSpan> SentimentSpans;
    public LabelledPositionsV2() {
        EntitySpans = new CSList<EntityLabelledSpan>();
        SentimentSpans = new CSList<LabelledSpan>();
    }

    public LabelledPositionsV2(String data) throws Exception {
        stringDeserialize(data);
    }

    public void addLabelledSpan(LabelledSpan lspan) throws Exception {
        SentimentSpans.add(lspan);
    }

    public void addLabelledSpans(CSList<LabelledSpan> lspans) throws Exception {
        SentimentSpans.addAll(lspans);
    }

    public void addEntityLabelledSpan(EntityLabelledSpan elspan) throws Exception {
        EntitySpans.add(elspan);
    }

    public void addPositions(ILabelledPositions lpositions, int offset) throws Exception {
        LabelledPositionsV2 lpositionsV2 = (LabelledPositionsV2)lpositions;
        if (lpositionsV2.SentimentSpans != null)
        {
            for (LabelledSpan lspan : lpositionsV2.SentimentSpans)
            {
                LabelledSpan offsetSpan = new LabelledSpan(lspan.getStart() + offset,lspan.getEnd() + offset,lspan.Labels);
                this.SentimentSpans.add(offsetSpan);
            }
        }
         
        if (lpositionsV2.EntitySpans != null)
        {
            for (EntityLabelledSpan lspan : lpositionsV2.EntitySpans)
            {
                EntityLabelledSpan offsetSpan = new EntityLabelledSpan(lspan.getStart() + offset,lspan.getEnd() + offset,lspan.Labels,lspan.Entity);
                this.EntitySpans.add(offsetSpan);
            }
        }
         
    }

    public PositionsDictionary getPositionsDictionary(CSList<String> keepEntityFilter, CSList<Character> keepLabelFilter) throws Exception {
        PositionsDictionary positions = new PositionsDictionary();
        if (EntitySpans != null)
            for (EntityLabelledSpan elspan : EntitySpans)
            {
                CSList<Character> foundlabels;
                boolean hasEntities, hasSentiments;
                RefSupport<Boolean> refVar___0 = new RefSupport<Boolean>();
                RefSupport<Boolean> refVar___1 = new RefSupport<Boolean>();
                RefSupport<CSList<Character>> refVar___2 = new RefSupport<CSList<Character>>();
                LabelledPosition.labelTypes(elspan.Labels,keepLabelFilter,refVar___0,refVar___1,refVar___2);
                hasEntities = refVar___0.getValue();
                hasSentiments = refVar___1.getValue();
                foundlabels = refVar___2.getValue();
                if (foundlabels == null)
                    continue;
                 
                String lcentity = elspan.Entity.toLowerCase();
                if (keepEntityFilter != null)
                {
                    boolean bFound = false;
                    for (String entityInFilter : keepEntityFilter)
                    {
                        if (lcentity.contains(entityInFilter))
                        {
                            bFound = true;
                            break;
                        }
                         
                    }
                    if (!bFound)
                        foundlabels.remove(IndexerLabel.EntityLabel);
                     
                }
                 
                if (foundlabels == null)
                    continue;
                 
                positions.addSpan(elspan,foundlabels);
            }
         
        if (SentimentSpans != null)
            for (LabelledSpan lspan : SentimentSpans)
            {
                CSList<Character> foundlabels;
                boolean hasEntities, hasSentiments;
                RefSupport<Boolean> refVar___3 = new RefSupport<Boolean>();
                RefSupport<Boolean> refVar___4 = new RefSupport<Boolean>();
                RefSupport<CSList<Character>> refVar___5 = new RefSupport<CSList<Character>>();
                LabelledPosition.labelTypes(lspan.Labels,keepLabelFilter,refVar___3,refVar___4,refVar___5);
                hasEntities = refVar___3.getValue();
                hasSentiments = refVar___4.getValue();
                foundlabels = refVar___5.getValue();
                if (foundlabels == null)
                    continue;
                 
                positions.addSpan(lspan,foundlabels);
            }
         
        return positions;
    }

    public String stringSerialize()  {
        StringWriter wr = new StringWriter();
        FieldSerializer.writeString("V2",wr);
        FieldSerializer.writeInt32(EntitySpans.size(),wr);
        for (EntityLabelledSpan lspan : EntitySpans)
            writeEntityLabelledSpan(lspan,wr);
        FieldSerializer.writeInt32(SentimentSpans.size(),wr);
        for (LabelledSpan lspan : SentimentSpans)
            writeLabelledSpan(lspan,wr);
        return wr.toString();
    }

    public void stringDeserialize(String data) throws Exception {
        EntitySpans = new CSList<EntityLabelledSpan>();
        SentimentSpans = new CSList<LabelledSpan>();
        StringReader rd = new StringReader(data);
        int entitycount = FieldSerializer.readInt32(rd);
        for (int i = entitycount;i > 0;i--)
        {
            EntityLabelledSpan lspan = readEntityLabelledSpan(rd);
            EntitySpans.add(lspan);
        }
        int sentimentscount = FieldSerializer.readInt32(rd);
        for (int i = sentimentscount;i > 0;i--)
        {
            LabelledSpan lspan = readLabelledSpan(rd);
            SentimentSpans.add(lspan);
        }
    }

    public static void writeEntityLabelledSpan(EntityLabelledSpan value, StringWriter wr)  {
        FieldSerializer.writeString(value.Entity,wr);
        writeLabelledSpan(value,wr);
    }

    public static void writeLabelledSpan(LabelledSpan value, StringWriter wr)  {
        FieldSerializer.writeInt32(value.getStart(),wr);
        FieldSerializer.writeInt32(value.getEnd(),wr);
        FieldSerializer.writeInt32(value.Labels.size(),wr);
        for (char label : value.Labels)
            wr.append(label);
    }

    public static LabelledSpan readLabelledSpan(StringReader rd) throws Exception {
        int start = FieldSerializer.readInt32(rd);
        int end = FieldSerializer.readInt32(rd);
        int labelcount = FieldSerializer.readInt32(rd);
        CSList<Character> labels = new CSList<Character>(labelcount);
        for (int j = labelcount;j > 0;j--)
        {
            char label = (char)rd.read();
            labels.add(label);
        }
        return new LabelledSpan(start,end,labels);
    }

    public static EntityLabelledSpan readEntityLabelledSpan(StringReader rd) throws Exception {
        String entity = FieldSerializer.readString(rd);
        LabelledSpan lspan = readLabelledSpan(rd);
        return new EntityLabelledSpan(lspan.getStart(),lspan.getEnd(),lspan.Labels,entity);
    }

}


