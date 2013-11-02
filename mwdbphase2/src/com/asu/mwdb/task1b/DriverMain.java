package com.asu.mwdb.task1b;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

import org.apache.commons.io.FileUtils;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.gui.MainWindow;
import com.asu.mwdb.math.Task3FindSimilarData.Entity;
import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.SerializeData;
import com.asu.mwdb.utils.Utils;

public class DriverMain {

	private static MatlabProxy proxy;
	private static Logger logger = new MyLogger().getupLogger();
	private static Integer wordLength = 0;
	private static Integer shiftLength = 0;
	private static Double rValue = 0.0;
	private static Double mean = 0.0;
	private static Double stdDeviation = 0.25;
	private static String matlabScriptLoc = null;
	private static Map<String, DictionaryBuilderPhase2> dictMap = new HashMap<String, DictionaryBuilderPhase2>();
	private static DictionaryBuilderPhase2 dictionary = null;

	public static void main(String args[]) {

		try {
			// Initialize connection to Matlab. Place the Matlab script files in
			// current directory.
			MatlabProxyFactory factory = new MatlabProxyFactory();
			proxy = factory.getProxy();
			matlabScriptLoc = "." + File.separator + "MatlabScripts";
			String path = "cd(\'" + matlabScriptLoc + "')";
			proxy.eval(path);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.println("Enter the value for r (gaussian bands):");
			rValue = Double.parseDouble(br.readLine());

			GaussianBands gb = new GaussianBands();
			double rBandValueRange[][] = gb.getGaussianBands(rValue, mean,
					stdDeviation);
			// get all inputs needed for task 1
			System.out.println("Enter database directory:");
			String databaseDirectory = br.readLine();
			
			cleanData(databaseDirectory);

			System.out.println("Enter value of word length (w):");
			wordLength = Integer.parseInt(br.readLine());

			System.out.println("Enter value of shift length (s):");
			shiftLength = Integer.parseInt(br.readLine());
			List<String> componentList = indexFiles(rBandValueRange, databaseDirectory);

			/***/
			
			System.out.println("1. Task1a");
			System.out.println("2. Task1b");
			System.out.println("3. Task1c");
			System.out.println("4. Task2b");
			System.out.println("5. Task2c");

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String choice = in.readLine();

			do{
			switch (Integer.parseInt(choice)) {
				
			case 1:
				System.out.println("Enter component folder for Task 1a:");
				String inputDirectory1a = in.readLine();
				executeTask1a(inputDirectory1a);
				break;
			case 2:
				System.out.println("Enter component folder for Task 1b:");
				String inputDirectory = br.readLine();

				executeTask1b(rBandValueRange, inputDirectory);
				break;
			case 3:
				executeTask1c(rBandValueRange, databaseDirectory);
				break;
			case 4:
				executeTask2b(componentList);
				break;
			case 5: 
				executeTask2c(componentList);
				break;
			}
			System.out.println("1. Task1a");
			System.out.println("2. Task1b");
			System.out.println("3. Task1c");
			System.out.println("4. Task2b");
			System.out.println("5. Task2c");
			choice = in.readLine();
			}while(!choice.equalsIgnoreCase("6"));
			in.close();
			/*System.out.println("Enter component folder for Task 1a:");
			String inputDirectory1a = in.readLine();
			executeTask1a(inputDirectory1a);*/
			/***/

			/*System.out.println("Enter component folder for Task 1b:");
			String inputDirectory = br.readLine();

			executeTask1b(rBandValueRange, inputDirectory);*/

			executeTask1c(rBandValueRange, databaseDirectory);

			// Disconnect the proxy from MATLAB
			proxy.exit();
			proxy.disconnect();
		} catch (Exception e) {
			// print the stack trace so that it will be easy to debug
			e.printStackTrace();
			System.out.println("Error while processing - " + e);
		}

	}

