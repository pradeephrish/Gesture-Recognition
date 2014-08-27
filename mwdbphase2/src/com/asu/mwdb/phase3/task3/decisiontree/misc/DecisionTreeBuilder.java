
package com.asu.mwdb.phase3.task3.decisiontree.misc;



public class DecisionTreeBuilder
    extends SimpleDecisionTreeBuilder {
    
    public DecisionTreeBuilder(ItemSet learningSet, 
			       AttributeSet testAttributes,
			       SymbolicAttribute goalAttribute) {
	super(new WeightedItemSet(learningSet), testAttributes, goalAttribute);
    }
}
