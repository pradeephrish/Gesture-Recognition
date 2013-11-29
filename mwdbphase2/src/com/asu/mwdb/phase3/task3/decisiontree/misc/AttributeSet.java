

package com.asu.mwdb.phase3.task3.decisiontree.misc;

import java.util.*;


/**
 * This class holds an ordered set of attributes. <p>
 * This object is immutable: attributes cannot be added or removed.  This
 * ensure that an attribute index will not change over time.
 **/
public class AttributeSet {
    
    private Vector attributes; 
    private Hashtable attributesHash; /* Retrieve an attribute index in
					 constant time */
    
    
    /**
     * Creates a new attribute set. 
     * 
     * @param attributes A Vector of {@link Attribute attributes} to include
     *                   in the set.
     **/
    public AttributeSet(Vector attributes) {
	if (attributes == null)
	    throw new IllegalArgumentException("Invalid attribute set");
	
	this.attributes = new Vector();
	attributesHash = new Hashtable();
	for (int i = 0; i < attributes.size(); i++)
	    add((Attribute) attributes.elementAt(i));
    }
    

    /**
     * Retreive the index of an attribute.
     *
     * @param attribute An attribute of the set.
     * @return The attribute index.  This index is such that
     *         0 <= index < size().
     **/
    public int indexOf(Attribute attribute) {
	Integer index = (Integer) attributesHash.get(attribute);
	
	if (index == null)
	    throw new IllegalArgumentException("Unknown attribute");
	
	return index.intValue();
    }
    

    /**
     * Retreive an attribute given its index.
     *
     * @param index An attribute index.  This index is such that
     *              0 <= index < size().
     * @return The indexed attribute.
     **/
    public Attribute attribute(int index) {
	if (index < 0 || index > attributes.size())
	    throw new IllegalArgumentException("Invalid index");
	
	return (Attribute) attributes.elementAt(index);
    }


    /**
     * Tests if an attribute belongs to the set.
     *
     * @param attribute The attribute to test.
     * @return <code>true</code> iff the attribute belongs to the set.
     **/
    public boolean contains(Attribute attribute) {
	if (attribute == null)
	    throw new IllegalArgumentException("Invalid 'null' attribute");
	
	return (attributesHash.get(attribute) != null);
    }


    /**
     * Finds an attribute using its name.
     *
     * @param name The searched attribute name.
     * @return The attribute if found, else <code>null</code>.
     **/
    public Attribute findByName(String name) {
	for (int i = 0; i < attributes.size(); i++) {
	    Attribute attribute = (Attribute) attributes.elementAt(i);
	 
	    if (attribute.name().equals(name))
		return attribute;
	}
	
	return null;
    }
    
	public List<Attribute> addByName(String name) {
		List<Attribute> attributeList= new ArrayList<Attribute>();
	for (int i = 0; i < attributes.size(); i++) {
	    Attribute attribute = (Attribute) attributes.elementAt(i);
	 
	    if (attribute.name().contains(name)){
	    	attributeList.add(attribute);
	    }
	}
	
	return attributeList;
    }

    
    /**
     * Returns the attributes of this set in the proper order.
     *
     * @return The attributes of this set.
     **/
    public Vector attributes() {
	Vector attributes = new Vector();

	for (int i = 0; i < this.attributes.size(); i++)
	    attributes.add(this.attributes.elementAt(i));

	return attributes;
    }

    
    /**
     * Returns the number of attributes in this set.
     *
     * @return The number of attributes.
     **/
    public int size() {
	return attributes.size();
    }

    
    /* Adds an attribute to the set */
    private void add(Attribute attribute) {
	if (attribute == null)
	    throw new IllegalArgumentException("Invalid 'null' attribute");

	Object oldValue = attributesHash.put(attribute, 
					     new Integer(attributes.size()));
	if (oldValue != null) {
	    attributesHash.put(attribute, oldValue); 
	    throw new IllegalArgumentException("Attribute already present");
	}
	
	attributes.add(attribute);
   }


    /**
     * Checks an object for equality.  An object is equal to this set if it
     * is an attribute set with the same attributes in the same order.
     *
     * @param attributeSet The set the compare for equality.
     * @return True iif the sets are equal.
     **/
    public boolean equals(Object attributeSet) {
	if (attributeSet == null || !(attributeSet instanceof AttributeSet))
	    return false;
	
	return attributes.equals(((AttributeSet) attributeSet).attributes);
    }

    
    public int hashCode() {
	return attributes.hashCode();
    }


    public String toString() {
	String s = "";

	for (int i = 0; i < attributes.size(); ) {
	    s += attributes.elementAt(i);
	
	    if (++i < attributes.size())
		s += " ";
	}

	return s;
    }
}
