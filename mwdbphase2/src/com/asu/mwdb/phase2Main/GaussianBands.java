package com.asu.mwdb.phase2Main;

import java.io.IOException;

import org.apache.commons.math3.distribution.NormalDistribution;


/**
 * This class is responsible for creating the band ranges for given user input - "r"
 * It will create a file having 3 columns which indicate the range of each band on Gaussian Curve
 * @author Kedar Joshi
 *
 */
public class GaussianBands {

	public double[][] getGaussianBands(double r,
			double mean, double std) throws IOException {
		
		// find the normal distribution
		NormalDistribution nd = new NormalDistribution(mean, std);
		double rBandValue[] = new double[(int) (r)];
		System.out.println("Finding out band ranges");
		for (int i = 0; i < r; i++) {
			// this will give us an array containing the possible lengths of each band
			// note that these will be from 0 to 1
			rBandValue[i] = (nd.probability((double) -(i + 1) * (1 / r),
					(double) (i + 1) * (1 / r)));
		}

			
		double rBandValuesRange[][] = new double[(int) (2 * r)][3];

		// Create a Gaussian band map
		int j = 0;
		double current = -1.1;
		for (int i = rBandValue.length - 2; i >= 0; i--) {
			rBandValuesRange[j][0] = current;
			rBandValuesRange[j][1] = -1 * rBandValue[i];
			rBandValuesRange[j][2] = j + 1;
			j++;
			current = -1 * rBandValue[i];
		}

		rBandValuesRange[j][0] = current;
		rBandValuesRange[j][1] = 0;
		rBandValuesRange[j][2] = j + 1;
		j++;
		current = 0;
		double prev=0;
		for (int i = 0; i < rBandValue.length; i++) {
			rBandValuesRange[j][0] = current;
			rBandValuesRange[j][1] = rBandValue[i];
			rBandValuesRange[j][2] = j + 1;
			j++;
			prev=current;
			current = rBandValue[i];
		}

		rBandValuesRange[j-1][0] = prev;
		rBandValuesRange[j-1][1] = 1.1;
		rBandValuesRange[j-1][2] = j;
		
		return rBandValuesRange;
	}
}