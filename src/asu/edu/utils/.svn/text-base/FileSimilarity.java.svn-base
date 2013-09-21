package asu.edu.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import asu.edu.loggers.MyLogger;

public class FileSimilarity {

	private static Logger logger = new MyLogger().getupLogger();

	public static List<String> getSimilarWordFromFiles(String filePath1,
			String filePath2) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(
				filePath1));
		String line = null;
		List<String> file1 = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			file1.add(line.trim());
		}
		reader.close();
		reader = new BufferedReader(new FileReader(
				filePath2));
		List<String> file2 = new ArrayList<String>();
		while ((line = reader.readLine()) != null) {
			file2.add(line.trim());
		}
		reader.close();

		Iterator<String> I1 = file1.iterator();

		List<String> similarWordList = new ArrayList<String>();
		while (I1.hasNext()) {
			String word1[] = I1.next().split("=");
			Iterator<String> I2 = file2.iterator();
			while (I2.hasNext()) {
				String word2[] = I2.next().split("=");
				if (word1[0].equals(word2[0])) {
					logger.info(word1[0] + "," + word1[1] + "," + word2[1]);
					similarWordList.add(word1[0] + "," + word1[1] + "," + word2[1]);
				}
			}
		}
		return similarWordList;
	}
	
	public static double getEucledianDistance(List<String> similarWordLIst){
		Iterator<String> I = similarWordLIst.iterator();
		int sum = 0;
		while(I.hasNext()){
			String word [] = I.next().split(",");
			sum =(int) Math.pow((Integer.parseInt(word[1]) - Integer.parseInt(word[2])),2);  
		}
		double dist = Math.sqrt((double) sum);
		logger.info("Distance : "+ dist);
		return dist;
	}
}
