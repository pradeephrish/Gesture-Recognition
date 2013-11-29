

package com.asu.mwdb.phase3.task3.decisiontree.misc;


/**
 * An abstract representation of an attribute.
 **/
abstract public class Attribute {
 
    private String name;

    
    /**
     * Creates an attribute.
     *
     * @param name The attribute name.
     **/
    public Attribute(String name) {
	if (name == null)
	    throw new IllegalArgumentException("Invalid name");

	this.name = name;
    }

    /**
     * Returns the attribute's name.
     *
     * @return The attribute's name, or 'null' if no name has been assigned.
     **/
    public String name() {
	return name;
    }

    /**
     * Returns a copy of this attribute with a new name.
     *
     * @param name The new attribute name.  Can be 'null'.
     *
     * @return A new copy of this attribute.
     **/
    abstract public Attribute copy(String name);

    public String toString() {
	return (name == null) ? "No name defined" : name;
    }
}
