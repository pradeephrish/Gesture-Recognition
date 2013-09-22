package asu.edu.math;

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

import asu.edu.loggers.MyLogger;
import asu.edu.setup.SetupSystem;

public class Task3FindSimilarData {
	private Map<String,Integer> tfGlobalMap;
	private String inputDirectory;
	private List<Map<String,List<Double>>> inputFileGesturewords; 
	private List<List<Map<String,List<Double>>>> dictionary;
	private List<List<Map<String,List<Double>>>> inputDictionary;
	private List<Map<String,Double>> dictionaryPerDocument; //per documents means per row
	private static Logger logger = new MyLogger().getupLogger();
	private ConstructGestureWords constructGestureWords;
	private Double maxTF;
	private DistanceFunction distanceFunction;
        private Entity entity;
        private HashMap<Integer,Double> similarityThread;
        
	public enum DistanceFunction{
		CosineFunction,
		Mahanolobis;
	}
	
	public enum Entity{
		TF,
		IDF,
		IDF2,
		TFIDF,
		TFIDF2;
	}
	
	
	
	public Task3FindSimilarData(Map<String,Integer> tfGlobalMap,List<List<Map<String,List<Double>>>> dictionary,List<Map<String,Double>> dictionaryPerDocument,Integer wordLength,Integer shiftLength,String inputDirectory,ConstructGestureWords constructGestureWords,DistanceFunction distanceFunction,Entity entity) throws IOException{
                this.distanceFunction=distanceFunction;
                this.entity=entity;
		this.tfGlobalMap = tfGlobalMap;
		this.dictionary=dictionary;
		this.inputDictionary = new ArrayList<List<Map<String,List<Double>>>>();
		this.dictionaryPerDocument=dictionaryPerDocument;
		this.inputDirectory = inputDirectory;
		this.constructGestureWords = constructGestureWords;
		inputFileGesturewords = new ArrayList<Map<String,List<Double>>>();
		init(wordLength,shiftLength,inputDirectory); //generated tf,idf,idf2 save to file
		
		//now we have two seperate dictionaries 'inputDictionary' for given input and 'dictionary' for sample data provided
		//we can directly process these two dictionaries instead of again reading from file
	
		//perform on TF
	//	HashMap<Integer, Double> tfSimilarScores = computeSimilarilty(inputDictionary,this.dictionary, DistanceFunction.CosineFunction,Entity.TF);
	//	HashMap<Integer, Double> tfidfSimilarScores = computeSimilarilty(inputDictionary,this.dictionary, DistanceFunction.CosineFunction,Entity.TFIDF);
	//	HashMap<Integer, Double> tfidf2SimilarScores = computeSimilarilty(inputDictionary,this.dictionary, DistanceFunction.CosineFunction,Entity.TFIDF2);
		
	//	logger.info("Top 10 TF Matching series");
	//	showLinkedHashMap(tfSimilarScores,10);
	//	logger.info("Top 10 TFIDF Matching series");
	//	showLinkedHashMap(tfidfSimilarScores, 10);
	//	logger.info("Top 10 TFIDF2 Matching series");
	//	showLinkedHashMap(tfidf2SimilarScores, 10);
		
		
	}

        public HashMap<Integer,Double> getSimilarityScore(){
            return computeSimilarilty(inputDictionary, dictionary, distanceFunction, entity);
        }
                
	private void showLinkedHashMap(HashMap<Integer, Double> tfidfSimilarScores,
			int i) {
		// TODO Auto-generated method stub
		for (Entry<Integer, Double> entry : tfidfSimilarScores.entrySet()) { 
		    Integer key = entry.getKey();
		    Double value = entry.getValue();
		    System.out.print(value+":"+ constructGestureWords.getFileNames()[key] +", ");
		    if(key == 10)
		    	break;
		}
		System.out.println("End");
	}

