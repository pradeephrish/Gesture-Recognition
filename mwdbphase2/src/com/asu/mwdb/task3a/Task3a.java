package com.asu.mwdb.task3a;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.asu.mwdb.utils.IConstants;
import com.asu.mwdb.utils.Utils;

import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;


public class Task3a {
	
	public static void executeTask3a(MatlabProxy proxy,String semanticInputDirectory,List<String> listOfComponents){
		

		for (int i = 0; i < listOfComponents.size(); i++) {
			String componentDir = listOfComponents.get(i).substring(listOfComponents.get(i).lastIndexOf(File.separator) + 1);
			String path =  semanticInputDirectory+File.separator+ componentDir;
			String parent = semanticInputDirectory.substring(semanticInputDirectory.lastIndexOf(File.separator)+1);
			String outputDirectory = new File(path).getParentFile().getParentFile().getAbsolutePath()+File.separator+parent+"-clusters"+File.separator+componentDir;
			if(!Utils.isDirectoryCreated(outputDirectory))
				return;
			
			try {
				executeTask3aAtomic(proxy, path+File.separator+IConstants.SEMANTICGG_TFIDF,componentDir);
				executeTask3aAtomic(proxy, path+File.separator+IConstants.SEMANTICGG_TFIDF2,componentDir);
				executeTask3aAtomic(proxy, path+File.separator+IConstants.SEMANTICGG_PCA,componentDir);
				executeTask3aAtomic(proxy, path+File.separator+IConstants.SEMANTICGG_SVD,componentDir);
				executeTask3aAtomic(proxy, path+File.separator+IConstants.SEMANTICGG_LDA,componentDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MatlabInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	

	private static void executeTask3aAtomic(MatlabProxy proxy,String targetFile,String component) throws IOException, MatlabInvocationException{
		File targetFileHandle = new File(targetFile);
		
		String temp = targetFileHandle.getParentFile().getParentFile().getAbsolutePath();
		String parent = temp.substring(temp.lastIndexOf(File.separator)+1);

		String outputDirectory = targetFileHandle.getParentFile().getParentFile().getParentFile().getAbsolutePath()+File.separator+parent+"-clusters";
		
		CSVReader csvReader = new CSVReader(new InputStreamReader(
				new FileInputStream(targetFileHandle.getAbsolutePath())));

		List<String[]> targetFileList = csvReader.readAll();

		String docArr[] = targetFileList.get(0);
		for(int j=1;j<targetFileList.size();j++){
			
			String[] line = targetFileList.get(j);
			Arrays.sort(line, new Comparator<String>() {
				@Override
				public int compare(final String entry1, final String entry2) {
					final double value1 = Double.parseDouble(entry1);
					final double value2 = Double.parseDouble(entry2);
					if (value1 > value2) {
						return -1;
					} else if (value1 < value2) {
						return 1;
					} else {
						return 0;
					}
				}
			});
			String tempFile = outputDirectory+File.separator+ IConstants.TEMPFILE+j+".csv";
			File tempFileHandle = new File(tempFile);
			String tempFinalResult = tempFileHandle.getParentFile().getAbsolutePath() + File.separator+"tempresult"+j+".csv"; 
			CSVWriter csvWriter = new CSVWriter(new FileWriter(tempFile), ',',
					CSVWriter.NO_QUOTE_CHARACTER,
					CSVWriter.DEFAULT_LINE_END);
			csvWriter.writeNext(line);
			csvWriter.close();

		
			String imagePath = outputDirectory+File.separator+component+File.separator+"elbow-"+targetFileHandle.getName()+"_"+j+".png";
			System.out.println(imagePath);
			proxy.eval("findElbow('" + tempFileHandle.getAbsolutePath() + "','" + tempFinalResult+ "','" + imagePath+ "')");
			
			csvReader = new CSVReader(new InputStreamReader(
					new FileInputStream(tempFinalResult)));
			List<String[]> tempFileList = csvReader.readAll();
			csvReader.close();

			Double elbowValue = Double.parseDouble(tempFileList.get(0)[0]);
			String [] actualLine = targetFileList.get(j);

//			System.out.println("Clusters for eigen vector : "+j);
			
			StringBuffer stringBuffer = new StringBuffer();
			
			for(int i =0 ;i<actualLine.length;i++){
				if(elbowValue <= Double.parseDouble(actualLine[i])){
//					System.out.print(docArr[i] + "   ");
					stringBuffer.append(docArr[i]+ "  ");
				}
			}
			
			FileUtils.writeStringToFile(new File(outputDirectory+File.separator+component+File.separator+"cluster-"+targetFileHandle.getName()+"_"+j+"_group.txt"),stringBuffer.toString());
			
			tempFileHandle.delete();
			new File(tempFinalResult).delete();
		}


	}
}
