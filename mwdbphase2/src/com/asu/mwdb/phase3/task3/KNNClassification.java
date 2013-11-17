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
	
	public static void knnClassify(MatlabProxy proxy, String databaseDirectoy, String gestureLabels) throws IOException, MatlabInvocationException{
		
		// just go to any component folder and get the file name order
		File[] dirs = new File(databaseDirectoy).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return pathname.isDirectory() && !name.contains(IConstants.ALL);
			}
		});
		File componentDir = dirs[0];
		File[] fileNames = componentDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return name.endsWith(".csv") && pathname.isFile() && !name.contains(IConstants.GAUSSIAN_FILE) 
					   && !name.contains(IConstants.NORMALIZED_FILE);
			}
		});
		
		TrainingDataMaker trainingDataMaker = new TrainingDataMaker();
		// return the test data files just to display results in output file in the following format
		// filename - label
		List<String> testDataFiles = trainingDataMaker.buildTrainingData(fileNames, gestureLabels);
		String trainingFile   = IConstants.DATA + File.separator + IConstants.TRAINING_FILE_NAME;
		String labelsFile     = IConstants.DATA + File.separator + IConstants.LABELS_FILE_NAME;
		String testingFile    = IConstants.DATA + File.separator + IConstants.TESTING_FILE_NAME;
		String tempOutputPath = IConstants.DATA + File.separator + IConstants.OUTPUT_LABELS_TEMP_KNN;
		String knnOutputPath  = IConstants.DATA + File.separator + IConstants.OUTPUT_LABELS_KNN;
		Utils.kNNClassify(proxy, new File(testingFile).getAbsolutePath() , new File(trainingFile).getAbsolutePath(), 
				new File(labelsFile).getAbsolutePath(), 2, new File(tempOutputPath).getAbsolutePath());
		

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
