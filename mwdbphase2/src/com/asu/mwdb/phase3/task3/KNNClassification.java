package com.asu.mwdb.phase3.task3;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Utils;

public class KNNClassification {
	
	public static void knnClassify(MatlabProxy proxy, String databaseDirectoy, String gestureLabels, int kValue, File[] fileNames, List<String> testDataFiles) throws IOException, MatlabInvocationException{
		String trainingFile   = IConstants.DATA + File.separator + IConstants.TRAINING_FILE_NAME;
		String labelsFile     = IConstants.DATA + File.separator + IConstants.LABELS_FILE_NAME;
		String testingFile    = IConstants.DATA + File.separator + IConstants.TESTING_FILE_NAME;
		String tempOutputPath = IConstants.DATA + File.separator + IConstants.OUTPUT_LABELS_TEMP_KNN;
		String knnOutputPath  = IConstants.DATA + File.separator + IConstants.OUTPUT_LABELS_KNN;
		String arg = "KNNClassifier('" + new File(testingFile).getAbsolutePath() + "','" + new File(trainingFile).getAbsolutePath() + "','"
				+ new File(labelsFile).getAbsolutePath() + "'," + kValue + ",'" + IConstants.EUCLIDEAN_DIST + "','" 
			    +  IConstants.NEAREST + "','" + new File(tempOutputPath).getAbsolutePath() + "')";
		proxy.eval(arg);
		

		List<String> tempLabels = FileUtils.readLines(new File(tempOutputPath));
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(knnOutputPath)), ',', CSVWriter.NO_QUOTE_CHARACTER);
		List<String[]> finalLabels = new ArrayList<String[]>();
		int i = 0;
		for(String testFile : testDataFiles) {
			String[] line = new String[2];
			line[0] = testFile;
			line[1] = tempLabels.get(i++);
			finalLabels.add(line);
		}
		csvWriter.writeAll(finalLabels);
		csvWriter.close();
	}
	
}