	private void init(Integer wordLength,Integer shiftLength, String inputDirectory) throws IOException { 
		// TODO Auto-generated method stub
		File file = new File(inputDirectory);
		File listFiles[] = {file}; 
		for (File file2 : listFiles) {
			BufferedReader in = new BufferedReader(new FileReader(file2));
			
			List<Map<String,List<Double>>> mapPerGestureFile = new ArrayList<Map<String,List<Double>>>(); 
			
			Map<String,List<Double>> wordMap = null;  //per row 
			while(in.ready())
			{
				wordMap = new HashMap<String, List<Double>>();  //per rows
				String series = in.readLine();
				
				String letters[]= series.split(",");
				Integer lastJIndex = -1; // for padding
				double totalWordCountPerDocument = 0.0; 
				for (int j = 0; j < letters.length-wordLength+1 ; j=j+shiftLength) {
					
					String word="";
					for (int k = j; k < wordLength+j; k++) {
						word+=letters[k];
					}
					
					//for TF
					if(wordMap.containsKey(word))
					{	
						List<Double> list = wordMap.get(word);
						list.set(0,list.get(0)+1.0F); //0 index for TF
						wordMap.put(word, list); 
					}
					else
					{
						List<Double> list = new ArrayList<Double>();
						list.add(0, 1.0);
						wordMap.put(word, list);					
					}			
					lastJIndex=j+shiftLength;
					++totalWordCountPerDocument;
				}
				Integer difference = letters.length - lastJIndex;
				if(difference > 0)
                {
				String paddedWord = "";
				Integer paddingSize = wordLength-difference;
				while(difference > 0)
				{
//					if(lastJIndex < letters.length)
						paddedWord+=letters[lastJIndex];
//					else
//						paddedWord+=paddedWord.charAt(paddedWord.length()-1);
					
					--difference;
					++lastJIndex;
				}
				
				while(paddingSize > 0)
				{
//					paddedWord+=paddedWord.charAt(paddedWord.length()-1);
					 paddedWord+=letters[lastJIndex-1];
	                  --paddingSize;
				}
				if(wordMap.containsKey(paddedWord))
				{	
					List<Double> list = wordMap.get(paddedWord);
					list.set(0,list.get(0)+1.0F); //0 index for TF
					wordMap.put(paddedWord, list); //updateMap for word
				}
				else
				{	
					List<Double> list = new ArrayList<Double>();
					list.add(0, 1.0);
					wordMap.put(paddedWord, list);
				}
				++totalWordCountPerDocument;
                }
				//Save Words to File - Saving TF
//				writeToFile(wordMap, tfOutputFolder+File.separator+fileNames[i].getName());
			
				wordMap = updateWordMapForTotalCountK(wordMap,totalWordCountPerDocument);// n/k , where n is frequency of word in doc/ k total freq
				mapPerGestureFile.add(wordMap);	
				inputFileGesturewords.add(wordMap); // copy for processing later -now used for finding max TF in give gesture file
				
			}
			//now creating new Input Dictionary for the input data 
			inputDictionary.add(mapPerGestureFile);
			//update TFGlobalMap
			
			
			maxTF = getMaxTF(inputFileGesturewords);
			
			 // updateGlobalMap(inputFileGesturewords);  
			
			//Update last row in Dictionary for IDF values which has only tf values
			processIDF(dictionary,inputDictionary,tfGlobalMap);
			
			//Comput IDF2 , First Create Local Dictionary Per Document then Compute IDF2
			processIDF2(inputDictionary,mapPerGestureFile);
		
			//save first element in the list
			writeToFile(inputDictionary.get(0),inputDirectory);
			
			
		}
		
	}
	
	
	private  Map<String, List<Double>> updateWordMapForTotalCountK(
			Map<String, List<Double>> wordMap, double totalWordCountPerDocument) {
		// TODO Auto-generated method stub
		Iterator iterator = wordMap.entrySet().iterator();
		while(iterator.hasNext()){
			 Map.Entry<String, List<Double>> entry = (Map.Entry<String, List<Double>>) iterator.next();
			 if(totalWordCountPerDocument> 0.0)
				 entry.getValue().set(0,entry.getValue().get(0)/totalWordCountPerDocument); //add all tf for total words
		}
	
		return wordMap;
	}
	
	
	private Double getMaxTF(List<Map<String, List<Double>>> inputDictionary2) {
		// TODO Auto-generated method stub
		List<Double> value = new ArrayList<Double>();
		for (int i = 0; i < inputDictionary2.size(); i++) {
			 Map<String, List<Double>> listMap = inputDictionary2.get(i);
			 Iterator iterator = listMap.entrySet().iterator();
			 while(iterator.hasNext()){
				 Entry<String, List<Double>> entry = (Entry<String,List<Double>>)iterator.next();
				 value.add(entry.getValue().get(0));
			 }
		}
		return Collections.max(value);
	}

