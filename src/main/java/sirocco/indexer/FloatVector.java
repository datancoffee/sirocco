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
import CS2JNet.System.StringSplitOptions;
import CS2JNet.System.StringSupport;
import opennlp.tools.util.Span;
import sirocco.indexer.DerivationStep;
import sirocco.indexer.FloatVector;
import sirocco.indexer.IGenericVector;
import sirocco.indexer.IndexerLabel;
import sirocco.indexer.IndexingConsts.SentimentValence;
import sirocco.indexer.util.LangUtils;
import sirocco.model.LabelledSpan;
import sirocco.util.HashUtils;

import java.util.HashMap;


/**
* Float values (scaled x10 on Init) with string dimension keys
*/
public class FloatVector extends HashMap<String,Float> implements IGenericVector
{
    public static final float DefaultValue = 0.0F;
    public static final float InitialValue = 10.0F;
    public static final String DimensionStart = "@@";
    public static final String ValueStart = "+";
    public static final String ZeroVector = "@@zero";
    public static final String ScoreDimension = "score";
    public static final String NegationDimension = "negation";
    public static final String SlyIronicSarcasticDimension = "sis";
    public static final String EntityDimension = "entity";
    public static final String PosOverrideDimension = "ignposoverride";
    public static final String OriginalTextDimension = "ignorgtxt";
    public static final String SignalsDimension = "ignsignals";
    public static final String IsLinkDimension = "islink";
    public static final String RegexOptionDimension = "regexoption";
    public static final String IsHashTagDimension = "ishashtag";
    public static final String IsIgnoreDimension = "isignore";
    public static final String FlagEnding = "[flag]";
    public static final String KeyEnding = "[key]";
    public static final String IgnoreDimensionStart = "ign";
    public static final CSList<String> MultiplicativeDimensions = new CSList<String>(new String[]{ ScoreDimension }); // dimensions with this prefix will be printed in intensitytoken, but will not be deserialized.

    public HashMap<String,Float> Flags;
    private HashMap<String,CSList<DerivationStep>> derivationSteps; // Hashmap key is a sentiment dimension 
    private CSList<String> shortkeys; // List of Signal shortkeys (usually, hashes) of idioms, emotion or quality words, interjections etc, that appear in the text
    
    public HashMap<String,CSList<DerivationStep>> getDerivationSteps()  {
        return derivationSteps;
    }

    public CSList<String> getShortkeys()  {
        return shortkeys;
    }
   
    private CSList<DerivationStep> dimDerivationSteps(String dimension)  {
        CSList<DerivationStep> dimlist = getDerivationSteps().get(dimension);
        if (dimlist == null)
        {
            dimlist = new CSList<DerivationStep>();
            getDerivationSteps().put(dimension, dimlist);
        }
         
        return dimlist;
    }

    public FloatVector()  {
        super();
        derivationSteps = new HashMap<String,CSList<DerivationStep>>();
        Flags = new HashMap<String,Float>();
        shortkeys = new CSList<String>();
    }

    public FloatVector(Span span, String initialDimension)  {
        this();
        FloatVector vector = new FloatVector();
        vector.put(initialDimension, InitialValue);
        this.accumulate(vector,span,true);
    }

    public void init(String[] keys, String[] fields, String vectorkey)  {
        for (int i = 0;i < keys.length;i++)
        {
            float value = (StringSupport.isNullOrEmpty(fields[i])) ? DefaultValue : InitialValue * Float.parseFloat(fields[i]);
            if (keys[i].endsWith(FlagEnding))
            {
                String flagdim = keys[i].substring(0, (0) + (keys[i].length() - FlagEnding.length()));
                this.Flags.put(flagdim, value);
            }
            else
                this.put(keys[i], value); 
        }
        String vectorshortkey = HashUtils.getShortkey(vectorkey);
        this.getShortkeys().add(vectorshortkey);
    }

