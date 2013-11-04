package com.asu.mwdb.phase2Main;

import java.util.List;
import java.util.Map;
/**
 * This class is responsible for calculating cosine similarities of two vectors
 * @author Kedar Joshi
 *
 */
public class CosineSimilarity {	
	/**
	 * Calculating cosine similarity based on two vectors. 
	 * This is used in Task 3
	 * @param hashMap - this is for query file
	 * @param hashMap2 - this is from database file
	 * @return
	 */
	public static double difference(Map<String, Double> hashMap,
			Map<String, Double> hashMap2) {
		String[] keys = new String[hashMap.size()];
		hashMap.keySet().toArray(keys);

		double ans = 0;

		for (int i = 0; i < keys.length; i++) {
			if (hashMap2.containsKey(keys[i])) {
				ans += hashMap.get(keys[i]) * hashMap2.get(keys[i]);
				
			}
		}

		double hashLength = 0;
		for (int i = 0; i < keys.length; i++) {
			hashLength += (hashMap.get(keys[i]) * hashMap.get(keys[i]));
		}
		hashLength = (double) Math.sqrt(hashLength);

		String[] keys2 = new String[hashMap2.size()];
		hashMap2.keySet().toArray(keys2);

		double hash2Length = 0;
		for (int i = 0; i < keys2.length; i++) {

			hash2Length += hashMap2.get(keys2[i]) * hashMap2.get(keys2[i]);

		}
		hash2Length = (double) Math.sqrt(hash2Length);
		if(hash2Length == 0 || hashLength == 0)
			return 0;
		
		return (double) (ans / (hash2Length * hashLength));
	}
	
	/**
	 * Cosine similarity for LSA space files with query file 
	 * @param queryData
	 * @param documentData
	 * @return
	 */
	public static double compareLSADocument(List<String[]> queryData, List<String[]> documentData) {
		double sum = 0.0;
		for(int i=0; i< queryData.size(); i++) {
			String[] querySensorData = queryData.get(i);
			String[] docSensorData = documentData.get(i);
			double ans1 = 0;
			for(int j=0; j<querySensorData.length; j++) {
				// a.b
				ans1 = ans1 + (Double.parseDouble(querySensorData[j]) * Double.parseDouble(docSensorData[j]));
			}
			
			// find |a|.|b|
			double hashLength = 0;
			for (int j = 0; j < querySensorData.length; j++) {
				hashLength += (Double.parseDouble(querySensorData[j]) * Double.parseDouble(querySensorData[j]));
			}
			hashLength = (double) Math.sqrt(hashLength);
			
			double hashLength2 = 0;
			for (int j = 0; j < docSensorData.length; j++) {
				hashLength2 += (Double.parseDouble(docSensorData[j]) * Double.parseDouble(docSensorData[j]));
			}
			hashLength2 = (double) Math.sqrt(hashLength2);
			
			if(hashLength == 0 || hashLength2 == 0){
				sum = sum + 0;
			} else {
				sum = sum + (ans1/ (hashLength2 * hashLength));
			}
		}
		return sum;
	}
}
