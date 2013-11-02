package com.asu.mwdb.task1c;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.asu.mwdb.task1b.AssignBandValues;
import com.asu.mwdb.task1b.DictionaryBuilderPhase2;
import com.asu.mwdb.task1b.NormalizeData;
import com.asu.mwdb.task1b.SearchDatabaseForSimilarity;

public class Task1c {

	public static void executeTask1c(double[][] rBandValueRange,
			String databaseDirectory, MatlabProxy proxy,Map<String, DictionaryBuilderPhase2> dictMap,Integer wordLength,Integer shiftLength) throws IOException,
			MatlabInvocationException {
		System.out.println("Enter input file name for Task 1c::");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String inputFileLocation = br.readLine();
		NormalizeData.NormalizeDataForSingleFile(proxy, inputFileLocation);
		System.out.println("Done normalization for Task 1c");
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
