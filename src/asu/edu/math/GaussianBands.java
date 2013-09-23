package asu.edu.math;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.NormalDistribution;

import asu.edu.loggers.MyLogger;

public class GaussianBands {

	private Logger logger = new MyLogger().getupLogger();

	public double[][] getGaussianBands(String sampleDataLoc, double r,
			double mean, double std) throws IOException {
		NormalDistribution nd = new NormalDistribution(mean, std);
		double rBandValue[] = new double[(int) (r)];
		for (int i = 0; i < r; i++) {
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

                
		/*BufferedWriter br = new BufferedWriter(new FileWriter(new File(
				sampleDataLoc + File.separator +"OUTPUTP1/"+ "rangeBandFile.csv")));
		for (int i = 0; i < rBandValuesRange.length; i++) {
			br.write(Double.toString(rBandValuesRange[i][0]) + ","
					+ Double.toString(rBandValuesRange[i][1]) + ","
					+ Double.toString(rBandValuesRange[i][2]));
			br.write("\r\n");
		}
		br.close();*/
		return rBandValuesRange;
	}
}