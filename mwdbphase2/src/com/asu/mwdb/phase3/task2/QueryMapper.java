package com.asu.mwdb.phase3.task2;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.ode.MainStateJacobianProvider;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.phase2Main.AssignBandValues;
import com.asu.mwdb.phase2Main.DictionaryBuilderPhase2;
import com.asu.mwdb.phase2Main.DriverMain;
import com.asu.mwdb.phase2Main.FileIOHelper;
import com.asu.mwdb.phase2Main.NormalizeData;
import com.asu.mwdb.phase2Main.SearchDatabaseForSimilarity;
import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.NumberedFileComparator;
import com.asu.mwdb.utils.Phase2Utils;
import com.asu.mwdb.utils.SerializeData;
import com.asu.mwdb.utils.Utils;

public class QueryMapper {
	private static MatlabProxy proxy;
	private static Integer wordLength;
	private static Integer shiftLength;
	private static String matlabScriptLoc;
	private static Map<String, DictionaryBuilderPhase2> dictMap;
	private static double rBandValueRange[][];
	private String gestureInputDirectory;
	private static String sampleInputDirectory;
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
			
			//IMPPPP
			//*******Integer corresponds to Index of File while Reading the Component Directory --  and Double corresponds to Score
			List<LinkedHashMap<Integer, Double>> scoresPerQueryComponent = tranform(); //this will transform all query documents using SVD principle components  
			addAll(scoresPerQueryComponent); //sums up query against all database components
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter k :");
			Integer k = Integer.parseInt(br.readLine());
			System.out.println("Enter l :");
			Integer l = Integer.parseInt(br.readLine());
			System.out.println("Enter gestureCount :");
			Integer gestureCount = Integer.parseInt(br.readLine());
			performLocalitySensitiveHashing(this.proxy, k, l,gestureCount);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Indexing failed for the given input directory "+gestureInputDirectory);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void addAll(List<LinkedHashMap<Integer, Double>> scoresPerQueryComponent) {
		// TODO Auto-generated method stub
		LinkedHashMap<Integer, Double> sum = new LinkedHashMap<Integer, Double>();
		for (int i = 0; i < scoresPerQueryComponent.size(); i++) {
			Set<Entry<Integer, Double>> entry = scoresPerQueryComponent.get(i).entrySet();
			Iterator<Entry<Integer, Double>> iterator = entry.iterator();
			while(iterator.hasNext()){
				Entry<Integer, Double> entry1 = iterator.next(); 
				if(sum.containsKey(entry1.getKey())){
					sum.put(entry1.getKey(), sum.get(entry1.getKey())+entry1.getValue()); //add old + new value
				}else{
					sum.put(entry1.getKey(), entry1.getValue());
				}
			}
		}
		writeLinkedHashMapToFile(sum);
	}

	private void writeLinkedHashMapToFile(LinkedHashMap<Integer, Double> sum) { //remember these integers later for getting file names, these are indexes
		// TODO Auto-generated method stub
		 Double factor = new File(gestureInputDirectory).listFiles().length * 20.0; // number of components * 20 
		 
		 Set<Integer> keyset = sum.keySet();  // key will be always from 1 to number of Documents
		 Iterator iterator = keyset.iterator();
		try {
			CSVWriter csvWriter = new CSVWriter(new FileWriter(IConstants.DATA_PHASE3 + File.separator+IConstants.TASK2+File.separator+IConstants.QUERY_GESTURE), ',',CSVWriter.NO_QUOTE_CHARACTER,CSVWriter.DEFAULT_LINE_END);
				
			for (int i = 0; i < keyset.size(); i++) {
				String[] array = new String[1];
				array[0]=String.valueOf(sum.get(i)/factor);  // i acting as a key , it's NATURAL ORDER of Java Reading Files
				csvWriter.writeNext(array);
			}
			csvWriter.close();	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private List<LinkedHashMap<Integer, Double>> tranform() throws IOException {
		// TODO Auto-generated method stub
		
		//this stores score of query against all the documents , outer list corresponds to components  , in the end we iterate and sum all of them to combine
		List<LinkedHashMap<Integer, Double>> scoresPerQueryComponent = new ArrayList<LinkedHashMap<Integer,Double>>();
		
		//why sampleInputDirectory  ?  we are storing dictMap fro Query Component Dictionary, and key is sample as sample database
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
			
			Map<Integer, Set<String>> queryWordMap = main.createWordsPerSensor(queryDictionary ); // always 20 length
			
			//this will 1 * N  for the query  where N = no of words, and 1 for only one query document
			List<Map<String, Double[]>> queryWordScores = main.createSensorWordScores(queryWordMap, queryDictionary , 3); // 3 for TF-IDF
			

			String svdSematicDir  = IConstants.DATA + File.separator + IConstants.SVD_SEMANTICS + File.separator + component;
			String svdSemanticOpDir = IConstants.DATA_PHASE3 + File.separator+IConstants.TASK2+ File.separator + IConstants.SVD_MAPPED + File.separator + component;
			if(!Utils.isDirectoryCreated(svdSemanticOpDir));
			//transform query data
			List<String[]> svdQueryTransform = DriverMain.mapQueryToLSASpace(svdSematicDir, svdSemanticOpDir, queryWordScores); 
			String svdTransformDirectory = "." + File.separator + IConstants.DATA + File.separator + IConstants.SVD_TRANSFORM + File.separator + component;
			File svdTransformedDirFile = new File(svdTransformDirectory);
			List<List<String[]>> svdTrasnsformData = Utils.convertDataForComparison(svdTransformDirectory, svdTransformedDirFile.listFiles());
			String svdTransformToFileDirectory = "." + File.separator + IConstants.DATA_PHASE3  + File.separator +IConstants.TASK2+ File.separator+ IConstants.SVD_TRANSFORM_GESTURE + File.separator + component;
			Utils.writeListOfListToDir(svdTrasnsformData, svdTransformToFileDirectory, Utils.getCSVFiles(new File(sampleInputDirectory+File.separator+component)));  
			
			
			LinkedHashMap<Integer, Double> output = DriverMain.searchForSimilarLSA(svdQueryTransform, svdTrasnsformData);
			
			
			System.out.println("Top 5 documents in SVD semantics are as follows:");
			DriverMain.displayMapResults(output, Utils.getCSVFiles(new File(sampleInputDirectory+File.separator+component)));
			
			
			
			scoresPerQueryComponent.add(output);
			
			
			
		}	
		return scoresPerQueryComponent;
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
					if(arg0.getName().endsWith(".csv")&&!arg0.getName().contains("gaussian")) // bad bad code ,problem with gaussian.csv, it's not getting deleted
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
	
	public static void performLocalitySensitiveHashing(MatlabProxy proxy,Integer k,Integer l,Integer gestureCount) throws IOException{
		
		String inputQueryFile = new File(IConstants.DATA_PHASE3 + File.separator+IConstants.TASK2+File.separator+IConstants.QUERY_GESTURE).getAbsolutePath();
		String svdFile = new File(IConstants.DATA+File.separator+IConstants.SVD_GG_COMBINED+File.separator+IConstants.GG_SVD_FILE_NAME).getAbsolutePath();

		String outputPath = IConstants.DATA_PHASE3 + File.separator+IConstants.TASK2+File.separator+"lsaResults.csv";
		
		outputPath = new File(outputPath).getAbsolutePath();
		try {

			//copy both files
			FileUtils.copyFileToDirectory(new File(inputQueryFile), new File(matlabScriptLoc));
			FileUtils.copyFileToDirectory(new File(svdFile), new File(matlabScriptLoc));
			
			System.out.println("final_lsh("+ k  +","+l+",'"+ outputPath +"',"+gestureCount+")"); 
			proxy.eval("final_lsh("+ k  +","+l+",'"+ outputPath +"',"+gestureCount+")");
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		getMatchedFileNames(outputPath);
	}	

	public static List<String> getMatchedFileNames(String outputPath) throws IOException{ 
		String fileString = FileUtils.readFileToString(new File(outputPath));
		
		List<String> fileNames = new ArrayList<String>();
		
		System.out.println(fileString);
		
		if(fileString.length()==0){
			System.out.println("There are not matched files, Please try agian with different value of k and l");
			return fileNames;
		}
		String[] matchedFilesIndex = fileString.split(",");
		for (int i = 0; i < matchedFilesIndex.length; i++) {
			System.out.println(matchedFilesIndex[i]);
		}
		

				sampleInputDirectory=(sampleInputDirectory==null)?"C:\\Users\\paddy\\Desktop\\sampleData":sampleInputDirectory;
				
				File[] list = Utils.getCSVFiles(new File(sampleInputDirectory+File.separator+'X')); //assuming there will x
			
				for (int i = 0; i < matchedFilesIndex.length; i++) {
						matchedFilesIndex[i]=matchedFilesIndex[i].replaceAll("\"", "");
						matchedFilesIndex[i]=matchedFilesIndex[i].replaceAll("\n", "");
					System.out.println("Matched File is "+ list[Integer.parseInt(matchedFilesIndex[i])-1].getName());
					fileNames.add(list[Integer.parseInt(matchedFilesIndex[i])-1].getName());
				}
				return fileNames;
	}
	
	
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException, IOException {	
		MatlabProxyFactory factory = new MatlabProxyFactory();
		proxy = factory.getProxy();
		matlabScriptLoc = "." + File.separator + "MatlabScripts";
		String path = "cd(\'" + matlabScriptLoc + "')";
		proxy.eval(path);
	
		performLocalitySensitiveHashing(proxy, 8, 10,80);		
	}
	
	
}
