

package com.asu.mwdb.phase3.task3.decisiontree.misc;


/**
 * An anchor node.  Each tree as one (and only one) anchor root node which is
 * root's father; this node has thus only one son.
 **/
class AnchorNode 
    extends Node {
    
    /**
     * The tree to which this this node is linked.
     **/
    protected final DecisionTree tree;
    private Node root;
    
    
    /**
     * Creates a new anchor node.  An new open node is created an set as this
     * node's son.
     *
     * @param tree The decision tree to which this node is linked.
     **/
    protected AnchorNode(DecisionTree tree) {
	super(0.);
	
	this.tree = tree;
	this.root = new OpenNode(0.);
	this.root.setFather(this);
    }
    
    protected void replaceSon(Node oldRoot, Node newRoot) {
	if (oldRoot != root)
	    throw new IllegalArgumentException("First argument is invalid.");
	
	this.root = newRoot;
    }
    
    /**
     * Returns this node's son. This son is also the tree root.
     *
     * @return The node's son.
     **/
    public Node son() {
	return root;
    }

    public Node son(int sonNb) {
	if (sonNb != 0)
	    throw new IllegalArgumentException("Argument must be 0");
	
	return root;
    }
    
    public boolean hasOpenNode() {
	return root.hasOpenNode();
    }
    
    protected void updateHasOpenNode() {
    }

    public int nbSons() {
	return 1;
    }

    public boolean isLeaf() {
	return false;
    }

    public DecisionTree tree() {
	return tree;
    }
}
