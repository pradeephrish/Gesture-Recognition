package com.asu.mwdb.phase3.task3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import org.apache.commons.io.FileUtils;

import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.phase3.task3.decisiontree.io.DecisionTreeToDot;
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

public class DecisionTreeClassification {

	public static void dtClassifyMatlab(MatlabProxy proxy,
			String databaseDirectory, String gesturesLabels, File[] fileNames,
			List<String> testDataFiles) throws MatlabInvocationException,
			IOException {
		String trainingFile = IConstants.DATA + File.separator
				+ IConstants.TRAINING_FILE_NAME;
		String labelsFile = IConstants.DATA + File.separator
				+ IConstants.LABELS_FILE_NAME;
		String testingFile = IConstants.DATA + File.separator
				+ IConstants.TESTING_FILE_NAME;
		String tempOutputPath = IConstants.DATA + File.separator
				+ IConstants.OUTPUT_LABELS_TEMP_DT;
		String dtOutputPath = IConstants.DATA + File.separator
				+ IConstants.OUTPUT_LABELS_DT;
		String arg = "DecisionTreeClassifier('"
				+ new File(trainingFile).getAbsolutePath() + "','"
				+ new File(testingFile).getAbsolutePath() + "','"
				+ new File(labelsFile).getAbsolutePath() + "','"
				+ new File(tempOutputPath).getAbsolutePath() + "')";
		proxy.eval(arg);

		List<String> tempLabels = FileUtils.readLines(new File(tempOutputPath));
		CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(
				new FileOutputStream(dtOutputPath)), ',',
				CSVWriter.NO_QUOTE_CHARACTER);
		List<String[]> finalLabels = new ArrayList<String[]>();
		int i = 0;
		for (String testFile : testDataFiles) {
			String[] line = new String[2];
			line[0] = testFile;
			line[1] = tempLabels.get(i++);
			finalLabels.add(line);
		}
		csvWriter.writeAll(finalLabels);
		csvWriter.close();

	}

	public static void dtClassify(String testDatFile, String trainingDataFile)
			throws FileFormatException, IOException {
		ItemSet learningSet = null;
		ItemSet testSet = null;
		
		try {
			learningSet = ItemSetReader.read(new FileReader(trainingDataFile));
		} catch (FileNotFoundException e) {
			System.err.println("File not found : " + trainingDataFile + ".");
			System.exit(-1);
		}

		try {
			testSet = ItemSetReader.read(new FileReader(testDatFile));

		} catch (FileNotFoundException e) {
			System.err.println("File not found : " + testDatFile + ".");
			System.exit(-1);
		}

		AttributeSet attributes = learningSet.attributeSet();

		Vector testAttributesVector = new Vector();
		List<Attribute> attributeList = attributes.addByName("attr");
		for (Attribute att : attributeList) {
			testAttributesVector.add(att);
		}

		AttributeSet testAttributes = new AttributeSet(testAttributesVector);
		SymbolicAttribute goalAttribute = (SymbolicAttribute) learningSet
				.attributeSet().findByName("label");

		DecisionTree tree = buildTree(learningSet, testAttributes,
				goalAttribute);
		System.out.println("Decision tree constructed is as follows:");
		printDot(tree);
		System.out.println("Labels guessed by decision tree are as followed:");
		for (double i = 0; i < testSet.size(); i++) {
			printGuess(testSet.item((int) i), tree);
		}
	}

	/*
	 * Build the decision tree.
	 */
	static private DecisionTree buildTree(ItemSet learningSet,
			AttributeSet testAttributes, SymbolicAttribute goalAttribute) {
		DecisionTreeBuilder builder = new DecisionTreeBuilder(learningSet,
				testAttributes, goalAttribute);

		return builder.build().decisionTree();
	}

	/*
	 * Prints a dot file content depicting a tree.
	 */
	static private void printDot(DecisionTree tree) {
		System.out.println((new DecisionTreeToDot(tree)).produce());
	}

	/*
	 * Prints an item's guessed goal attribute value.
	 */
	static private void printGuess(Item item, DecisionTree tree) {
		AttributeSet itemAttributes = tree.getAttributeSet();
		SymbolicAttribute goalAttribute = tree.getGoalAttribute();

		KnownSymbolicValue guessedGoalAttributeValue = tree
				.guessGoalAttribute(item);

		String s = "The value guessed by the tree is "
				+ tree.getGoalAttribute().valueToString(
						guessedGoalAttributeValue);

		System.out.println(s);
	}

}
