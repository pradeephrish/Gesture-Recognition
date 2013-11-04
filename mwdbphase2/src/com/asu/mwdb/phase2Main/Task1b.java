package com.asu.mwdb.phase2Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.utils.Phase2Utils;
import com.asu.mwdb.utils.Utils;

public class Task1b {

	
	public static void executeTask1b(double[][] rBandValueRange,
			String inputDirectory, MatlabProxy proxy,Map<String, DictionaryBuilderPhase2> dictMap,Integer wordLength,Integer shiftLength) throws IOException,
			MatlabInvocationException {
		System.out.println("Enter query file name for Task 1b::");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inputFileLocation = br.readLine();
		NormalizeData.NormalizeDataForSingleFile(proxy, inputFileLocation);
		System.out.println("Done normalization for Task 1b");
		AssignBandValues.assignGaussianCurveTask3(proxy, inputFileLocation,
				rBandValueRange);
		DictionaryBuilderPhase2 componentDictionary = dictMap.get(inputDirectory);
		
		// this will construct query dictionary and also print top 3 similar documents based on TF-IDF/TF-IDF2 space
		SearchDatabaseForSimilarity task3FindSimilarData = new SearchDatabaseForSimilarity(
				componentDictionary.getTfIDFMapGlobal(), componentDictionary.getTfMapArrayIDF(),
				componentDictionary.getTfMapArrayIDF2(), wordLength, shiftLength,
				inputFileLocation, componentDictionary);
		
		
		// get the dictionary that you just created
		List<List<Map<String,List<Double>>>> queryDictionary = task3FindSimilarData.getInputDictionary();
		Phase2Utils main = new Phase2Utils();
		Map<Integer, Set<String>> queryWordMap = main.createWordsPerSensor(queryDictionary);
		List<Map<String, Double[]>> queryWordScores = main.createSensorWordScores(queryWordMap, queryDictionary , 3);
	
		String queryFilePath = "." + File.separator + "data" + File.separator + "queryWords";
		File file = new File(queryFilePath);
		File[] queryFiles = file.listFiles();
		
		// TODO - Create folders for each component and fetch the semantic values from that location
		String semanticDir = "." + File.separator + "data" + File.separator + "pca-semantic";
		String semanticOutputDirectory = "." + File.separator + "data" + File.separator + "pca-semantic-mapped";
		
		File semanticFileDirObj = new File(semanticDir);
		File[] semanticFiles = semanticFileDirObj.listFiles();
		Iterator<Map<String, Double[]>> queryIt = queryWordScores.iterator();
		List<String[]> queryTransformList = new ArrayList<String[]>();
		for(File semanticFile : semanticFiles) {
			List<Map<String, Double>> semanticData = getSemanticFileData(semanticFile);
			Map<String, Double[]> queryMap =null;
			if(queryIt.hasNext()){
				queryMap=queryIt.next();
			}
			Iterator<Map<String, Double>> semanticIterator = semanticData.iterator();
			String xyz[] = new String[3];
			int k =0 ; 
			while(semanticIterator.hasNext()){
				Map<String, Double> semanticMap = semanticIterator.next();
				String[] keys = new String[queryMap.size()];
				queryMap.keySet().toArray(keys);
				double ans = 0;
				for (int i = 0; i < keys.length; i++) {
					if (semanticMap.containsKey(keys[i])) {
						ans += queryMap.get(keys[i])[0] * semanticMap.get(keys[i]);
					}
				}
				xyz[k]= "" + ans;
				k++;
			}
			queryTransformList.add(xyz);
			
		}
		 CSVWriter csvWriter = new CSVWriter(new FileWriter(semanticOutputDirectory + File.separator + "queryMapped.csv"), ',',
				CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END);
		
		 for(int i =0; i< queryTransformList.size(); i++) {
			 csvWriter.writeNext(queryTransformList.get(i));
		 }
		 csvWriter.close();
		 
		 File fileTemp = new File(inputDirectory);
		 File[] listFiles = fileTemp.listFiles();
		 String transformedDirectory = "." + File.separator + "data" + File.separator + "pca-transform";
		 List<List<String[]>> pcaTrasnsformData = Utils.convertDataForComparison(transformedDirectory, listFiles);
		 
		 HashMap<Integer, Double> pcaScores = searchForSimilarLSA(queryTransformList, pcaTrasnsformData);
		 File fileObj = new File(inputDirectory);
		 File[] files = fileObj.listFiles();
			
		System.out.println("Top 5 documents with similar PCA are as follows:");
		displayMapResults(pcaScores, files);
		System.out.println();
	}
	
	private static List<Map<String, Double>> getSemanticFileData(
			File semanticFile) throws IOException {
		List<Map<String, Double>> semanticData = new ArrayList<Map<String,Double>>();
		CSVReader csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(semanticFile.getAbsolutePath())));
		String[] words = csvReader.readNext();
		// read next 3 lines which have values and populate our map
		for(int i=0; i<3; i++) {
			Map<String, Double> vectorMap = new HashMap<String, Double>();
			String[] vectorValuesStr = csvReader.readNext();
			for(int j=0; j < words.length; j++) {
				vectorMap.put(words[j], Double.parseDouble(vectorValuesStr[j]));
			}
			semanticData.add(vectorMap);
			
		}
		csvReader.close();
		return semanticData;
	}
	
	private static LinkedHashMap<Integer, Double> searchForSimilarLSA(
			List<String[]> queryData, List<List<String[]>> pcaTrasnsformData) {
		HashMap<Integer,Double> scores = new HashMap<Integer, Double>();
		for(int i =0; i<pcaTrasnsformData.size(); i++) {
			List<String[]> documentData = pcaTrasnsformData.get(i);
			double score = CosineSimilarity.compareLSADocument(queryData, documentData);
			scores.put(i, score);
		}
		return SearchDatabaseForSimilarity.sortHashMapByValuesD(scores);
	}
	
	private static void displayMapResults(HashMap<Integer, Double> tfidfSimilarScores, File[] files) {
		int counter = 0;
		for (Entry<Integer, Double> entry : tfidfSimilarScores.entrySet()) { 
		    Integer key = entry.getKey();		    
		    File file = files[key];		    
		    System.out.println((counter + 1) + "  " + file.getAbsolutePath() + "        ");
		    counter = counter + 1;
		    if(counter == 5) {
		    	break;
		    }
		}
		System.out.println("**********************");
	}
}