	private LinkedHashMap<Integer,Double> computeSimilarilty(
			List<List<Map<String, List<Double>>>> inputDictionary,
			List<List<Map<String, List<Double>>>> dictionary, DistanceFunction distanceFunction, Entity entity) {
		// TODO Auto-generated method stub
		
		HashMap<Integer,Double> scores = new HashMap<Integer,Double>();
			
		
		if(distanceFunction.equals(DistanceFunction.CosineFunction)) 
		{
			
			if(inputDictionary.get(0).size()!=dictionary.get(0).size())
			{
				logger.log(Level.SEVERE, "** Something wrong with data or Calculations **");
			}
			for (int i = 0; i < dictionary.size(); i++) {
				List<Map<String, List<Double>>> entry = dictionary.get(i);
				List<Map<String,List<Double>>> inputEntry = inputDictionary.get(0);// always 0
				double sum = 0.0d;
				try{
				for (int k = 0; k < inputEntry.size(); k++) {
					Map<String, Double> inputMap = convertMap(inputEntry.get(k),entity); 
					Map<String, Double> dictMap = convertMap(entry.get(k),entity);
					sum+=CosineSimilarity.difference(inputMap, dictMap);
				}}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					System.out.println("There are no 20 rows/same number of rows in the input file");
					logger.log(Level.SEVERE, "There are no 20 rows/same number of rows in the input file");
				}
					scores.put(i,sum);
				}
			}
			
