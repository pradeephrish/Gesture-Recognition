package com.asu.mwdb.math;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.asu.mwdb.loggers.MyLogger;
import com.asu.mwdb.setup.SetupSystem;

public class ConstructGestureWords {

    private Map<String, Integer> tfIDFMapGlobal = new HashMap<String, Integer>();  //global map
    private List<List<Map<String, List<Double>>>> tfMapArrayIDF = new ArrayList<List<Map<String, List<Double>>>>();
    private List<Map<String, Double>> tfMapArrayIDF2 = new ArrayList<Map<String, Double>>();
    private static Logger logger = new MyLogger().getupLogger();
    private File[] fileNames;
    private List<Double> setOfIDF = new ArrayList<Double>();
    private List<Double> setOfIDF2 = new ArrayList<Double>();

   
    public void constructGestureWords(int wordLength, int shiftLength, String seriesInputFolder) throws IOException {
        //read File
        File directory = new File(seriesInputFolder);
        fileNames = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                String name = pathname.getName().toLowerCase();
                return name.endsWith(".csv") && pathname.isFile();
            }
        });
        for (int i = 0; i < fileNames.length; i++) {
            BufferedReader in = new BufferedReader(new FileReader(fileNames[i]));

            List<Map<String, List<Double>>> mapPerGestureFile = new ArrayList<Map<String, List<Double>>>();

            Map<String, List<Double>> wordMap = null;  //per row 
            while (in.ready()) {
                wordMap = new HashMap<String, List<Double>>();  //per rows
                String series = in.readLine();
                String letters[] = series.split(",");
                Integer lastJIndex = -1; // for padding
                double totalWordCountPerDocument = 0.0;
                for (int j = 0; j < letters.length - wordLength + 1; j = j + shiftLength) {

                    String word = "";
                    for (int k = j; k < wordLength + j; k++) {
                        word += letters[k];
                    }

                    //for TF
                    if (wordMap.containsKey(word)) {
                        List<Double> list = wordMap.get(word);
                        list.set(0, list.get(0) + 1.0F); //0 index for TF
                        wordMap.put(word, list);
                    } else {
                        List<Double> list = new ArrayList<Double>();
                        list.add(0, 1.0);
                        wordMap.put(word, list);
                    }
                    lastJIndex = j + shiftLength;
                    ++totalWordCountPerDocument;
                }
                Integer difference = letters.length - lastJIndex;
                if (difference > 0) {
                    String paddedWord = "";
                    Integer paddingSize = wordLength - difference;
                    while (difference > 0) {
//					if(lastJIndex < letters.length)
                        paddedWord += letters[lastJIndex];
//					else
//						paddedWord+=paddedWord.charAt(paddedWord.length()-1);

                        --difference;
                        ++lastJIndex;
                    }

                    while (paddingSize > 0) {
//					paddedWord+=paddedWord.charAt(paddedWord.length()-1);
                        paddedWord += letters[lastJIndex - 1];
                        --paddingSize;
                    }
                    if (wordMap.containsKey(paddedWord)) {
                        List<Double> list = wordMap.get(paddedWord);
                        list.set(0, list.get(0) + 1.0F); //0 index for TF
                        wordMap.put(paddedWord, list); //updateMap for word
                    } else {
                        List<Double> list = new ArrayList<Double>();
                        list.add(0, 1.0);
                        wordMap.put(paddedWord, list);
                    }
                    ++totalWordCountPerDocument;
                }
                //Save Words to File - Saving TF
//				writeToFile(wordMap, tfOutputFolder+File.separator+fileNames[i].getName());

                wordMap = updateWordMapForTotalCountK(wordMap, totalWordCountPerDocument); // n/k , where n is frequency of word in doc/ k total freq
                mapPerGestureFile.add(wordMap);
            }
            getTfMapArrayIDF().add(mapPerGestureFile);




            //count idf2 per document
            processIDF2Values(mapPerGestureFile);
        }

        //populate global map for IDF values List<LIst<Map>
        createIDFMap(getTfMapArrayIDF());

        //Generate IDF Files from Global Map  * TF Values
        generateIDFFiles();



        //Generate IDF2 Files
        generateIDF2Files();


        //normalize tfidf and tfidf2
        normalizeDictionary();

        writeToFile(getTfMapArrayIDF(), seriesInputFolder);


    }

    private Map<String, List<Double>> updateWordMapForTotalCountK(
            Map<String, List<Double>> wordMap, double totalWordCountPerDocument) {
        // TODO Auto-generated method stub
        Iterator iterator = wordMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, List<Double>> entry = (Map.Entry<String, List<Double>>) iterator.next();
            if (totalWordCountPerDocument > 0.0) {
                entry.getValue().set(0, entry.getValue().get(0) / totalWordCountPerDocument); //add all tf for total words
            }
        }

        return wordMap;
    }

    private void normalizeDictionary() {

        Collections.sort(setOfIDF, Collections.reverseOrder());
        Collections.sort(setOfIDF2, Collections.reverseOrder());


        Double maxIDF = setOfIDF.get(0) == 0.0 ? 1.0 : setOfIDF.get(0);
        Double maxIDF2 = setOfIDF2.get(0) == 0.0 ? 1.0 : setOfIDF2.get(0);

        // TODO Auto-generated method stub
        for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
            List<Map<String, List<Double>>> gestureDocument = getTfMapArrayIDF().get(i);
            for (int j = 0; j < gestureDocument.size(); j++) {
                Map<String, List<Double>> tempMap = gestureDocument.get(j);
                Iterator it = tempMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, List<Double>> pairs = (Map.Entry) it.next();
                    //normalize tf-idf
                    pairs.getValue().set(3, pairs.getValue().get(3) / maxIDF);
                    //normalize tf-idf2
                    pairs.getValue().set(4, pairs.getValue().get(4) / maxIDF2);
                }
            }
        }
    }

    private void writeToFile(
            List<List<Map<String, List<Double>>>> tfMapArrayIDF3, String task1OutputFolder) throws IOException {
        // TODO Auto-generated method stub
        File task1OutputFileObj = new File(task1OutputFolder + File.separator
                + "task1Output");
        if (task1OutputFileObj.exists()) {
            SetupSystem.delete(task1OutputFileObj);
        }
        File task1OutputFolderAll = new File(task1OutputFolder + File.separator
                + "task1OutputAll");
        if (task1OutputFolderAll.exists()) {
            SetupSystem.delete(task1OutputFolderAll);
        }
        if (task1OutputFileObj.mkdir() && task1OutputFolderAll.mkdir()) {
            for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
                List<Map<String, List<Double>>> gestureDocument = getTfMapArrayIDF()
                        .get(i);

                FileWriter fileWriter3 = new FileWriter(task1OutputFileObj
                        + File.separator + (fileNames[i].getName()));
                FileWriter fileWriter5 = new FileWriter(task1OutputFolderAll
                        + File.separator + (fileNames[i].getName()));

                BufferedWriter bufferWriter3 = new BufferedWriter(fileWriter3);
                BufferedWriter bufferWriter5 = new BufferedWriter(fileWriter5);

                for (int j = 0; j < gestureDocument.size(); j++) {
                    Map<String, List<Double>> tempMap = gestureDocument.get(j);
                    Iterator it = tempMap.entrySet().iterator();
                    // FileWriter fileWriter3 = new
                    // FileWriter(outputFolder+File.separator+(i+1)+".csv",true);
                    // FileWriter fileWriter5 = new
                    // FileWriter(alloutputFolder+File.separator+(i+1)+".csv",true);
                    // BufferedWriter bufferWriter3 = new
                    // BufferedWriter(fileWriter3);
                    // BufferedWriter bufferWriter5 = new
                    // BufferedWriter(fileWriter5);
                    while (it.hasNext()) {
                        Map.Entry<String, List<Double>> pairs = (Map.Entry) it
                                .next();
                        List<Double> idf = pairs.getValue();
                        // bufferWriter3.write(pairs.getKey()+":"+pairs.getValue().get(0)+","+pairs.getValue().get(3)+","+pairs.getValue().get(4)+";");
                        bufferWriter3.write(pairs.getKey() + ":"
                                + pairs.getValue().get(0) + ","
                                + pairs.getValue().get(1) + ","
                                + pairs.getValue().get(2) + ";");// change by
                        // akshay
                        bufferWriter5.write(pairs.getKey() + ":"
                                + pairs.getValue().get(0) + ","
                                + pairs.getValue().get(1) + ","
                                + pairs.getValue().get(2) + ","
                                + pairs.getValue().get(3) + ","
                                + pairs.getValue().get(4) + ";");
                        // bufferWriter.write(pairs.getKey()+":"+pairs.getValue()*(inverse)+" ");
                        // //seperated by space
                        // bufferWriter.write("\r\n");
                    }
                    bufferWriter3.write("\r\n");
                    bufferWriter5.write("\r\n");
                }
                bufferWriter3.close();
                bufferWriter5.close();
            }
        } else {
            System.out.println("Could not create output folders");
        }

    }

    private void generateIDF2Files() throws IOException {
        // TODO Auto-generated method stub

        for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
            List<Map<String, List<Double>>> gestureDocument = getTfMapArrayIDF().get(i);
            Map<String, Double> idf2PerDocument = getTfMapArrayIDF2().get(i);

            for (int j = 0; j < gestureDocument.size(); j++) {
                Map<String, List<Double>> tempMap = gestureDocument.get(j);
                Iterator it = tempMap.entrySet().iterator();

//				FileWriter fileWriter = new FileWriter(outputFolder+File.separator+(i+1)+".csv",true);
//				BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
                while (it.hasNext()) {
                    Map.Entry<String, List<Double>> pairs = (Map.Entry) it.next();
                    Double inverse = (new Double(getTfMapArrayIDF().get(0).size()) / idf2PerDocument.get(pairs.getKey())); //already inversse
                    List<Double> tf = pairs.getValue();
                    //tf.add(tf.get(0)*Math.log(inverse)); // 2 for idf2
                    tf.add(Math.log(inverse)); //tfidf2 change by akshay
                    //calculate tf-idf and tf-idf2
                    tf.add(tf.get(0) * tf.get(1));
                    //     setOfIDF.add(tf.get(0)*tf.get(1));
                    setOfIDF.add(tf.get(1));			//change by akshay
                    tf.add(tf.get(0) * tf.get(2));
                    //setOfIDF2.add(tf.get(0)*tf.get(2));
                    setOfIDF2.add(tf.get(2));			//change by akshay


//						bufferWriter.write(pairs.getKey()+":"+pairs.getValue()*(inverse)+" "); //seperated by space
//						bufferWriter.write("\r\n");				    
                }
//				  bufferWriter.close();
            }
        }
    }

    private void processIDF2Values(
            List<Map<String, List<Double>>> mapPerGestureFile) {
        // TODO Auto-generated method stub
//		tfMapArrayIDF2

        Map<String, Double> idf2PerDocument = new HashMap<String, Double>(); // per univariate series

        for (int i = 0; i < mapPerGestureFile.size(); i++) {
            Map<String, List<Double>> tmpMap = mapPerGestureFile.get(i);
            Iterator iterator = tmpMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, List<Double>> pairs = (Entry<String, List<Double>>) iterator.next();
                if (idf2PerDocument.containsKey(pairs.getKey())) {
                    idf2PerDocument.put(pairs.getKey(), idf2PerDocument.get(pairs.getKey()) + 1.0);
                } else {
                    idf2PerDocument.put(pairs.getKey(), 1.0);
                }
            }
        }
        getTfMapArrayIDF2().add(idf2PerDocument);
    }


    /*private static void generateIDF2(Map<String, Float> idf2MapDocument,String outputFolder) throws IOException {
     // TODO Auto-generated method stub
		
     for (int i = 0; i < tfMap.size(); i++) {
     List<Map<String,Float>> gestureDocument = tfMap.get(i);
     for (int j = 0; j < gestureDocument.size(); j++) {
     Map<String,Float> tempMap = gestureDocument.get(i);
     Iterator it = tempMap.entrySet().iterator();
     while (it.hasNext()) {
     Map.Entry<String,Integer> pairs = (Map.Entry)it.next();
				        
     if(tfIDFMapGlobal.containsKey(pairs.getKey()))
     tfIDFMapGlobal.put(pairs.getKey(), tfIDFMapGlobal.get(pairs.getKey())+1);
     else
     tfIDFMapGlobal.put(pairs.getKey(), 1);
				    
     }
     }
     }
		
		
     for (int i = 0; i < tfMapArray.size()-20; i=i+20) {
     for(int j=i;j < 20+i;j++)
     {
     Map<String,Float> tfMapforRow = tfMapArray.get(j);
     Iterator iterator = tfMapforRow.entrySet().iterator();
     while(iterator.hasNext()){
     Map.Entry<String,Float> pairs = (Map.Entry)iterator.next();
     if(tfIDFMapGlobal.containsKey(pairs.getKey()))
     {
     Float inverse = new Float(tfMapArray.size())/tfIDFMapGlobal.get(pairs.getKey()); //already inversse
     pairs.setValue(pairs.getValue()*inverse);
     }
					
     }
     writeToFile(tfMapArray.get(j), outputFolder+File.separator+(j+1)+".csv"); 
     }
     }
     }*/
    private void generateIDFFiles() throws IOException {
        // TODO Auto-generated method stub

        for (int i = 0; i < getTfMapArrayIDF().size(); i++) {
            List<Map<String, List<Double>>> gestureDocument = getTfMapArrayIDF().get(i);
            for (int j = 0; j < gestureDocument.size(); j++) {
                Map<String, List<Double>> tempMap = gestureDocument.get(j);
                Iterator it = tempMap.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<String, List<Double>> pairs = (Map.Entry) it.next();
                    Double inverse = (new Double(getTfMapArrayIDF().size() * getTfMapArrayIDF().get(0).size()) / getTfIDFMapGlobal().get(pairs.getKey())); //already inversse
                    Double idf = (Math.log(inverse)); //IDF Change as Suggested bu Akshay
                    pairs.getValue().add(idf); // 1 for IDF
                }
            }
        }
    }

    private void createIDFMap(List<List<Map<String, List<Double>>>> tfMap) {

        for (int i = 0; i < tfMap.size(); i++) {
            List<Map<String, List<Double>>> gestureDocument = tfMap.get(i);
            for (int j = 0; j < gestureDocument.size(); j++) {
                Map<String, List<Double>> tempMap = gestureDocument.get(j);
                Iterator it = tempMap.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Integer> pairs = (Map.Entry) it.next();

                    if (getTfIDFMapGlobal().containsKey(pairs.getKey())) {
                        getTfIDFMapGlobal().put(pairs.getKey(), getTfIDFMapGlobal().get(pairs.getKey()) + 1);
                    } else {
                        getTfIDFMapGlobal().put(pairs.getKey(), 1);
                    }

                }
            }
        }
    }

    public void writeToFile(Map wordMap, String fileName) throws IOException {

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));

        Iterator it = wordMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            out.println(pairs.getKey() + "," + pairs.getValue() + ":");
        }
        out.close();
    }

    public static void main(String[] args) {
        try {
            new ConstructGestureWords().constructGestureWords(3, 2, "data//sampledata//letter//X");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setTfIDFMapGlobal(Map<String, Integer> tfIDFMapGlobal) {
        this.tfIDFMapGlobal = tfIDFMapGlobal;
    }

    public Map<String, Integer> getTfIDFMapGlobal() {
        return tfIDFMapGlobal;
    }

    public void setTfMapArrayIDF2(List<Map<String, Double>> tfMapArrayIDF2) {
        this.tfMapArrayIDF2 = tfMapArrayIDF2;
    }

    public List<Map<String, Double>> getTfMapArrayIDF2() {
        return tfMapArrayIDF2;
    }

    public void setTfMapArrayIDF(List<List<Map<String, List<Double>>>> tfMapArrayIDF) {
        this.tfMapArrayIDF = tfMapArrayIDF;
    }

    public List<List<Map<String, List<Double>>>> getTfMapArrayIDF() {
        return tfMapArrayIDF;
    }

    public void setFileNames(File[] fileNames) {
        this.fileNames = fileNames;
    }

    public File[] getFileNames() {
        return fileNames;
    }
}
