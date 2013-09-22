package asu.edu.utils;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import asu.edu.loggers.MyLogger;

public class NormalizeData {

	private Logger logger = new MyLogger().getupLogger();

	public NormalizeData(MatlabProxy proxy, String matlabScriptLoc,
			String sampleDataLoc) throws IOException, MatlabInvocationException {
		logger.info("Starting Normalization");

		String axisW = sampleDataLoc + "/W";
                
                System.out.println("Location is "+axisW);
                
		File fileW = new File(axisW);
		String[] directoriesW = fileW.list();
		for (int i = 0; i < directoriesW.length; i++) {
			if (directoriesW[i].contains("csv")) {
				String axisWFile = axisW + "/" + directoriesW[i];
				String normalAxisWFile = sampleDataLoc + "/normalize/W" + "/"
						+ directoriesW[i];
				proxy.eval("normalize('" + axisWFile + "','" + normalAxisWFile
						+ "')");
			}
		}

		String axisX = sampleDataLoc + "/X";
		File fileX = new File(axisX);
		String[] directoriesX = fileX.list();
		for (int i = 0; i < directoriesX.length; i++) {
			if (directoriesX[i].contains("csv")) {
				String axisXFile = axisX + "/" + directoriesX[i];
				String normalAxisXFile = sampleDataLoc + "/normalize/X" + "/"
						+ directoriesX[i];
				proxy.eval("normalize('" + axisXFile + "','" + normalAxisXFile
						+ "')");
			}
		}

		String axisY = sampleDataLoc + "/Y";
		File fileY = new File(axisY);
		String[] directoriesY = fileY.list();
		for (int i = 0; i < directoriesY.length; i++) {
			if (directoriesY[i].contains("csv")) {
				String axisYFile = axisY + "/" + directoriesY[i];
				String normalAxisYFile = sampleDataLoc + "/normalize/Y" + "/"
						+ directoriesY[i];
				proxy.eval("normalize('" + axisYFile + "','" + normalAxisYFile
						+ "')");
			}
		}

		String axisZ = sampleDataLoc + "/Z";
		File fileZ = new File(axisZ);
		String[] directoriesZ = fileZ.list();
		for (int i = 0; i < directoriesZ.length; i++) {
			if (directoriesZ[i].contains("csv")) {
				String axisZFile = axisZ + "/" + directoriesZ[i];
				String normalAxisZFile = sampleDataLoc + "/normalize/Z" + "/"
						+ directoriesZ[i];
				proxy.eval("normalize('" + axisZFile + "','" + normalAxisZFile
						+ "')");
			}
		}
		logger.info("Normalization Done !");
	}

}
