package com.asu.mwdb.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.asu.mwdb.loggers.MyLogger;
import au.com.bytecode.opencsv.CSVReader;

public class ConstructGestureVectors {

	private Map<String, Integer> globalMap = new HashMap<String, Integer>();
	private List<Map<String, List<Map<String, List<Double>>>>> folderMapTFIDF = new ArrayList<Map<String, List<Map<String, List<Double>>>>>();
	private Map<String, Map<String, Double>> folderMapTFIDF2 = new HashMap<String, Map<String, Double>>();
	private List<Double> setOfIDF = new ArrayList<Double>();
	private List<Double> setOfIDF2 = new ArrayList<Double>();
	private  Logger logger = new MyLogger().getupLogger();

	// We shall create Gesture vector per axis or angle folder
	public void constructGestureVectorPerFolder(String databaseFolder,
			int w, int s) {
		try {
			constructGestureWords(databaseFolder + "\\letter\\W",
					databaseFolder + "\\task1_new\\W", databaseFolder
					+ "\\task2_new\\W", w, s);

			// constructGestureWords(databaseFolder + "\\letter\\X",
			// databaseFolder + "\\task1_new\\X", databaseFolder
			// + "\\task2_new\\X", w, s);
			//
			// constructGestureWords(databaseFolder + "\\letter\\Y",
			// databaseFolder + "\\task1_new\\Y", databaseFolder
			// + "\\task2_new\\Y", w, s);
			//
			// constructGestureWords(databaseFolder + "\\letter\\Z",
			// databaseFolder + "\\task1_new\\Z", databaseFolder
			// + "\\task2_new\\Z", w, s);
			logger.info("Gesture files ready with W angle");
		} catch (IOException e) {
			logger.log(Level.FINEST, "IO Error :", e);
		}

	}

