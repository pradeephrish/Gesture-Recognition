package com.asu.mwdb.setup;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.asu.mwdb.loggers.MyLogger;
import java.util.List;

public class CreateFileStructure {

	public String inputDataLocation;
	public String matlabScriptLocation;
	private Logger logger = new MyLogger().getupLogger();
        
	public CreateFileStructure(String fileLocation,List<File> directories) throws IOException {
                this.inputDataLocation = fileLocation;
		setUpFileStructure();

		logger.info("Sample data Loc : " + inputDataLocation);
		{
                        File outputDirectory = new File(inputDataLocation + "/OUTPUTP1");
                        if(!outputDirectory.exists()){
                            outputDirectory.mkdir();
                        }else{
                            delete(outputDirectory);
                            outputDirectory.mkdir();
                        }
                    
			File normalizeDirectory = new File(inputDataLocation + "/OUTPUTP1/normalize");
			if (!normalizeDirectory.exists()) {
				normalizeDirectory.mkdir();
			} else {
				logger.info("Normalize directory is not empty, deleting it.");
				delete(normalizeDirectory);
				normalizeDirectory.mkdir();
			}
                        
                        for (int i = 0; i < directories.size(); i++) {
                            normalizeDirectory = new File(inputDataLocation + "/OUTPUTP1/normalize/"+directories.get(i).getName());
                            normalizeDirectory.mkdir();
                         }
		}

		{
			File letterDirectory = new File(inputDataLocation + "/OUTPUTP1/letter");
			if (!letterDirectory.exists()) {
				letterDirectory.mkdir();
			} else {
				logger.info("Normalize directory is not empty, deleting it.");
				delete(letterDirectory);
				letterDirectory.mkdir();
			}
                        
                        for (int i = 0; i < directories.size(); i++) {
                            letterDirectory = new File(inputDataLocation + "/OUTPUTP1/letter/"+directories.get(i).getName());
                            letterDirectory.mkdir();
                         }
		}
		
		{
			File rangeFile = new File(inputDataLocation + "/OUTPUTP1/"+File.separator+"rangeBandFile.csv");
			if(rangeFile.exists()){
				rangeFile.delete();
			}
		}
	}

	public void setUpFileStructure() {
		Properties prop = new Properties();
		try {
			// load a properties file
			prop.load(new FileInputStream(".\\config\\config.properties"));
			matlabScriptLocation = prop.getProperty("MatlabScriptLoc");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getScriptLocation() {
		Properties prop = new Properties();
		try {
			// load a properties file
			prop.load(new FileInputStream(".\\config\\config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return prop.getProperty("MatlabScriptLoc");
	}

	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					delete(fileDelete);
				}
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			file.delete();
		}
	}
}
