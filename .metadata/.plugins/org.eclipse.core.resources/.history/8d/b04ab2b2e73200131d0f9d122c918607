import java.io.IOException;
import java.util.logging.Logger;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import asu.edu.loggers.MyLogger;
import asu.edu.math.ConstructGestureWords;
import asu.edu.math.GaussianBands;
import asu.edu.matlab.MatlabObject;
import asu.edu.setup.SetupSystem;
import asu.edu.utils.LetterRange;
import asu.edu.utils.NormalizeData;

public class master {

	private static MatlabProxy proxy;
	private static Logger logger = new MyLogger().getupLogger();

	public static void main(String args[]) throws MatlabConnectionException,
			MatlabInvocationException, IOException, InterruptedException {

		MatlabObject matlabObject = new MatlabObject();
		proxy = matlabObject.getMatlabProxy();

		// Setup the file system
		SetupSystem ss = new SetupSystem();

		String path = "cd(\'" + ss.matlabScriptLoc + "')";
		proxy.eval(path);
		
		// Normalize files in X, Y, Z, W folders
		// Find these normalized files in normalize folder
		new NormalizeData(proxy, ss.matlabScriptLoc, ss.sampleDataLoc);

		GaussianBands gb = new GaussianBands();
		double rBandValueRange[][] = gb.getGaussianBands(ss.sampleDataLoc,
				Double.parseDouble(ss.r), Double.parseDouble(ss.mean),
				Double.parseDouble(ss.standardDeviation));

		LetterRange.assignToGaussianCurve(proxy, ss.matlabScriptLoc,
				ss.sampleDataLoc,rBandValueRange);

		
		ConstructGestureWords constructGestureWords  = new ConstructGestureWords();
		
		constructGestureWords.getAllGestureFile(Integer.parseInt(ss.w), Integer.parseInt(ss.s), ss.sampleDataLoc);
		
		
		// Disconnect the proxy from MATLAB
		proxy.exit();
		proxy.disconnect();

	}
}