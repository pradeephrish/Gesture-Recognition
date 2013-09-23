package com.asu.mwdb.setup;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.asu.mwdb.loggers.MyLogger;

public class SetupSystem {

	public String sampleDataLoc;
	public String matlabScriptLoc;
	private Logger logger = new MyLogger().getupLogger();
        
        
     
	public SetupSystem(String fileLocation) throws IOException {
                this.sampleDataLoc = fileLocation;
		setFileLoc();

		logger.info("Sample data Loc : " + sampleDataLoc);
		{
                        File outputDirectory = new File(sampleDataLoc + "/OUTPUTP1");
                        if(!outputDirectory.exists()){
                            outputDirectory.mkdir();
                        }else{
                            delete(outputDirectory);
                            outputDirectory.mkdir();
                        }
                    
			File normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize");
			if (!normalizeDirectory.exists()) {
				normalizeDirectory.mkdir();
			} else {
				logger.info("Normalize directory is not empty, deleting it.");
				delete(normalizeDirectory);
				normalizeDirectory.mkdir();
			}
			normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/W");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/X");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/Y");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/Z");
			normalizeDirectory.mkdir();
		}

		{
			File letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter");
			if (!letterDirectory.exists()) {
				letterDirectory.mkdir();
			} else {
				logger.info("Normalize directory is not empty, deleting it.");
				delete(letterDirectory);
				letterDirectory.mkdir();
			}
			letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/W");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/X");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/Y");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/Z");
			letterDirectory.mkdir();
		}

		
		
		{
			File task1Directory = new File(sampleDataLoc + "/OUTPUTP1/task1");
			if (!task1Directory.exists()) {
				task1Directory.mkdir();
			} else {
				logger.info("Task1 directory is not empty, deleting it.");
				delete(task1Directory);
				task1Directory.mkdir();
			}
			task1Directory = new File(sampleDataLoc + "/OUTPUTP1/task1/W");
			task1Directory.mkdir();
			task1Directory = new File(sampleDataLoc + "/OUTPUTP1/task1/X");
			task1Directory.mkdir();
			task1Directory = new File(sampleDataLoc + "/OUTPUTP1/task1/Y");
			task1Directory.mkdir();
			task1Directory = new File(sampleDataLoc + "/OUTPUTP1/task1/Z");
			task1Directory.mkdir();
		}
		
		{
			File task2Directory = new File(sampleDataLoc + "/OUTPUTP1/task2");
			if (!task2Directory.exists()) {
				task2Directory.mkdir();
			} else {
				logger.info("Task2 directory is not empty, deleting it.");
				delete(task2Directory);
				task2Directory.mkdir();
			}
			task2Directory = new File(sampleDataLoc + "/OUTPUTP1/task2/W");
			task2Directory.mkdir();
			task2Directory = new File(sampleDataLoc + "/OUTPUTP1/task2/X");
			task2Directory.mkdir();
			task2Directory = new File(sampleDataLoc + "/OUTPUTP1/task2/Y");
			task2Directory.mkdir();
			task2Directory = new File(sampleDataLoc + "/OUTPUTP1/task2/Z");
			task2Directory.mkdir();
		}
		
		{
			File rangeFile = new File(sampleDataLoc + "/OUTPUTP1/"+File.separator+"rangeBandFile.csv");
			if(rangeFile.exists()){
				rangeFile.delete();
			}
		}
	}

	public void setFileLoc() {
		Properties prop = new Properties();
		try {
			// load a properties file
			prop.load(new FileInputStream(".\\config\\config.properties"));

			// get the property value and print it out
//			sampleDataLoc = prop.getProperty("SampleDataLoc");   // Passed By UI
                        
			matlabScriptLoc = prop.getProperty("MatlabScriptLoc");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void delete(File file) throws IOException {
		if (file.isDirectory()) {
			// directory is empty, then delete it
			if (file.list().length == 0) {
				file.delete();
			} else {
				// list all the directory contents
				String files[] = file.list();
				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					delete(fileDelete);
				}
				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
				}
			}
		} else {
			// if file, then delete it
			file.delete();
		}
	}
}
