package com.asu.mwdb.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.asu.mwdb.loggers.MyLogger;
import au.com.bytecode.opencsv.CSVReader;

public class TopGestureWords {

	private static Logger logger = new MyLogger().getupLogger();
	
	public static void getTop10HighTF() throws IOException{
		CSVReader csvReader = new CSVReader(new FileReader("C:\\Users\\Dwaraka\\Desktop\\Fall2013\\MWDB\\mwdbworkspace\\mwdbproject\\data\\sampledata\\termfrequency\\W\\1.csv"));
		List <String[]> TF = csvReader.readAll();
		csvReader.close();
		Iterator<String[]> I = TF.iterator();
		String grp [][] = new String[TF.size()][2];
		int i=0;
		while(I.hasNext()){
			String temp[] = I.next();
			grp[i]=temp;
			logger.info(temp[0]+","+temp[1]);
			i++;
		}
		
		Arrays.sort(grp, new Comparator<String[]>() {
            @Override
            public int compare(final String[] entry1, final String[] entry2) {
                final double time1 = Double.parseDouble(entry1[1]);
                final double time2 = Double.parseDouble(entry2[1]);
                if(time1>time2){
                	return -1;
                }else if (time1 < time2){
                	return 1;
                }else{
                	return 0;
                }
            }
        });
		
		for(i =0;i<10;i++){
			String[] s = grp[i];
			logger.info(s[0] + " " + s[1]);
		}
		
		BufferedWriter br = new BufferedWriter(new FileWriter(new File(
				"c:"+ File.separator +"Data" + File.separator + "sample.csv")));
		for (i = 0; i < 10; i++) {
			String[] s = grp[i];
			br.write(s[0] + ","
					+ s[1]);
			br.write("\r\n");
		}
		br.close();
	}
}