    public void initFromIntensityToken(String token)  {
        if (StringSupport.equals(token, ZeroVector))
            return ;
         
        String[] dsa = new String[]{ DimensionStart };
        String[] vsa = new String[]{ ValueStart };
        String[] dimvalues = StringSupport.Split(token, dsa, StringSplitOptions.RemoveEmptyEntries);
        for (String dimvalue : dimvalues)
        {
            if (!dimvalue.startsWith(IgnoreDimensionStart))
            {
                String[] pair = StringSupport.Split(dimvalue, vsa, StringSplitOptions.None);
                String key = pair[0];
                float value = (StringSupport.isNullOrEmpty(pair[1])) ? DefaultValue : Float.parseFloat(pair[1]);
                this.put(key, value);
            }
            else if (dimvalue.startsWith(SignalsDimension)) // import all shortkeys
            {
            	String[] pair = StringSupport.Split(dimvalue, vsa, StringSplitOptions.None);
                if (!StringSupport.isNullOrEmpty(pair[1])) {
                	String[] ssa = new String[]{ "," };
                	String[] shortkeyarr = StringSupport.Split(pair[1], ssa, StringSplitOptions.None);
                	shortkeys.addAll(new CSList<String>(shortkeyarr));
                }
            }
             
        }

    }

    public String[] getDimensions()  {
        String[] dimensions = this.keySet().toArray(new String[this.keySet().size()]);
        return dimensions;
    }

    public String toCSV()  {
        String retvalue = null;
        if (this.values().size() > 0)
        {
            retvalue = "";
            for (float value : this.values())
                retvalue += (StringSupport.equals(retvalue, "")) ? String.valueOf(value) : "," + String.valueOf(value);
        }
         
        return retvalue;
    }

    public static String toCSV(CSList<String> sa)  {
        String retvalue = null;
        if (sa.size() > 0)
        {
            retvalue = "";
            for (String s : sa)
                retvalue += (retvalue.isEmpty() ? s : "," + s);
        }
         
        return retvalue;
    }
    
    
    public void accumulate(FloatVector othervector)  {
        accumulate(othervector,(Span)null,true);
    }

    public void accumulate(FloatVector othervector, boolean addDerivationSteps)  {
        accumulate(othervector,(Span)null,addDerivationSteps);
    }

    public void accumulate(FloatVector othervector, Span span, boolean addDerivationSteps)  {
        CSList<Span> spans = null;
        if (span != null)
        {
            spans = new CSList<Span>();
            spans.add(span);
        }
         
        accumulate(othervector,spans,addDerivationSteps, null);
    }

    
    public void accumulate(FloatVector othervector, CSList<Span> spans, boolean addDerivationSteps, Span scopeSpan)  {
        if (othervector == null)
            return ;
         
        for (Entry<String,Float> otherkvp : othervector.entrySet())
        {
            if (otherkvp.getValue() == DefaultValue)
                continue;
             
            Float value = this.get(otherkvp.getKey());
            if (value == null || value == DefaultValue)
                this.put(otherkvp.getKey(), otherkvp.getValue());
            else
            {
                if (MultiplicativeDimensions.contains(otherkvp.getKey()))
                    this.put(otherkvp.getKey(), value * otherkvp.getValue());
                else
                    this.put(otherkvp.getKey(), value + otherkvp.getValue()); 
            } 
            if (addDerivationSteps)
            {
            	CSList<DerivationStep> stepsToAdd = null;
            	if (scopeSpan == null)
            		stepsToAdd = othervector.dimDerivationSteps(otherkvp.getKey());
            	else {
            		stepsToAdd = new CSList<DerivationStep>();
            		for (DerivationStep step: othervector.dimDerivationSteps(otherkvp.getKey())) 
            			if (scopeSpan.contains(step.DerivationSpan))
            				stepsToAdd.add(step);
             	}
            		
                this.dimDerivationSteps(otherkvp.getKey()).addAll(stepsToAdd);
                
                if (spans != null)
                {
                	// TODO: This is where I need to add the Shortkeys! With Spans, so that I can filter
                    CSList<DerivationStep> steps = DerivationStep.createList(DerivationStep.AccumulateAction,spans);
                    this.dimDerivationSteps(otherkvp.getKey()).addAll(steps);
                }
                 
            }
             
            CSList<String> combinationDimensions = SentimentDimension.DimensionsRequiringCombinations.get(otherkvp.getKey());

            if (combinationDimensions != null)
            {
                if (this.sumOfIntensities(combinationDimensions) >= SentimentDimension.CombinationThreshold)
                {
                    String aggregatingDimension = SentimentDimension.sentimentOfCombined(otherkvp.getKey());
                    for (String dimension : combinationDimensions)
                    {
                        this.moveDimensionValues(dimension,aggregatingDimension);
                    }
                }
                 
            }
             
        }
        
        // Go through dimensions and reset the ones that were left without derivationSteps after filtering by scopeSpan
        if (addDerivationSteps && scopeSpan != null)
        {
        	 for (Entry<String,Float> kvp : this.entrySet())
             {
                 String dimension = kvp.getKey();
                 if (this.dimDerivationSteps(dimension).size() == 0)
                	 this.put(dimension, DefaultValue);
             }
        }
        
        // Merge shortkeys
        // TODO: see above for where to add 
        this.getShortkeys().addAll(othervector.getShortkeys());
        
    }
    
