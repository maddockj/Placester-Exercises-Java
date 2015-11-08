package com.placester.test;

import java.util.Random;

/*
 * Implement a 6 sided die with weights on the sides, so that we don't have an even probability distribution, but it is 
 * weighted by a list of weights passed in at construction time
 * 
 * After 10k iterations of throwing this die, the results should closely match the desired distribution, and this should
 * be reproducible in the unit test in
 * 
 * src/test/com/placester/test/WeightedDiceTest
 */
public class SixSidedWeightedDie extends WeightedDie
{
	private final Random randomFloatGen = new Random();
	private final float[] weights;
	
    //NOTE: since these are weights on a probability distribution, these should sum to one, and the incoming array
    // should be of length 6. You should throw if either of these preconditions is false
    public SixSidedWeightedDie(float[] weights)
    {
        super(weights);
        
        // Validate input count
        if (weights == null || weights.length != 6)
        {
        	throw new IllegalArgumentException("Six weights are required, not " + weights.length);
        }
        
        // Validate input total
        float weight = 0.00f;
        for (float f : weights)
        {
        	weight += f;
        }        
        if (weight != 1.0f)
        {
        	throw new IllegalArgumentException("Total weight must be 1, not " + weight);
        }
        
        // Good to go
        this.weights = weights;
    }

    //Throw the die: this should produce a value in [1,6]
    @Override
    public int throwDie()
    {
    	// Create a random threshold between 0 and 1
    	float threshold = randomFloatGen.nextFloat();
    	
    	// Iterate through the weights on each side, totaling until we pass the random threshold.
    	// Works because larger weights have a greater chance of getting the total over the threshold.
        float tempWeight = 0.0f;
        for (int i = 0; i < 6; i++)
        {
        	tempWeight += weights[i];
        	if (tempWeight >= threshold)
        	{
        		// The array is 0-based, but the die is 1-based, so add one for the return value
        		return i+1;
        	}
        }
        
    	// Because the weight total equals 1 and the threshold is <= 1, we should never go past side 6
    	// without meeting or crossing the threshold. Getting here means that assumption has failed.
        throw new RuntimeException("Somthing went very unexpectedly wrong");
    }

}
