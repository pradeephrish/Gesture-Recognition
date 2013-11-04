package com.asu.mwdb.phase2Main;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {

	public Logger getupLogger() {

		Logger logger = Logger.getLogger("MWDBLogger");
		FileHandler fh;

		try {
			// Configure the basic parameters of logger
			fh = new FileHandler("." + File.separator + "mwdb.log", true);
			logger.addHandler(fh);
			logger.setLevel(Level.ALL);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException e) {
			System.out.println("Error while setting logger configuration - " + e);
		} catch (IOException e) {
			System.out.println("Error while setting logger configuration - " + e);
		}
		return logger;

	}

}