    /*
     * sso 7/7/18: Before adding the scopeSpan filter
    public void accumulate(FloatVector othervector, CSList<Span> spans, boolean addDerivationSteps)  {
        if (othervector == null)
            return ;
         
        for (Entry<String,Float> otherkvp : othervector.entrySet())
        {
            if (otherkvp.getValue() == DefaultValue)
                continue;
             
            Float value = this.get(otherkvp.getKey());
            if (value == null || value == DefaultValue)
                this.put(otherkvp.getKey(), otherkvp.getValue());
            else
            {
                if (MultiplicativeDimensions.contains(otherkvp.getKey()))
                    this.put(otherkvp.getKey(), value * otherkvp.getValue());
                else
                    this.put(otherkvp.getKey(), value + otherkvp.getValue()); 
            } 
            if (addDerivationSteps)
            {
                this.dimDerivationSteps(otherkvp.getKey()).addAll(othervector.dimDerivationSteps(otherkvp.getKey()));
                if (spans != null)
                {
                    CSList<DerivationStep> steps = DerivationStep.createList(DerivationStep.AccumulateAction,spans);
                    this.dimDerivationSteps(otherkvp.getKey()).addAll(steps);
                }
                 
            }
             
            CSList<String> combinationDimensions = SentimentDimension.DimensionsRequiringCombinations.get(otherkvp.getKey());

            if (combinationDimensions != null)
            {
                if (this.sumOfIntensities(combinationDimensions) >= SentimentDimension.CombinationThreshold)
                {
                    String aggregatingDimension = SentimentDimension.sentimentOfCombined(otherkvp.getKey());
                    for (String dimension : combinationDimensions)
                    {
                        this.moveDimensionValues(dimension,aggregatingDimension);
                    }
                }
                 
            }
             
        }
    
        // Merge shortkeys
        this.getShortkeys().addAll(othervector.getShortkeys());
        
    }
    */

    public void removeUnusedCombinationParts()  {
        for (String dimension : SentimentDimension.DimensionsRequiringCombinations.keySet())
        {
            this.dimDerivationSteps(dimension).clear();
            this.remove(dimension);
        }
    }

    public void moveDimensionValues(String fromDimension, String toDimension)  {
        if (StringSupport.equals(fromDimension, toDimension))
            return ;
         
        Float toValue = this.get(toDimension);
        if (toValue == null)
            this.put(toDimension, DefaultValue);
         
        Float fromValue = this.get(fromDimension);
        if (fromValue != null)
        {
            this.put(toDimension, this.get(toDimension) + fromValue);
            this.dimDerivationSteps(toDimension).addAll(this.dimDerivationSteps(fromDimension));
            this.dimDerivationSteps(fromDimension).clear();
            this.remove(fromDimension);
        }
         
    }

    public void moveAllToAmbiguousSentiment()  {
        if (this.keySet().size() == 1 && this.containsKey(NegationDimension))
        {
            this.dimDerivationSteps(NegationDimension).clear();
            this.remove(NegationDimension);
            return ;
        }
         
        String[] keys = this.keySet().toArray(new String[this.keySet().size()]); // TODO: do we need to create a new array, or can we just iterate in the for loop
        Float ambvalue = this.get(SentimentDimension.Ambiguous);
        if (ambvalue == null)
            this.put(SentimentDimension.Ambiguous, DefaultValue);
         
        for (String key : keys)
        {
            if (SentimentDimension.DimensionsRequiringModifier.contains(key))
            {
                this.dimDerivationSteps(key).clear();
                this.remove(key);
                continue;
            }
             
            Float value = this.get(key);
            if ((value != null) && (value != DefaultValue) && (!key.equals(SentimentDimension.Ambiguous)))
            {
                this.put(SentimentDimension.Ambiguous, this.get(SentimentDimension.Ambiguous) + value);
                this.dimDerivationSteps(SentimentDimension.Ambiguous).addAll(this.dimDerivationSteps(key));
                this.dimDerivationSteps(key).clear();
                this.remove(key);
            }
             
        }
    }

