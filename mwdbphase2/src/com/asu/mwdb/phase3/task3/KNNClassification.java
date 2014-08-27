package com.asu.mwdb.phase3.task3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import org.apache.commons.io.FileUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Utils;

public class KNNClassification {
	
	public static void knnClassifyMatlab(MatlabProxy proxy, String databaseDirectoy, String gestureLabels, int kValue, File[] fileNames, List<String> testDataFiles) throws IOException, MatlabInvocationException{
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
	
	
	public static void knnClassify(String gestureLabels, List<String> testDataFiles,int k) throws IOException {
		String trainingFile   = IConstants.DATA + File.separator + IConstants.TRAINING_FILE_NAME;
		String testingFile    = IConstants.DATA + File.separator + IConstants.TESTING_FILE_NAME;

		CSVReader csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(trainingFile)));
		List<String[]> trainingData = csvReader.readAll();
		csvReader.close();
		
		csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(gestureLabels)));
		List<String[]> labelsData = csvReader.readAll();
		csvReader.close();
		
		csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(testingFile)));
		List<String[]> testingData = csvReader.readAll();
		csvReader.close();
		System.out.println("Labels after applying K-Nearest Neighbour are as follows:");
		String knnOutputDir = IConstants.DATA + File.separator + IConstants.KNN_OP_DIR;
		if(!Utils.isDirectoryCreated(knnOutputDir)) {
			System.out.println("Error while creating output directory for KNN");
			return;
		}
		FileWriter knnWriter = new FileWriter(new File(knnOutputDir + File.separator + IConstants.KNN_OP_FILE));
		for(String [] testRowData : testingData){
			String [][] score = new String[trainingData.size()][2];
			int i=0;
			for(String [] trainRowData : trainingData){
				score[i][0]=trainRowData[0];
				score[i][1]= ""+calculateEucledianDistance(testRowData, trainRowData).doubleValue();
				i++;
			}
			score = Utils.sortStringArray(score);
			String outputString = "File Name: "+ testRowData[0]+", Label: "+labelDecider(score, labelsData, k);
			System.out.println(outputString);
			knnWriter.write(outputString + "\n");
		}
		knnWriter.close();
		
	}
	
	private static Double calculateEucledianDistance(String [] test, String [] train){
		Double sum = 0.0;
		
		for(int i=1;i<test.length;i++){
			Double value1 = Double.parseDouble(test[i]) - Double.parseDouble(train[i]);
			value1 = value1*value1;
			sum = sum+value1;
		}
		Double dist  = Math.sqrt(sum);
		return dist;
	}

	private static String labelDecider(String [][] score , List<String []> gestureLabels, int k){
		Map <String , Integer> labelCountMapper = new HashMap<String, Integer>();
		
		for(int i =0;i<k;i++){
			for(String[] label : gestureLabels){
				if(label[0].equals(score[i][0])){
					if(labelCountMapper.containsKey(label[1])){
						Integer count = labelCountMapper.get(label[1]);
						count = count +1;
						labelCountMapper.put(label[1],count);
					}else{
						labelCountMapper.put(label[1], 1);
					}
				}
			}
			
			
		}
		LinkedHashMap<String,Integer>  rankedLabel = sortHashMapByValuesD(labelCountMapper);
		return rankedLabel.keySet().iterator().next();
	}
	
	public static LinkedHashMap sortHashMapByValuesD(Map <String , Integer>  passedMap) {
		   List mapKeys = new ArrayList(passedMap.keySet());
		   List mapValues = new ArrayList(passedMap.values());
		   Collections.sort(mapValues,Collections.reverseOrder());
		   Collections.sort(mapKeys,Collections.reverseOrder());

		   LinkedHashMap sortedMap = 
		       new LinkedHashMap();

		   Iterator valueIt = mapValues.iterator();
		   while (valueIt.hasNext()) {
		       Object val = valueIt.next();
		    Iterator keyIt = mapKeys.iterator();

		    while (keyIt.hasNext()) {
		        Object key = keyIt.next();
		        String comp1 = passedMap.get(key).toString();
		        String comp2 = val.toString();

		        if (comp1.equals(comp2)){
		            passedMap.remove(key);
		            mapKeys.remove(key);
		            sortedMap.put(key, (Integer)val);
		            break;
		        }

		    }

		}
		return sortedMap;
		}
	
}
