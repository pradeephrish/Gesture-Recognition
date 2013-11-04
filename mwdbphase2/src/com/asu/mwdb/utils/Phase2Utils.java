/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asu.mwdb.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


/**
 * 
 * @author paddy
 */
public class Phase2Utils extends javax.swing.JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Phase 2 Methods
	 * 
	 * @throws MatlabConnectionException
	 * @throws MatlabInvocationException
	 * @throws IOException
	 ***/

	public void executePCA(String inputLocation, List<List<String>> order,MatlabProxy proxy)
			throws MatlabConnectionException, MatlabInvocationException,
			IOException {

		String component = inputLocation.substring(inputLocation.lastIndexOf(File.separator) + 1);
		
		File[] files = new File(inputLocation).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return name.endsWith(".csv") && pathname.isFile();
			}
		});
		
		if(!Utils.isDirectoryCreated(files[0].getParentFile().getParentFile().getParentFile().getAbsolutePath()
				+ File.separator + IConstants.PCA_DIR+File.separator+ component)){
			System.out.println("Failed Creation of Directory "+ files[0].getParentFile().getParentFile().getParentFile().getAbsolutePath()
				+ File.separator + IConstants.PCA_DIR+File.separator+ component);
			return;
		
		}
		for (int i = 0; i < files.length; i++) {
			
			System.out.println(files[i].getParentFile().getParentFile().getParentFile().getAbsolutePath()
					+ File.separator + IConstants.PCA_DIR+File.separator+ component + File.separator
					+ files[i].getName());
			proxy.eval("PCAFinder('" + files[i].getAbsolutePath() + "','"
					+ files[i].getParentFile().getParentFile().getParentFile().getAbsolutePath()
					+ File.separator + IConstants.PCA_DIR+File.separator+ component + File.separator
					+ files[i].getName() + "')");
		}
		
		if(!Utils.isDirectoryCreated(files[0].getParentFile().getParentFile().getParentFile()
				.getAbsolutePath()
				+ File.separator
				+ IConstants.PCA_SEMANTICS+File.separator+component)){
			System.out.println("File Creation failed "+files[0].getParentFile().getParentFile().getParentFile()
				.getAbsolutePath()
				+ File.separator
				+ IConstants.PCA_SEMANTICS+File.separator+component);
			return;
		}

		for (int i = 0; i < order.size(); i++) {
			// read sensor 0 data and print
			String pcaFileName = files[i].getParentFile().getParentFile().getParentFile().getAbsolutePath()
					+ File.separator + IConstants.PCA_DIR +File.separator+component+ File.separator + i + ".csv";
				
			String pcaSemanticFileName = files[i].getParentFile().getParentFile().getParentFile()
					.getAbsolutePath()
					+ File.separator
					+ IConstants.PCA_SEMANTICS+File.separator+component
					+ File.separator + i + ".csv";

			CSVReader csvReader = new CSVReader(new InputStreamReader(
					new FileInputStream(pcaFileName)));
			CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
					new FileOutputStream(pcaSemanticFileName)));

			List<String> orderList = order.get(i);
			String[] list = new String[orderList.size()];

			for (int j = 0; j < orderList.size(); j++) {
				list[j] = orderList.get(j);
			}
			csvWriter.writeNext(list);

			for (int j = 0; j < 3; j++) {
				csvWriter.writeNext(csvReader.readNext());
			}
			csvWriter.close();
			csvReader.close();
		}
	}

	public void executePCAGG(String fileLocation, List<String> docOrder, MatlabProxy proxy)
			throws MatlabConnectionException, MatlabInvocationException,
			IOException {
		File file = new File(fileLocation);
		
		String temp = fileLocation.substring(0,fileLocation.lastIndexOf(File.separator));
		String componentName = temp.substring(temp.lastIndexOf(File.separator) + 1);
		String outputFile =IConstants.DATA
				+ File.separator + IConstants.PCA_DIR_GG + File.separator + componentName + File.separator
				+ "pca_" + file.getName() ;
		proxy.eval("PCAFinder('" + fileLocation + "','" + new File(outputFile).getAbsolutePath()  + "')");

		String pcaFileName = new File(outputFile).getAbsolutePath();
		String pcaSemanticFileName = IConstants.DATA + File.separator
				+ IConstants.PCA_DIR_GG + File.separator +componentName+File.separator+ "semanticgg_"
				+ file.getName();

		CSVReader csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(pcaFileName)));
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
				new FileOutputStream(pcaSemanticFileName)));

		String[] list = new String[docOrder.size()];

		for (int j = 0; j < docOrder.size(); j++) {
			list[j] = docOrder.get(j);
		}
		csvWriter.writeNext(list);

		for (int j = 0; j < 3; j++) { // consider top 3
			csvWriter.writeNext(csvReader.readNext());
		}
		csvWriter.close();
		csvReader.close();
	}

	public void exectuteLDA(String inputLocation, List<List<String>> order,
			Integer ktopics,MatlabProxy proxy) throws MatlabConnectionException,
			MatlabInvocationException, IOException {
		
		String component = inputLocation.substring(inputLocation.lastIndexOf(File.separator) + 1);


		File[] files = new File(inputLocation).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return name.endsWith(".csv") && pathname.isFile();
			}
		});
		
		if(!Utils.isDirectoryCreated(files[0].getParentFile().getParentFile().getParentFile()
							.getAbsolutePath() + File.separator + IConstants.OUTPUT_DIR+File.separator+component))
			return;
		
		for (int i = 0; i < files.length; i++) {
			proxy.eval("ldamain('"
					+ files[i].getAbsolutePath()
					+ "','"
					+ files[i].getParentFile().getParentFile().getParentFile()
							.getAbsolutePath() + File.separator + IConstants.OUTPUT_DIR+File.separator+component
					+ File.separator + files[i].getName() + "'," + ktopics
					+ ")"); // three for 3 topics
		}
		
		if(!Utils.isDirectoryCreated(files[0].getParentFile()
					.getParentFile().getParentFile().getAbsolutePath()
					+ File.separator
					+ IConstants.LDA_SEMANTICS
					+ File.separator+component))
			return;

		for (int i = 0; i < order.size(); i++) {
			// read sensor 0 data and print

			String ldaFileName = files[i].getParentFile().getParentFile().getParentFile()
					.getAbsolutePath() + File.separator + IConstants.OUTPUT_DIR+File.separator+component
			+ File.separator + i + ".csv";
			String ldaSemanticFileName = files[i].getParentFile()
					.getParentFile().getParentFile().getAbsolutePath()
					+ File.separator
					+ IConstants.LDA_SEMANTICS
					+ File.separator+component+File.separator
					+ i
					+ ".csv";

			CSVReader csvReader = new CSVReader(new InputStreamReader(
					new FileInputStream(ldaFileName)));
			CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
					new FileOutputStream(ldaSemanticFileName)));

			List<String> orderList = order.get(i);
			String[] list = new String[orderList.size()];

			for (int j = 0; j < orderList.size(); j++) {
				list[j] = orderList.get(j);
			}
			csvWriter.writeNext(list);

			for (int j = 0; j < 3; j++) {
				csvWriter.writeNext(csvReader.readNext());
			}
			csvWriter.close();
			csvReader.close();

		}

	}

	public String[][][] transformDataForLDA(String inputLocation)
			throws IOException {
		
		String component = inputLocation.substring(inputLocation.lastIndexOf(File.separator) + 1);

		File[] files = new File(inputLocation).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return name.endsWith(".csv") && pathname.isFile();
			}
		});
		String[][][] data3D = new String[files.length][][];
		for (int i = 0; i < files.length; i++) {
			CSVReader csvReader = new CSVReader(new InputStreamReader(
					new FileInputStream(files[i])));
			List<String[]> csvRead = csvReader.readAll();
			String[][] data2D = new String[csvRead.size()][];
			for (int j = 0; j < csvRead.size(); j++) {
				/** Tranform data for svd **/
				String[] input = csvRead.get(j);
				for (int k = 0; k < input.length; k++) {
					input[k] = String.valueOf(j + 1) + ':' + input[k];
				}
				/***/
				data2D[j] = input;
			}
			data3D[i] = data2D;
			csvReader.close();
		}

		data3D = transform(data3D);

		
		if(!Utils.isDirectoryCreated(new File(inputLocation).getParentFile()
					.getParentFile().getParentFile()
					+ File.separator
					+ IConstants.LDA_DIR
					+ File.separator + IConstants.INPUT_DIR+ File.separator + component))
			return null;
		
		for (int i = 0; i < files.length; i++) {
			String ldaOutputFile = new File(inputLocation).getParentFile()
					.getParentFile().getParentFile()
					+ File.separator
					+ IConstants.LDA_DIR
					+ File.separator + IConstants.INPUT_DIR+ File.separator + component
					+ File.separator + files[i].getName();
			CSVWriter csvWrite = new CSVWriter(new OutputStreamWriter(
					new FileOutputStream(ldaOutputFile)), '\t',
					CSVWriter.NO_QUOTE_CHARACTER);
			for (int j = 0; j < data3D[i].length; j++) {
				csvWrite.writeNext(data3D[i][j]);
			}
			csvWrite.close();
		}
		return data3D;
		// note still need to transform whole data, to have Doucument vs Topic Distribution
	}

	// tranpose tensor
	private String[][][] transform(String[][][] data3D) {
		for (int i = 0; i < data3D.length; i++) {
			String[][] data2D = data3D[i];
			String[][] tranpose2D = new String[data2D[0].length][data2D.length];
			for (int j = 0; j < data2D.length; j++) {
				for (int k = 0; k < data2D[j].length; k++) {
					tranpose2D[k][j] = data2D[j][k];
				}
			}
			data3D[i] = tranpose2D;
		}
		return data3D;
	}

	public void executeSVD(String inputLocation, List<List<String>> order,MatlabProxy proxy)
			throws MatlabConnectionException, MatlabInvocationException,
			IOException {

		String component = inputLocation.substring(inputLocation.lastIndexOf(File.separator) + 1);
		File[] files = new File(inputLocation).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return name.endsWith(".csv") && pathname.isFile();
			}
		});
		if(!Utils.isDirectoryCreated(files[0].getParentFile().getParentFile().getParentFile().getAbsolutePath()
				+ File.separator + IConstants.SVD_DIR+File.separator+ component)){
			System.out.println("Failed Creation of Directory "+ files[0].getParentFile().getParentFile().getParentFile().getAbsolutePath()
				+ File.separator + IConstants.SVD_DIR+File.separator+ component);
			return;
		
		}
		for (int i = 0; i < files.length; i++) {
			System.out.println(files[i].getParentFile().getParentFile().getParentFile().getAbsolutePath()
					+ File.separator + IConstants.SVD_DIR+File.separator+ component + File.separator
					+ files[i].getName());
			proxy.eval("SVDFinder('" + files[i].getAbsolutePath() + "','"
					+ files[i].getParentFile().getParentFile().getParentFile().getAbsolutePath()
					+ File.separator + IConstants.SVD_DIR + File.separator + component +File.separator
					+ files[i].getName() + "')");
		}

		
		if(!Utils.isDirectoryCreated(files[0].getParentFile().getParentFile().getParentFile()
				.getAbsolutePath()
				+ File.separator
				+ IConstants.SVD_SEMANTICS+File.separator+component)){
			System.out.println("File Creation failed "+files[0].getParentFile().getParentFile().getParentFile()
				.getAbsolutePath()
				+ File.separator
				+ IConstants.SVD_SEMANTICS+File.separator+component);
			return;
		}
		
		for (int i = 0; i < order.size(); i++) {
			// read sensor 0 data and print

			String pcaFileName = files[i].getParentFile().getParentFile().getParentFile().getAbsolutePath()
					+ File.separator + IConstants.SVD_DIR +File.separator+component+ File.separator + i + ".csv";
			String pcaSemanticFileName = files[i].getParentFile().getParentFile().getParentFile()
					.getAbsolutePath()
					+ File.separator
					+ IConstants.SVD_SEMANTICS+File.separator+component
					+ File.separator + i + ".csv";

			CSVReader csvReader = new CSVReader(new InputStreamReader(
					new FileInputStream(pcaFileName)));
			CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
					new FileOutputStream(pcaSemanticFileName)));

			List<String> orderList = order.get(i);
			String[] list = new String[orderList.size()];

			for (int j = 0; j < orderList.size(); j++) {
				list[j] = orderList.get(j);
			}
			csvWriter.writeNext(list);

			for (int j = 0; j < 3; j++) { // top 3
				csvWriter.writeNext(csvReader.readNext());
			}
			csvWriter.close();
			csvReader.close();
		}
	}

	// Populates Map<String,List<String>> sensorWords
	public Map<Integer, Set<String>> createWordsPerSensor(
			List<List<Map<String, List<Double>>>> dictionary) {

		Map<Integer, Set<String>> sensorWords = new HashMap<Integer, Set<String>>();

		// iterate multivariate documents
		for (int i = 0; i < dictionary.size(); i++) {
			// iterate univariate document
			for (int j = 0; j < dictionary.get(i).size(); j++) {
				// iterate each sensor -- note assuming 0 to 19

				if (sensorWords.get(j) != null)
					sensorWords.get(j)
							.addAll(dictionary.get(i).get(j).keySet());
				else {
					Set<String> wordsPerSensor = new HashSet<String>();
					wordsPerSensor.addAll(dictionary.get(i).get(j).keySet());
					sensorWords.put(j, wordsPerSensor);
				}
			}
		}
		return sensorWords;
	}

	public List<List<String>> savewordstoCSV(
			List<Map<String, Double[]>> sensorWordsScores,
			String outputDirectory) {

		List<List<String>> orderofDimenions = new ArrayList<List<String>>();
		try {
			for (int i = 0; i < sensorWordsScores.size(); i++) {
				// System.out.println("Input Directory Path is "+inputDirectoryPath);
				// inputDirectoryPath=(inputDirectoryPath==null)?"data\\":inputDirectoryPath+"\\OUTPUTP1\\phase2\\";
				// System.out.println("Input Directory Path is"+inputDirectoryPath);

				// check this later
				CSVWriter csvWriter = new CSVWriter(new FileWriter(
						outputDirectory + File.separator + i + ".csv"), ',',
						CSVWriter.NO_QUOTE_CHARACTER,
						CSVWriter.DEFAULT_LINE_END);

				Iterator<Entry<String, Double[]>> iterator = sensorWordsScores
						.get(i).entrySet().iterator();

				List<String> wordOrder = new ArrayList<String>();
				while (iterator.hasNext()) {
					Entry<String, Double[]> entry = iterator.next();
					List<String> list = new ArrayList<String>();

					wordOrder.add(entry.getKey());

					Double[] array = entry.getValue();
					for (int j = 0; j < array.length; j++) {
						list.add(String.valueOf(array[j]));
					}
					String[] stringList = list.toArray(new String[list.size()]);
					csvWriter.writeNext(stringList);
				}
				orderofDimenions.add(wordOrder);
				csvWriter.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orderofDimenions;
	}

	// Populate List<List<List<Double>>> sensorWordsScores
	public List<Map<String, Double[]>> createSensorWordScores(
			Map<Integer, Set<String>> sensorWords,
			List<List<Map<String, List<Double>>>> dictionary, int choice) {
		List<Map<String, Double[]>> sensorWordsScores = new ArrayList<Map<String, Double[]>>();

		for (int i = 0; i < sensorWords.size(); i++) { // assuming keys start
														// from 0 to 19
			// get words
			List<String> words = new ArrayList<String>(sensorWords.get(i)); // since
																			// order
																			// doesn't
																			// matter

			Map<String, Double[]> dimensionAgainstAllDocuments = new HashMap<String, Double[]>();
			// insert all words with zero initial value for tf-idf / tf-idf2
			for (int j = 0; j < words.size(); j++) {
				dimensionAgainstAllDocuments.put(words.get(j),
						new Double[dictionary.size()]);
			}

			// now iterate dictionary
			for (int k = 0; k < dictionary.size(); k++) {
				Map<String, List<Double>> map = dictionary.get(k).get(i); // get
																			// i
																			// th
																			// map
																			// of
																			// all
																			// multivariate
																			// series
																			// documents

				for (int j = 0; j < words.size(); j++) {
					if (map.containsKey(words.get(j))) {
						Double[] temp = dimensionAgainstAllDocuments.get(words
								.get(j));
						temp[k] = map.get(words.get(j)).get(choice); // pass th
																		// approprite
																		// index
																		// for
																		// tf or
																		// tf-idf
						// tf-idf
					} else {
						// else it's value is already zero - so this part is not
						// required at all
						Double[] temp = dimensionAgainstAllDocuments.get(words
								.get(j));
						temp[k] = 0.0; // for 3 for tf-idf
					}
				}
			}
			// iterated dictionary for ith sensor, add it to main list
			sensorWordsScores.add(dimensionAgainstAllDocuments);
		}

		return sensorWordsScores;
	}

	public void executeSVDGG(String fileLocation, List<String> docOrder, MatlabProxy proxy)
			throws MatlabConnectionException, MatlabInvocationException,
			IOException {
		File file = new File(fileLocation);
		
		String temp = fileLocation.substring(0,fileLocation.lastIndexOf(File.separator));
		String componentName = temp.substring(temp.lastIndexOf(File.separator) + 1);
		String outputFile =IConstants.DATA
				+ File.separator + IConstants.SVD_DIR_GG + File.separator + componentName + File.separator
				+ "svd_" + file.getName() ;
		proxy.eval("SVDFinder('" + fileLocation + "','" + new File(outputFile).getAbsolutePath()  + "')");

		String svdFileName = new File(outputFile).getAbsolutePath();
		String svdSemanticFileName = IConstants.DATA + File.separator
				+ IConstants.SVD_DIR_GG + File.separator +componentName+File.separator+ "semanticgg_"
				+ file.getName();

		CSVReader csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(svdFileName)));
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
				new FileOutputStream(svdSemanticFileName)));

		String[] list = new String[docOrder.size()];

		for (int j = 0; j < docOrder.size(); j++) {
			list[j] = docOrder.get(j);
		}
		csvWriter.writeNext(list);

		for (int j = 0; j < 3; j++) { // consider top 3
			csvWriter.writeNext(csvReader.readNext());
		}
		csvWriter.close();
		csvReader.close();
	}
}