    private void resetMappingState(){
    	// Prepare for the remapping operation
    	// Because derivations step objects can be contained in derivation lists of multiple dimensions
    	// set the IsRemapped to false
        for (Entry<String,CSList<DerivationStep>> dimderivsteps : derivationSteps.entrySet())
            for (DerivationStep derivstep : dimderivsteps.getValue())
            	derivstep.IsRemapped = false;
    }
    
    public void remapSpans(HashMap<String,Span> spanmap)  {
    	
    	/*
    	 * sso 7/6/2018: Deep Parsing requires handling cases when Sentiment Spans can be trees of other things but tokens
    	 */
    	HashMap<Integer,Integer> spanStarts = new HashMap<Integer,Integer>();
    	HashMap<Integer,Integer> spanEnds = new HashMap<Integer,Integer>();
    	
        for (Entry<String,Span> mapping : spanmap.entrySet())
        {
        	String key = mapping.getKey();
        	int oldStart = LangUtils.spanStartFromKey(key);
        	int oldEnd = LangUtils.spanEndFromKey(key);
        	
        	int newStart = mapping.getValue().getStart();
        	int newEnd = mapping.getValue().getEnd();
        	
        	spanStarts.put(new Integer(oldStart), new Integer(newStart));
        	spanEnds.put(new Integer(oldEnd), new Integer(newEnd));
        	
        }

        resetMappingState();
        
        for (Entry<String,CSList<DerivationStep>> dimderivsteps : derivationSteps.entrySet())
        {
            for (DerivationStep derivstep : dimderivsteps.getValue())
            {
            	if (!derivstep.IsRemapped) {
	            	Integer oldStart = derivstep.DerivationSpan.getStart();
	            	Integer newStart = spanStarts.get(oldStart);
	
	            	Integer oldEnd = derivstep.DerivationSpan.getEnd();
	            	Integer newEnd = spanEnds.get(oldEnd);
	            	
	            	Span newSpan = null;
	            	
	            	if (newStart != null && newEnd == null)
	            		newSpan = new Span(newStart,oldEnd);
	            	else if (newStart == null && newEnd != null)
	            		newSpan = new Span(oldStart,newEnd);
	            	else if (newStart != null && newEnd != null)
	            		newSpan = new Span(newStart,newEnd);
	            	
	                if (newSpan != null) {
	                    derivstep.DerivationSpan = newSpan;
	                    derivstep.IsRemapped = true;
	                }
            	}
            }
        }
        
    	
        /* 
         * sso 7/6/2018
         * 
        for (Entry<String,CSList<DerivationStep>> dimderivsteps : derivationSteps.entrySet())
        {
            for (DerivationStep derivstep : dimderivsteps.getValue())
            {
                Span newSpan = spanmap.get(LangUtils.spanKey(derivstep.DerivationSpan));
                if (newSpan != null)
                    derivstep.DerivationSpan = newSpan;
                 
            }
        }
        */
    }

    
    public CSList<LabelledSpan> getDerivationSpans()  {
        CSList<LabelledSpan> lspans = new CSList<LabelledSpan>();
        HashMap<String,CSList<Character>> allSpanLabels = new HashMap<String,CSList<Character>>();
        for (Entry<String,CSList<DerivationStep>> dimderivsteps : derivationSteps.entrySet())
        {
            for (DerivationStep derivstep : dimderivsteps.getValue())
            {
                Span span = derivstep.DerivationSpan;
                CSList<Character> spanLabels = allSpanLabels.get(span);
                if (spanLabels == null)
                {
                    spanLabels = new CSList<Character>();
                    String spanKey = LangUtils.spanKey(span);
                    allSpanLabels.put(spanKey, spanLabels);
                }
                 
                spanLabels.add(IndexerLabel.labelOfSentiment(dimderivsteps.getKey()));
            }
        }
        for (Entry<String,CSList<Character>> kvp : allSpanLabels.entrySet())
        {
        	String spanKey = kvp.getKey();
        	int start = LangUtils.spanStartFromKey(spanKey);
        	int end = LangUtils.spanEndFromKey(spanKey);
            lspans.add(new LabelledSpan(start,end,kvp.getValue()));
        }
        return lspans;
    }

