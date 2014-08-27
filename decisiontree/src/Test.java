import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import be.ac.ulg.montefiore.run.jadti.AttributeSet;
import be.ac.ulg.montefiore.run.jadti.DecisionTree;
import be.ac.ulg.montefiore.run.jadti.DecisionTreeBuilder;
import be.ac.ulg.montefiore.run.jadti.FileFormatException;
import be.ac.ulg.montefiore.run.jadti.Item;
import be.ac.ulg.montefiore.run.jadti.ItemSet;
import be.ac.ulg.montefiore.run.jadti.KnownSymbolicValue;
import be.ac.ulg.montefiore.run.jadti.SymbolicAttribute;
import be.ac.ulg.montefiore.run.jadti.io.ItemSetReader;


//http://www.run.montefiore.ulg.ac.be/~francois/software/jaDTi/example/

public class Test {
	public static void main(String[] args) throws FileFormatException, FileNotFoundException, IOException {
		String dbFileName = "resources/zoo.db";
	    ItemSet learningSet = ItemSetReader.read(new FileReader(dbFileName));
	    AttributeSet attributeSet = (AttributeSet) learningSet.attributeSet();
	    
	    
	    Vector testAttributesVector = new Vector();
	     testAttributesVector.add(((be.ac.ulg.montefiore.run.jadti.AttributeSet) attributeSet).findByName("legs"));
	     testAttributesVector.add(((be.ac.ulg.montefiore.run.jadti.AttributeSet) attributeSet).findByName("tail"));

	     AttributeSet testAttributes = new AttributeSet(testAttributesVector); 
	     SymbolicAttribute goalAttribute =(SymbolicAttribute) learningSet.attributeSet().findByName("type");
	    DecisionTreeBuilder builder = new DecisionTreeBuilder(learningSet, testAttributes, goalAttribute);
	    DecisionTree tree = builder.build().decisionTree();
	    
	    
	    Item item = learningSet.item(2);
	    KnownSymbolicValue guessedGoalAttributeValue = tree.guessGoalAttribute(item);
	     
	    System.out.println(goalAttribute.valueToString(guessedGoalAttributeValue));
	    
	    
	    
	}
}
