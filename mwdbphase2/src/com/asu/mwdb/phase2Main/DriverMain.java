package com.asu.mwdb.phase2Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

import org.apache.commons.io.FileUtils;

import au.com.bytecode.opencsv.CSVWriter;


import com.asu.mwdb.phase2Main.SearchDatabaseForSimilarity.UserChoice;
import com.asu.mwdb.task3a.Task3a;
import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.NumberedFileComparator;
import com.asu.mwdb.utils.Phase2Utils;
import com.asu.mwdb.utils.Utils;

public class DriverMain {

	private static MatlabProxy proxy;
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

			// Read the Band size - r
			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			System.out.println("Enter the value for r (gaussian bands):");
			rValue = Double.parseDouble(br.readLine());

			// Create a Gaussian bands of size r
			GaussianBands gb = new GaussianBands();
			double rBandValueRange[][] = gb.getGaussianBands(rValue, mean,
					stdDeviation);
			
			// Read the database directory
			System.out.println("Enter database directory:");
			String databaseDirectory = br.readLine();

			// Clean Data 
			cleanData(databaseDirectory);

			// Read window length w
			System.out.println("Enter value of word length (w):");
			wordLength = Integer.parseInt(br.readLine());

			// Read shift length s
			System.out.println("Enter value of shift length (s):");
			shiftLength = Integer.parseInt(br.readLine());
			
			// Index files based on TF, IDF, IDF2, TF-IDF, TF-IDF2
			List<String> componentList = indexFiles(rBandValueRange, databaseDirectory);

			// Options to run the tasks in Phase 2
			
			System.out.println("Please enter choice for task you want to execute:");
			System.out.println("1. Task1a - Identify Top 3 Latent Semantics");
			System.out.println("2. Task1a - Run Task 1a On All Components");
			System.out.println("3. Task1b - Search Similar Gestures For Given Component");
			System.out.println("4. Task1c - Search Similar Gestures In Entire Database");
			System.out.println("5. Task2b - Create Gesture-Gesture Matrix and Run PCA");
			System.out.println("6. Task2c - Create Gesture-Gesture Matrix and Run SVD");
			System.out.println("7. Task3a - Partition Gestures into 3 Groups");
			System.out.println("8. Exit");

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String choice = in.readLine();