    public void applyNegationAndMultiplication()  {
        // calculate multiplicator
        float multScore = 1.0F;
        CSList<DerivationStep> multSteps = new CSList<DerivationStep>();
        for (String multDim : MultiplicativeDimensions)
        {
            Float multDimScore = this.get(multDim);
            if (multDimScore != null)
            {
                multScore *= multDimScore;
                multSteps.addAll(dimDerivationSteps(multDim));
                dimDerivationSteps(multDim).clear();
                this.remove(multDim);
            }
             
        }
        // check if there are any spans that require modifiers to be sentiment
        if (multSteps.size() > 0)
        {
            for (String modreqdim : SentimentDimension.DimensionsRequiringModifier)
            {
                Float modreqscore = this.get(modreqdim);
                if (modreqscore != null)
                {
                    String sntdim = SentimentDimension.sentimentOfModified(modreqdim);
                    Float sntdimorigvalue = this.get(sntdim);
                    if (sntdimorigvalue != null)
                    {
                        this.put(sntdim, sntdimorigvalue + modreqscore * multScore);
                    }
                    else
                        this.put(sntdim, modreqscore * multScore); 
                    dimDerivationSteps(sntdim).addAll(dimDerivationSteps(modreqdim));
                    dimDerivationSteps(sntdim).addAll(multSteps);
                    dimDerivationSteps(modreqdim).clear();
                    this.remove(modreqdim);
                }
                 
            }
        }
         
        boolean isNegation = false;
        Float negationscore = this.get(NegationDimension);
        if (negationscore != null)
            isNegation = (negationscore != FloatVector.DefaultValue);
         
        // if the only remaining dimension if Negation, then there is really nothing to negate
        if (this.keySet().size() == 1 && (isNegation))
        {
            this.dimDerivationSteps(NegationDimension).clear();
            this.remove(NegationDimension);
            return ;
        }
         
        // Multiply ambiguous before the loop so that not to double-count other dimensions
        if (isNegation)
        {
            Float amboriginalvalue = this.get(SentimentDimension.Ambiguous);
            if (amboriginalvalue == null)
            {
            	// create dimension for easy add-up below
                this.put(SentimentDimension.Ambiguous, DefaultValue);
                amboriginalvalue = DefaultValue;
            }
            
            // add steps here
            if (multSteps.size() > 0)
            {
                this.put(SentimentDimension.Ambiguous, amboriginalvalue * multScore);
                dimDerivationSteps(SentimentDimension.Ambiguous).addAll(multSteps);
            }
             
        }
         
        String[] keys = this.keySet().toArray(new String[this.keySet().size()]); // TODO: do we need to create a new array, or can we just iterate in the for loop
        for (String key : keys)
        {
            if (key.equals(SentimentDimension.Ambiguous))
                continue;
             
            Float value = this.get(key);
            if (value != null && value != DefaultValue)
            {
                if (isNegation)
                {
                    if (!key.equalsIgnoreCase(FloatVector.NegationDimension))
                        // the value of negation dimension in itself is not important
                        this.put(SentimentDimension.Ambiguous, this.get(SentimentDimension.Ambiguous) + value * multScore);
                     
                    this.dimDerivationSteps(SentimentDimension.Ambiguous).addAll(this.dimDerivationSteps(key));
                    this.dimDerivationSteps(key).clear();
                    this.remove(key);
                }
                else
                {
                    this.put(key, value * multScore);
                    this.dimDerivationSteps(key).addAll(multSteps);
                } 
            }
             
        }
    }

    public boolean hasIntensities()  {
        for (Entry<String,Float> kvp : this.entrySet())
        {
            if ((kvp.getValue() != DefaultValue) && !SentimentDimension.DimensionsRequiringModifier.contains(kvp.getKey()))
                return true;
             
        }
        return false;
    }

    public float sumAllIntensities()  {
        float sum = DefaultValue;
        for (Entry<String,Float> kvp : this.entrySet())
        {
            if ((kvp.getValue() != DefaultValue) && !SentimentDimension.DimensionsRequiringModifier.contains(kvp.getKey()))
                sum += kvp.getValue();
             
        }
        return sum;
    }

    public float sumAllSentimentIntensities()  {
        return sumOfIntensities(SentimentDimension.AllSentimentDimensions);
    }

