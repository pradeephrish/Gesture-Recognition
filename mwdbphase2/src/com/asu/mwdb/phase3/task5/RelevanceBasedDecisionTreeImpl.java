package com.asu.mwdb.phase3.task5;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.Set;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.phase2Main.AssignBandValues;
import com.asu.mwdb.phase2Main.DictionaryBuilderPhase2;
import com.asu.mwdb.phase2Main.DriverMain;
import com.asu.mwdb.phase2Main.NormalizeData;
import com.asu.mwdb.phase2Main.SearchDatabaseForSimilarity;
import com.asu.mwdb.phase3.task3.DecisionTreeClassification;
import com.asu.mwdb.phase3.task3.decisiontree.io.ItemSetReader;
import com.asu.mwdb.phase3.task3.decisiontree.misc.Attribute;
import com.asu.mwdb.phase3.task3.decisiontree.misc.AttributeSet;
import com.asu.mwdb.phase3.task3.decisiontree.misc.DecisionTree;
import com.asu.mwdb.phase3.task3.decisiontree.misc.DecisionTreeBuilder;
import com.asu.mwdb.phase3.task3.decisiontree.misc.FileFormatException;
import com.asu.mwdb.phase3.task3.decisiontree.misc.Item;
import com.asu.mwdb.phase3.task3.decisiontree.misc.ItemSet;
import com.asu.mwdb.phase3.task3.decisiontree.misc.KnownSymbolicValue;
import com.asu.mwdb.phase3.task3.decisiontree.misc.SymbolicAttribute;
import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Phase2Utils;
import com.asu.mwdb.utils.Utils;

public class RelevanceBasedDecisionTreeImpl {
	private static MatlabProxy proxy;
	private static Integer wordLength;
	private static Integer shiftLength;
	private static String matlabScriptLoc;
	private static Map<String, DictionaryBuilderPhase2> dictMap;
	private static double rBandValueRange[][];
	private String gestureInputDirectory;
	private static String sampleInputDirectory;
	private Map<String,List<List<Map<String, List<Double>>>>> dictMapOfQuery = new HashMap<String, List<List<Map<String, List<Double>>>>>();
	private boolean userChoiceContinue;
	private static List<String> componentOrder  = new ArrayList<String>();
	