			return  sortHashMapByValuesD(scores);
		}

	private Map<String, Double> convertMap(Map<String, List<Double>> map,
			Entity entity) {
		// TODO Auto-generated method stub
			Map<String,Double> values = new HashMap<String, Double>(); 
			Iterator<Entry<String, List<Double>>> iterator = map.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String, List<Double>> entry = iterator.next();
				values.put(entry.getKey(), entry.getValue().get(entity.ordinal()));
			}
			
		return values;
	}

	private void writeToFile(List<Map<String, List<Double>>> gestureDocument,String inputFolder) throws IOException {
		// TODO Auto-generated method stub
		
		File task3OutputFileObj = new File(inputFolder + File.separator
				+ "task3Output");
		if (task3OutputFileObj.exists()) {
			SetupSystem.delete(task3OutputFileObj);
		}
		File task3OutputFolderAll = new File(inputFolder + File.separator
				+ "task3OutputAll");
		if (task3OutputFolderAll.exists()) {
			SetupSystem.delete(task3OutputFolderAll);
		}
		if (task3OutputFileObj.mkdir() && task3OutputFolderAll.mkdir()) {
		
		
		FileWriter fileWriter3 = new FileWriter(task3OutputFileObj +File.separator+"task3.csv",true);
		FileWriter fileWriter5 = new FileWriter(task3OutputFolderAll+File.separator+"task3.csv",true);
		BufferedWriter bufferWriter3 = new BufferedWriter(fileWriter3); 
		BufferedWriter bufferWriter5 = new BufferedWriter(fileWriter5);
		
		
		for (int j = 0; j < gestureDocument.size(); j++) {
			Map<String,List<Double>> tempMap = gestureDocument.get(j);
			Iterator it = tempMap.entrySet().iterator();				
			  while (it.hasNext()) {
			        Map.Entry<String,List<Double>> pairs = (Map.Entry)it.next();
			        List<Double> idf = pairs.getValue();
			        bufferWriter3.write(pairs.getKey()+":"+pairs.getValue().get(0)+","+pairs.getValue().get(1)+","+pairs.getValue().get(2)+";");
			        bufferWriter5.write(pairs.getKey()+":"+pairs.getValue().get(0)+","+pairs.getValue().get(1)+","+pairs.getValue().get(2)+","+pairs.getValue().get(3)+","+pairs.getValue().get(4)+";");
//					bufferWriter.write(pairs.getKey()+":"+pairs.getValue()*(inverse)+" "); //seperated by space
//					bufferWriter.write("\r\n");				    
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

	private void processIDF2(List<List<Map<String, List<Double>>>> inputDictionary, List<Map<String, List<Double>>> mapPerGestureFile) {  
		// TODO Auto-generated method stub
		
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
				Double inverse = new Double(( oneFile.size())/ idf2PerDocument.get(pair.getKey())) ; // tf value at 0
				
				//expresosnlfslkflslfd
				Double expression = 0.5 * (pair.getValue().get(0));  //from Sallen & Buckley tfidf formula
				if(maxTF>0.0)
				{
					expression=0.5 + expression/maxTF;
				}
				else
				{
					expression = 0.0;
				}
				
				
				pair.getValue().set(2,Math.log(inverse)); // IDF2 will go to second index
				
				//add TF-IDF2
				pair.getValue().add(pair.getValue().get(0)*pair.getValue().get(2));
				
			}
		}
		
		
	}

	private void processIDF(List<List<Map<String, List<Double>>>> dictionary,List<List<Map<String, List<Double>>>> inputDictionary,
			Map<String, Integer> tfGlobalMap2) {
		// TODO Auto-generated method stub
		List<Map<String,List<Double>>> oneFile = inputDictionary.get(0); //since single file input, only one element in list 
		for (int i = 0; i < oneFile.size(); i++) {
			Map<String,List<Double>> mapForRow = oneFile.get(i);
			Iterator<Entry<String, List<Double>>> iterator = mapForRow.entrySet().iterator();
			while(iterator.hasNext()){
				Map.Entry<String, List<Double>> pair = iterator.next();
				
				//after discussion with kedar
				Integer globalMapCount = tfGlobalMap2.get(pair.getKey());
				if(globalMapCount==null)
					globalMapCount = 1;
				
				Double inverse = new Double(( oneFile.size() * dictionary.size())/ globalMapCount ) ; // tf value at 0 , size is now N, because pushing input tfidfs to new dictionary
				Double expression = 0.5 * (pair.getValue().get(0));  //from Sallen & Buckley tfidf formula
				if(maxTF>0.0)
				{
					expression=0.5 + expression/maxTF;
				}
				else
				{
					expression = 0.0;
				}
				pair.getValue().add(Math.log(inverse)); // idf
				pair.getValue().add(0.0); // temporary value idf2,  will be added by processIDF2
				pair.getValue().add(Math.log(inverse)*expression); // tf-idf
			}
		}
	}

	
	
	
	
	private void updateGlobalMap(
			
			
		//Mr. Candan Said , this is not required	
			
			List<Map<String, List<Double>>> inputFileGesturewords2) {
		// TODO Auto-generated method stub
		for (int i = 0; i < inputFileGesturewords2.size(); i++) {
			Map<String, List<Double>> tmpMap = inputFileGesturewords2.get(i); 
			Iterator<Entry<String, List<Double>>> mapIterator = tmpMap.entrySet().iterator();
			while(mapIterator.hasNext()){
				Map.Entry<String, List<Double>> pairs = mapIterator.next();
				if(tfGlobalMap.containsKey(pairs.getKey())){
					tfGlobalMap.put(pairs.getKey(), tfGlobalMap.get(pairs.getKey())+1);
				}else
					tfGlobalMap.put(pairs.getKey(),1);
			}
		}
	}

	public static void main(String[] args) {
		try {
		ConstructGestureWords constructGestureWords  = new ConstructGestureWords();
		constructGestureWords.constructGestureWords(3, 2, "data//sampledata//letter//X");
	
//		Task3FindSimilarData task3FindSimilarData =  new Task3FindSimilarData(constructGestureWords.getTfIDFMapGlobal(),constructGestureWords.getTfMapArrayIDF(),constructGestureWords.getTfMapArrayIDF2(),3,2,"data//sampledata//input",constructGestureWords);
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//Sort in reverse order of scores
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
