package com.asu.mwdb.phase2Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is responsible for calculating similarity scores between the query file
 * and the database files
 * @author Kedar Joshi
 *
 */
public class SearchDatabaseForSimilarity {
	
	private Map<String,Integer> tfGlobalMap;
	private List<Map<String,List<Double>>> inputFileGesturewords; 
	private List<List<Map<String,List<Double>>>> dictionary;
	private List<List<Map<String,List<Double>>>> inputDictionary;
	public List<List<Map<String, List<Double>>>> getInputDictionary() {
		return inputDictionary;
	}

	private List<Map<String,Double>> dictionaryPerDocument; //per documents means per row
	private DictionaryBuilderPhase2 dictionaryCreator;
	private Double maxTF;
	
	/**
	 * Can be used later on to find similarity 
	 */
	enum DistanceFunction{
		CosineFunction,
		Mahanolobis;
	}
	
	/**
	 * User choice for displaying TF, IDF etc values
	 */
	public enum UserChoice {
		TF,
		IDF,
		IDF2,
		TFIDF,
		TFIDF2,
		PCA_LSA,
		SVD_LSA,
		LDA_LSA;
	}
	
	
	/**
	 * This constructor will make a call similar to Task 1 to calculate TF IDF etc 
	 * and then compute similarity scores
	 * @param tfGlobalMap
	 * @param dictionary
	 * @param dictionaryPerDocument
	 * @param wordLength
	 * @param shiftLength
	 * @param inputFilePath
	 * @param constructGestureWords
	 * @throws IOException
	 */
	public SearchDatabaseForSimilarity(Map<String,Integer> tfGlobalMap,List<List<Map<String,List<Double>>>> dictionary,List<Map<String,Double>> dictionaryPerDocument,Integer wordLength,Integer shiftLength,String inputFilePath,DictionaryBuilderPhase2 dictionaryCreator) throws IOException{
		this.tfGlobalMap = tfGlobalMap;
		this.dictionary=dictionary;
		this.inputDictionary = new ArrayList<List<Map<String,List<Double>>>>();
		this.dictionaryPerDocument=dictionaryPerDocument;
		this.dictionaryCreator = dictionaryCreator;
		inputFileGesturewords = new ArrayList<Map<String,List<Double>>>();
		// calculate new dictionary for query file
		init(wordLength,shiftLength,inputFilePath);

		HashMap<Integer, Double> tfIDFScores = computeSimilarilty(inputDictionary,this.dictionary, DistanceFunction.CosineFunction,UserChoice.TFIDF);
		HashMap<Integer, Double> tfIDF2Scores = computeSimilarilty(inputDictionary,this.dictionary, DistanceFunction.CosineFunction,UserChoice.TFIDF2);
		
		
		System.out.println("Top 5 documents with similar TF-IDF values are as follows:");
		displayMapResults(tfIDFScores);
		System.out.println("Top 5 documents with similar TF-IDF2 values are as follows:");
		displayMapResults(tfIDF2Scores);
	}

	/**
	 * Print the results onto screen displaying top 10 similar values
	 * @param tfidfSimilarScores
	 * @param i
	 */
	private void displayMapResults(HashMap<Integer, Double> tfidfSimilarScores) {
		int counter = 0;
		for (Entry<Integer, Double> entry : tfidfSimilarScores.entrySet()) { 
		    Integer key = entry.getKey();		    
		    File file = dictionaryCreator.getFileNames()[key];
		    String fileName = file.getAbsolutePath();
		    fileName = fileName.replace("letters" + File.separator, "");		    
		    System.out.println((counter + 1)+ " - " + fileName + "        " + entry.getValue());
		    counter = counter + 1;
		    if(counter == 5) {
		    	break;
		    }
		}
		System.out.println("******************************************************************");
	}

