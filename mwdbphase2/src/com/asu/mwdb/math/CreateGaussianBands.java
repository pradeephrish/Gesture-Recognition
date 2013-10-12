package com.asu.mwdb.math;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.asu.mwdb.loggers.MyLogger;

public class CreateGaussianBands {

	private Logger logger = new MyLogger().getupLogger();

	public double[][] getGaussianBands(String sampleDataLoc, double r,
			double mean, double std) throws IOException {
		NormalDistribution nd = new NormalDistribution(mean, std);
		double rBandValue[] = new double[(int) (r)];
		for (int i = 0; i < r; i++) {
			rBandValue[i] = (nd.probability((double) -(i + 1) * (1 / r),
					(double) (i + 1) * (1 / r)));
		}

		double bands[][] = new double[(int) (2 * r)][3];

		// Create a Gaussian band map
		int j = 0;
		double current = -1.1;
		for (int i = rBandValue.length - 2; i >= 0; i--) {
			bands[j][0] = current;
			bands[j][1] = -1 * rBandValue[i];
			bands[j][2] = j + 1;
			j++;
			current = -1 * rBandValue[i];
		}

		bands[j][0] = current;
		bands[j][1] = 0;
		bands[j][2] = j + 1;
		j++;
		current = 0;
		double prev=0;
		for (int i = 0; i < rBandValue.length; i++) {
			bands[j][0] = current;
			bands[j][1] = rBandValue[i];
			bands[j][2] = j + 1;
			j++;
			prev=current;
			current = rBandValue[i];
		}

		bands[j-1][0] = prev;
		bands[j-1][1] = 1.1;
		bands[j-1][2] = j;

		return bands;
	}
}