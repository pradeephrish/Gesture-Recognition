package com.asu.mwdb.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.phase2Main.CosineSimilarity;
import com.asu.mwdb.phase2Main.FileIOHelper;
import com.asu.mwdb.phase2Main.SearchDatabaseForSimilarity.UserChoice;

public class Utils {

	/**
	 * Get matrix from the file
	 * @param filePath
	 * @param offset
	 * @return
	 * @throws IOException
	 */
	public static Double[][]  getMatrix(String filePath,Integer offset) throws IOException //offset to skip first row in case of pca  
	{
		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath)));
		List<String[]> data = csvReader.readAll();
		for (int i = 0; i < offset; i++) {
			data.remove(i);
		}
		Double[][] matrix = new Double[data.size()][data.get(0).length];
		for (int i = 0; i < data.size(); i++) {
			String[] row = data.get(i);
			for (int j = 0; j < row.length; j++) {
				matrix[i][j]=Double.parseDouble(row[j]);
			}
		}
		csvReader.close();
		return matrix;
	}


	/**
	 * Transform data 
	 * inputDirectoryOne is always latent semantics 
	 * @param inputDirectoryOne
	 * @param inputDirectoryTwo
	 * @param outputDirectory
	 * @throws IOException
	 */
	public static void tranformData(String inputDirectoryOne,String inputDirectoryTwo,String outputDirectory) throws IOException{
		File[] filesOne = new File(inputDirectoryOne).listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				String name = pathname.getName().toLowerCase();
				return name.endsWith(".csv") && pathname.isFile();
			}
		});		
		for (int i = 0; i < filesOne.length; i++) {
			String otherFile = inputDirectoryTwo+File.separator+filesOne[i].getName();
			File file = new File(otherFile);
			if(file.exists())	
			{
				Double[][] matrix1 = getMatrix( filesOne[i].getAbsolutePath(), 1);
				Double[][] matrix2 = getMatrix( otherFile, 0);
				Double[][] outputMatrix = multiplicar(matrix1, matrix2);
				outputMatrix = transposeMatrix(outputMatrix);
				saveMatrixFile(outputMatrix,outputDirectory,filesOne[i].getName());
			}else{
				System.out.println("Corresponding file for multiplication not found "+ otherFile);
				return;
			}

		}

		System.out.println("Transform Successfull");
	}


	/**
	 * Save matrix to file
	 * @param outputMatrix
	 * @param outputDirectory
	 * @param name
	 * @throws IOException
	 */
	public static void saveMatrixFile(Double[][] outputMatrix,
			String outputDirectory, String name) throws IOException {
		CSVWriter writer = new CSVWriter(new FileWriter(new File(outputDirectory+File.separator+name)), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		for (int rowCount = 0; rowCount < outputMatrix.length; rowCount++) {
			String[] currentRowString = new String[outputMatrix[rowCount].length];
			for (int i = 0; i < outputMatrix[rowCount].length; i++) {
				currentRowString[i]=String.valueOf(outputMatrix[rowCount][i]);
			}
			writer.writeNext(currentRowString);
		}
		writer.close();
	}

	/**
	 * Multiple two matrix
	 * @param A
	 * @param B
	 * @return
	 */
	public static Double[][] multiplicar(Double[][] A, Double[][] B) {

		int aRows = A.length;
		int aColumns = A[0].length;
		int bRows = B.length;
		int bColumns = B[0].length;

		if (aColumns != bRows) {
			throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
		}

		Double[][] C = new Double[aRows][bColumns];

		for (int i = 0; i < aRows; i++) { // aRow
			for (int j = 0; j < bColumns; j++) { // bColumn
				C[i][j]=0.0;
				for (int k = 0; k < aColumns; k++) { // aColumn
					C[i][j] += A[i][k] * B[k][j];
				}
			}
		}

		return C;
	}
	
	/**
	 * Print results
	 * @param results
	 */
	public static void print(Double[][] results)
	{
		for (int i = 0; i < results.length; i++) {
			for (int j = 0; j < results[i].length; j++) {
				System.out.print(results[i][j]+" ");
			}
			System.out.println();
		}
	}
	


	/**
	 * Transpose a matrix
	 * @param data2D
	 * @return
	 */
	public static Double[][] transposeMatrix(Double[][] data2D) {
		Double[][] tranpose2D = new Double[data2D[0].length][data2D.length];
		for (int j = 0; j < data2D.length; j++) {
			for (int k = 0; k < data2D[j].length; k++) {
				tranpose2D[k][j]=data2D[j][k];
			}
		}
		return tranpose2D;
	}

	/**
	 * Get the transformed data for LSA and convert it into a format
	 * which can be used for comparison of query doc and database document
	 * @throws IOException 
	 */
	@SuppressWarnings("unchecked")
	public static List<List<String[]>> convertDataForComparison(String inputDirectory, File[] listFiles) throws IOException {
		File file = new File(inputDirectory);
		File[] files = file.listFiles();
		Arrays.sort(files, new NumberedFileComparator());
		File firstFile = files[0];
		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(firstFile.getAbsolutePath()))); 
		List<List<String[]>> allFileData = new ArrayList<List<String[]>>();
		List <String[]> fileData = csvReader.readAll();
		csvReader.close();
		Iterator <String[]> fileDataIterator = fileData.iterator();
		while(fileDataIterator.hasNext()){
			String data[] = fileDataIterator.next();
			List <String[]> singleList = new ArrayList<String[]>();
			singleList.add(data);
			allFileData.add(singleList);
		}
		for(int i=1 ;i<files.length;i++){
			File sensorFile =files[i];
			csvReader = new CSVReader(new InputStreamReader(new FileInputStream(sensorFile.getAbsolutePath()))); 
			List <String[]> fileData1 = csvReader.readAll();
			csvReader.close();
			Iterator <String[]> fileDataIterator1 = fileData1.iterator();
			Iterator <List<String[]>> allFileDataIterator = allFileData.iterator();
			while(fileDataIterator1.hasNext()){
				String data[] = fileDataIterator1.next();
				List<String[]> singleFileList = allFileDataIterator.next();
				singleFileList.add(data);
			}
		}
		return allFileData;
		
	}

	
	/**
	 * Once the gesture-gesture matrix is created, write the matrix into file 
	 * for future reference
	 * @param entity
	 * @throws IOException
	 */
	public static void writeGestureGestureToFile(UserChoice entity,String folderPath,double[][] doubleValues) throws IOException {
		String fileName = folderPath+ File.separator;
		switch(entity) {
			case TFIDF:
						fileName = fileName + "ggTFIDF.csv";
						break;
			case TFIDF2:
						fileName = fileName + "ggTFIDF2.csv";
						break;
			case PCA_LSA:
						fileName = fileName + "ggPCA.csv";
						break;
			case SVD_LSA:
					fileName = fileName + "ggSVD.csv";
						break;
			case LDA_LSA:
					fileName = fileName + "ggLDA.csv";
						break;
			default:	
					    break;
		} 
		CSVWriter writer = new CSVWriter(new FileWriter(new File(fileName)), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		for (int rowCount = 0; rowCount < doubleValues.length; rowCount++) {
			String[] currentRowString = new String[doubleValues.length];
			double[] currentRowDouble = doubleValues[rowCount];
			for (int rowCount2 = 0; rowCount2 < currentRowString.length; rowCount2++) {
				currentRowString[rowCount2] = String.valueOf(currentRowDouble[rowCount2]);
			}
			writer.writeNext(currentRowString);
		}
		writer.close();
	}
	
	/**
	 * COmpute similarity for TF-IDF and TF-IDF2
	 * @param dictionary
	 * @param entity
	 * @return
	 */
	public static double[][] computeSimilarilty(List<List<Map<String, List<Double>>>> dictionary, UserChoice entity) {
		double[][] doubleValues= null;
		doubleValues = new double[dictionary.size()][dictionary.size()];
		for (int currentFileIndex = 0; currentFileIndex < dictionary.size(); currentFileIndex++) {
			List<Map<String, List<Double>>> currentGesture = dictionary.get(currentFileIndex);
			for (int comparedFileIndex = 0; comparedFileIndex < dictionary.size(); comparedFileIndex++) {
				List<Map<String, List<Double>>> comparedGesture = dictionary.get(comparedFileIndex);
				double sum = 0.0d;
				try {
					for (int k = 0; k < comparedGesture.size(); k++) {
						Map<String, Double> inputMap = convertMap(
								comparedGesture.get(k), entity);
						Map<String, Double> dictMap = convertMap(
								currentGesture.get(k), entity);
						sum += CosineSimilarity.difference(inputMap, dictMap);
					}
					DecimalFormat df = new DecimalFormat("#.###");
					doubleValues[currentFileIndex][comparedFileIndex] = Double.parseDouble(df.format(sum))/20.0;
					doubleValues[comparedFileIndex][currentFileIndex] = Double.parseDouble(df.format(sum))/20.0;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return doubleValues;
	}
	
	/**
	 * Convert map into single dimension
	 * @param map
	 * @param entity
	 * @return
	 */
	private static Map<String, Double> convertMap(Map<String, List<Double>> map,
			UserChoice entity) {
			Map<String,Double> values = new HashMap<String, Double>(); 
			Iterator<Entry<String, List<Double>>> iterator = map.entrySet().iterator();
			while(iterator.hasNext()){
				Entry<String, List<Double>> entry = iterator.next();
				values.put(entry.getKey(), entry.getValue().get(entity.ordinal()));
			}
			
		return values;
	}
	
	/**
	 * Convert file to required format
	 */
	public static List<String> convertList(File[] fileList){
		List<String> documentOrder = new ArrayList<String>();
		for (int i = 0; i < fileList.length; i++) {
			documentOrder.add(fileList[i].getName());
		}
		return documentOrder;
	}
	
	/**
	 * Check if its a directory
	 * @param path
	 * @return
	 */
	public static boolean isDirectoryCreated(String path){
		try {
		File file = new File(path);
		if(file.exists()){
			FileIOHelper.delete(file);
			}
		if(!file.mkdir()){
			System.out.println("File creation failed "+file.getAbsolutePath());
			return false;
		}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}


	/**
	 * Prepare Gesture-Gesture matrix LSA
	 * @param out
	 * @return
	 */
	public static double[][] getGestureGestureMatrixLSA(List<List<String[]>> out) {
		double[][] output = new double[out.size()][out.size()];
		for (int i = 0; i < out.size(); i++) {
			for (int j = 0; j < out.size();j++) {
				output[i][j]=CosineSimilarity.compareLSADocument(out.get(i), out.get(j))/20;
			}
		}
		return output;
	}
	
	/**
	 * Write a List<List<String[]>> type data to disk
	 * @param listOfList
	 * @param fileName
	 * @param fileList
	 * @throws IOException
	 */
	public static void writeListOfListToDir(List<List<String[]>> listOfList, String fileName, File fileList[]) throws IOException{
		Iterator<List<String[]>> listOfListIterator = listOfList.iterator();
		File fileNameHandle = new File(fileName);
		if(!fileNameHandle.exists()){
			fileNameHandle.mkdir();
		}
		for(int i =0 ;i< listOfList.size();i++){
			List<String[]> singleList = listOfList.get(i);
			CSVWriter csvWriter = new CSVWriter(new FileWriter(fileName + File.separator +  fileList[i].getName()), ',',
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END);
			csvWriter.writeAll(singleList);
			csvWriter.close();
		}
	}
	
	/**
	 * Function to read the latent semantic file and return it 
	 * in a format required by the program
	 * @param semanticFile
	 * @return
	 * @throws IOException
	 */
	public static List<Map<String, Double>> getSemanticFileData(
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
	 * Get only csv files from the directory
	 * @param dirFile
	 * @return
	 */
	public static File [] getCSVFiles(File dirFile){
		File[] fileList = dirFile.listFiles(new FileFilter() {
			@Override
			public boolean accept(File path) {
				String name = path.getName().toLowerCase();
				return name.endsWith(".csv") && path.isFile();
			}
		});
		return fileList;
	}
	
}
