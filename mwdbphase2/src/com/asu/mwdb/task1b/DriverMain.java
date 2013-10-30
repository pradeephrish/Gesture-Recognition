package com.asu.mwdb.task1b;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

import org.apache.commons.io.FileUtils;

import com.asu.mwdb.gui.MainWindow;

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

			System.out.println("Enter value of word length (w):");
			wordLength = Integer.parseInt(br.readLine());

			System.out.println("Enter value of shift length (s):");
			shiftLength = Integer.parseInt(br.readLine());
			indexFiles(rBandValueRange, databaseDirectory);

			/***/

			System.out.println("Enter component folder for Task 1a:");
			String inputDirectory1a = br.readLine();
			executeTask1a(inputDirectory1a);
			/***/

			System.out.println("Enter component folder for Task 1b:");
			String inputDirectory = br.readLine();

			executeTask1b(rBandValueRange, inputDirectory);

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

	private static void executeTask1a(String inputDirectory)
			throws IOException, MatlabConnectionException,
			MatlabInvocationException {
		// TODO Auto-generated method stub

		MainWindow main = new MainWindow();

		List<List<Map<String, List<Double>>>> getDictionary = dictMap.get(
				inputDirectory).getTfMapArrayIDF();

		Map<Integer, Set<String>> variable1 = main
				.createWordsPerSensor(getDictionary);
		List<Map<String, Double[]>> computedScores = main
				.createSensorWordScores(variable1, getDictionary);

		List<List<String>> order = main.savewordstoCSV(computedScores);
		for (int i = 0; i < order.size(); i++) {
			System.out.println(order.get(i).toString());
		}

		main.transformDataForLDA("data");

		System.out.println("1. PCA");
		System.out.println("2. SVD");
		System.out.println("3. LDA");

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String choice = br.readLine();

		switch (Integer.parseInt(choice)) {
		case 1:
			main.executePCA("data", order); // 1a
			break;
		case 2:
			main.executeSVD("data", order);
			break;
		case 3:
			main.exectuteLDA("data/lda/input", order, 3); // 3 latent semantics
			break;
		}

		// main.executePCA("data",order); // 1a
		// main.executeSVD("data", order);
		// main.exectuteLDA("data/lda/input", order,3);
	}

	/**
	 * Task 1: Read a gesture file and normalize it. After that assign letters
	 * to normalized files and construct dictionary out of this folder
	 * 
	 * @throws IOException
	 * @throws MatlabInvocationException
	 */
	private static void indexFiles(double[][] rBandValueRange,
			String databaseDirectory) throws IOException,
			MatlabInvocationException {

		// delete previously created files
		// cleanTask1Data(inputDirectory);

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
			return;
		}

		// For files without folders
		if (gestureFolders.length == 0) {

			copyAllFiles(new File(databaseDirectory), allFiles);
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
		// For folders
		if (!(gestureFolders.length == 0)) {
			for (File folder : gestureFolders) {
				copyAllFiles(folder, allFiles);
				// Normalize data between -1 and 1
				buildDictionary(folder, rBandValueRange);
			}
		}

		buildDictionary(allFiles, rBandValueRange);

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

	private static void cleanTask1Data(String inputDirectory)
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
					|| fileName.contains("rangeBandFile")) {
				FileIOHelper.delete(file);
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
		GaussianBands gb = new GaussianBands();
		int position = inputFileLocation.lastIndexOf(File.separator);
		String currentDirectory = null;
		if (position > 0) {
			currentDirectory = inputFileLocation.substring(0, position);
		}
		AssignBandValues.assignGaussianCurveTask3(proxy, inputFileLocation,
				rBandValueRange);
		DictionaryBuilderPhase2 dictionary = dictMap.get(inputDirectory);
		SearchDatabaseForSimilarity task3FindSimilarData = new SearchDatabaseForSimilarity(
				dictionary.getTfIDFMapGlobal(), dictionary.getTfMapArrayIDF(),
				dictionary.getTfMapArrayIDF2(), wordLength, shiftLength,
				inputFileLocation, dictionary);

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
		GaussianBands gb = new GaussianBands();
		int position = inputFileLocation.lastIndexOf(File.separator);
		String currentDirectory = null;
		if (position > 0) {
			currentDirectory = inputFileLocation.substring(0, position);
		}
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