	/**
	 * This function is similar to dictionary creator function
	 * @param wordLength
	 * @param shiftLength
	 * @param inputFileName
	 * @throws IOException
	 */
	private void init(Integer wordLength,Integer shiftLength, String inputFileName) throws IOException { 
		File file = new File(inputFileName);
		int position = inputFileName.lastIndexOf(File.separator);
		if (position > 0) {
			String currentDirectory = inputFileName.substring(0, position);
			String lettersFileLocation = currentDirectory + File.separator + "gaussian.csv";
			BufferedReader in = new BufferedReader(new FileReader(new File(lettersFileLocation)));
			
			List<Map<String,List<Double>>> mapPerGestureFile = new ArrayList<Map<String,List<Double>>>(); 
			
			Map<String,List<Double>> wordMap = null;  
			while(in.ready()) {
				wordMap = new HashMap<String, List<Double>>(); 
				String series = in.readLine();
				String letters[]= series.split(",");
				Integer lastLocationForRef = -1; // for padding
				double totalWordCountPerDocument = 0.0; 
				for (int curLineCharLocation = 0; curLineCharLocation < letters.length
						- wordLength + 1; curLineCharLocation = curLineCharLocation
						+ shiftLength) {
					String currentWord = "";
					for (int currentWordLocation = curLineCharLocation; currentWordLocation < wordLength
							+ curLineCharLocation; currentWordLocation++) {
						currentWord += letters[currentWordLocation];
					}

					addWordToMap(wordMap, currentWord);
					lastLocationForRef = curLineCharLocation + shiftLength;
					totalWordCountPerDocument = totalWordCountPerDocument + 1;
				}
				Integer difference = letters.length - lastLocationForRef;
				if (difference > 0) {
					String paddedWord = "";
					Integer paddingSize = wordLength - difference;
					while (difference > 0) {
						paddedWord = paddedWord + letters[lastLocationForRef];
						difference = difference - 1;
						lastLocationForRef = lastLocationForRef + 1;
					}
					while (paddingSize > 0) {
						paddedWord = paddedWord + letters[lastLocationForRef - 1];
						paddingSize = paddingSize - 1;
					}
				addWordToMap(wordMap, paddedWord);
				totalWordCountPerDocument = totalWordCountPerDocument + 1;
                }			
				wordMap = updateWordMapForTotalCountK(wordMap,totalWordCountPerDocument);// n/k , where n is frequency of word in doc/ k total freq
				mapPerGestureFile.add(wordMap);	
				inputFileGesturewords.add(wordMap); // copy for processing later -now used for finding max TF in give gesture file
				
			}
			//now creating new Input Dictionary for the input data 
			inputDictionary.add(mapPerGestureFile);
			//update TFGlobalMap
			maxTF = getMaxTF(inputFileGesturewords);
			//Update last row in Dictionary for IDF values which has only tf values
			calculateIDF(dictionary,inputDictionary,tfGlobalMap);
			//Compute IDF2 , First Create Local Dictionary Per Document then Compute IDF2
			calculateIDF2(inputDictionary,mapPerGestureFile);
			//save first element in the list
			writeToFile(inputDictionary.get(0),currentDirectory);
			
			
		} else {
			System.out.println("Error while processing Task 3 input");
		}
		
	}
	/**
	 * Simply add this word to given map
	 * @param wordMap
	 * @param currentWord
	 */
	private void addWordToMap(Map<String, List<Double>> wordMap,
			String currentWord) {
		if (wordMap.containsKey(currentWord)) {
			List<Double> list = wordMap.get(currentWord);
			list.set(0, list.get(0) + 1.0F); // 0 index for TF
			wordMap.put(currentWord, list);
		} else {
			List<Double> list = new ArrayList<Double>();
			list.add(0, 1.0);
			wordMap.put(currentWord, list);
		}
	}

	/**
	 * This function is actually calculating the TF values for each word 
	 * and setting those values back into the original map
	 * @param wordMap
	 * @param totalWordCountPerDocument
	 * @return
	 */
	private  Map<String, List<Double>> updateWordMapForTotalCountK(
			Map<String, List<Double>> wordMap, double totalWordCountPerDocument) {
		Iterator<Entry<String, List<Double>>> iterator = wordMap.entrySet().iterator();
		while(iterator.hasNext()){
			 Map.Entry<String, List<Double>> entry = (Map.Entry<String, List<Double>>) iterator.next();
			 if(totalWordCountPerDocument> 0.0)
				 entry.getValue().set(0,entry.getValue().get(0)/totalWordCountPerDocument);
		}
	
		return wordMap;
	}
	
