package com.asu.mwdb.task1b;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import com.asu.mwdb.utils.IConstants;


import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

public class NormalizeData {

	private static Logger logger = new MyLogger().getupLogger();

	public static void NormalizeTask1Data(MatlabProxy proxy,
			String inputDirectory) throws IOException, MatlabInvocationException {
		logger.info("Starting normalization for Task 1");
		File file = new File(inputDirectory);
		String[] filesList = file.list();
		String outputDirectory = inputDirectory + File.separator + IConstants.NORMALIZED_FILE;
		File outputDirectoryFileObject = new File(outputDirectory);
		if (outputDirectoryFileObject.exists()) {
			FileIOHelper.delete(outputDirectoryFileObject);
		}
		if (outputDirectoryFileObject.mkdir()) {
			for (int i = 0; i < filesList.length; i++) {
				if (filesList[i].contains("csv")) {
					String normalizedFile = outputDirectory + File.separator + filesList[i];
					String inputFileName = inputDirectory + File.separator + filesList[i];
					proxy.eval("normalize('" + inputFileName + "','"
							+ normalizedFile + "')");
				}
			}
		} else {
			logger.info("Error while creating directory for normalized files, task not completed");
		}
	}
	
	public static void NormalizeDataForSingleFile(MatlabProxy proxy,
			String inputFileLocation) throws MatlabInvocationException {
		int position = inputFileLocation.lastIndexOf(File.separator);
		if (position > 0) {
			String currentDirectory = inputFileLocation.substring(0, position);
			String normalizedOutputFile = currentDirectory + File.separator
					+ IConstants.NORMALIZED_FILE + ".csv";
			proxy.eval("normalize('" + inputFileLocation + "','"
					+ normalizedOutputFile + "')");
		} else {
			logger.info("Error occurred while reading input directory, task not completed");
		}
	}
}
