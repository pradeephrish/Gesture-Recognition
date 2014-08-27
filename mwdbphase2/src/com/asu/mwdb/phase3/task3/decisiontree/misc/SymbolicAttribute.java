
package com.asu.mwdb.phase3.task3.decisiontree.misc;



/**
 * A symbolic attribute.  Symbolic attributes have a finite set of
 * possible values represented by a positive integer.
 **/
public class SymbolicAttribute 
    extends Attribute {
    
    public final int nbValues;
    
    
    /**
     * Builds a new unnamed symbolic attribute.
     * 
     * @param nbValues The number of different values allowed for this 
     *                 attribute.  The allowed attribute values are
     *                 0...<code>nbValues - 1</code>.
     **/
    public SymbolicAttribute(int nbValues) {
	this(null, nbValues);
    }
    
    /**
     * Builds a new named symbolic attribute.
     *
     * @param name The attribute name.
     * @param nbValues The number of different values allowed for this 
     *                 attribute.
     **/
    public SymbolicAttribute(String name, int nbValues) {
	super(name);
	
	if (nbValues <= 0)
	    throw new IllegalArgumentException("The number of allowed " +
					       "attribute values must be " +
					       "strictly positive");
	    this.nbValues = nbValues;
    }
    
    public Attribute copy(String name) {
	return new SymbolicAttribute(name, nbValues);
    }

    /**
     * Converts a symbolic value to string.
     *
     * @param value The value to convert.
     * @return The value converted to a String.
     */
    public String valueToString(SymbolicValue value) {
	return "" + value;
    }
}
