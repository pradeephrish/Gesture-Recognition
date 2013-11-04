package com.asu.mwdb.phase2Main;

import java.io.File;
import java.io.IOException;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.asu.mwdb.utils.IConstants;

public class NormalizeData {

	public static void NormalizeTask1Data(MatlabProxy proxy,
			String inputDirectory) throws IOException, MatlabInvocationException {
		System.out.println("Normalizing files for " + inputDirectory);
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
					proxy.eval("normalize_p1('" + inputFileName + "','"
							+ normalizedFile + "')");
				}
			}
		} else {
			System.out.println("Error while creating directory for normalized files, task not completed");
		}
	}
	
	public static void NormalizeDataForSingleFile(MatlabProxy proxy,
			String inputFileLocation) throws MatlabInvocationException {
		int position = inputFileLocation.lastIndexOf(File.separator);
		if (position > 0) {
			String currentDirectory = inputFileLocation.substring(0, position);
			String normalizedOutputFile = currentDirectory + File.separator
					+ IConstants.NORMALIZED_FILE + ".csv";
			proxy.eval("normalize_p1('" + inputFileLocation + "','"
					+ normalizedOutputFile + "')");
		} else {
			System.out.println("Error occurred while reading input directory, task not completed");
		}
	}
}
