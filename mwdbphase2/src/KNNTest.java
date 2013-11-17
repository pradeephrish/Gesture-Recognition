import java.io.File;
import java.io.IOException;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import com.asu.mwdb.matlab.MatlabObject;
import com.asu.mwdb.utils.IConstants;


public class KNNTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws MatlabConnectionException 
	 * @throws MatlabInvocationException 
	 */
	public static void main(String[] args) throws IOException, MatlabConnectionException, MatlabInvocationException {
		String sampleFilePath = IConstants.DATA + File.separator + "sample.csv"; 
		String trainingFilePath = IConstants.DATA + File.separator + "training.csv";
		String classFilePath = IConstants.DATA + File.separator + "group.csv";
		String outputFilePath = IConstants.DATA + File.separator + "classesOP.csv";
		String kValue = "2";
		MatlabProxy proxy = MatlabObject.getInstance();
		String arg = "KNNClassifier('" + sampleFilePath + "','" + trainingFilePath + "','"
				+ classFilePath + "','" + kValue + "','" + "euclidean" + "','" 
			    +  "nearest" + "','" + outputFilePath + "')";
		proxy.eval(arg);
		
		System.out.println();
	}

}
