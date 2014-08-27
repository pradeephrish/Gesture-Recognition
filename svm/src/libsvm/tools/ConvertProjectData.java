package libsvm.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ConvertProjectData {
	public static void main(String[] args) throws IOException {
		CSVReader csvReader =new CSVReader(new FileReader(new File("ggSVD.csv")));
		CSVWriter csvWriter =new  CSVWriter(new FileWriter(new File("ggSVDO.csv")),' ',CSVWriter.NO_QUOTE_CHARACTER);
		List<String[]> input = csvReader.readAll();
		List<String[]> output = new ArrayList<String[]>();
		
		//80 labels - using Kmean
		//String labels[] = {"5","5","1","3","1","1","2","2","4","2","1","1","1","3","4","4","5","2","4","1","2","2","4","5","5","4","3","2","3","2","4","4","5","5","1","1","2","1","2","1","3","1","2","1","1","3","1","3","3","3","3","3","3","3","2","3","5","2","2","4","5","5","5","1","1","3","3","4","4","5","5","1","5","5","5","3","2","3","5","4"};
		
		//60 labels - using Kmean
		String labels[] = {"3","3","5","2","3","3","2","2","4","4","4","3","2","2","3","3","5","2","3","2","3","2","1","2","3","1","1","2","1","5","5","5","4","1","4","3","5","2","4","3","1","3","1","5","5","5","2","1","3","2","2","4","4","3","4","1","1","3","5","5"};
		
		for (int i = 0; i < input.size(); i++) {
			String[] row = input.get(i);
			String[] outputRow=new String[row.length+1];
			for (int j = 0; j < row.length; j++) {
				row[j]=j+":"+row[j];
			}
			outputRow[0]=labels[i];
			for (int j = 0; j < row.length; j++) {
				outputRow[j+1]=row[j];
			}
			output.add(outputRow);
		}
		csvWriter.writeAll(output);
	}
}