	/**
	 * This will return the max TF value in an entire gesture file
	 * @param inputDictionary2
	 * @return
	 */
	private Double getMaxTF(List<Map<String, List<Double>>> inputDictionary2) {
		List<Double> value = new ArrayList<Double>();
		for (int i = 0; i < inputDictionary2.size(); i++) {
			 Map<String, List<Double>> listMap = inputDictionary2.get(i);
			 Iterator<Entry<String, List<Double>>> iterator = listMap.entrySet().iterator();
			 while(iterator.hasNext()){
				 Entry<String, List<Double>> entry = (Entry<String,List<Double>>)iterator.next();
				 value.add(entry.getValue().get(0));
			 }
		}
		return Collections.max(value);
	}
	
	
	/**
	 * Compute similarity between the query file and database files
	 * The measure used for similarity is Cosine similarity for now
	 * @param inputDictionary
	 * @param dictionary
	 * @param distanceFunction
	 * @param entity
	 * @return
	 */
	private LinkedHashMap<Integer,Double> computeSimilarilty(
			List<List<Map<String, List<Double>>>> inputDictionary,
			List<List<Map<String, List<Double>>>> dictionary, DistanceFunction distanceFunction, UserChoice entity) {
		HashMap<Integer,Double> scores = new HashMap<Integer,Double>();
			
		
		if(distanceFunction.equals(DistanceFunction.CosineFunction)) {
			
			if (inputDictionary.get(0).size() != dictionary.get(0).size()) {
				System.out.println(
						"** Something wrong with data or Calculations **");
			}
			for (int i = 0; i < dictionary.size(); i++) {
				List<Map<String, List<Double>>> entry = dictionary.get(i);
				List<Map<String, List<Double>>> inputEntry = inputDictionary
						.get(0);// always 0
				double sum = 0.0d;
				try {
					for (int k = 0; k < inputEntry.size(); k++) {
						Map<String, Double> inputMap = convertMap(inputEntry.get(k), entity);
						Map<String, Double> dictMap = convertMap(entry.get(k), entity);
						sum = sum + CosineSimilarity.difference(inputMap, dictMap);
					}
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("There are no 20 rows/same number of rows in the input file");
					System.out.println(
							"There are no 20 rows/same number of rows in the input file");
				}
				scores.put(i, sum);
			}
		}
			
			return  sortHashMapByValuesD(scores);
		}

	private Map<String, Double> convertMap(Map<String, List<Double>> map,
			UserChoice entity) {
			Map<String,Double> values = new HashMap<String, Double>(); 
			Iterator<Entry<String, List<Double>>> iterator = map.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String, List<Double>> entry = iterator.next();
				values.put(entry.getKey(), entry.getValue().get(entity.ordinal()));
			}
			
