package com.asu.mwdb.utils;

import java.io.File;
import java.util.Comparator;

public class NumberedFileComparator implements Comparator {

	public int compare(Object o1, Object o2) {
		File file1 = (File) o1;
		File file2 = (File) o2;
		String f1 =  file1.getName();
		String f2 =  file2.getName();
		int val = f1.compareTo(f2);
		if (val != 0) {
			String number1 = f1.substring(0, f1.lastIndexOf("."));
			String number2 = f2.substring(0, f2.lastIndexOf("."));
			int int1 = Integer.parseInt(number1);
			int int2 = Integer.parseInt(number2);
			val = int1 - int2;
		}
		return val;
	}
}