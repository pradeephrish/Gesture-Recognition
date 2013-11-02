package com.asu.mwdb.task1a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;

import com.asu.mwdb.gui.MainWindow;
import com.asu.mwdb.task1b.DictionaryBuilderPhase2;
import com.asu.mwdb.utils.Utils;

public class Task1a {

	public static void executeTask1a(String inputDirectory,Map<String, DictionaryBuilderPhase2> dictMap)
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
	
	
}