    public float sumOfIntensities(CSList<String> dimensions)  {
        float sum = DefaultValue;
        for (Entry<String,Float> kvp : this.entrySet())
        {
            if ((kvp.getValue() != DefaultValue) && (dimensions.contains(kvp.getKey())))
                sum += kvp.getValue();
             
        }
        return sum;
    }

    public SentimentValence dominantValence()  {
        float positive = 0.0F;
        float negative = 0.0F;
        float ambiguous = 0.0F;
        float general = 0.0F;
        for (Entry<String,Float> kvp : this.entrySet())
        {
            if (kvp.getValue() == DefaultValue)
                continue;
             
            if (SentimentValenceHelper.PositiveDimensions.contains(kvp.getKey()))
                positive += kvp.getValue();
            else if (SentimentValenceHelper.NegativeDimensions.contains(kvp.getKey()))
                negative += kvp.getValue();
            else if (SentimentValenceHelper.AmbiguousDimensions.contains(kvp.getKey()))
                ambiguous += kvp.getValue();
            else if (SentimentValenceHelper.GeneralDimensions.contains(kvp.getKey()))
                general += kvp.getValue();
                
        }
        if (positive + negative + ambiguous + general == 0.0F)
            return SentimentValence.Zero;
        else if (positive + negative + ambiguous == 0.0F)
            return SentimentValence.General;
          
        // general is excluded in ratio calculations
        float posratio = positive / (positive + negative + ambiguous);
        float negratio = negative / (positive + negative + ambiguous);
        float ambratio = ambiguous / (positive + negative + ambiguous);
        if (posratio > SentimentValenceHelper.DominantValenceThreshold)
            return SentimentValence.Positive;
        else if (negratio > SentimentValenceHelper.DominantValenceThreshold)
            return SentimentValence.Negative;
        else
            return SentimentValence.Ambiguous;  
    }

    public boolean hasCompatibleValence(FloatVector othervector)  {
        SentimentValence thisvalence = dominantValence();
        SentimentValence othervalence = othervector.dominantValence();
        if ((thisvalence == SentimentValence.Zero) || (othervalence == SentimentValence.Zero))
            return true;
         
        if ((thisvalence == SentimentValence.General) || (othervalence == SentimentValence.General))
            return true;
         
        if ((thisvalence == othervalence) && (thisvalence != SentimentValence.Ambiguous))
            return true;
         
        return false;
    }

    public String toString() {
        try
        {
            StringBuilder sb = new StringBuilder();
            for (Entry<String,Float> kvp : this.entrySet())
            {
                if (kvp.getValue() != DefaultValue)
                    sb.append("{" + kvp.getKey() + "|" + kvp.getValue() + "|" + LangUtils.printDerivationStepList(getDerivationSteps().get(kvp.getKey()),",") + "}");
                 
            }
            return sb.toString();
        }
        catch (RuntimeException re)
        {
            throw re;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    
    }

    public String toIntensityToken()  {
        StringBuilder sb = new StringBuilder();
        for (Entry<String,Float> kvp : this.entrySet())
        {
            sb.append(dimensionToken(kvp.getKey(),String.valueOf(kvp.getValue())));
        }
        if (getShortkeys().size() > 0)
        {
        	String listofshortkeys=FloatVector.toCSV(getShortkeys());
        	sb.append(dimensionToken(SignalsDimension,listofshortkeys));
        }
        if (sb.length() == 0)
            sb.append(ZeroVector);
         
        return sb.toString();
    }

    public static String dimensionToken(String dimension, String value) {
        return DimensionStart + dimension + ValueStart + value;
    }

    public static boolean isIntensityToken(String token) {
        return token.startsWith(DimensionStart);
    }

    public static String getDimensionValueFromIntensityToken(String intensitytoken, String dimension)  {
        String dimensionstart = FloatVector.DimensionStart + dimension;
        int dimpos = intensitytoken.indexOf(dimensionstart);
        if (dimpos >= 0)
        {
            int start = dimpos + dimensionstart.length() + FloatVector.ValueStart.length();
            int end = intensitytoken.indexOf(FloatVector.DimensionStart, start) - 1;
            if (end < 0)
                end = intensitytoken.length() - 1;
             
            String value = intensitytoken.substring(start, end + 1);
            return value;
        }
        else
            return null; 
    }

}


