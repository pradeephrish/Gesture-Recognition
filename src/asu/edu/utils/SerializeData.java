package asu.edu.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import asu.edu.math.ConstructGestureWords;

public class SerializeData {
	
	private static void serialize(String outputFileName,Object object) throws IOException{
		FileOutputStream fileOutputStream = new FileOutputStream(outputFileName);
		ObjectOutputStream objectOutputStream  = new ObjectOutputStream(fileOutputStream);
		objectOutputStream.writeObject(object);
	}
	
	
	
	private  static Object deserialize(String inputFileName) throws ClassNotFoundException, IOException{
		FileInputStream fileInputStream = new FileInputStream(inputFileName);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		
		return objectInputStream.readObject();
	
	
	}
	
    
}
