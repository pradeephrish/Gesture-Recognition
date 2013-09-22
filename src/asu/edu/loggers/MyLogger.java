package asu.edu.loggers;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyLogger {

	public Logger getupLogger() {

		Logger logger = Logger.getLogger("MyLog");
		FileHandler fh;

		try {
			// This block configure the logger with handler and formatter
			fh = new FileHandler(".\\logs\\mwdb.log", true);
			logger.addHandler(fh);
			logger.setLevel(Level.ALL);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return logger;

	}

}