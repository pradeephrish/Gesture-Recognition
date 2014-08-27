package com.asu.mwdb.phase2Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class DictionaryBuilderPhase2 { 
	
	/**
	 * Global map for TF IDF values
	 */
	private  Map<String,Integer> tfIDFMapGlobal = new HashMap<String,Integer>();
	/**
	 * Global dictionary containing all values and maps/vectors
	 */
	private  List<List<Map<String,List<Double>>>> tfMapArrayIDF = new ArrayList<List<Map<String,List<Double>>>>();
	/**
	 * Similar to the one above, this one is specifically designed for IDF2 values
	 */
	private  List<Map<String,Double>> tfMapArrayIDF2 = new ArrayList<Map<String,Double>>();
	/**
	 * List of files in input directory
	 */
	private  File[] fileNames; 
	/**
	 * Storage for all IDF values
	 */
	private  List<Double> idfValues = new ArrayList<Double>();
	/**
	 * Storage for all IDF2 values
	 */
	private  List<Double> idf2Values = new ArrayList<Double>();
	
	/**
	 * Given a input folder name, this function will create a dictionary of TF, IDF, TF-IDF, TF-IDF2 values
	 * @param wordLength
	 * @param shiftLength
	 * @param lettersInputFolder
	 * @param outputFolder
	 * @param alloutputFolder
	 * @throws IOException
	 */
	public void createDictionary(int wordLength, int shiftLength, String lettersInputFolder) throws IOException{
		//read File
		// go to letters directory
		String seriesLetterFolder = lettersInputFolder + File.separator + "letters";
		File directory = new File(seriesLetterFolder);  
		fileNames  = directory.listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File pathname) {
		        String name = pathname.getName().toLowerCase();
		        return name.endsWith(".csv") && pathname.isFile();
		    }
		});
		for (int i = 0; i < fileNames.length; i++) {
			
			// Overall structure is as follows:
			// On each row, for each word - we have a Map of String and List<Double> indicating a word
			// and list for TF, IDF, and IDF2 values. So for each csv file, you will have a List of these maps.
			// the size of the list is 20. Eventually the global dictionary will be a List of such lists.
			// in case of sample data, the global dictionary is of 60 lists.
			BufferedReader in = new BufferedReader(new FileReader(fileNames[i]));
			
			
			// for each document
			List<Map<String,List<Double>>> mapPerGestureFile = new ArrayList<Map<String,List<Double>>>(); 
			
			Map<String,List<Double>> wordMap = null;  //per row 
			while(in.ready()) {
				wordMap = new HashMap<String, List<Double>>();  //per rows
				String series = in.readLine();
				String letters[]= series.split(",");
				Integer lastLocationForRef = -1; // for padding
				double totalWordCountPerDocument = 0.0; 
				for (int curLineCharLocation = 0; curLineCharLocation < letters.length
						- wordLength + 1; curLineCharLocation = curLineCharLocation
						+ shiftLength) {
					// this for loop runs on each line and curLineCharLocation
					// indicates the current pointer
					// from which we should be forming the word
					// extract a word and move the shift length
					String currentWord = "";
					for (int currentWordLocation = curLineCharLocation; currentWordLocation < wordLength
							+ curLineCharLocation; currentWordLocation++) {
						// this inner for loop denotes the current word to be
						// created
						currentWord = currentWord
								+ letters[currentWordLocation];
					}
					addWordToMap(wordMap, currentWord);
					lastLocationForRef = curLineCharLocation + shiftLength;
					totalWordCountPerDocument = totalWordCountPerDocument + 1;
				}
				// check to see if we have any leftover strings. If yes then pad that word using
				// the last character pointed by lastLocationForRef
				Integer difference = letters.length - lastLocationForRef;
				if (difference > 0) {
					String paddedWord = "";
					Integer extraPaddingSize = wordLength - difference;
					while (difference > 0) {
						// this while loop will simply create the padded word 
						// to be appended at the end
						paddedWord = paddedWord +  letters[lastLocationForRef];
						difference = difference - 1;
						lastLocationForRef = lastLocationForRef + 1; //advance to next location
					}
					while(extraPaddingSize > 0) {
						 paddedWord = paddedWord + letters[lastLocationForRef-1];
		                 extraPaddingSize = extraPaddingSize - 1;
					}
					addWordToMap(wordMap, paddedWord);
					totalWordCountPerDocument = totalWordCountPerDocument + 1;
	              }
				wordMap = updateWordMapForTotalCountK(wordMap,totalWordCountPerDocument); // n/k , where n is frequency of word in doc/ k total freq
				mapPerGestureFile.add(wordMap);				
			}
			getTfMapArrayIDF().add(mapPerGestureFile);
			//count idf2 per document
			initIDF2Map(mapPerGestureFile);
			
			in.close();
		}
		
		//populate global map for IDF values List<LIst<Map>
		 initIDFMap(getTfMapArrayIDF());
		 //Generate IDF Files from Global Map  * TF Values
		 calculateIDFValues();
		 //Generate IDF2 Files
		 calculateIDF2Values();
		 //normalize tfidf and tfidf2
		 normalizeDictionary(); 
		 createDatabaseFiles(getTfMapArrayIDF(),lettersInputFolder);
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
			
		    //bad logic - should have used Double Array
		 	list.add(1,0.0);
		 	list.add(2,0.0);
		 	list.add(3,0.0);
		 	list.add(4,0.0);
		 	list.add(5,0.0); // unnormilzed TF
		 	// 		
		 	
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
				 entry.getValue().set(5,entry.getValue().get(0)); //store unnormized version
				 entry.getValue().set(0,entry.getValue().get(0)/totalWordCountPerDocument); //add all tf for total words
			 	}
	
		return wordMap;
	}
	
	/**
	 * After all values are calculated, normalize those
	 */
	private void normalizeDictionary() {
		
		Collections.sort(idfValues, Collections.reverseOrder());
		Collections.sort(idf2Values, Collections.reverseOrder());
		Double maxIDF=idfValues.get(0)==0.0?1.0:idfValues.get(0);
		Double maxIDF2=idf2Values.get(0)==0.0?1.0:idf2Values.get(0);	
		for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
			List<Map<String,List<Double>>> gestureDocument = getTfMapArrayIDF().get(i);
			for (int j = 0; j < gestureDocument.size(); j++) {
				Map<String,List<Double>> tempMap = gestureDocument.get(j);
				Iterator<Entry<String, List<Double>>> it = tempMap.entrySet().iterator();
				  while (it.hasNext()) {
				        Map.Entry<String,List<Double>> pairs = (Map.Entry)it.next();
				        	//normalize tf-idf
				        	pairs.getValue().set(3, pairs.getValue().get(3)/maxIDF);
				        	//normalize tf-idf2
				        	pairs.getValue().set(4, pairs.getValue().get(4)/maxIDF2);
				  }
			}
		}
	}
	
	private void createDatabaseFiles(
			List<List<Map<String, List<Double>>>> tfMapArrayIDF3, String task1OutputFolder) throws IOException {
		File task1OutputFileObj = new File(task1OutputFolder + File.separator
				+ "task1Output");
		if (task1OutputFileObj.exists()) {
			FileIOHelper.delete(task1OutputFileObj);
		}
		File task1OutputFolderAll = new File(task1OutputFolder + File.separator
				+ "task1OutputAll");
		if (task1OutputFolderAll.exists()) {
			FileIOHelper.delete(task1OutputFolderAll);
		}
		if (task1OutputFileObj.mkdir() && task1OutputFolderAll.mkdir()) {
			for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
				List<Map<String, List<Double>>> gestureDocument = getTfMapArrayIDF()
						.get(i);

				FileWriter fileWriter3 = new FileWriter(task1OutputFileObj
						+ File.separator + (fileNames[i].getName()));
				FileWriter fileWriter5 = new FileWriter(task1OutputFolderAll
						+ File.separator + (fileNames[i].getName()));

				BufferedWriter bufferWriter3 = new BufferedWriter(fileWriter3);
				BufferedWriter bufferWriter5 = new BufferedWriter(fileWriter5);

				for (int j = 0; j < gestureDocument.size(); j++) {
					Map<String, List<Double>> tempMap = gestureDocument.get(j);
					Iterator<Entry<String, List<Double>>> it = tempMap.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, List<Double>> pairs = (Map.Entry) it.next();
						bufferWriter3.write(pairs.getKey() + ":"
								+ pairs.getValue().get(0) + ","
								+ pairs.getValue().get(1) + ","
								+ pairs.getValue().get(2) + ";");
						bufferWriter5.write(pairs.getKey() + ":"
								+ pairs.getValue().get(0) + ","
								+ pairs.getValue().get(1) + ","
								+ pairs.getValue().get(2) + ","
								+ pairs.getValue().get(3) + ","
								+ pairs.getValue().get(4) + ";");
					}
					bufferWriter3.write("\r\n");
					bufferWriter5.write("\r\n");
				}
				bufferWriter3.close();
				bufferWriter5.close();
			}
		} else {
			System.out.println("Could not create output folders");
		}

	}

	/**
	 * Again this is just a pre processing step
	 * @param tfMap
	 */
	private  void initIDFMap(List<List<Map<String,List<Double>>>> tfMap){
		for (int i = 0; i < tfMap.size(); i++) {
			List<Map<String,List<Double>>> gestureDocument = tfMap.get(i);
			for (int j = 0; j < gestureDocument.size(); j++) {
				Map<String,List<Double>> tempMap = gestureDocument.get(j);
				Iterator<Entry<String, List<Double>>> it = tempMap.entrySet().iterator();
				  while (it.hasNext()) {
				        Map.Entry<String,Integer> pairs = (Map.Entry)it.next(); 
				        if(getTfIDFMapGlobal().containsKey(pairs.getKey()))
				        	getTfIDFMapGlobal().put(pairs.getKey(), getTfIDFMapGlobal().get(pairs.getKey())+1);
				        else
				        	getTfIDFMapGlobal().put(pairs.getKey(), 1);
				    }
			}
		}
	}
	
	
	/**
	 * Find - a word has occurred in how many documents in a gesture file
	 * Just update the count - this is a pre processing step
	 * @param mapPerGestureFile
	 */
	private void initIDF2Map(List<Map<String, List<Double>>> mapPerGestureFile) {
		Map<String,Double> idf2PerDocument = new HashMap<String, Double>(); // per univariate series
		
		for (int i = 0; i < mapPerGestureFile.size(); i++) {
			Map<String,List<Double>> tmpMap = mapPerGestureFile.get(i);
			Iterator<Entry<String, List<Double>>> iterator = tmpMap.entrySet().iterator();
			while(iterator.hasNext()) {
				Map.Entry<String, List<Double>> pairs = (Entry<String, List<Double>>) iterator.next();
				if(idf2PerDocument.containsKey(pairs.getKey()))
						idf2PerDocument.put(pairs.getKey(), idf2PerDocument.get(pairs.getKey())+1.0); 
				else
						idf2PerDocument.put(pairs.getKey(), 1.0);
			}
		}
		getTfMapArrayIDF2().add(idf2PerDocument);
	}

	/**
	 * Calculate IDF values
	 * @throws IOException
	 */
	private void calculateIDFValues() throws IOException {
		for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
			List<Map<String, List<Double>>> gestureDocument = getTfMapArrayIDF()
					.get(i);
			for (int j = 0; j < gestureDocument.size(); j++) {
				Map<String, List<Double>> tempMap = gestureDocument.get(j);
				Iterator<Entry<String, List<Double>>> it = tempMap.entrySet()
						.iterator();
				while (it.hasNext()) {
					Map.Entry<String, List<Double>> pairs = (Map.Entry) it
							.next();
					Double inverse = (new Double(getTfMapArrayIDF().size()
							* getTfMapArrayIDF().get(0).size()) / getTfIDFMapGlobal()
							.get(pairs.getKey()));
					Double idf = (Math.log(inverse));
					pairs.getValue().set(1,idf); // 1 for IDF
				}
			}
		}
	}
	

	/** 
	 * Calculate actual IDF2 values
	 * @throws IOException
	 */
	private void calculateIDF2Values() throws IOException {
		for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
			List<Map<String, List<Double>>> gestureDocument = getTfMapArrayIDF()
					.get(i);
			Map<String, Double> idf2PerDocument = getTfMapArrayIDF2().get(i);

			for (int j = 0; j < gestureDocument.size(); j++) {
				Map<String, List<Double>> tempMap = gestureDocument.get(j);
				Iterator<Entry<String, List<Double>>> it = tempMap.entrySet()
						.iterator();
				while (it.hasNext()) {
					Map.Entry<String, List<Double>> pairs = (Map.Entry) it
							.next();
					Double inverse = (new Double(getTfMapArrayIDF().get(0)
							.size()) / idf2PerDocument.get(pairs.getKey())); // already
																				// inversse
					List<Double> tf = pairs.getValue();
					tf.set(2,Math.log(inverse));
					tf.set(3,tf.get(0) * tf.get(1));  //tf-idf
					idfValues.add(tf.get(1));
					tf.set(4,tf.get(0) * tf.get(2));  //tf-idf2
					idf2Values.add(tf.get(2));
				}
			}
		}
	}


	/*
	 * Normal Getters and Setters
	 */
	public void setTfIDFMapGlobal(Map<String,Integer> tfIDFMapGlobal) {
		this.tfIDFMapGlobal = tfIDFMapGlobal;
	}
	public  Map<String,Integer> getTfIDFMapGlobal() {
		return tfIDFMapGlobal;
	}
	public  void setTfMapArrayIDF2(List<Map<String,Double>> tfMapArrayIDF2) {
		this.tfMapArrayIDF2 = tfMapArrayIDF2;
	}
	public  List<Map<String,Double>> getTfMapArrayIDF2() {
		return tfMapArrayIDF2;
	}
	public  void setTfMapArrayIDF(List<List<Map<String,List<Double>>>> tfMapArrayIDF) {
		this.tfMapArrayIDF = tfMapArrayIDF;
	}
	public  List<List<Map<String,List<Double>>>> getTfMapArrayIDF() {
		return tfMapArrayIDF;
	}
	public void setFileNames(File[] fileNames) {
		this.fileNames = fileNames;
	}
	public File[] getFileNames() {
		return fileNames;
	}
}
