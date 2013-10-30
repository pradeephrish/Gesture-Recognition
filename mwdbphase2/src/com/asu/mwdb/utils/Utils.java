package com.asu.mwdb.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class Utils {
	
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
		return matrix;
	}
	
	public static void tranformData(String inputDirectoryOne,String inputDirectoryTwo,String outputDirectory) throws IOException{
		File[] filesOne = new File(inputDirectoryOne).listFiles(new FileFilter() {
   			
   			@Override
   			public boolean accept(File pathname) {
   				// TODO Auto-generated method stub
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
	
	
	private static void saveMatrixFile(Double[][] outputMatrix,
			String outputDirectory, String name) throws IOException {
		// TODO Auto-generated method stub
		CSVWriter writer = new CSVWriter(new FileWriter(new File(outputDirectory+File.separator+name)), ',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
		for (int rowCount = 0; rowCount < outputMatrix.length; rowCount++) {
			String[] currentRowString = new String[outputMatrix.length];
			for (int i = 0; i < outputMatrix[rowCount].length; i++) {
				currentRowString[i]=String.valueOf(outputMatrix[rowCount][i]);
			}
			writer.writeNext(currentRowString);
		}
		writer.close();
	}

	public static Double[][] multiplicar(Double[][] A, Double[][] B) {

        int aRows = A.length;
        int aColumns = A[0].length;
        int bRows = B.length;
        int bColumns = B[0].length;

        if (aColumns != bRows) {
            throw new IllegalArgumentException("A:Rows: " + aColumns + " did not match B:Columns " + bRows + ".");
        }

        Double[][] C = new Double[aRows][bColumns];
       /* for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                C[i][j] = 0.00000;
            }
        }*/

        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
            		C[i][j]=0.0;
                for (int k = 0; k < aColumns; k++) { // aColumn
                	System.out.println("LOL");
                	System.out.println(C[i][j]);
                	System.out.println(A[i][k]);
                	System.out.println(B[k][j]);
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }

        return C;
    }
	public static void print(Double[][] results)
	{
		for (int i = 0; i < results.length; i++) {
			for (int j = 0; j < results[i].length; j++) {
				System.out.print(results[i][j]+" ");
			}
			System.out.println();
		}
	}
	public static void main(String[] args) throws IOException {
		Double[][] data1 = Utils.getMatrix("1.csv",0);
		print(data1);
		Double[][] data2 = Utils.getMatrix("2.csv",0);
		print(data2);
		
		//mutliply matrix
		Double[][] results = Utils.multiplicar(data1, data2);
		print(results);
	}
	
	  
    //tranpose tensor
     private static Double[][] transposeMatrix(Double[][] data2D) {
		// TODO Auto-generated method stub
				Double[][] tranpose2D = new Double[data2D[0].length][data2D.length];
				for (int j = 0; j < data2D.length; j++) {
					for (int k = 0; k < data2D[j].length; k++) {
							tranpose2D[k][j]=data2D[j][k];
					}
				}
		return tranpose2D;
	}
	
}
