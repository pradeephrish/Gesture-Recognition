package com.asu.mwdb.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import com.asu.mwdb.loggers.MyLogger;
import au.com.bytecode.opencsv.CSVReader;

public class AssignLetter {

	private static Logger logger = new MyLogger().getupLogger();

	/**
	 * 
	 * @param rBandValuesFull
	 * @param key
	 *            Given a key value, assign it to corresponding band in Gaussian
	 *            Curve. This will be then used to form words out of it
	 * @throws MatlabInvocationException
	 * @throws IOException
	 */
	public static void assignToGaussianCurve(MatlabProxy proxy,
			String matlabScriptLoc, String sampleDataLoc,
			double rBandValueRange[][],List<File> directory) throws MatlabInvocationException,
			IOException {
            
                for (int j = 0; j < directory.size(); j++) {
                    String axisW = sampleDataLoc + File.separator + "normalize"
				+ File.separator + directory.get(j).getName();
		File fileW = new File(axisW);
		String[] directoriesW = fileW.list();
		for (int i = 0; i < directoriesW.length; i++) {
			if (directoriesW[i].contains("csv")) {
//				String bandRangeFile = sampleDataLoc + File.separator+ "rangeBandFile.csv";
				String letterAxisWFile = sampleDataLoc + File.separator
						+ "letter" + File.separator + directory.get(j).getName() + File.separator
						+ directoriesW[i];
				String normalAxisWFile = sampleDataLoc + "/normalize/"+directory.get(j).getName() + "/"
						+ directoriesW[i];
				assignLetter(normalAxisWFile, letterAxisWFile, rBandValueRange);
//				proxy.eval("letterrange('" + normalAxisWFile + "','"
//						+ letterAxisWFile + "','" + bandRangeFile + "')");
			}
		}
		logger.info(" Done with "+directory.get(j).getName()+" folder ...");
                }
	}

	public static void assignLetter(String normalAxisWFile,
			String letterAxisWFile, double rBandValueRange[][])
			throws IOException {

		CSVReader csvReader = new CSVReader(new FileReader(normalAxisWFile));
		List<String[]> lines = csvReader.readAll();
		csvReader.close();
		List<String> writeLines = new ArrayList<String>();
		for (String[] line : lines) {
			String  newLine = new String();
			int flag=0;
			for (int i=0;i<line.length;i++) {
				String sensorPoint = line[i];
				int value = (int)binarySearch(rBandValueRange,
						Double.parseDouble(sensorPoint),
						rBandValueRange.length -1, 0);
				if(flag==0){
					newLine= "d"+value;
					flag=1;
				}else{
					newLine= newLine+",d"+value;
				}
				
			}
			writeLines.add(newLine);
		}
		
		BufferedWriter br = new BufferedWriter(new FileWriter(new File(
				letterAxisWFile)));
		for( String line : writeLines ){
			br.write(line);
			br.write("\r\n");
		}
		
		br.close();
	}

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