		return values;
	}

	private void writeToFile(List<Map<String, List<Double>>> gestureDocument,String inputFolder) throws IOException {
		File task3OutputFileObj = new File(inputFolder + File.separator
				+ "task3Output");
		if (task3OutputFileObj.exists()) {
			FileIOHelper.delete(task3OutputFileObj);
		}
		File task3OutputFolderAll = new File(inputFolder + File.separator
				+ "task3OutputAll");
		if (task3OutputFolderAll.exists()) {
			FileIOHelper.delete(task3OutputFolderAll);
		}
		if (task3OutputFileObj.mkdir() && task3OutputFolderAll.mkdir()) {
		
		
		FileWriter fileWriter3 = new FileWriter(task3OutputFileObj +File.separator+"task3.csv",true);
		FileWriter fileWriter5 = new FileWriter(task3OutputFolderAll+File.separator+"task3.csv",true);
		BufferedWriter bufferWriter3 = new BufferedWriter(fileWriter3); 
		BufferedWriter bufferWriter5 = new BufferedWriter(fileWriter5);
		
		
		for (int j = 0; j < gestureDocument.size(); j++) {
			Map<String,List<Double>> tempMap = gestureDocument.get(j);
			Iterator<Entry<String, List<Double>>> it = tempMap.entrySet().iterator();				
			  while (it.hasNext()) {
			        Map.Entry<String,List<Double>> pairs = (Map.Entry)it.next();
			        bufferWriter3.write(pairs.getKey()+":"+pairs.getValue().get(0)+","+pairs.getValue().get(1)+","+pairs.getValue().get(2)+";");
			        bufferWriter5.write(pairs.getKey()+":"+pairs.getValue().get(0)+","+pairs.getValue().get(1)+","+pairs.getValue().get(2)+","+pairs.getValue().get(3)+","+pairs.getValue().get(4)+";");	    
			    }
		      bufferWriter3.write("\r\n");
		      bufferWriter5.write("\r\n");
		}
		
		  bufferWriter3.close();
		  bufferWriter5.close();
		} else {
			System.out.println("Could not create output directories for Task 3");
		}
	}

	/**
	 * Calculate IDF2 value for query file
	 * @param inputDictionary
	 * @param mapPerGestureFile
	 */
	private void calculateIDF2(List<List<Map<String, List<Double>>>> inputDictionary, List<Map<String, List<Double>>> mapPerGestureFile) {  
	
		Map<String,Double> idf2PerDocument = new HashMap<String, Double>(); // per univariate series
		
		for (int i = 0; i < mapPerGestureFile.size(); i++) {
			Map<String,List<Double>> tmpMap = mapPerGestureFile.get(i);
			Iterator iterator = tmpMap.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String, List<Double>> pairs = (Entry<String, List<Double>>) iterator.next();
				if(idf2PerDocument.containsKey(pairs.getKey()))
						idf2PerDocument.put(pairs.getKey(), idf2PerDocument.get(pairs.getKey())+1.0F); 
				else
						idf2PerDocument.put(pairs.getKey(), 1.0);
			}
		}
		dictionaryPerDocument.add(idf2PerDocument);
		
		List<Map<String,List<Double>>> oneFile = inputDictionary.get(0); // only single element in list 
		for (int i = 0; i < oneFile.size(); i++) {
			Map<String,List<Double>> mapForRow = oneFile.get(i);
			Iterator<Entry<String, List<Double>>> iterator = mapForRow.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String, List<Double>> pair = iterator.next();
				Double inverse = new Double(( oneFile.size())/ idf2PerDocument.get(pair.getKey()));
				Double expression = 0.5 * (pair.getValue().get(0));  //from Sallen & Buckley tfidf formula
				if(maxTF>0.0) {
					expression=0.5 + expression/maxTF;
				}
				else {
					expression = 0.0;
				}
				pair.getValue().set(2,Math.log(inverse));
				pair.getValue().add(pair.getValue().get(0)*pair.getValue().get(2));
			}
		}		
	}

	/**
	 * IDF for query file
	 * @param dictionary
	 * @param inputDictionary
	 * @param tfGlobalMap2
	 */
	private void calculateIDF(List<List<Map<String, List<Double>>>> dictionary,List<List<Map<String, List<Double>>>> inputDictionary,
			Map<String, Integer> tfGlobalMap2) {
		List<Map<String,List<Double>>> currentFile = inputDictionary.get(0); //since single file input, only one element in list 
		for (int i = 0; i < currentFile.size(); i++) {
			Map<String,List<Double>> mapForRow = currentFile.get(i);
			Iterator<Entry<String, List<Double>>> iterator = mapForRow.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String, List<Double>> pair = iterator.next();
				
				//after discussion with kedar
				Integer globalMapCount = tfGlobalMap2.get(pair.getKey());
				if(globalMapCount==null)
					globalMapCount = 1;
				
				Double inverse = new Double(( currentFile.size() * dictionary.size())/ globalMapCount ) ; // tf value at 0 , size is now N, because pushing input tfidfs to new dictionary
				Double expression = 0.5 * (pair.getValue().get(0));  //from Sallen & Buckley tfidf formula
				if(maxTF > 0.0) {
					expression=0.5 + expression/maxTF;
				}
				else {
					expression = 0.0;
				}
				pair.getValue().add(Math.log(inverse)); // idf
				pair.getValue().add(0.0); // temporary value idf2,  will be added by processIDF2
				pair.getValue().add(Math.log(inverse)*expression); // tf-idf
			}
		}
	}

	public static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
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
		            sortedMap.put(key, (Double)val);
		            break;
		        }

		    }

		}
		return sortedMap;
		}
	
		
}
