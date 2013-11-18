package com.asu.mwdb.phase3.task2;


import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.asu.mwdb.phase2Main.AssignBandValues;
import com.asu.mwdb.phase2Main.DictionaryBuilderPhase2;
import com.asu.mwdb.phase2Main.DriverMain;
import com.asu.mwdb.phase2Main.NormalizeData;
import com.asu.mwdb.phase2Main.SearchDatabaseForSimilarity;
import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Phase2Utils;
import com.asu.mwdb.utils.Utils;

public class QueryMapper {
	private static MatlabProxy proxy;
	private static Integer wordLength;
	private static Integer shiftLength;
	private static String matlabScriptLoc;
	private static Map<String, DictionaryBuilderPhase2> dictMap;
	private static double rBandValueRange[][];
	private String gestureInputDirectory;
	private String sampleInputDirectory;
	private Map<String,List<List<Map<String, List<Double>>>>> dictMapOfQuery = new HashMap<String, List<List<Map<String, List<Double>>>>>();

	

	public QueryMapper(Map<String, DictionaryBuilderPhase2> dictMap,Integer wordLength,Integer shiftLenght,String matlabScriptLoc,double rBandValueRange[][],MatlabProxy proxy,String gestureInputDirectory,String sampleInputDirectory){
		this.wordLength=wordLength;
		this.shiftLength=shiftLenght;
		this.matlabScriptLoc=matlabScriptLoc;
		this.proxy=proxy;
		this.dictMap=dictMap;
		this.rBandValueRange=rBandValueRange;
		this.gestureInputDirectory=gestureInputDirectory;
		this.sampleInputDirectory=sampleInputDirectory;
		try {
			init();  //this will index all query components
			tranform(); //this will transform all query documents using SVD principle components  
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Indexing failed for the given input directory "+gestureInputDirectory);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void tranform() throws IOException {
		// TODO Auto-generated method stub
		
		File[] queryInputDirectory = new File(sampleInputDirectory).listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File arg0) {
				// TODO Auto-generated method stub
				if(!arg0.getName().contains("all"))
					return true;
				
				return false;
			}
		});  // note iterating on sampleInput,because same key
		for (int i = 0; i < queryInputDirectory.length; i++) {
			Integer index = queryInputDirectory[i].getAbsolutePath().lastIndexOf(File.separator)+1;
			String component =   queryInputDirectory[i].getAbsolutePath().substring(index); //either X, Y , Z or W of Query Gesture
			Phase2Utils main = new Phase2Utils();
			List<List<Map<String, List<Double>>>> queryDictionary = dictMapOfQuery.get(queryInputDirectory[i].getAbsolutePath());
			
			Map<Integer, Set<String>> queryWordMap = main.createWordsPerSensor(queryDictionary );
			List<Map<String, Double[]>> queryWordScores = main.createSensorWordScores(queryWordMap, queryDictionary , 3);
			String svdSematicDir  = IConstants.DATA + File.separator + IConstants.SVD_SEMANTICS + File.separator + component;
			String svdSemanticOpDir = IConstants.DATA_PHASE3 + File.separator + IConstants.SVD_MAPPED + File.separator + component;
			if(!Utils.isDirectoryCreated(svdSemanticOpDir))return;
			List<String[]> svdQueryTransformList = DriverMain.mapQueryToLSASpace(svdSematicDir, svdSemanticOpDir, queryWordScores);
			String svdTransformDirectory = "." + File.separator + IConstants.DATA + File.separator + IConstants.SVD_TRANSFORM + File.separator + component;
			File svdTransformedDirFile = new File(svdTransformDirectory);
			List<List<String[]>> svdTrasnsformData = Utils.convertDataForComparison(svdTransformDirectory, svdTransformedDirFile.listFiles());
			String svdTransformToFileDirectory = "." + File.separator + IConstants.DATA_PHASE3  + File.separator + IConstants.SVD_TRANSFORM_GESTURE + File.separator + component;
			Utils.writeListOfListToDir(svdTrasnsformData, svdTransformToFileDirectory, Utils.getCSVFiles(new File(sampleInputDirectory+File.separator+component)));  //god help us
		}	
	}
	private void init() throws IOException, MatlabInvocationException{
		//read  X directory from query and read X directory from Sample  , similary for Y, Z, W
		
		File[] queryInputDirectory = new File(gestureInputDirectory).listFiles();
		for (int i = 0; i < queryInputDirectory.length; i++) {
			
			Integer index = queryInputDirectory[i].getAbsolutePath().lastIndexOf(File.separator)+1;
			
			String component =   queryInputDirectory[i].getAbsolutePath().substring(index); //either X, Y , Z or W of Query Gesture 
			
			String dictMapKey = sampleInputDirectory+File.separator+component;
			
			DictionaryBuilderPhase2 correspondDictionary = dictMap.get(dictMapKey); //this is database dictionary for above componet(variable above) of query 

			
			File[] inputFiles = new File(queryInputDirectory[i].getAbsolutePath()).listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File arg0) {
					// TODO Auto-generated method stub
					if(arg0.getName().endsWith(".csv"))
						return true;
					
					return false;
				}
			});
			
			//assuming theres is only one file
			
			String inputFilePath = inputFiles[0].getAbsolutePath();
			
			NormalizeData.NormalizeDataForSingleFile(proxy, inputFilePath);
			AssignBandValues.assignGaussianCurveTask3(proxy, inputFilePath,rBandValueRange); //this will create gaussian.csv
			SearchDatabaseForSimilarity searchDatabaseForSimilarity = new SearchDatabaseForSimilarity(correspondDictionary.getTfIDFMapGlobal(), correspondDictionary.getTfMapArrayIDF(), correspondDictionary.getTfMapArrayIDF2(), wordLength, shiftLength, inputFilePath);
			List<List<Map<String, List<Double>>>> queryDatabase = searchDatabaseForSimilarity.getInputDictionary();
			dictMapOfQuery.put(dictMapKey, queryDatabase);
		}
		
	}
	
	
	
}
