package com.asu.mwdb.phase2Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import au.com.bytecode.opencsv.CSVReader;

import com.asu.mwdb.utils.IConstants;

public class AssignBandValues {


	/**
	 * Given a key value, assign it to corresponding band in Gaussian
	 * Curve. This will be then used to form words out of it
	 * @param rBandValuesFull
	 * @param key
	 * @throws MatlabInvocationException
	 * @throws IOException
	 */
	public static void assignToGaussianCurveTask1(MatlabProxy proxy,
			String matlabScriptLoc, String inputDirectory,
			double rBandValueRange[][]) throws MatlabInvocationException,
			IOException {
		File file = new File(inputDirectory);
		String[] filesList = file.list();
		
		String gaussianOutputDirectory = inputDirectory + File.separator + IConstants.LETTERS_FILE;
		File outputDirectoryFileObject = new File(gaussianOutputDirectory);
		if (outputDirectoryFileObject.exists()) {
			FileIOHelper.delete(outputDirectoryFileObject);
		}
		if(outputDirectoryFileObject.mkdir()) {
			for (int i = 0; i < filesList.length; i++) {
				if (filesList[i].contains("csv") && !filesList[i].contains(IConstants.RANGED_BAND)) {
					String inputFileLocation = inputDirectory + File.separator + IConstants.NORMALIZED_FILE + File.separator +  filesList[i];
					String gausianFileLocation = inputDirectory + File.separator + IConstants.LETTERS_FILE + File.separator + filesList[i];
					assignLetterToValue(inputFileLocation, gausianFileLocation, rBandValueRange);
				}
			}
		} else {
			System.out.println("Error while creating directory for gaussian files");
		}
	}

	public static void assignGaussianCurveTask3(MatlabProxy proxy, String inputFileLocation,
			double rBandValueRange[][]) throws IOException {
		int position = inputFileLocation.lastIndexOf(File.separator);
		if (position > 0) {
			String currentDirectory = inputFileLocation.substring(0, position);
			String gaussianFileOutput = currentDirectory + File.separator
					+  IConstants.GAUSSIAN_FILE + ".csv";
			String normalizedFileLocation = currentDirectory + File.separator + IConstants.NORMALIZED_FILE + ".csv";
			assignLetterToValue(normalizedFileLocation, gaussianFileOutput, rBandValueRange);
			System.out.println("Done with assigning band letters for Task 3");
		} else {
			System.out.println("Error while assigning band letters for Task 3");
		}
	}

	
	
	/**
	 * Once we get a value from normalized file, assign each value
	 * to corresponding letter
	 * @param normalAxisWFile
	 * @param letterAxisWFile
	 * @param rBandValueRange
	 * @throws IOException
	 */
	public static void assignLetterToValue(String normalAxisWFile,
			String letterAxisWFile, double rBandValueRange[][])
			throws IOException {

		CSVReader csvReader = new CSVReader(new FileReader(normalAxisWFile));
		List<String[]> lines = csvReader.readAll();
		csvReader.close();
		List<String> writeLines = new ArrayList<String>();
		for (String[] line : lines) {
			String newLine = new String();
			int flag = 0;
			for (int i = 0; i < line.length; i++) {
				String sensorPoint = line[i];
				// Use bianry search. Otherwise we will have to do a lot 
				// of comparisons to find the correct band for assigning letter
				int value = (int) binarySearch(rBandValueRange,
						Double.parseDouble(sensorPoint),
						rBandValueRange.length - 1, 0);
				if (flag == 0) {
					newLine = "d" + value;
					flag = 1;
				} else {
					newLine = newLine + ",d" + value;
				}

			}
			writeLines.add(newLine);
		}

		BufferedWriter br = new BufferedWriter(new FileWriter(new File(
				letterAxisWFile)));
		for (String line : writeLines) {
			br.write(line);
			br.write("\r\n"); // needed so that it can be read in Matlab
		}

		br.close();
	}

	/**
	 * Common binary search algorithm
	 * @param rBandValueRange
	 * @param value
	 * @param high
	 * @param low
	 * @return
	 */
	public static double binarySearch(double rBandValueRange[][], double value,
			int high, int low) {
		int mid = (high + low) / 2;
		double result =99;

		if (value < rBandValueRange[mid][0]) {
			result=binarySearch(rBandValueRange, value, mid - 1, low);
			
		} else if (value > rBandValueRange[mid][0]) {
			if (value <= rBandValueRange[mid][1]) {
				return rBandValueRange[mid][2];
			} else {
				result =binarySearch(rBandValueRange, value, high, mid + 1);
			}

		} 
		return result;
	}
}
