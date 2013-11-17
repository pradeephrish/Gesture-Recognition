package com.asu.mwdb.phase3.task3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Utils;

public class TrainingDataMaker {
	public List<String> buildTrainingData(File[] fileNames, String gestureLabels) throws IOException {
		String ggFilePath  = IConstants.DATA + File.separator + IConstants.PCA_GG_COMBINED + File.separator + IConstants.GG_PCA_FILE_NAME;
		
		CSVReader csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(gestureLabels)));
		List<String[]> fileData = csvReader.readAll();
		csvReader.close();
		
		csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(ggFilePath)));
		List<String[]> ggFileData = csvReader.readAll();
		csvReader.close();
		
		List<String[]> trainingData = new ArrayList<String[]>();
		String[] labels = new String[fileData.size()];
		int count = 0;
		for(String[] line : fileData) {
			int index = Utils.getFileIndex(fileNames, line[0]);
			labels[count++] = line[1];
			if(index != -1) {
				trainingData.add(ggFileData.get(index));
			}
		}
		
		// write training data and labels into separate files so that they can be used in KNN
		String trainingFile = IConstants.DATA + File.separator + IConstants.TRAINING_FILE_NAME;
		String labelsFile   = IConstants.DATA + File.separator + IConstants.LABELS_FILE_NAME;
		String testingFile  = IConstants.DATA + File.separator + IConstants.TESTING_FILE_NAME;
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(trainingFile)), ',', CSVWriter.NO_QUOTE_CHARACTER);
		csvWriter.writeAll(trainingData);
		csvWriter.close();
		
		FileWriter writer = new FileWriter(labelsFile);
		for(String label : labels) {
			writer.write(label + "\n");
		}
		writer.close();
		
		List<String> testFileNames = diffFileName(fileNames, fileData);
		List<String[]> testingData = new ArrayList<String[]>();
		for(int i=0; i < testFileNames.size(); i++) {
			int index = Utils.getFileIndex(fileNames, testFileNames.get(i));
			if(index != -1) {
				testingData.add(ggFileData.get(index));
			}
		}
		csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(testingFile)), ',', CSVWriter.NO_QUOTE_CHARACTER);
		csvWriter.writeAll(testingData);
		csvWriter.close();
		return testFileNames;
	}
	
	private static List<String> diffFileName(File [] fileNames,List<String[]> candanData){
		List<String> testingFiles = new ArrayList<String>();
		for ( int i =0 ;i<fileNames.length;i++){
			if(!hasString(candanData,fileNames[i].getName())){
				testingFiles.add(fileNames[i].getName());
			}
		}
		return testingFiles;
	}
	
	public static boolean hasString(List<String[]> str,String name ){
		for(int j=0;j<str.size();j++){
			if(str.get(j)[0].equals(name)){
				return true;
			}
		}
		return false;
	}
}