	private static void executeTask2b(List<String> inputDirectoryKey) throws IOException, MatlabConnectionException, MatlabInvocationException{
		//pre-processing
		System.out.println("Executing task2b   .....  ");
		for (int i = 0; i < inputDirectoryKey.size(); i++) {
			System.out.println("\tExecuting for task : "+inputDirectoryKey.get(i));
			if(!dictMap.containsKey(inputDirectoryKey.get(i))){
				System.out.println("\tWrong inputDirectory given  ");
			}else
			{
				DictionaryBuilderPhase2 dictionaryHolder = dictMap.get(inputDirectoryKey.get(i));
				List<List<Map<String, List<Double>>>> currentDictionary = dictionaryHolder.getTfMapArrayIDF();
				String componentDir = inputDirectoryKey.get(i).substring(inputDirectoryKey.get(i).lastIndexOf(File.separator) + 1);
				//make dir
//				String path = IConstants.DATA+File.separator+IConstants.PCA_DIR_GG+File.separator+componentDir;
				File file = new File(IConstants.DATA+File.separator+IConstants.PCA_DIR_GG+File.separator+componentDir);
				String path = file.getAbsolutePath();
				if(file.exists()){
						FileIOHelper.delete(file);
				}
				
				if(!file.mkdir()){
					System.out.println("File Creatation failed for "+file.getAbsolutePath());
				}else{
					//directory creation done
					double[][] outputtfidf = Utils.computeSimilarilty(currentDictionary, Entity.TFIDF);
					double[][] outputtfidf2 = Utils.computeSimilarilty(currentDictionary, Entity.TFIDF2);
					//
					//now implemented it for PCA, SVD, LDA latent semantics
					//
					Utils.writeGestureGestureToFile(Entity.TFIDF, path, outputtfidf);
					Utils.writeGestureGestureToFile(Entity.TFIDF2, path, outputtfidf2);
					MainWindow mainWindow = new MainWindow();
					mainWindow.executePCAGG(path+File.separator+"ggTFIDF.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executePCAGG(path+File.separator+"ggTFIDF2.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
				}
			}
		}
		
		System.out.println("Succefully executed task2b");
	}
	
	private static void executeTask2c(List<String> inputDirectoryKey) throws IOException, MatlabConnectionException, MatlabInvocationException{
		//pre-processing
		System.out.println("Executing task2c   .....  ");
		for (int i = 0; i < inputDirectoryKey.size(); i++) {
			System.out.println("\tExecuting for task 2c: "+inputDirectoryKey.get(i));
			if(!dictMap.containsKey(inputDirectoryKey.get(i))){
				System.out.println("\tWrong inputDirectory given  ");
			}else
			{
				DictionaryBuilderPhase2 dictionaryHolder = dictMap.get(inputDirectoryKey.get(i));
				List<List<Map<String, List<Double>>>> currentDictionary = dictionaryHolder.getTfMapArrayIDF();
				String componentDir = inputDirectoryKey.get(i).substring(inputDirectoryKey.get(i).lastIndexOf(File.separator) + 1);
				//make dir
//				String path = IConstants.DATA+File.separator+IConstants.PCA_DIR_GG+File.separator+componentDir;
				File file = new File(IConstants.DATA+File.separator+IConstants.SVD_DIR_GG+File.separator+componentDir);
				String path = file.getAbsolutePath();
				if(file.exists()){
						FileIOHelper.delete(file);
				}
				
				if(!file.mkdir()){
					System.out.println("File Creatation failed for "+file.getAbsolutePath());
				}else{
					//directory creation done
					double[][] outputtfidf = Utils.computeSimilarilty(currentDictionary, Entity.TFIDF);
					double[][] outputtfidf2 = Utils.computeSimilarilty(currentDictionary, Entity.TFIDF2);
					//
					//now implemented it for PCA, SVD, LDA latent semantics
					//
					Utils.writeGestureGestureToFile(Entity.TFIDF, path, outputtfidf);
					Utils.writeGestureGestureToFile(Entity.TFIDF2, path, outputtfidf2);
					MainWindow mainWindow = new MainWindow();
					mainWindow.executeSVDGG(path+File.separator+"ggTFIDF.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executeSVDGG(path+File.separator+"ggTFIDF2.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
				}
			}
		}
		
		System.out.println("Succefully executed task2b");
	}
	
	
	
	
	private static void executeTask1a(String inputDirectory)
			throws IOException, MatlabConnectionException,
			MatlabInvocationException {
		// TODO Auto-generated method stub

		MainWindow main = new MainWindow();

		List<List<Map<String, List<Double>>>> getDictionary = dictMap.get(inputDirectory).getTfMapArrayIDF();

		Map<Integer, Set<String>> variable1 = main
				.createWordsPerSensor(getDictionary);
		
		/**************/ //for PCA and SVD
		List<Map<String, Double[]>> computedScores = main
				.createSensorWordScores(variable1, getDictionary,3);
		List<List<String>> order = main.savewordstoCSV(computedScores,"data");
		/***************/
		
		/**************/ //for LDA
		List<Map<String, Double[]>> computedScoresLDA = main
				.createSensorWordScores(variable1, getDictionary,5);
		List<List<String>> orderLDA = main.savewordstoCSV(computedScoresLDA,"data//lda//baseinput");
		main.transformDataForLDA("data//lda//baseinput");  //this write data to "data/lda/input"
		/***************/
		
		

		System.out.println("1. PCA");
		System.out.println("2. SVD");
		System.out.println("3. LDA");
		System.out.println("4. Exit");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String choice = br.readLine();

		while(!choice.equalsIgnoreCase("4")){
		switch (Integer.parseInt(choice)) {
		case 1:
			main.executePCA("data", order); // 1a
			Utils.tranformData("data/pca-semantic", "data", "data/pca-transform");
			break;
		case 2:
			main.executeSVD("data", order);
			Utils.tranformData("data/svd-semantic", "data", "data/svd-transform");
			break;
		case 3:
			main.exectuteLDA("data/lda/input", orderLDA, 3); // 3 latent semantics
			Utils.tranformData("data/lda/lda-semantic", "data", "data/lda/lda-transform");
			break;
		case 4: 
			break;
		}
		System.out.println("1. PCA");
		System.out.println("2. SVD");
		System.out.println("3. LDA");
		System.out.println("3. EXIT");
		choice = br.readLine();
		}
	}

	/**
	 * Task 1: Read a gesture file and normalize it. After that assign letters
	 * to normalized files and construct dictionary out of this folder
	 * 
	 * @throws IOException
	 * @throws MatlabInvocationException
	 * return List of Components
	 */
	private static List<String> indexFiles(double[][] rBandValueRange,
			String databaseDirectory) throws IOException,
			MatlabInvocationException {

		List<String> componentList = new ArrayList<String>();
		
		File dir = new File(databaseDirectory);
		File allFiles = new File(databaseDirectory + File.separator + "all");
		if (allFiles.exists()) {
			FileIOHelper.delete(allFiles);
		}
		// Get folders inside Database Folder
		File[] gestureFolders = dir.listFiles(new FileFilter() {
			public boolean accept(File path) {
				return path.isDirectory();
			}
		});

		if (!allFiles.mkdir()) {
			System.out
					.println("Error while creating intermediate directory, task not complete");
			return null;
		}

		// For files without folders
		if (gestureFolders.length == 0) {

			componentList.add(databaseDirectory);
			
			File folder = new File(databaseDirectory);
			File[] fileNames = folder.listFiles(new FileFilter() {
				@Override
				public boolean accept(File path) {
					String name = path.getName().toLowerCase();
					return name.endsWith(".csv") && path.isFile();
				}
			});
			// Normalize data between -1 and 1
			buildDictionary(new File(databaseDirectory), rBandValueRange);

		}
		else{
			
			copyAllFiles(new File(databaseDirectory), allFiles);
			
			
			for (File folder : gestureFolders) {
				componentList.add(folder.getAbsolutePath());
				copyAllFiles(folder, allFiles);
				// Normalize data between -1 and 1
				buildDictionary(folder, rBandValueRange);
			}
			buildDictionary(allFiles, rBandValueRange);
			componentList.add(allFiles.getAbsolutePath());
			
		}
		
		return componentList;
	}

	private static void buildDictionary(File folder, double[][] rBandValueRange)
			throws IOException, MatlabInvocationException {
		// Normalize data between -1 and 1
		NormalizeData.NormalizeTask1Data(proxy, folder.getAbsolutePath());
		AssignBandValues.assignToGaussianCurveTask1(proxy, matlabScriptLoc,
				folder.getAbsolutePath(), rBandValueRange);

		// Construct Gesture Vector files
		dictionary = new DictionaryBuilderPhase2();
		dictionary.createDictionary(wordLength, shiftLength,
				folder.getAbsolutePath());
		dictMap.put(folder.getAbsolutePath(), dictionary);
	}

	private static void copyAllFiles(File source, File dest) throws IOException {

		File[] fileNames = source.listFiles(new FileFilter() {
			@Override
			public boolean accept(File path) {
				String name = path.getName().toLowerCase();
				return name.endsWith(".csv") && path.isFile();
			}
		});

		for (File file : fileNames) {
			String newFileName = dest + File.separator + source.getName() + "_"
					+ file.getName();
			File newFileObj = new File(newFileName);
			FileUtils.copyFile(file, newFileObj);
		}
	}

	private static void cleanData(String inputDirectory)
			throws IOException {
		File fileObj = new File(inputDirectory);
		File[] files = fileObj.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.contains("gaussian")
					|| fileName.contains("normalized")
					|| fileName.contains("letters")
					|| fileName.contains("task1Output")
					|| fileName.contains("task1OutputAll")
					|| fileName.contains("task3Output")
					|| fileName.contains("task3OutputAll")
					|| fileName.contains("rangeBandFile")
					|| fileName.contains("all")
					) {
				FileIOHelper.delete(file);
			}
			else if(fileName.contains("W")) {
				cleanData(inputDirectory + File.separator + "W");
			} 
			else if(fileName.contains("X")) {
				cleanData(inputDirectory + File.separator + "X");
			} 
			else if(fileName.contains("Y")) {
				cleanData(inputDirectory + File.separator + "Y");
			} 
			else if(fileName.contains("Z")) {
				cleanData(inputDirectory + File.separator + "Z");
			} 
		}
	}

