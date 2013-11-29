

package com.asu.mwdb.phase3.task3.decisiontree.misc;

import java.util.*;


/**
 * A learning decision tree.  This kind of tree associates to each node
 * a learning set of elements matching this node.<p>
 * All the nodes of the tree implement the {@link LearningNode LearningNode}
 * interface.
 **/
public class LearningDecisionTree 
    extends DecisionTree {
    
    /**
     * Creates an empty learning decision tree.
     *
     * @param attributeSet A set of attribute.  The set of attributes of the
     *                     items given to this tree.  Can be set to 'null'.
     * @param goalAttribute The goalAttribute.  Can be set to 'null'.
     **/
    public LearningDecisionTree(AttributeSet attributeSet, 
				SymbolicAttribute goalAttribute,
				ItemSet learningSet) {
	super(attributeSet, goalAttribute);

	root().replace(new LearningOpenNode(0, learningSet));
    }

    
    /**
     * Returns a {@link DecisionTree decision tree} equivalent to this
     * learning tree (i.e. without learning sets).
     *
     * @return A {@link DecisionTree decision} tree.
     **/
    public DecisionTree decisionTree() {
	DecisionTree tree = 
	    new DecisionTree(getAttributeSet(), getGoalAttribute());
	
	Iterator BFIterator = breadthFirstIterator();
	LinkedList list = new LinkedList();
	list.add(tree.root());
	
	while (BFIterator.hasNext()) {
	    OpenNode openNode = (OpenNode) list.removeLast();
	    Node newNode = convertNode((Node) BFIterator.next());
	    
	    for (int i = 0; i < newNode.nbSons(); i++)
		list.addFirst(newNode.son(i));
	    
	    openNode.replace(newNode);
	}
	
	return tree;
    }
    
    
    private Node convertNode(Node node) {
	if (node instanceof LearningTestNode)
	    return convertTestNode((LearningTestNode) node);
	else if (node instanceof LearningLeafNode)
	    return convertLeafNode((LearningLeafNode) node);
	else
	    return convertOpenNode((LearningOpenNode) node);
    }
    
    
    private TestNode convertTestNode(LearningTestNode node) {
	return new ScoreTestNode(node.weight, node.test(), node.getScore());
    }
    
    
    private LeafNode convertLeafNode(LearningLeafNode node) {
	LeafNode leafNode = new LeafNode(node.learningSet().size());
	
	leafNode.setEntropy(node.learningSet().entropy(getGoalAttribute()));
	leafNode.setGoalValueDistribution(node.goalValuesDistribution());
		
	return leafNode;
    }
    
    
    private OpenNode convertOpenNode(LearningOpenNode node) {
	return new OpenNode(node.weight);
    }
}
