

package com.asu.mwdb.phase3.task3.decisiontree.misc;


/**
 * This exception reports the readinf of an invalid (syntatically incorrect)
 * file.
 */
public class FileFormatException
    extends RuntimeException {
    
    /**
     * Creates a new object reporting the reading of an invalid file.
     */
    public FileFormatException() {
    }
    
    /**
     * Creates a new object reporting the reading of an invalid file.
     *
     * @param s A string describing the problem.
     */
    public FileFormatException(String s) {
	super(s);
    }
}
