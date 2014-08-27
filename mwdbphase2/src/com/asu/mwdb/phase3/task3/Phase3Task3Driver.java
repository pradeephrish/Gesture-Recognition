package com.asu.mwdb.phase3.task3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.asu.mwdb.utils.Utils;

public class Phase3Task3Driver {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter database directory:");
		String databaseDirectory = br.readLine();
		System.out.println("Enter the file for gestures and labels:");
		String gesturesLabels = br.readLine();
		while(!Utils.isFilePresent(gesturesLabels)) {
			System.out.println("File not found, please check the input");
			gesturesLabels = br.readLine();
		}
		System.out.println("Enter value of K for KNN Classification:");
		int kValue = Integer.parseInt(br.readLine());
		
		TrainingDataMaker trainingDataMaker = new TrainingDataMaker();
		// return the test data files just to display results in output file in the following format
		// filename - label
		File[] fileNames = Utils.getFileOrder(databaseDirectory);
		List<String> testDataFiles = trainingDataMaker.buildTrainingData(fileNames, gesturesLabels);
		KNNClassification.knnClassify(gesturesLabels,testDataFiles, kValue);
//		/DecisionTreeClassification.dtClassify("", "");
		

	}

}
