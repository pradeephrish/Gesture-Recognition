package com.asu.mwdb.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class Utils {
	
	public static Double[][]  getMatrix(String filePath) throws IOException
	{
		CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(filePath)));
		List<String[]> data = csvReader.readAll();
		Double[][] matrix = new Double[data.size()][data.get(0).length];
		for (int i = 0; i < data.size(); i++) {
			String[] row = data.get(i);
			for (int j = 0; j < row.length; j++) {
				matrix[i][j]=Double.parseDouble(row[j]);
			}
		}
		return matrix;
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
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                C[i][j] = 0.00000;
            }
        }

        for (int i = 0; i < aRows; i++) { // aRow
            for (int j = 0; j < bColumns; j++) { // bColumn
                for (int k = 0; k < aColumns; k++) { // aColumn
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
		Double[][] data1 = Utils.getMatrix("1.csv");
		print(data1);
		Double[][] data2 = Utils.getMatrix("2.csv");
		print(data2);
		
		//mutliply matrix
		Double[][] results = Utils.multiplicar(data1, data2);
		print(results);
	}
}
