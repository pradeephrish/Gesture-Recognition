package com.asu.mwdb.setup;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import com.asu.mwdb.loggers.MyLogger;
import java.util.List;

public class SetupSystem {

	public String sampleDataLoc;
	public String matlabScriptLoc;
	private Logger logger = new MyLogger().getupLogger();
        
        
     
	public SetupSystem(String fileLocation,List<File> directories) throws IOException {
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
                        
                        for (int i = 0; i < directories.size(); i++) {
                            normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/"+directories.get(i).getName());
                            normalizeDirectory.mkdir();
                         }
                        
			/*normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/W");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/X");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/Y");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/OUTPUTP1/normalize/Z");
			normalizeDirectory.mkdir();*/
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
                        
                        for (int i = 0; i < directories.size(); i++) {
                            letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/"+directories.get(i).getName());
                            letterDirectory.mkdir();
                         }
                        
                        
			/*letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/W");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/X");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/Y");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/OUTPUTP1/letter/Z");
			letterDirectory.mkdir();*/
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
			matlabScriptLoc = prop.getProperty("MatlabScriptLoc");
		} catch (IOException e) {
			e.printStackTrace();
		}
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