	public RelevanceBasedDecisionTreeImpl(Map<String, DictionaryBuilderPhase2> dictMap,Integer wordLength,Integer shiftLenght,String matlabScriptLoc,double rBandValueRange[][],MatlabProxy proxy,String gestureInputDirectory,String sampleInputDirectory){
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
			// Outer list corresponds to X, Y , W
			List<LinkedHashMap<Integer, Double>> scoresPerQueryComponent = tranform(); //this will transform all query documents using SVD principle components
			LinkedHashMap<Integer, Double> sum = addAll(scoresPerQueryComponent);
			
			//sort by value
			sum=SearchDatabaseForSimilarity.sortHashMapByValuesD(sum);
			
			System.out.println("On Summed Data");
			DriverMain.displayMapResults(sum,Utils.getCSVFiles(new File(sampleInputDirectory+File.separator+"X")));
			
			
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter k :");
			Integer k = Integer.valueOf(br.readLine());
			
			relvanceFeedbackDecisionTree(k,sum,scoresPerQueryComponent,Utils.getCSVFiles(new File(sampleInputDirectory+File.separator+"X")),null,new ArrayList<Integer>());
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Indexing failed for the given input directory "+gestureInputDirectory);
		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void relvanceFeedbackDecisionTree(Integer k,
			LinkedHashMap<Integer, Double> sum,
			List<LinkedHashMap<Integer, Double>> scoresPerQueryComponent,File[] files,HashMap<Integer,String> labels,List<Integer> trainingfileOrder) {
		// TODO Auto-generated method stub
		//show top k results
		int counter = 0;
		if(labels==null)
			labels = getRelevancyInput(sum,files,k);

		//construct training data format for decision tree
		CSVWriter csvWriter = null;
		String inputDT = IConstants.DATA + File.separator + IConstants.DT_OP_DIR + File.separator +"task5train.db";
		try {
			csvWriter = new CSVWriter(new FileWriter(inputDT), ' ',CSVWriter.NO_QUOTE_CHARACTER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String input[] = new String[(scoresPerQueryComponent.size()+1)*2];
		List<String[]> listInput = new ArrayList<String[]>();
		
		int i=0;
		System.out.println();
		int attr =1;
		for (; i < scoresPerQueryComponent.size()*2; i=i+2) {
			input[i]="attr"+(attr++);
			input[i+1]=" numerical "; 
		}
		input[i]="label";
		input[i+1]=" symbolic";
		listInput.add(input);
		
		Iterator<Entry<Integer, Double>> iterator = sum.entrySet().iterator();
		int count = 0;
		while(iterator.hasNext()){
			if(count < k){
			Entry<Integer, Double> entry = iterator.next(); //file name as a key , use this to fetch value from X,y etc Components 
			Integer index = entry.getKey();
			
			//save file order  - save file[] indexes4
			trainingfileOrder.add(index);
			//done 
			
			String inputRow[]=new String[scoresPerQueryComponent.size()+1];
			int j = 0;
			for (; j < scoresPerQueryComponent.size(); j++) {  //iterate over components
				inputRow[j]=Double.toString(scoresPerQueryComponent.get(j).get(index));
			}
			if(labels.get(index)!=null){
				inputRow[j]=labels.get(index);
			}
			else{
				inputRow[j]="n";
			}
			listInput.add(inputRow);
			}else{
				break;
			}
		}
		csvWriter.writeAll(listInput);	
		try {
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<Integer> testingFileOrder = createTestingData(scoresPerQueryComponent,files,trainingfileOrder);
		
		//do decision tree job
		String inputDTTest = IConstants.DATA + File.separator + IConstants.DT_OP_DIR + File.separator +"task5testing.db";
		try {
			labels = performDTClassification(inputDTTest, inputDT, files,labels,trainingfileOrder,testingFileOrder);
		
			labels=showKResults(labels,sum,k,files);
			//show new result sort by score and relevancy
			if(userChoiceContinue)
			{
				relvanceFeedbackDecisionTree(k,sum,scoresPerQueryComponent,files,labels,trainingfileOrder);
			}else
			{
				System.out.println("Thank you !");
			}
		} catch (FileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


		
	}

	private HashMap<Integer, String> showKResults(
			HashMap<Integer, String> labels,
			LinkedHashMap<Integer, Double> sum, Integer k,File files[]) {
		// TODO Auto-generated method stub
	
		LinkedHashMap<Integer,Double> yLabelFiles = new LinkedHashMap<Integer, Double>();
		LinkedHashMap<Integer,Double> nLabelFiles = new LinkedHashMap<Integer, Double>();
		
		Integer[] keys = sum.keySet().toArray(new Integer[0]);
		for (int i = 0; i < keys.length; i++) {
			if(labels.get(keys[i]).equalsIgnoreCase("y")){
				yLabelFiles.put(keys[i], sum.get(keys[i]));
			}else{
				nLabelFiles.put(keys[i], sum.get(keys[i]));
			}
		}
		
		//sort (descending)
		yLabelFiles=SearchDatabaseForSimilarity.sortHashMapByValuesD(yLabelFiles);
		nLabelFiles=SearchDatabaseForSimilarity.sortHashMapByValuesD(nLabelFiles);
		
		int j=0;
		Iterator<Entry<Integer, Double>> yIterator = yLabelFiles.entrySet().iterator();
		while(yIterator.hasNext()){
			
			if(j < k){
			Entry<Integer, Double> entry = yIterator.next();
			System.out.println("FileName "+files[entry.getKey()]+"Score : "+entry.getValue());
			System.out.println("Relevant ?");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				String input = br.readLine();
				if(input.equalsIgnoreCase("y")){
					labels.put(entry.getKey(), "y");	
				}
				else{
					labels.put(entry.getKey(), "n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			++j;
			}
		}
		
		if(j < k){
			Iterator<Entry<Integer, Double>> nIterator = nLabelFiles.entrySet().iterator();
			while(yIterator.hasNext()){
				if(j < k){
				Entry<Integer, Double> entry = nIterator.next();
				System.out.println("FileName "+files[entry.getKey()]+"Score : "+entry.getValue());
				System.out.println("Relevant ?");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				try {
					String input = br.readLine();
					if(input.equalsIgnoreCase("y")){
						labels.put(entry.getKey(), "y");	
					}
					else{
						labels.put(entry.getKey(), "n");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				++j;
				}
			}
			
		}
		System.out.println("Do you want to continue(y/n)?");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			String user=br.readLine();
			
			if(user.equalsIgnoreCase("y")){
				userChoiceContinue=true;
			}else
			{
				userChoiceContinue=false;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return labels;
	}


	public List<Integer> createTestingData(List<LinkedHashMap<Integer, Double>> scoresPerQueryComponent,File[] files, List<Integer> fileOrder){
		
		//testing file order
		List<Integer> testingFileOrder = new ArrayList<Integer>();
		
		CSVWriter csvWriter = null;
		String inputDT = IConstants.DATA + File.separator + IConstants.DT_OP_DIR + File.separator +"task5testing.db";
		try {
			csvWriter = new CSVWriter(new FileWriter(inputDT), ' ',CSVWriter.NO_QUOTE_CHARACTER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String input[] = new String[(scoresPerQueryComponent.size()+1)*2];
		List<String[]> listInput = new ArrayList<String[]>();
		
		int i=0;
		System.out.println();
		int attr =1;
		for (; i < scoresPerQueryComponent.size()*2; i=i+2) {
			input[i]="attr"+(attr++);
			input[i+1]=" numerical "; 
		}
		input[i]="label";
		input[i+1]=" symbolic";
		listInput.add(input);
		
		
			Set<Integer> keys = scoresPerQueryComponent.get(0).keySet();
			
			//remove training key order
			for (int j = 0; j < fileOrder.size(); j++) {
				keys.remove(fileOrder.get(j)); 
			}
			
			Integer[] keyArray = keys.toArray(new Integer[0]);  // this will not contain training keys
			
			
			
			for (int j = 0; j < keyArray.length; j++) {
				String inputRow[]=new String[scoresPerQueryComponent.size()+1];
				int k=0;
				for (; k < scoresPerQueryComponent.size(); k++) {  //iterate over components
					inputRow[k]=Double.toString(scoresPerQueryComponent.get(k).get(keyArray[j]));
				}
				inputRow[k]="0";
				listInput.add(inputRow);
				testingFileOrder.add(keyArray[j]);
			}
		csvWriter.writeAll(listInput);	
		try {
			csvWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return testingFileOrder;
	}

	public static HashMap<Integer, String> performDTClassification(String testdatafile,String trainingdatafile,File[] file, HashMap<Integer, String> labels, List<Integer> trainingfileOrder, List<Integer> testingFileOrder) throws FileFormatException, IOException{
		ItemSet trainingSet = null;
		ItemSet testSet = null;
		// read the training data 
		try {
			trainingSet = ItemSetReader.read(new FileReader(trainingdatafile));
		} catch (FileNotFoundException e) {
			System.err.println("File not found : " + trainingdatafile + ".");
			System.exit(-1);
		}
		// Read the testing data
		try {
			testSet = ItemSetReader.read(new FileReader(testdatafile));
		} catch (FileNotFoundException e) {
			System.err.println("File not found : " + testdatafile + ".");
			System.exit(-1);
		}

		AttributeSet attributes = trainingSet.attributeSet();

		Vector testAttributesVector = new Vector();
		List<Attribute> attributeList = attributes.addByName("attr");
		for (Attribute att : attributeList) {
			testAttributesVector.add(att);
		}

		AttributeSet testAttributes = new AttributeSet(testAttributesVector);
		SymbolicAttribute goalAttribute = (SymbolicAttribute) trainingSet
				.attributeSet().findByName("label");

		
		DecisionTree tree = buildTree(trainingSet, testAttributes, 	goalAttribute);
		String dtOutputDirectory = IConstants.DATA + File.separator + IConstants.DT_OP_DIR;
		FileWriter dtWriter = new FileWriter(new File(dtOutputDirectory + File.separator + (resultID++)+ IConstants.DT_REL_OP_FILE));
		for (int i = 0; i < testSet.size(); i++) {
			String str = "File Name: " + file[((int) i)].getName() + ", Label: ";
			str = printGuess(str, testSet.item((int) i), tree);
			dtWriter.write(str + "\n");
			
			/*******/
			
			String label = predictLabel(testSet.item((int) i), tree);
			
			//update this label in lables corresponding to file
			labels.put(testingFileOrder.get(i), label);
			
			/******/
			
		}
		dtWriter.close();
		return labels;
	}
	
	static Integer resultID= 1;
	
	static private DecisionTree buildTree(ItemSet learningSet,
			AttributeSet testAttributes, SymbolicAttribute goalAttribute) {
		DecisionTreeBuilder builder = new DecisionTreeBuilder(learningSet,
				testAttributes, goalAttribute);

		return builder.build().decisionTree();
	}
	
	static private String printGuess(String str, Item item, DecisionTree tree) {
		AttributeSet itemAttributes = tree.getAttributeSet();
		SymbolicAttribute goalAttribute = tree.getGoalAttribute();

		KnownSymbolicValue guessedGoalAttributeValue = tree
				.guessGoalAttribute(item);

		String s = str 
				+ tree.getGoalAttribute().valueToString(
						guessedGoalAttributeValue);

		return s;
	}
	
	static private String predictLabel(Item item, DecisionTree tree) {
		AttributeSet itemAttributes = tree.getAttributeSet();
		SymbolicAttribute goalAttribute = tree.getGoalAttribute();

		KnownSymbolicValue guessedGoalAttributeValue = tree
				.guessGoalAttribute(item);

		return  tree.getGoalAttribute().valueToString(
						guessedGoalAttributeValue);
	}

	private HashMap<Integer, String> getRelevancyInput(LinkedHashMap<Integer, Double> sum, File[] files,Integer k) {
		// TODO Auto-generated method stub
		HashMap<Integer,String> labels = new HashMap<Integer,String>();
		int counter=0;
		for (Entry<Integer, Double> entry : sum.entrySet()) { 
			Integer key = entry.getKey();		    
			File file = files[key];		    
			System.out.println((counter + 1) + " - " + file.getAbsolutePath() + "        " + entry.getValue());
			System.out.println("Is this relevant(y/n)?");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				labels.put(key, br.readLine());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(counter++ == k-1) {
				break;
			}
		}
		return labels;
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
//			DriverMain.displayMapResults(output, Utils.getCSVFiles(new File(sampleInputDirectory+File.separator+component)));
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
			
			componentOrder.add(component); //maitain order of components
			
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
	
	public static void main(String[] args) throws MatlabConnectionException, MatlabInvocationException, IOException {	
//		performDTClassification("C:\\Users\\paddy\\git\\mwdb\\mwdbphase2\\data\\DT\\task5testing.db", "C:\\Users\\paddy\\git\\mwdb\\mwdbphase2\\data\\DT\\task5train.db",Utils.getFileOrder("C:\\Users\\paddy\\Desktop\\sampleData\\X"));
			
	}
	
	private LinkedHashMap<Integer, Double> addAll(List<LinkedHashMap<Integer, Double>> scoresPerQueryComponent) {
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
		return sum;
	}
	
	
	
}
