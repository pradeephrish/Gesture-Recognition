package com.asu.mwdb.phase3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.asu.mwdb.phase3.task2.QueryMapper;
import com.asu.mwdb.phase3.task3.DecisionTreeClassification;
import com.asu.mwdb.phase3.task3.KNNClassification;
import com.asu.mwdb.phase3.task3.TrainingDataMaker;
import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Utils;

public class Phase3DriverMain {

	public static void phase3DriverMainRun(String databaseDirectory) throws IOException {
		phase3Task3(databaseDirectory);
	}
	
	
	public static void phase3Task3(String databaseDirectory) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter the file for gestures and labels:");
		String gesturesLabels = br.readLine();
		while(!Utils.isFilePresent(gesturesLabels)) {
			System.out.println("File not found, please check the input");
			gesturesLabels = br.readLine();
		}
		System.out.println("Enter value of K for KNN Classification:");
		int kValue = Integer.parseInt(br.readLine());
		
		TrainingDataMaker trainingDataMaker = new TrainingDataMaker();
		File[] fileNames = Utils.getFileOrder(databaseDirectory);
		List<String> testDataFiles = trainingDataMaker.buildTrainingData(fileNames, gesturesLabels);
		KNNClassification.knnClassify(gesturesLabels,testDataFiles, kValue);
		String testdbFile = IConstants.DATA + File.separator + IConstants.TESTDB_FOR_DT;
		String traindbFile = IConstants.DATA + File.separator + IConstants.TRAININGDB_FOR_DT;
		DecisionTreeClassification.dtClassify(testdbFile, traindbFile, testDataFiles);
	}
	

}
