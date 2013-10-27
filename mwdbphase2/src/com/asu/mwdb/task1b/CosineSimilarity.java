package com.asu.mwdb.task1b;

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

		float ans = 0;

		for (int i = 0; i < keys.length; i++) {
			if (hashMap2.containsKey(keys[i])) {
				ans += hashMap.get(keys[i]) * hashMap2.get(keys[i]);
				
			}
		}

		float hashLength = 0;
		for (int i = 0; i < keys.length; i++) {
			hashLength += (hashMap.get(keys[i]) * hashMap.get(keys[i]));
		}
		hashLength = (float) Math.sqrt(hashLength);

		String[] keys2 = new String[hashMap2.size()];
		hashMap2.keySet().toArray(keys2);

		float hash2Length = 0;
		for (int i = 0; i < keys2.length; i++) {

			hash2Length += hashMap2.get(keys2[i]) * hashMap2.get(keys2[i]);

		}
		hash2Length = (float) Math.sqrt(hash2Length);
		if(hash2Length == 0 || hashLength == 0)
			return 0;
		
		return (float) (ans / (hash2Length * hashLength));
	}
}