	public void constructGestureWords(String letterFolder,
			String outputFolder1, String outputFolder2, int w, int s)
					throws IOException {

		File dir = new File(letterFolder);

		File[] symbolFiles = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return filename.endsWith(".csv");
			}
		});

		for (File f : symbolFiles) {
			List<String[]> symbolMatrix = null;
			try {

				CSVReader csvReader = new CSVReader(new FileReader(f));
				symbolMatrix = csvReader.readAll();
				csvReader.close();
			} catch (IOException e) {
				logger.log(Level.FINEST, "IO Error :", e);
			}
			Map<String, List<Map<String, List<Double>>>> perFileDetail = new HashMap<String, List<Map<String, List<Double>>>>();
			List<Map<String, List<Double>>> mapPerFile = new ArrayList<Map<String, List<Double>>>();
			Map<String, List<Double>> mapPerSensor = null;
			Iterator<String[]> I = symbolMatrix.iterator();
			// Parsing per sensor
			while (I.hasNext()) {
				mapPerSensor = new HashMap<String, List<Double>>();
				String rowSymbols[] = I.next();
				int extra = -1;
				double wordCount = 0;
				for (int i = 0; i < rowSymbols.length - w + 1; i = i + s) {
					StringBuffer word = new StringBuffer();
					for (int j = i; j < w + i; j++) {
						word.append(rowSymbols[j]);
					}
					// for TF
					if (mapPerSensor.containsKey(word.toString())) {
						List<Double> list = mapPerSensor.get(word.toString());
						// Incrementing term frequency
						list.set(0, list.get(0) + 1);
						mapPerSensor.put(word.toString(), list);
					} else {
						List<Double> list = new ArrayList<Double>();
						list.add(0, 1.0);
						mapPerSensor.put(word.toString(), list);
					}
					extra = i + s;
					wordCount++;
				}
				int dif = rowSymbols.length - extra;

				if (dif > 0) {
					StringBuffer paddedWord = new StringBuffer();
					int paddingLen = w - dif;
					while (dif > 0) {
						paddedWord.append(rowSymbols[extra]);
						--dif;
						++extra;
					}

					while (paddingLen > 0) {
						paddedWord.append(rowSymbols[extra - 1]);
						--paddingLen;
					}
					if (mapPerSensor.containsKey(paddedWord.toString())) {
						List<Double> list = mapPerSensor.get(paddedWord
								.toString());
						// Incrementing term frequency
						list.set(0, list.get(0) + 1);
						mapPerSensor.put(paddedWord.toString(), list);
					} else {
						List<Double> list = new ArrayList<Double>();
						list.add(0, 1.0);
						mapPerSensor.put(paddedWord.toString(), list);
					}
					wordCount++;
				}
				mapPerSensor = updateWordMapForTotalCountK(mapPerSensor,
						wordCount);
				mapPerFile.add(mapPerSensor);
			}
			// printFileMap(mapPerFile);
			perFileDetail.put(f.getName(), mapPerFile);
			folderMapTFIDF.add(perFileDetail);

			// calculate IDF2, its per file and number of sensors
			calculateAndUpdateIDF2Map(mapPerFile, f.getName());
		}

		createGlobalMap(folderMapTFIDF);

		generateIDFFiles();

		generateIDF2Files();

		normalizeDictionary();

		dumpToDataBase(folderMapTFIDF, outputFolder1, outputFolder2);
	}

	public Map<String, List<Double>> updateWordMapForTotalCountK(
			Map<String, List<Double>> mapPerSensor, double wordCount) {
		Iterator iterator = mapPerSensor.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, List<Double>> entry = (Map.Entry<String, List<Double>>) iterator
					.next();
			if (wordCount > 0.0)
				entry.getValue().set(0, entry.getValue().get(0) / wordCount); // add
			// all
			// tf
			// for
			// total
			// words
		}

		return mapPerSensor;
	}

	private void normalizeDictionary() {

		Collections.sort(setOfIDF, Collections.reverseOrder());
		Collections.sort(setOfIDF2, Collections.reverseOrder());

		Double maxIDF = setOfIDF.get(0) == 0.0 ? 1.0 : setOfIDF.get(0);
		Double maxIDF2 = setOfIDF2.get(0) == 0.0 ? 1.0 : setOfIDF2.get(0);
		for (Map<String, List<Map<String, List<Double>>>> fileMap : folderMapTFIDF) {

			Iterator<String> keySetIterator = fileMap.keySet().iterator();
			while (keySetIterator.hasNext()) {
				String key = keySetIterator.next();
				List<Map<String, List<Double>>> mapPerFile = fileMap.get(key);
				for (Map<String, List<Double>> sensorMap : mapPerFile) {
					Iterator I = sensorMap.entrySet().iterator();
					while (I.hasNext()) {
						Map.Entry<String, List<Double>> pairs = (Map.Entry) I
								.next();
						// normalize tf-idf
						pairs.getValue().set(3,
								pairs.getValue().get(3) / maxIDF);
						// normalize tf-idf2
						pairs.getValue().set(4,
								pairs.getValue().get(4) / maxIDF2);

					}
				}
			}
		}
	}

	private void dumpToDataBase(
			List<Map<String, List<Map<String, List<Double>>>>> folderMapTFIDF3,
			String outputFolder1, String outputFolder2) {

		for (Map<String, List<Map<String, List<Double>>>> fileMap : folderMapTFIDF3) {
			Iterator<String> keySetIterator = fileMap.keySet().iterator();
			while (keySetIterator.hasNext()) {
				String key = keySetIterator.next();
				List<Map<String, List<Double>>> mapPerFile = fileMap.get(key);
				try {
					BufferedWriter br1 = new BufferedWriter(new FileWriter(
							outputFolder1 + File.separator + key, true));
					BufferedWriter br2 = new BufferedWriter(new FileWriter(
							outputFolder2 + File.separator + key, true));
					for (Map<String, List<Double>> sensorMap : mapPerFile) {
						Iterator I = sensorMap.entrySet().iterator();
						while (I.hasNext()) {
							Map.Entry<String, List<Double>> pairs = (Map.Entry) I
									.next();
							List<Double> idf = pairs.getValue();
							br1.write(pairs.getKey() + ":"
									+ pairs.getValue().get(0) + ","
									+ pairs.getValue().get(3) + ","
									+ pairs.getValue().get(4) + ";");
							br2.write(pairs.getKey() + ":"
									+ pairs.getValue().get(0) + ","
									+ pairs.getValue().get(1) + ","
									+ pairs.getValue().get(2) + ","
									+ pairs.getValue().get(3) + ","
									+ pairs.getValue().get(4) + ";");
						}
						br1.write("\r\n");
						br2.write("\r\n");
					}

					br1.close();
					br2.close();
				} catch (IOException e) {
					logger.log(Level.FINEST, "IO Error :", e);
				}
			}
		}

	}

	private void generateIDF2Files() {
		for (Map<String, List<Map<String, List<Double>>>> fileMap : folderMapTFIDF) {

			Iterator<String> keySetIterator = fileMap.keySet().iterator();
			while (keySetIterator.hasNext()) {
				String key = keySetIterator.next();
				Map<String, Double> idf2PerFile = folderMapTFIDF2.get(key);
				List<Map<String, List<Double>>> mapPerFile = fileMap.get(key);
				for (Map<String, List<Double>> sensorMap : mapPerFile) {
					Iterator I = sensorMap.entrySet().iterator();
					while (I.hasNext()) {
						Map.Entry<String, List<Double>> pairs = (Map.Entry) I
								.next();
						
						Double inverse = (new Double(mapPerFile.size())/idf2PerFile.get(pairs.getKey())); 
				        List<Double> tf = pairs.getValue(); 
				        tf.add(tf.get(0)*Math.log(inverse));

						// calculate tf-idf and tf-idf2
						tf.add(tf.get(0) * tf.get(1));
						setOfIDF.add(tf.get(0)*tf.get(1));
						tf.add(tf.get(0) * tf.get(2));
						setOfIDF2.add(tf.get(0)*tf.get(2));
					}
				}

			}
		}
	}

	private void generateIDFFiles() {
		for (Map<String, List<Map<String, List<Double>>>> fileMap : folderMapTFIDF) {

			Iterator<String> keySetIterator = fileMap.keySet().iterator();
			while (keySetIterator.hasNext()) {
				String key = keySetIterator.next();
				List<Map<String, List<Double>>> mapPerFile = fileMap.get(key);
				for (Map<String, List<Double>> sensorMap : mapPerFile) {
					Iterator I = sensorMap.entrySet().iterator();
					while (I.hasNext()) {
						Map.Entry<String, List<Double>> pairs = (Map.Entry) I
								.next();

						Double inverse = (new Double(folderMapTFIDF.size()
								* mapPerFile.size()) / globalMap.get(pairs
										.getKey()));
						Double idf = pairs.getValue().get(0)
								* (Math.log(inverse));
						pairs.getValue().add(idf);
					}
				}
			}
		}

	}

	private void createGlobalMap(
			List<Map<String, List<Map<String, List<Double>>>>> folderMapTFIDF1) {

		for (Map<String, List<Map<String, List<Double>>>> fileMap : folderMapTFIDF1) {

			Iterator<String> keySetIterator = fileMap.keySet().iterator();
			while (keySetIterator.hasNext()) {
				String key = keySetIterator.next();
				List<Map<String, List<Double>>> mapPerFile = fileMap.get(key);
				for (Map<String, List<Double>> sensorMap : mapPerFile) {
					Iterator I = sensorMap.entrySet().iterator();
					while (I.hasNext()) {
						Map.Entry<String, Integer> pairs = (Map.Entry) I.next();

						if (globalMap.containsKey(pairs.getKey()))
							globalMap.put(pairs.getKey(),
									globalMap.get(pairs.getKey()) + 1);
						else
							globalMap.put(pairs.getKey(), 1);
					}
				}

			}

		}
	}

	public void printFileMap(List<Map<String, List<Double>>> map) {
		for (Map<String, List<Double>> mapPerSenor : map) {
			Iterator temp = mapPerSenor.entrySet().iterator();
			logger.info("Sensor data :");
			while (temp.hasNext()) {
				Map.Entry<String, List<Double>> pairs = (Map.Entry) temp.next();
				logger.info("" + pairs.getKey() + "  "
						+ pairs.getValue().get(0));
			}
		}
	}

	public void calculateAndUpdateIDF2Map(
			List<Map<String, List<Double>>> map, String fileName) {
		Map<String, Double> idf2PerFile = new HashMap<String, Double>();

		for (Map<String, List<Double>> tmpMap : map) {
			Iterator I = tmpMap.entrySet().iterator();
			while (I.hasNext()) {
				Map.Entry<String, List<Double>> pairs = (Entry<String, List<Double>>) I
						.next();
				if (idf2PerFile.containsKey(pairs.getKey()))
					idf2PerFile.put(pairs.getKey(),
							idf2PerFile.get(pairs.getKey()) + 1);
				else
					idf2PerFile.put(pairs.getKey(), 1.0);
			}
		}
		folderMapTFIDF2.put(fileName, idf2PerFile);
	}

	public static void main(String args[]) {
		ConstructGestureVectors c = new ConstructGestureVectors();
		
		c.constructGestureVectorPerFolder(".\\data\\sampledata", 4, 4);
	}
}
