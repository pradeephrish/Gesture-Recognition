package com.asu.mwdb.phase3.task3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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
				String str[] = ggFileData.get(index);
				String str1[] = new String [str.length+1];
				str1[0] = line[0];
				for(int i=0;i<str.length;i++){
					str1[i+1]=str[i];
				}
				trainingData.add(str1);
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
				String str[] = ggFileData.get(index);
				String str1[] = new String [str.length+1];
				str1[0] = testFileNames.get(i);
				for(int j=0;j<str.length;j++){
					str1[j+1]=str[j];
				}
				testingData.add(str1);
			}
		}
		csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(testingFile)), ',', CSVWriter.NO_QUOTE_CHARACTER);
		csvWriter.writeAll(testingData);
		csvWriter.close();
		
		// making decision tree data training file
		FileWriter writerDecisionTree = new FileWriter(new File(IConstants.DATA + File.separator + IConstants.TRAININGDB_FOR_DT));
		writerDecisionTree.write("trainingdata" + "\n");
		
		int size = trainingData.get(0).length - 1;
		
		String headerLine = new String("");
		for(int i=1; i<=size; i++) {
			headerLine = headerLine + "attr" + i + " " + "numerical ";
		}
		headerLine = headerLine + "label " + "symbolic";
		writerDecisionTree.write(headerLine + "\n");
		
		int labelCount = 0;
		for(String[] line : trainingData) {
			String finalLine = "";
			for(int j=1; j<line.length; j++) {
				if(line[j].contains("E")) {
					finalLine = finalLine + "0" + " ";
				} else {
					finalLine = finalLine + line[j] + " ";
				}
				
			}
			finalLine = finalLine + labels[labelCount++] + "\n";
			writerDecisionTree.write(finalLine);
		}
		writerDecisionTree.close();
		// ***********************************************************
		// make decision tree data for testing file
		writerDecisionTree = new FileWriter(new File(IConstants.DATA + File.separator + IConstants.TESTDB_FOR_DT));
		writerDecisionTree.write("testingdata" + "\n");
		
		size = testingData.get(0).length - 1;
		headerLine = new String("");
		for(int i=1; i<=size; i++) {
			headerLine = headerLine + "attr" + i + " " + "numerical ";
		}
		headerLine = headerLine + "label " + "symbolic";
		writerDecisionTree.write(headerLine + "\n");
		labelCount = 0;
		for(String[] line : testingData) {
			String finalLine = "";
			for(int j=1; j<line.length; j++) {
				if(line[j].contains("E")) {
					finalLine = finalLine + "0" + " ";
				} else {
					finalLine = finalLine + line[j] + " ";
				}
				
			}
			finalLine = finalLine + "0" + "\n";
			writerDecisionTree.write(finalLine);
		}
		writerDecisionTree.close();
		
		
		
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
