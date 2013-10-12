package asu.edu.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

import asu.edu.loggers.MyLogger;

public class SetupSystem {

	public String sampleDataLoc;
	public String matlabScriptLoc;
	public String r;
	public String w;
	public String s;
	public String mean;
	public String standardDeviation;
	private Logger logger = new MyLogger().getupLogger();

	public SetupSystem() throws IOException {

		setFileLoc();

		logger.info("Sample data Loc : " + sampleDataLoc);
		{
			File normalizeDirectory = new File(sampleDataLoc + "/normalize");
			if (!normalizeDirectory.exists()) {
				normalizeDirectory.mkdir();
			} else {
				logger.info("Normalize directory is not empty, deleting it.");
				delete(normalizeDirectory);
				normalizeDirectory.mkdir();
			}
			normalizeDirectory = new File(sampleDataLoc + "/normalize/W");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/normalize/X");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/normalize/Y");
			normalizeDirectory.mkdir();
			normalizeDirectory = new File(sampleDataLoc + "/normalize/Z");
			normalizeDirectory.mkdir();
		}

		{
			File letterDirectory = new File(sampleDataLoc + "/letter");
			if (!letterDirectory.exists()) {
				letterDirectory.mkdir();
			} else {
				logger.info("Normalize directory is not empty, deleting it.");
				delete(letterDirectory);
				letterDirectory.mkdir();
			}
			letterDirectory = new File(sampleDataLoc + "/letter/W");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/letter/X");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/letter/Y");
			letterDirectory.mkdir();
			letterDirectory = new File(sampleDataLoc + "/letter/Z");
			letterDirectory.mkdir();
		}

		
		
		{
			File task1Directory = new File(sampleDataLoc + "/task1");
			if (!task1Directory.exists()) {
				task1Directory.mkdir();
			} else {
				logger.info("Task1 directory is not empty, deleting it.");
				delete(task1Directory);
				task1Directory.mkdir();
			}
			task1Directory = new File(sampleDataLoc + "/task1/W");
			task1Directory.mkdir();
			task1Directory = new File(sampleDataLoc + "/task1/X");
			task1Directory.mkdir();
			task1Directory = new File(sampleDataLoc + "/task1/Y");
			task1Directory.mkdir();
			task1Directory = new File(sampleDataLoc + "/task1/Z");
			task1Directory.mkdir();
		}
		
		{
			File task2Directory = new File(sampleDataLoc + "/task2");
			if (!task2Directory.exists()) {
				task2Directory.mkdir();
			} else {
				logger.info("Task2 directory is not empty, deleting it.");
				delete(task2Directory);
				task2Directory.mkdir();
			}
			task2Directory = new File(sampleDataLoc + "/task2/W");
			task2Directory.mkdir();
			task2Directory = new File(sampleDataLoc + "/task2/X");
			task2Directory.mkdir();
			task2Directory = new File(sampleDataLoc + "/task2/Y");
			task2Directory.mkdir();
			task2Directory = new File(sampleDataLoc + "/task2/Z");
			task2Directory.mkdir();
		}
		
		{
			File rangeFile = new File(sampleDataLoc + File.separator+"rangeBandFile.csv");
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
			sampleDataLoc = prop.getProperty("SampleDataLoc");
			matlabScriptLoc = prop.getProperty("MatlabScriptLoc");
			r = prop.getProperty("r");
			w = prop.getProperty("w");
			s = prop.getProperty("s");
			mean = prop.getProperty("Mean");
			standardDeviation = prop.getProperty("StandardDeviation");
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