	/**
	 * Task 3: Given a query file - find 10 most similar gesture files based on
	 * TF/IDF/TD-IDF etc values. The user will specify parameter for comparison.
	 * 
	 * @throws IOException
	 * @throws MatlabInvocationException
	 */
	private static void executeTask1b(double[][] rBandValueRange,
			String inputDirectory) throws IOException,
			MatlabInvocationException {
		System.out.println("Enter query file name for Task 1b::");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inputFileLocation = br.readLine();
		NormalizeData.NormalizeDataForSingleFile(proxy, inputFileLocation);
		logger.info("Done normalization for Task 1b");
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
		MainWindow main = new MainWindow();
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

	/**
	 * Task 3: Given a query file - find 10 most similar gesture files based on
	 * TF/IDF/TD-IDF etc values. The user will specify parameter for comparison.
	 * 
	 * @throws IOException
	 * @throws MatlabInvocationException
	 */
	private static void executeTask1c(double[][] rBandValueRange,
			String databaseDirectory) throws IOException,
			MatlabInvocationException {
		System.out.println("Enter input file name for Task 1c::");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inputFileLocation = br.readLine();
		NormalizeData.NormalizeDataForSingleFile(proxy, inputFileLocation);
		logger.info("Done normalization for Task 1c");
		int position = inputFileLocation.lastIndexOf(File.separator);
		AssignBandValues.assignGaussianCurveTask3(proxy, inputFileLocation,
				rBandValueRange);
		DictionaryBuilderPhase2 dictionary = dictMap.get(databaseDirectory
				+ File.separator + "all");
		SearchDatabaseForSimilarity task3FindSimilarData = new SearchDatabaseForSimilarity(
				dictionary.getTfIDFMapGlobal(), dictionary.getTfMapArrayIDF(),
				dictionary.getTfMapArrayIDF2(), wordLength, shiftLength,
				inputFileLocation, dictionary);

	}

}
