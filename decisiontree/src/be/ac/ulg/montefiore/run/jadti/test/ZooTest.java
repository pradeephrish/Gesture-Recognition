/* jaDTi package - v0.6.1 */

package be.ac.ulg.montefiore.run.jadti.test;

import be.ac.ulg.montefiore.run.jadti.*;
import be.ac.ulg.montefiore.run.jadti.io.*;
import java.io.*;
import java.util.*;


/*
 * A short example program of the jaDTi library.
 */
public class ZooTest {

    //final static String dbFileName = "resources/zoo.db";
	final static String dbFileName = "resources/traindb.db";
	final static String dbFileNameTest = "resources/testdb.db";
    final static String jadtiURL = "http://www.run.montefiore.ulg.ac.be/" +
	"~francois/software/jaDTi/";
    
    
    static public void main(String[] args)
	throws IOException {
	
	ItemSet learningSet = null;
	ItemSet testSet = null;
	try {
	    learningSet = ItemSetReader.read(new FileReader(dbFileName));
	    testSet = ItemSetReader.read(new FileReader(dbFileNameTest));
	}
	catch(FileNotFoundException e) {
	    System.err.println("File not found : " + dbFileName + ".");
	    System.err.println("This file is included in the source " +
			       "distribution of jaDti.  You can find it at " +
			       jadtiURL);
	    System.exit(-1);
	}
	
	AttributeSet attributeSet = learningSet.attributeSet();
	
	Vector testAttributesVector = new Vector();
	List<Attribute> attributeList = attributeSet.addByName("attr");
//	for(Attribute att : attributeList){
//		testAttributesVector.add(att);
//	}
	
	testAttributesVector.add(attributeSet.findByName("attr1"));
	testAttributesVector.add(attributeSet.findByName("attr2"));
	testAttributesVector.add(attributeSet.findByName("attr3"));
	testAttributesVector.add(attributeSet.findByName("attr4"));
	testAttributesVector.add(attributeSet.findByName("attr5"));
	testAttributesVector.add(attributeSet.findByName("attr6"));
	testAttributesVector.add(attributeSet.findByName("attr7"));
	testAttributesVector.add(attributeSet.findByName("attr8"));
	testAttributesVector.add(attributeSet.findByName("attr9"));
	testAttributesVector.add(attributeSet.findByName("attr10"));
	testAttributesVector.add(attributeSet.findByName("attr11"));
	testAttributesVector.add(attributeSet.findByName("attr12"));
	testAttributesVector.add(attributeSet.findByName("attr13"));
	testAttributesVector.add(attributeSet.findByName("attr14"));
	testAttributesVector.add(attributeSet.findByName("attr15"));
	testAttributesVector.add(attributeSet.findByName("attr16"));
	testAttributesVector.add(attributeSet.findByName("attr17"));
	testAttributesVector.add(attributeSet.findByName("attr18"));
	testAttributesVector.add(attributeSet.findByName("attr19"));
	testAttributesVector.add(attributeSet.findByName("attr20"));
	testAttributesVector.add(attributeSet.findByName("attr21"));
	testAttributesVector.add(attributeSet.findByName("attr22"));
	testAttributesVector.add(attributeSet.findByName("attr23"));
	testAttributesVector.add(attributeSet.findByName("attr24"));
	testAttributesVector.add(attributeSet.findByName("attr25"));
	testAttributesVector.add(attributeSet.findByName("attr26"));
	testAttributesVector.add(attributeSet.findByName("attr27"));
	testAttributesVector.add(attributeSet.findByName("attr28"));
	testAttributesVector.add(attributeSet.findByName("attr29"));
	testAttributesVector.add(attributeSet.findByName("attr30"));
	testAttributesVector.add(attributeSet.findByName("attr31"));
	testAttributesVector.add(attributeSet.findByName("attr32"));
	testAttributesVector.add(attributeSet.findByName("attr33"));
	testAttributesVector.add(attributeSet.findByName("attr34"));
	testAttributesVector.add(attributeSet.findByName("attr35"));
	testAttributesVector.add(attributeSet.findByName("attr36"));
	testAttributesVector.add(attributeSet.findByName("attr37"));
	testAttributesVector.add(attributeSet.findByName("attr38"));
	testAttributesVector.add(attributeSet.findByName("attr39"));
	testAttributesVector.add(attributeSet.findByName("attr40"));
	testAttributesVector.add(attributeSet.findByName("attr41"));
	testAttributesVector.add(attributeSet.findByName("attr42"));
	testAttributesVector.add(attributeSet.findByName("attr43"));
	testAttributesVector.add(attributeSet.findByName("attr44"));
	testAttributesVector.add(attributeSet.findByName("attr45"));
	testAttributesVector.add(attributeSet.findByName("attr46"));
	testAttributesVector.add(attributeSet.findByName("attr47"));
	testAttributesVector.add(attributeSet.findByName("attr48"));
	testAttributesVector.add(attributeSet.findByName("attr49"));
	testAttributesVector.add(attributeSet.findByName("attr50"));
	testAttributesVector.add(attributeSet.findByName("attr51"));
	testAttributesVector.add(attributeSet.findByName("attr52"));
	testAttributesVector.add(attributeSet.findByName("attr53"));
	testAttributesVector.add(attributeSet.findByName("attr54"));
	testAttributesVector.add(attributeSet.findByName("attr55"));
	testAttributesVector.add(attributeSet.findByName("attr56"));
	testAttributesVector.add(attributeSet.findByName("attr57"));
	testAttributesVector.add(attributeSet.findByName("attr58"));
	testAttributesVector.add(attributeSet.findByName("attr59"));
	testAttributesVector.add(attributeSet.findByName("attr60"));

	
	AttributeSet testAttributes = new AttributeSet(testAttributesVector);
	SymbolicAttribute goalAttribute =
	    (SymbolicAttribute) learningSet.attributeSet().findByName("label");

	DecisionTree tree = buildTree(learningSet, testAttributes,
				      goalAttribute);
	System.out.println("something");
	printDot(tree);
	System.out.println("something");
	for(double i = 0; i < testSet.size(); i++){
		printGuess(testSet.item((int)i), tree);
	}
	
	//printGuess(learningSet.item(1), tree);
    }

    
    /*
     * Build the decision tree.
     */
    static private DecisionTree buildTree(ItemSet learningSet, 
					  AttributeSet testAttributes, 
					  SymbolicAttribute goalAttribute) {
	DecisionTreeBuilder builder = 
	    new DecisionTreeBuilder(learningSet, testAttributes,
				    goalAttribute);
	
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
	
	KnownSymbolicValue goalAttributeValue = 
	    (KnownSymbolicValue) item.valueOf(itemAttributes, goalAttribute);
	KnownSymbolicValue guessedGoalAttributeValue = 
	    tree.guessGoalAttribute(item);

//	String s = "Item goal attribute value is " +
//	    goalAttribute.valueToString(goalAttributeValue) + "\n";
//	
	String s = "The value guessed by the tree is " + 
	    tree.getGoalAttribute().valueToString(guessedGoalAttributeValue);
	
	System.out.println(s);
    }
}
