package com.asu.mwdb.phase3.svmlib.tools;

import java.io.IOException;
import java.util.Arrays;


public class AllPossible {
	public static void main(String[] args) {
			
		
			String[] sOptions={"0","1","2","3","4"};
			String[] tOptions={"0","1","2","3","4"};
			
			String trainArgv[]= new String[8];
			trainArgv[0]="-s";
			trainArgv[2]="-t";
			trainArgv[4]="-b";
			trainArgv[5]="1";
			trainArgv[6]="training.data";
			
			String predictArgv[]="-b 1 test.data model.data output.data".split(" ");
			System.out.println(Arrays.toString(predictArgv));
			
			for (int i = 0; i < sOptions.length; i++) {
				trainArgv[1]=String.valueOf(i);
				for (int j = 0; j < tOptions.length; j++) {
					trainArgv[3]=String.valueOf(j);
					
					trainArgv[7]="model.data"+i+j;
					
					predictArgv[3]="model.data"+i+j;
					
					predictArgv[4]="output.data"+i+j;
					
					System.out.println(Arrays.toString(trainArgv));
					System.out.println(Arrays.toString(predictArgv));
					System.out.println("Checkout Output file ");
						try {
							SVMTrain.main(trainArgv);
							SVMPredict.main(predictArgv);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}catch (Exception e) {
							// TODO: handle exception
							e.printStackTrace();
						}
					System.out.println("Output File is "+"output.data"+i+j);
						
				}
			}
			
			
			
			
			//training
			//-s 1 -t 4 -b 1 training.data model.data
			
			//predict
			//-b 1 test.data model.data output.data 
			
//			SVMTrain.main(null);
//			SVMPredict.main(null);

	}
}
