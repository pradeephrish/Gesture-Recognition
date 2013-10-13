package com.asu.mwdb.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.asu.mwdb.gui.MainWindow;
import com.asu.mwdb.math.ConstructGestureWords;

public class SerializeData {
	
	public static void serialize(String outputFileName,Object object) throws IOException{
		FileOutputStream fileOutputStream = new FileOutputStream(outputFileName);
		ObjectOutputStream objectOutputStream  = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(object);
	}
	
	
	
	private  static Object deserialize(String inputFileName) throws ClassNotFoundException, IOException{
		FileInputStream fileInputStream = new FileInputStream(inputFileName);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		
		return objectInputStream.readObject();
	
	
	}
	
	
	public static void main(String[] args) throws ClassNotFoundException {
		try {
		
			
			List<List<Map<String, List<Double>>>> dictionary = (List<List<Map<String, List<Double>>>>) deserialize("data/test.obj");
			
			
			MainWindow main = new MainWindow();
			
			Map<Integer, Set<String>> variable1 = main.createWordsPerSensor(dictionary);
            List<Map<String, Double[]>> computedScores = main.createSensorWordScores(variable1, dictionary);
            
            
            System.out.println("Scores of phase 2 : "+computedScores.size());
			
            //print all
            
            for (int i = 0; i < computedScores.size(); i++) {
				Iterator<Entry<String, Double[]>> iterator = computedScores.get(i).entrySet().iterator();
				
				System.out.println("***************************");
				System.out.println("\tSensor\t"+i);
            	while(iterator.hasNext()){
            		Entry<String, Double[]> entry = iterator.next();
            		System.out.println(entry.getKey()+"\t"+Arrays.toString(entry.getValue()));
            	}
            	System.out.println("***************************");
				
			}
            
            
			
//			Map map = constructGestureWords.getTfIDFMapGlobal();
			
//			serialize("data/test.obj", map);
		
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
