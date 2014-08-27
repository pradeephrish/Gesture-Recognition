package com.asu.mwdb.phase3.svmlib.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;

import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Utils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class RunSVM {
	public static void classifySVM(List<String> testFileNames) throws IOException {
		
		String trainingDataFile = IConstants.DATA + File.separator + IConstants.TRAINING_FILE_NAME;
		String svmTrainingData  = IConstants.DATA + File.separator + IConstants.TRAINDATA_SVM_FORMAT;
		String testingDataFile  = IConstants.DATA + File.separator + IConstants.TESTING_FILE_NAME;
		String svmTestingData   = IConstants.DATA + File.separator + IConstants.TESTDATA_SVM_FORMAT;
		String labelsFile 	    = IConstants.DATA + File.separator + IConstants.LABELS_FILE_NAME;
		
		/* Prepare training data format for SVM */
		CSVReader csvReader = new CSVReader(new FileReader(new File(trainingDataFile)));
		CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(svmTrainingData)),' ',CSVWriter.NO_QUOTE_CHARACTER);
		List<String[]> input = csvReader.readAll();
		List<String[]> output = new ArrayList<String[]>();
		CSVReader labelsReader = new CSVReader(new FileReader(new File(labelsFile)));
		List<String[]> labelsData = labelsReader.readAll();
		labelsReader.close();
		String labels[] = new String[labelsData.size()];
		for(int i=0; i < labelsData.size(); i++) {
			labels[i] = labelsData.get(i)[0];
		}
		for (int i = 0; i < input.size(); i++) {
			String[] row = input.get(i);
			String[] rowTemp = new String[row.length-1];
			String[] outputRow = new String[row.length];
			for (int j = 1; j < row.length; j++) {
				rowTemp[j-1]=(j-1)+":"+row[j];
			}
			outputRow[0]=labels[i];
			for (int j = 0; j < rowTemp.length; j++) {
				outputRow[j+1]=rowTemp[j];
			}
			output.add(outputRow);
		}
		csvWriter.writeAll(output);
		csvReader.close();
		csvWriter.close();
		
		/* Prepare testing data format for SVM */
		csvReader = new CSVReader(new FileReader(new File(testingDataFile)));
		csvWriter = new CSVWriter(new FileWriter(new File(svmTestingData)),' ',CSVWriter.NO_QUOTE_CHARACTER);
		input = csvReader.readAll();
		output = new ArrayList<String[]>();
		for (int i = 0; i < input.size(); i++) {
			String[] row = input.get(i);
			String[] rowTemp = new String[row.length-1];
			String[] outputRow = new String[row.length];
			for (int j = 1; j < row.length; j++) {
				rowTemp[j-1]=(j-1)+":"+row[j];
			}
			outputRow[0]="9999";
			for (int j = 0; j < rowTemp.length; j++) {
				outputRow[j+1]=rowTemp[j];
			}
			output.add(outputRow);
		}
		csvWriter.writeAll(output);
		csvReader.close();
		csvWriter.close();
		
		// train svm model
		String svmModelDir = IConstants.DATA + File.separator + IConstants.SVM_OP_LOC;
		if(!Utils.isDirectoryCreated(svmModelDir)) {
			System.out.println("Error while creating svm model directory");
		}
		String modelFile = svmModelDir + File.separator + IConstants.MODEL_FILE_NAME;
		String arg = "-s 0 -t 0 -b 1 " + svmTrainingData + " " + modelFile;
		SVMTrain.main(arg.split(" "));
		System.out.println("SVM Model trained successfully");
		
		// predict svm output 
		String tempSVMOutput = svmModelDir + File.separator + IConstants.SVM_TEMP_OUTPUT;
		arg = "-b 1 " + svmTestingData + " " + modelFile + " " +  tempSVMOutput;
		SVMPredict.main(arg.split(" "));
		System.out.println("Predicted results successfully");
		
		BufferedReader svmOpReader = new BufferedReader(new FileReader(new File(tempSVMOutput)));
		String line = null;
		FileWriter svmWriter = new FileWriter(new File(svmModelDir + File.separator + IConstants.SVM_FINAL_OUTPUT));
		svmOpReader.readLine(); // ignore the fist line
		int count = 0;
		while((line = svmOpReader.readLine()) != null) {
			String[] tokens = line.split(" ");
			String label = tokens[0];
			String outputLine = "File Name: " + testFileNames.get(count++) + ", Label: " + label.substring(0, label.indexOf("."));
			System.out.println(outputLine);
			svmWriter.write(outputLine + "\n");
		}
		svmWriter.close();
	}
}
