package com.asu.mwdb.phase3.task3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Utils;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

public class DecisionTreeClassification {

	public static void dtClassify(MatlabProxy proxy, String databaseDirectory, String gesturesLabels, File[] fileNames, List<String> testDataFiles) throws MatlabInvocationException, IOException {
		String trainingFile   = IConstants.DATA + File.separator + IConstants.TRAINING_FILE_NAME;
		String labelsFile     = IConstants.DATA + File.separator + IConstants.LABELS_FILE_NAME;
		String testingFile    = IConstants.DATA + File.separator + IConstants.TESTING_FILE_NAME;
		String tempOutputPath = IConstants.DATA + File.separator + IConstants.OUTPUT_LABELS_TEMP_DT;
		String dtOutputPath  = IConstants.DATA + File.separator + IConstants.OUTPUT_LABELS_DT;
		String arg = "DecisionTreeClassifier('" + new File(trainingFile).getAbsolutePath() + "','" + new File(testingFile).getAbsolutePath() + "','"
				+ new File(labelsFile).getAbsolutePath() + "','" + new File(tempOutputPath).getAbsolutePath() + "')";
		proxy.eval(arg);
		

		List<String> tempLabels = FileUtils.readLines(new File(tempOutputPath));
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(dtOutputPath)), ',', CSVWriter.NO_QUOTE_CHARACTER);
		List<String[]> finalLabels = new ArrayList<String[]>();
		int i = 0;
		for(String testFile : testDataFiles) {
			String[] line = new String[2];
			line[0] = testFile;
			line[1] = tempLabels.get(i++);
			finalLabels.add(line);
		}
		csvWriter.writeAll(finalLabels);
		csvWriter.close();
		
	}

}
