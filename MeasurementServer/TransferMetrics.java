//package com.mitate;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;


public class TransferMetrics implements Serializable {
	long[] laLatencies;
	int[] iaNoOfBytes;
	float[] faThroughput;
	
    float fLatencyConfInterval;
    float fThroughpuConfInterval;
    
    TransferMetrics() {
    	
    }
	
	TransferMetrics(long[] laLatencies, int[] iaNoOfBytes, float[] faThroughput) {
		this.laLatencies = laLatencies;
		this.iaNoOfBytes =  iaNoOfBytes;
		this.faThroughput = faThroughput;		
	}
	
	public void calculateConfInterval() {
		long[] templatencies = laLatencies.clone();
		Arrays.sort(templatencies);
		
		int elim = 0;
			while(elim < templatencies.length)
			{
				if(templatencies[elim] == 0)
				elim = elim + 1;
				else
				break;
			}

		float fLatencyMean = MNEPUtilities.getSum(laLatencies) / (float)(laLatencies.length - elim + 1);
		float fSum = 0.0f;
		for(int i=elim; i<templatencies.length; i++) {
			fSum = (float)Math.pow((templatencies[i] - fLatencyMean), 2);
		}
		float fStandardDeviation = (float)Math.sqrt(fSum / (laLatencies.length - elim + 1));
		fLatencyConfInterval = (float)(1.96 * fStandardDeviation / (Math.sqrt(laLatencies.length - elim + 1)));

		float fThroughputMean = MNEPUtilities.getSumThroughput(faThroughput) / (float)(faThroughput.length - elim + 1);
		fSum = 0.0f;
		for(int i=elim; i<templatencies.length; i++) {
			fSum = (float)Math.pow((templatencies[i] - fThroughputMean), 2);
		}
		fStandardDeviation= (float)Math.sqrt(fSum / (faThroughput.length - elim + 1));
		fThroughpuConfInterval = (float)(1.96 * fStandardDeviation / (Math.sqrt(faThroughput.length - elim + 1)));
	}
}