			do{
				switch (Integer.parseInt(choice)) {
				// Task1a
				case 1:
					System.out.println("Enter component folder for Task 1a:");
					String inputDirectory1a = in.readLine();
					executeTask1a(inputDirectory1a,false);
					break;
				// Task1a- for all components					
				case 2:
					for (int i = 0; i < componentList.size(); i++) {
						executeTask1a(componentList.get(i),true);
					}
					break;
				// Task1b
				case 3:
					System.out.println("Enter component folder for Task 1b:");
					String inputDirectory = br.readLine();
					executeTask1b(rBandValueRange, inputDirectory);
					break;
					
				// Task1c
				case 4:
					String inputDirectory1c = databaseDirectory + File.separator + IConstants.ALL;
					executeTask1b(rBandValueRange, inputDirectory1c);
					break;
				// Task2b
				case 5:
					executeTask2b(componentList);
					break;
				// Task2c
				case 6: 
					executeTask2c(componentList);
					break;
				// Task3a
				case 7: 
					Task3a.executeTask3a(proxy, IConstants.DATA+File.separator+IConstants.PCA_DIR_GG, componentList);
					Task3a.executeTask3a(proxy, IConstants.DATA+File.separator+IConstants.SVD_DIR_GG, componentList);
					break;
				}
				System.out.println("Please enter choice for task you want to execute:");
				System.out.println("1. Task1a - Identify Top 3 Latent Semantics");
				System.out.println("2. Task1a - Run Task 1a On All Components");
				System.out.println("3. Task1b - Search Similar Gestures For Given Component");
				System.out.println("4. Task1c - Search Similar Gestures In Entire Database");
				System.out.println("5. Task2b - Create Gesture-Gesture Matrix and Run PCA");
				System.out.println("6. Task2c - Create Gesture-Gesture Matrix and Run SVD");
				System.out.println("7. Task3a - Partition Gestures into 3 Groups");
				System.out.println("8. Exit");
				choice = in.readLine();
			} while(!choice.equalsIgnoreCase("8"));
			in.close();
			// Disconnect the proxy from MATLAB
			proxy.exit();
			proxy.disconnect();
		} catch (Exception e) {
			// print the stack trace so that it will be easy to debug
			e.printStackTrace();
			System.out.println("Error while processing - " + e);
		}

	}

	/**
	 * Task 2b execution
	 * @param inputDirectoryKey
	 * @throws IOException
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	private static void executeTask2b(List<String> inputDirectoryKey) throws IOException, MatlabConnectionException, MatlabInvocationException{
		//pre-processing
		System.out.println("Executing task2b   .....  ");
		for (int i = 0; i < inputDirectoryKey.size(); i++) {
			System.out.println("\tExecuting for task : "+inputDirectoryKey.get(i));
			if(!dictMap.containsKey(inputDirectoryKey.get(i))){
				System.out.println("\tWrong inputDirectory given  ");
			} else 	{
				DictionaryBuilderPhase2 dictionaryHolder = dictMap.get(inputDirectoryKey.get(i));
				List<List<Map<String, List<Double>>>> currentDictionary = dictionaryHolder.getTfMapArrayIDF();
				String componentDir = inputDirectoryKey.get(i).substring(inputDirectoryKey.get(i).lastIndexOf(File.separator) + 1);
				File file = new File(IConstants.DATA+File.separator+IConstants.PCA_DIR_GG+File.separator+componentDir);
				String path = file.getAbsolutePath();
				if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.PCA_DIR_GG+File.separator+componentDir))
					return;

				{
					//directory creation done
					double[][] outputtfidf = Utils.computeSimilarilty(currentDictionary, UserChoice.TFIDF);
					double[][] outputtfidf2 = Utils.computeSimilarilty(currentDictionary, UserChoice.TFIDF2);
					//now implemented it for PCA, SVD, LDA latent semantics
					
					//pca latent semantic
					List<List<String[]>> out = Utils.convertDataForComparison(IConstants.DATA+File.separator+IConstants.PCA_TRANSFORM+File.separator+componentDir, dictionaryHolder.getFileNames());
					double[][] pcaGGLS = Utils.getGestureGestureMatrixLSA(out);

					//svd latent semantic
					List<List<String[]>> out1 = Utils.convertDataForComparison(IConstants.DATA+File.separator+IConstants.SVD_TRANSFORM+File.separator+componentDir, dictionaryHolder.getFileNames());
					double[][] svdGGLS = Utils.getGestureGestureMatrixLSA(out1);

					//lda latent semantic
					List<List<String[]>> out2 = Utils.convertDataForComparison(IConstants.DATA+File.separator+ IConstants.LDA_DIR + File.separator + IConstants.LDA_TRANSFORM+File.separator+componentDir, dictionaryHolder.getFileNames());
					double[][] ldaGGLS = Utils.getGestureGestureMatrixLSA(out2);
					Utils.writeGestureGestureToFile(UserChoice.TFIDF, path, outputtfidf);
					Utils.writeGestureGestureToFile(UserChoice.TFIDF2, path, outputtfidf2);
					Utils.writeGestureGestureToFile(UserChoice.PCA_LSA, path, pcaGGLS);
					Utils.writeGestureGestureToFile(UserChoice.SVD_LSA, path, svdGGLS);
					Utils.writeGestureGestureToFile(UserChoice.LDA_LSA, path, ldaGGLS);

					Phase2Utils mainWindow = new Phase2Utils();
					mainWindow.executePCAGG(path+File.separator+"ggTFIDF.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executePCAGG(path+File.separator+"ggTFIDF2.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executePCAGG(path+File.separator+"ggPCA.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executePCAGG(path+File.separator+"ggSVD.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executePCAGG(path+File.separator+"ggLDA.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
				}
			}
		}

		System.out.println("Succefully executed task2b");
	}

	/**
	 * Task 2c execution
	 * @param inputDirectoryKey
	 * @throws IOException
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	private static void executeTask2c(List<String> inputDirectoryKey) throws IOException, MatlabConnectionException, MatlabInvocationException{
		//pre-processing
		System.out.println("Executing task2c   .....  ");
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

				File file = new File(IConstants.DATA+File.separator+IConstants.SVD_DIR_GG+File.separator+componentDir);
				String path = file.getAbsolutePath();

				if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.SVD_DIR_GG+File.separator+componentDir))
					return;

				{
					//directory creation done
					double[][] outputtfidf = Utils.computeSimilarilty(currentDictionary, UserChoice.TFIDF);
					double[][] outputtfidf2 = Utils.computeSimilarilty(currentDictionary, UserChoice.TFIDF2);
					//
					//now implemented it for PCA, SVD, LDA latent semantics

					//pca latent semantic
					List<List<String[]>> out = Utils.convertDataForComparison(IConstants.DATA+File.separator+IConstants.SVD_TRANSFORM+File.separator+componentDir, dictionaryHolder.getFileNames());
					double[][] pcaGGLS = Utils.getGestureGestureMatrixLSA(out);

					//svd latent semantic
					List<List<String[]>> out1 = Utils.convertDataForComparison(IConstants.DATA+File.separator+IConstants.SVD_TRANSFORM+File.separator+componentDir, dictionaryHolder.getFileNames());
					double[][] svdGGLS = Utils.getGestureGestureMatrixLSA(out1);

					//lda latent semantic
					List<List<String[]>> out2 = Utils.convertDataForComparison(IConstants.DATA+File.separator+ IConstants.LDA_DIR + File.separator + IConstants.LDA_TRANSFORM+File.separator+componentDir, dictionaryHolder.getFileNames());
					double[][] ldaGGLS = Utils.getGestureGestureMatrixLSA(out2);
					//

					Utils.writeGestureGestureToFile(UserChoice.TFIDF, path, outputtfidf);
					Utils.writeGestureGestureToFile(UserChoice.TFIDF2, path, outputtfidf2);
					Utils.writeGestureGestureToFile(UserChoice.PCA_LSA, path, pcaGGLS);
					Utils.writeGestureGestureToFile(UserChoice.SVD_LSA, path, svdGGLS);
					Utils.writeGestureGestureToFile(UserChoice.LDA_LSA, path, ldaGGLS);

					Phase2Utils mainWindow = new Phase2Utils();
					mainWindow.executeSVDGG(path+File.separator+"ggTFIDF.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executeSVDGG(path+File.separator+"ggTFIDF2.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executeSVDGG(path+File.separator+"ggPCA.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executeSVDGG(path+File.separator+"ggSVD.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
					mainWindow.executeSVDGG(path+File.separator+"ggLDA.csv", Utils.convertList(dictionaryHolder.getFileNames()), proxy);
				}
			}
		}

		System.out.println("Succefully executed task2c");
	}



	/**
	 * Task 1a execution
	 * @param inputDirectory
	 * @param isAll
	 * @throws IOException
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 */
	private static void executeTask1a(String inputDirectory,boolean isAll)
			throws IOException, MatlabConnectionException,
			MatlabInvocationException {
		Phase2Utils main = new Phase2Utils();
		List<List<Map<String, List<Double>>>> getDictionary = dictMap.get(inputDirectory).getTfMapArrayIDF();

		String componentDir = inputDirectory.substring(inputDirectory.lastIndexOf(File.separator) + 1);

		if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir))
			return;

		Map<Integer, Set<String>> variable1 = main
				.createWordsPerSensor(getDictionary);

		/**************/ //for PCA and SVD
		List<Map<String, Double[]>> computedScores = main
				.createSensorWordScores(variable1, getDictionary,3);
		List<List<String>> order = main.savewordstoCSV(computedScores,IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir);
		/***************/

		/**************/ //for LDA
		List<Map<String, Double[]>> computedScoresLDA = main.createSensorWordScores(variable1, getDictionary,5);

		if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.BASE_DATA+File.separator+componentDir))
			return;
		List<List<String>> orderLDA = main.savewordstoCSV(computedScoresLDA,IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.BASE_DATA+File.separator+componentDir);
		main.transformDataForLDA(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.BASE_DATA+File.separator+componentDir);
		/***************/


		if(!isAll)
		{
			System.out.println("1. PCA");
			System.out.println("2. SVD");
			System.out.println("3. LDA");
			System.out.println("4. Go Back");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String choice = br.readLine();

			while(!choice.equalsIgnoreCase("4")){
				switch (Integer.parseInt(choice)) {
				case 1:
					main.executePCA(IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, order,proxy); // 1a
					//Directory creation
					if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.PCA_TRANSFORM+ File.separator + componentDir))
						return;
					// end
					Utils.tranformData(IConstants.DATA+File.separator+IConstants.PCA_SEMANTICS+ File.separator + componentDir, IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.PCA_TRANSFORM+ File.separator + componentDir);
					break;
				case 2:
					main.executeSVD(IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, order,proxy);

					//Directory creation
					if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.SVD_TRANSFORM+ File.separator + componentDir))
						return;

					Utils.tranformData(IConstants.DATA+File.separator+IConstants.SVD_SEMANTICS+ File.separator + componentDir, IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.SVD_TRANSFORM+ File.separator + componentDir);
					break;
				case 3:
					main.exectuteLDA(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.INPUT_DIR+File.separator+componentDir, orderLDA, 3,proxy); // 3 latent semantics
					if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.LDA_TRANSFORM+File.separator+componentDir))
						return;
					Utils.tranformData(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.LDA_SEMANTICS+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.BASE_DATA+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.LDA_TRANSFORM+File.separator+componentDir);
					break;
				case 4: 
					break;
				}
				System.out.println("1. PCA");
				System.out.println("2. SVD");
				System.out.println("3. LDA");
				System.out.println("4. GO Back");
				choice = br.readLine();
			}
		}else
		{
			main.executePCA(IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, order,proxy); // 1a
			if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.PCA_TRANSFORM+ File.separator + componentDir))
				return;
			Utils.tranformData(IConstants.DATA+File.separator+IConstants.PCA_SEMANTICS+ File.separator + componentDir, IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.PCA_TRANSFORM+ File.separator + componentDir);
			main.executeSVD(IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, order,proxy);
			if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.SVD_TRANSFORM+ File.separator + componentDir))
				return;
			Utils.tranformData(IConstants.DATA+File.separator+IConstants.SVD_SEMANTICS+ File.separator + componentDir, IConstants.DATA+File.separator+IConstants.BASE_DATA+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.SVD_TRANSFORM+ File.separator + componentDir);
			main.exectuteLDA(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.INPUT_DIR+File.separator+componentDir, orderLDA, 3,proxy); // 3 latent semantics
			if(!Utils.isDirectoryCreated(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.LDA_TRANSFORM+File.separator+componentDir))
				return;
			Utils.tranformData(IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.LDA_SEMANTICS+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.BASE_DATA+File.separator+componentDir, IConstants.DATA+File.separator+IConstants.LDA_DIR+File.separator+IConstants.LDA_TRANSFORM+File.separator+componentDir);
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
			// Normalize data between -1 and 1
			buildDictionary(new File(databaseDirectory), rBandValueRange);
		} else {

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

	/**
	 * Build Dictoionary
	 * @param folder
	 * @param rBandValueRange
	 * @throws IOException
	 * @throws MatlabInvocationException
	 */
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

	/**
	 * Copy all files from all the components and create a ALL folder for task 1c 
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
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

	/**
	 * Clean data from data base folder
	 * @param inputDirectory
	 * @throws IOException
	 */
	private static void cleanData(String inputDirectory)
			throws IOException {
		File fileObj = new File(inputDirectory);
		File[] files = fileObj.listFiles();
		for (File file : files) {
			String fileName = file.getName();
			if (fileName.contains(IConstants.GAUSSIAN_FILE)
					|| fileName.contains(IConstants.NORMALIZED_FILE)
					|| fileName.contains(IConstants.LETTERS_FILE)
					|| fileName.contains(IConstants.TASK1_OUTPUT)
					|| fileName.contains(IConstants.TASK1_OUTPUT_ALL)
					|| fileName.contains(IConstants.TASK3_OUTPUT)
					|| fileName.contains(IConstants.TASK3_OUTPUT_ALL)
					|| fileName.contains(IConstants.RANGED_BAND)
					|| fileName.contains(IConstants.ALL)
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
	 * Task 1b execution
	 * @param rBandValueRange
	 * @param inputDirectory
	 * @throws IOException
	 * @throws MatlabInvocationException
	 */
	private static void executeTask1b(double[][] rBandValueRange,
			String inputDirectory) throws IOException,
			MatlabInvocationException {
		System.out.println("Enter query file name:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inputFileLocation = br.readLine();
		NormalizeData.NormalizeDataForSingleFile(proxy, inputFileLocation);
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

		String componentDir = inputDirectory.substring(inputDirectory.lastIndexOf(File.separator) + 1);

		String pcaSemanticDir = IConstants.DATA + File.separator + IConstants.PCA_SEMANTICS + File.separator + componentDir;
		String svdSematicDir  = IConstants.DATA + File.separator + IConstants.SVD_SEMANTICS + File.separator + componentDir;
		String ldaSemanticDir = IConstants.DATA + File.separator + IConstants.LDA_DIR + File.separator + IConstants.LDA_SEMANTICS + File.separator + componentDir;

		String pcaSemanticOpDir = IConstants.DATA + File.separator + IConstants.PCA_MAPPED + File.separator + componentDir;
		String svdSemanticOpDir = IConstants.DATA + File.separator + IConstants.SVD_MAPPED + File.separator + componentDir;
		String ldaSemanticOpDir = IConstants.DATA + File.separator + IConstants.LDA_MAPPED + File.separator + componentDir;

		if(!Utils.isDirectoryCreated(pcaSemanticOpDir)) return;
		if(!Utils.isDirectoryCreated(svdSemanticOpDir)) return;
		if(!Utils.isDirectoryCreated(ldaSemanticOpDir)) return;

		List<String[]> pcaQueryTransformList = null;
		List<String[]> svdQueryTransformList = null;
		List<String[]> ldaQueryTransformList = null;
		try {
			pcaQueryTransformList = mapQueryToLSASpace(pcaSemanticDir, pcaSemanticOpDir, queryWordScores);
			svdQueryTransformList = mapQueryToLSASpace(svdSematicDir, svdSemanticOpDir, queryWordScores);
			ldaQueryTransformList = mapQueryToLSASpace(ldaSemanticDir, ldaSemanticOpDir, queryWordScores);
		} catch(Exception e) {
			System.out.println("Error while processing top 3 latent semantic file");
		}

		// per Sensor
		String pcaTransformDirectory = "." + File.separator + IConstants.DATA + File.separator + IConstants.PCA_TRANSFORM + File.separator + componentDir;
		String svdTransformDirectory = "." + File.separator + IConstants.DATA + File.separator + IConstants.SVD_TRANSFORM + File.separator + componentDir;
		String ldaTransformDirectory = "." + File.separator + IConstants.DATA + File.separator +IConstants.LDA_DIR +File.separator+ IConstants.LDA_TRANSFORM + File.separator + componentDir;

		File pcaTransformedDirFile = new File(pcaTransformDirectory);
		File svdTransformedDirFile = new File(svdTransformDirectory);
		File ldaTransformedDirFile = new File(ldaTransformDirectory);
		
		

		List<List<String[]>> pcaTrasnsformData = Utils.convertDataForComparison(pcaTransformDirectory, pcaTransformedDirFile.listFiles());
		List<List<String[]>> svdTrasnsformData = Utils.convertDataForComparison(svdTransformDirectory, svdTransformedDirFile.listFiles());
		List<List<String[]>> ldaTrasnsformData = Utils.convertDataForComparison(ldaTransformDirectory, ldaTransformedDirFile.listFiles());

		// Sensor to Gesture
		String pcaTransformToFileDirectory = "." + File.separator + IConstants.DATA + File.separator + IConstants.PCA_TRANSFORM_GESTURE + File.separator + componentDir;
		String svdTransformToFileDirectory = "." + File.separator + IConstants.DATA + File.separator + IConstants.SVD_TRANSFORM_GESTURE + File.separator + componentDir;
		String ldaTransformToFileDirectory = "." + File.separator + IConstants.DATA + File.separator + IConstants.LDA_TRANSFORM_GESTURE + File.separator + componentDir;

		Utils.writeListOfListToDir(pcaTrasnsformData, pcaTransformToFileDirectory, Utils.getCSVFiles(new File(inputDirectory)));
		Utils.writeListOfListToDir(svdTrasnsformData, svdTransformToFileDirectory, Utils.getCSVFiles(new File(inputDirectory)));
		Utils.writeListOfListToDir(ldaTrasnsformData, ldaTransformToFileDirectory, Utils.getCSVFiles(new File(inputDirectory)));

		HashMap<Integer, Double> pcaScores = searchForSimilarLSA(pcaQueryTransformList, pcaTrasnsformData);
		HashMap<Integer, Double> svdScores = searchForSimilarLSA(svdQueryTransformList, svdTrasnsformData);
		HashMap<Integer, Double> ldaScores = searchForSimilarLSA(ldaQueryTransformList, ldaTrasnsformData);

		System.out.println("Top 5 documents in PCA semantics are as follows:");
		displayMapResults(pcaScores, Utils.getCSVFiles(new File(inputDirectory)));
		System.out.println("Top 5 documents in SVD semantics are as follows:");
		displayMapResults(svdScores, Utils.getCSVFiles(new File(inputDirectory)));
		System.out.println("Top 5 documents in LDA semantics are as follows:");
		displayMapResults(ldaScores, Utils.getCSVFiles(new File(inputDirectory)));
	}

	/**
	 * Map the query file to LSA space
	 * @param semanticDir
	 * @param semanticOutputDirectory
	 * @param queryWordScores
	 * @return
	 * @throws IOException
	 */
	private static List<String[]> mapQueryToLSASpace(String semanticDir,
			String semanticOutputDirectory, List<Map<String, Double[]>> queryWordScores) throws IOException {
		File semanticFileDirObj = new File(semanticDir);
		File[] semanticFiles = semanticFileDirObj.listFiles();
		Arrays.sort(semanticFiles, new NumberedFileComparator());
		Iterator<Map<String, Double[]>> queryIt = queryWordScores.iterator();
		List<String[]> queryTransformList = new ArrayList<String[]>();
		for(File semanticFile : semanticFiles) {
			List<Map<String, Double>> semanticData = Utils.getSemanticFileData(semanticFile);
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
		CSVWriter csvWriter = new CSVWriter(new FileWriter(semanticOutputDirectory + File.separator +  IConstants.QUERY_MAPPED + ".csv"), ',',
				CSVWriter.NO_QUOTE_CHARACTER,
				CSVWriter.DEFAULT_LINE_END);

		for(int i =0; i< queryTransformList.size(); i++) {
			csvWriter.writeNext(queryTransformList.get(i));
		}
		csvWriter.close();
		return queryTransformList;
	}

	/**
	 * Similarity of files in LSA space
	 * @param queryData
	 * @param pcaTrasnsformData
	 * @return
	 */
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

	/**
	 * Display top 5 similar files based on score
	 * @param tfidfSimilarScores
	 * @param files
	 */
	private static void displayMapResults(HashMap<Integer, Double> tfidfSimilarScores, File[] files) {
		int counter = 0;
		for (Entry<Integer, Double> entry : tfidfSimilarScores.entrySet()) { 
			Integer key = entry.getKey();		    
			File file = files[key];		    
			System.out.println((counter + 1) + " - " + file.getAbsolutePath() + "        " + entry.getValue());
			counter = counter + 1;
			if(counter == 5) {
				break;
			}
		}
		System.out.println("******************************************************************");
	}
}
