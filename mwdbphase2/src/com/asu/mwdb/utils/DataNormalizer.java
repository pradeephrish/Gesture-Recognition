package com.asu.mwdb.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.asu.mwdb.loggers.MyLogger;

public class DataNormalizer {

	private Logger logger = new MyLogger().getupLogger();

	public DataNormalizer(MatlabProxy proxy, String matlabScriptLoc,
			String sampleDataLoc,List<File> directories) throws IOException, MatlabInvocationException {
		logger.info("Normalization Started");

                for (int j = 0; j < directories.size(); j++) {
                        String axisW = sampleDataLoc + "/"+directories.get(j).getName();
                
                        System.out.println("Location is "+axisW);
                
                        File fileW = new File(axisW);
                        String[] directoriesW = fileW.list();
                        for (int i = 0; i < directoriesW.length; i++) {
			if (directoriesW[i].contains("csv")) {
				String axisWFile = axisW + "/" + directoriesW[i];
				String normalAxisWFile = sampleDataLoc + "/OUTPUTP1/normalize/"+directories.get(j).getName() + "/"
						+ directoriesW[i];
				proxy.eval("normalize('" + axisWFile + "','" + normalAxisWFile
						+ "')");
			}
		}
                }
	}
}
