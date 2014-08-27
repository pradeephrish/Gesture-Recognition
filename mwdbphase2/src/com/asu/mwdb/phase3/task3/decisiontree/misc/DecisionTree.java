

package com.asu.mwdb.phase3.task3.decisiontree.misc;

import java.util.*;



public class DecisionTree {
    
    private final AnchorNode anchor;
    private AttributeSet attributeSet;
    private SymbolicAttribute goalAttribute;
    
    
    /**
     * Creates a empty decision tree.
     *
     * @param attributeSet A set of attribute.  The set of attributes of the
     *                     items given to this tree.  Can be set to 'null'.
     * @param goalAttribute The goalAttribute.  Can be set to 'null'.
     **/
    public DecisionTree(AttributeSet attributeSet, 
			SymbolicAttribute goalAttribute) {
	anchor = new AnchorNode(this);
	this.attributeSet = attributeSet;
	this.goalAttribute = goalAttribute;
    }
    
    
    /**
     * Guess goal attribute value of an item.
     *
     * @param item The item compatible with the tree attribute set.
     * @return The goal attribute value, or -1 if the matching leaf node does
     *         not define a goal attribute.
     **/
    public KnownSymbolicValue guessGoalAttribute(Item item) {
	double[] distribution = goalValueDistribution(item);
	
	int index = -1;
	double max = -1.;
	
	for (int i = 0; i < distribution.length; i++)
	    if (distribution[i] > max) {
		index = i;
		max = distribution[i];
	    }
	
	return new KnownSymbolicValue(index);
    }
    
    
    /**
     * Finds the leaf/open node matching an item.  All the (tested) attributes
     * of the item must be known.
     *
     * @param item An item compatible with the tree attribute set.
     * @return The leaf node matching <code>item</code>.
     **/
    public Node leafNode(Item item) {
	if (getAttributeSet() == null || getGoalAttribute() == null)
	    throw new CannotCallMethodException("No attribute set or goal " +
						"attribute defined");
	
	AttributeSet attributeSet = getAttributeSet();
	Node node = root();
	
	while (!(node.isLeaf())) {
	    TestNode testNode = (TestNode) node;
	    
	    int testAttributeIndex =
		attributeSet.indexOf(testNode.test().attribute);
	    
	    node = testNode.
		matchingSon(item.valueOf(testAttributeIndex));
	}
	
	return node;
    }
    
    
    /**
     * Finds the goal value distribution matching an item.  This distribution
     * describes the probability of each potential goal value for this item.
     *
     * @param item An item compatible with the tree attribute set.
     * @return The goal attribute value distribution for the item
     *         <code>item</code>.
     **/
    public double[] goalValueDistribution(Item item) {
	return goalValueDistribution(item, root());
    }
    
    
    protected double[] goalValueDistribution(Item item, Node node) {
	if (node.isLeaf())
	    return ((LeafNode) node).getGoalValueDistribution();
	else
	    if (node instanceof TestNode) {
		TestNode testNode = (TestNode) node;
		
		int testAttributeIndex = 
		    attributeSet.indexOf(testNode.test().attribute);
		
		if (item.valueOf(testAttributeIndex).isUnknown()) {
		    double[] distribution = 
			new double[getGoalAttribute().nbValues];
		    
		    Arrays.fill(distribution, 0.);
		    
		    for (int i = 0; i < testNode.nbSons(); i++)
			add(distribution,
			    times(goalValueDistribution(item, testNode.son(i)),
				  testNode.son(i).weight));
		    
		    times(distribution, 1. / testNode.weight);
		    
		    return distribution;
		} else {
		    Node nextNode = 
			testNode.matchingSon(item.valueOf(testAttributeIndex));
		    
		    return goalValueDistribution(item, nextNode);
		}
	    } else
		throw new CannotCallMethodException("Open node found while " +
						    "exploring tree");
    }
    
    
    /**
     * Returns the root node.
     *
     * @return This tree's root node.
     */
    public Node root() {
	return anchor.son();
    }
    
    
    /**
     * Check if a given node is the root node.
     * 
     * @param node the node to check.
     * @return <code>true</code> iff the argument is the root node of this tree.
     **/
    public boolean isRoot(Node node) {
	if (node == null)
	    throw new IllegalArgumentException("Invalid 'null' argument");

	return (node.equals(root()));
    }
    
    
    /**
     * Change this tree's attribute set. The set of attributes of the items
     * given to this tree. 
     *
     * @param attributeSet The new attribute set. Can be set to 'null'.
     **/
    public void setAttributeSet(AttributeSet attributeSet) {
	this.attributeSet = attributeSet;
    }
    
    
    /**
     * Returns this tree's attribute set.
     *
     * @return This tree's attribute set.  A 'null' value means thaht the set is
     *         undefined.
     **/
    public AttributeSet getAttributeSet() {
	return attributeSet;
    }
    
    
    /**
     * Change this tree's goal attribute. The goal attribute is the attribute
     * guessed by the tree.  
     *
     * @param goalAttribute The new tree's goal attribute.  Can be set to
     *                      'null' if unknown.
     **/
    public void setGoalAttribute(SymbolicAttribute goalAttribute) {
	this.goalAttribute = goalAttribute;
    }
    
    
    /**
     * Get this tree's goal attribute. The goal attribute is the attribute
     * guessed by the tree.
     *
     * @return The tree's goal attribute.  Returns 'null' if unknown.
     **/
    public SymbolicAttribute getGoalAttribute() {
	return goalAttribute;
    }
    
    
    /**
     * Returns the leftmost open node of the tree.
     * 'Leftmost' means that the son chosen at each test node while
     * descending the tree is the smallest number.
     *
     * @return The leftmost open node of the tree, or <code>null</code> if the
     *         tree has no open node.
     **/
    public OpenNode openNode() {
	return anchor.openNode();
    }
    
    
    /**
     * Checks if the tree has open nodes.
     *
     * @return <code>true</code> iff the tree has open nodes.
     **/
    public boolean hasOpenNode() {
	return anchor.hasOpenNode();
    }

    
    /**
     * Returns the nodes of the tree.  The iterator returns the node according
     * to a breadth first search.  The sons of a node are returned left to
     * right (i.e. with increasing son number).
     *
     * @return An iterator over the tree's nodes.
     */
    public Iterator breadthFirstIterator() {
	return new DecisionTreeBFIterator(root());
    }

    
    private double[] times(double[] distribution, double weight) {
	for (int i = 0; i < distribution.length; i++)
	    distribution[i] *= weight;

	return distribution;
    }

    
    private double[] add(double[] d1, double[] d2) {
	if (d1.length != d2.length)
	    throw new IllegalArgumentException("distributions must have " +
					       "the same number of elements");
	
	for (int i = 0; i < d1.length; i++)
	    d1[i] += d2[i];

	return d1;
    }
}
