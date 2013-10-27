/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.asu.mwdb.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;


import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import com.asu.mwdb.loggers.MyLogger;
import com.asu.mwdb.math.ConstructGestureWords;
import com.asu.mwdb.math.CreateGaussianBands;
import com.asu.mwdb.math.Task3FindSimilarData;
import com.asu.mwdb.math.Task3FindSimilarData.DistanceFunction;
import com.asu.mwdb.math.Task3FindSimilarData.Entity;
import com.asu.mwdb.matlab.MatlabObject;
import com.asu.mwdb.setup.CreateFileStructure;
import com.asu.mwdb.utils.AssignLetter;
import com.asu.mwdb.utils.DataNormalizer;
import com.asu.mwdb.utils.SerializeData;
import com.asu.mwdb.utils.ShowHeatpMap;

/**
 *
 * @author paddy
 */
public class MainWindow extends javax.swing.JFrame {
    
    //For Task1
    
    private String inputDirectoryPath = null;
    private FilePicker dialog = null;
    private Double bandValue  = null; //r
    private Double mean = null; //
    private Double standardDeviation = null; //
    private Double wordLength = null; // 
    private Double shiftLength = null; //
    
    private static MatlabProxy proxy;
    private static Logger logger = new MyLogger().getupLogger();
    private ProgressBarUpdator progressBar;
    
    private List<ConstructGestureWords> constructGestureWords = new ArrayList<ConstructGestureWords>();
    private List<File> listOfDirectories;
    private String inputFilePath;
    
    private Image image; //heatmap image
    private ShowHeatpMap heatMapVisualize;
    private CreateFileStructure ss;
    
    
    /***  Phase 2 Data Structure  ***/
    //Holds data of sensor against list of words/dimensions
    private Map<Integer,List<String>> sensorWords = new HashMap<Integer, List<String>>();
    /*
     * Below,  (From Left to Right)  
     *              First List corresponds to 20 sensors   -assume this is i 
     *              Second Map corresponds to words/dimesnions for ith Sensor  -assume this is j
     *              Third  List contains only two elements, first is TF-IDF and second is TF-IDF2 , which correponds to jth word in ith sensor -- therefore size always 20                 
     */
    private List<Map<String,Double[]>> sensorWordsScores = new ArrayList<Map<String,Double[]>>();
    
    
    /***  Phase 2 Methods  
     * @throws MatlabConnectionException 
     * @throws MatlabInvocationException 
     * @throws IOException ***/
    
    
    public void executePCA(String inputLocation,List<List<String>> order) throws MatlabConnectionException, MatlabInvocationException, IOException{
    	MatlabObject matlabObject = new MatlabObject();
        proxy = matlabObject.getMatlabProxy();
          
        String scriptLocation = CreateFileStructure.getScriptLocation();
        scriptLocation="."+File.separator+scriptLocation;
        System.out.println("Script Location"+scriptLocation);
        
        String path = "cd(\'" + scriptLocation + "')";
        
        proxy.eval(path);
        
        File[] files = new File(inputLocation).listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				// TODO Auto-generated method stub
				String name = pathname.getName().toLowerCase();
                return name.endsWith(".csv") && pathname.isFile();
			}
		});
        for (int i = 0; i < files.length; i++) {  
        	System.out.println("path is "+files[i].getParent()+i+File.separator+files[i].getName()); 
        	proxy.eval("PCAFinder('" + files[i].getAbsolutePath() + "','" + files[i].getParentFile().getAbsolutePath()+File.separator+"pca"+File.separator+files[i].getName()+ "')");
		}
        //************************* now print  top-3 latent semantics
        
       /* String newLineMark = System.getProperty("line.separator");

        String leftAlignFormat = "| %-15s ";
        String topRow =   "+-----------------";
        String columnName="| Column name     |";
        String bottomRow= "+-----------------+";*/
        
        for (int i = 0; i < order.size(); i++) {
        	/*for (int j = 0; j < order.get(i).size(); j++) {
        		leftAlignFormat+=" | %f"; 
        		topRow+="+--------";
        		columnName+="|"+order.get(i).get(j)+"|";
        		bottomRow+="+--------";
			}
        	leftAlignFormat+= "|" + newLineMark;
        	topRow+="+" + newLineMark;
        	bottomRow+="+" + newLineMark;
        	columnName+="|" + newLineMark;
        	
        	System.out.format(topRow);
            System.out.printf(columnName);
            System.out.format(bottomRow);*/
            
            
            //read sensor 0 data and print
            
            String pcaFileName =  files[i].getParentFile().getAbsolutePath()+File.separator+"pca"+File.separator+i+".csv";
            String pcaSemanticFileName = files[i].getParentFile().getAbsolutePath()+File.separator+"pca-semantic"+File.separator+i+".csv";
            
            CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(pcaFileName)));
            CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(pcaSemanticFileName))); 
            
            List<String> orderList = order.get(i);
            String[] list=new String[orderList.size()];
            
            for (int j = 0; j < orderList.size(); j++) {
				list[j]=orderList.get(j);
			}
            csvWriter.writeNext(list);
            
            for (int j = 0; j < 3; j++) {
				csvWriter.writeNext(csvReader.readNext()); 
			}
            csvWriter.close();
            csvReader.close();
            
            /*for (int j = 0; j < 3; j++) {
            	String[] array = csvReader.readNext();
            	Object[] objectArray = new Object[array.length+1];
            	objectArray[0]="Semantic "+(j+1);
            	for (int k = 0; k < array.length; k++) {
            		 objectArray[k]=new Double(array[k]);
				}
            	System.out.format(leftAlignFormat, objectArray);	
			}*/
        }
    } 
        
        public void executeSVD(String inputLocation,List<List<String>> order) throws MatlabConnectionException, MatlabInvocationException, IOException{
        	MatlabObject matlabObject = new MatlabObject();
            proxy = matlabObject.getMatlabProxy();
              
            String scriptLocation = CreateFileStructure.getScriptLocation();
            scriptLocation="."+File.separator+scriptLocation;
            System.out.println("Script Location"+scriptLocation);
            
            String path = "cd(\'" + scriptLocation + "')";
            
            proxy.eval(path);
            
            File[] files = new File(inputLocation).listFiles(new FileFilter() {
    			
    			@Override
    			public boolean accept(File pathname) {
    				// TODO Auto-generated method stub
    				String name = pathname.getName().toLowerCase();
                    return name.endsWith(".csv") && pathname.isFile();
    			}
    		});
            for (int i = 0; i < files.length; i++) {  
            	System.out.println("path is "+files[i].getParent()+i+File.separator+files[i].getName()); 
            	proxy.eval("SVDFinder('" + files[i].getAbsolutePath() + "','" + files[i].getParentFile().getAbsolutePath()+File.separator+"svd"+File.separator+files[i].getName()+ "')");
    		}
            //************************* now print  top-3 latent semantics
            
           /* String newLineMark = System.getProperty("line.separator");

            String leftAlignFormat = "| %-15s ";
            String topRow =   "+-----------------";
            String columnName="| Column name     |";
            String bottomRow= "+-----------------+";*/
            
            for (int i = 0; i < order.size(); i++) {
            	/*for (int j = 0; j < order.get(i).size(); j++) {
            		leftAlignFormat+=" | %f"; 
            		topRow+="+--------";
            		columnName+="|"+order.get(i).get(j)+"|";
            		bottomRow+="+--------";
    			}
            	leftAlignFormat+= "|" + newLineMark;
            	topRow+="+" + newLineMark;
            	bottomRow+="+" + newLineMark;
            	columnName+="|" + newLineMark;
            	
            	System.out.format(topRow);
                System.out.printf(columnName);
                System.out.format(bottomRow);*/
                
                
                //read sensor 0 data and print
                
                String pcaFileName =  files[i].getParentFile().getAbsolutePath()+File.separator+"svd"+File.separator+i+".csv";
                String pcaSemanticFileName = files[i].getParentFile().getAbsolutePath()+File.separator+"svd-semantic"+File.separator+i+".csv";
                
                CSVReader csvReader = new CSVReader(new InputStreamReader(new FileInputStream(pcaFileName)));
                CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(new FileOutputStream(pcaSemanticFileName))); 
                
                List<String> orderList = order.get(i);
                String[] list=new String[orderList.size()];
                
                for (int j = 0; j < orderList.size(); j++) {
    				list[j]=orderList.get(j);
    			}
                csvWriter.writeNext(list);
                
                for (int j = 0; j < 3; j++) {
    				csvWriter.writeNext(csvReader.readNext()); 
    			}
                csvWriter.close();
                csvReader.close();
                
                /*for (int j = 0; j < 3; j++) {
                	String[] array = csvReader.readNext();
                	Object[] objectArray = new Object[array.length+1];
                	objectArray[0]="Semantic "+(j+1);
                	for (int k = 0; k < array.length; k++) {
                		 objectArray[k]=new Double(array[k]);
    				}
                	System.out.format(leftAlignFormat, objectArray);	
    			}*/

            }

        
        
        
        
        
        
        
        //System.out.format("+-----------------+------+" + newLineMark);
        
        
    }
    
    
    
    
    
    
    //Populates  Map<String,List<String>> sensorWords
    public Map<Integer,Set<String>> createWordsPerSensor(List<List<Map<String, List<Double>>>> dictionary){
    	
    	Map<Integer,Set<String>> sensorWords = new HashMap<Integer, Set<String>>();
    	
    	//iterate multivariate documents
    	for (int i = 0; i < dictionary.size(); i++) {
			//iterate univariate document
    		for (int j = 0; j < dictionary.get(i).size(); j++) {
				//iterate each sensor   -- note assuming  0 to 19
    			
    			if(sensorWords.get(j)!=null)
    					sensorWords.get(j).addAll(dictionary.get(i).get(j).keySet());
    			else
    			{
    				Set<String> wordsPerSensor = new HashSet<String>();
    				wordsPerSensor.addAll(dictionary.get(i).get(j).keySet());
    				sensorWords.put(j, wordsPerSensor);
    			}
			}
		}
    	return sensorWords;
    }
    
    public List<List<String>> savewordstoCSV(List<Map<String,Double[]>> sensorWordsScores){
    	List<List<String>> orderofDimenions = new ArrayList<List<String>>();
    	try{
    	for (int i = 0; i < sensorWordsScores.size(); i++) {
    		//System.out.println("Input Directory Path is "+inputDirectoryPath);
            //inputDirectoryPath=(inputDirectoryPath==null)?"data\\":inputDirectoryPath+"\\OUTPUTP1\\phase2\\";
            //System.out.println("Input Directory Path is"+inputDirectoryPath);
    		
            //check this later
			CSVWriter csvWriter = new CSVWriter(new FileWriter("data\\"+i+".csv"));
			csvWriter = new CSVWriter(new FileWriter("data\\"+i+".csv"),',', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
			
			Iterator<Entry<String, Double[]>> iterator = sensorWordsScores.get(i).entrySet().iterator();
			
			System.out.println("***************************");
			System.out.println("\tSensor\t"+i);
			List<String> wordOrder = new ArrayList<String>();
        	while(iterator.hasNext()){
        		Entry<String, Double[]> entry = iterator.next();
        		List<String> list = new ArrayList<String>();
        		//list.add(entry.getKey());  //for testing, not needed, since matlab will need only values of dimensions
        		
        		wordOrder.add(entry.getKey());
        		
        		Double[] array = entry.getValue();
        		for (int j = 0; j < array.length; j++) {
					list.add(String.valueOf(array[j]));
				}
        		csvWriter.writeNext(list.toArray(new String[list.size()]));
        	}
        	orderofDimenions.add(wordOrder);
        	
        	System.out.println("***************************");
			csvWriter.close();
		}
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    	return orderofDimenions;
    }
    
    
    //Populate List<List<List<Double>>> sensorWordsScores
    public List<Map<String,Double[]>> createSensorWordScores(Map<Integer,Set<String>> sensorWords,List<List<Map<String, List<Double>>>> dictionary)
    {
    	List<Map<String,Double[]>> sensorWordsScores = new ArrayList<Map<String,Double[]>>();
    	
    	for (int i = 0; i < sensorWords.size(); i++) {  //assuming keys start from 0 to 19
    		//get words
    		List<String> words = new ArrayList<String>(sensorWords.get(i)); //since order doesn't matter
    		
    		Map<String,Double[]> dimensionAgainstAllDocuments = new HashMap<String, Double[]>();
    		//insert all words with zero initial value for tf-idf / tf-idf2
    		for (int j = 0; j < words.size(); j++) {
				dimensionAgainstAllDocuments.put(words.get(j), new Double[dictionary.size()]);
			}
    		
    		//now iterate dictionary
			for (int k = 0; k < dictionary.size(); k++) {
				Map<String, List<Double>> map = dictionary.get(k).get(i);  //get i th map  of all multivariate series documents
				
				for (int j = 0; j < words.size(); j++) {
					if(map.containsKey(words.get(j))){
							 Double[] temp = dimensionAgainstAllDocuments.get(words.get(j));
							 temp[k]=map.get(words.get(j)).get(3);   //for 3 for tf-idf
					}else
					{
					//else it's value is already zero - so this part is not required at all
					Double[] temp = dimensionAgainstAllDocuments.get(words.get(j));
					temp[k]=0.0;   //for 3 for tf-idf
					}
				}
			}
    		//iterated dictionary for  ith sensor,  add it to main list
			sensorWordsScores.add(dimensionAgainstAllDocuments);
		}
    	
    	return sensorWordsScores;
    }
    
    /***/
    
    
    
    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        this.setTitle("MWDB: Phase I"); 
        initComponents();
        progressBar = new ProgressBarUpdator(jProgressBar1);
        new Thread(progressBar).start();
        progressBar.setValue(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jComboBox4 = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jToggleButton3 = new javax.swing.JToggleButton();
        jToggleButton4 = new javax.swing.JToggleButton();
        checkbox1 = new java.awt.Checkbox();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jLabel13 = new javax.swing.JLabel();
        jToggleButton2 = new javax.swing.JToggleButton();
        jLabel14 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jButton2 = new javax.swing.JButton();
        jToggleButton1 = new javax.swing.JToggleButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jProgressBar1 = new javax.swing.JProgressBar();
        jLabel8 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N

        jLabel15.setText("Select Input File");

        jButton4.setText("Browse");
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel16.setText("Enter your choice");

        jComboBox4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TF", "IDF", "IDF2", "TF-IDF", "TF-IDF2" }));
        jComboBox4.setMaximumSize(new java.awt.Dimension(67, 23));
        jComboBox4.setMinimumSize(new java.awt.Dimension(67, 23));
        jComboBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
        );

        jToggleButton3.setSelected(true);
        jToggleButton3.setText("Draw HeatMap");
        jToggleButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jToggleButton3MouseClicked(evt);
            }
        });
        jToggleButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton3ActionPerformed(evt);
            }
        });

        jToggleButton4.setText("Highlight");
        jToggleButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton4ActionPerformed(evt);
            }
        });

        checkbox1.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        checkbox1.setLabel("Draw & HightLight");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel16))
                        .addGap(183, 183, 183)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox4, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(107, 107, 107))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jToggleButton3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(checkbox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(180, 180, 180)
                                .addComponent(jToggleButton4))
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(18, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton4))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jComboBox4, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jToggleButton3)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jToggleButton4)
                        .addComponent(checkbox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane2.addTab("Task 2", jPanel2);

        jLabel10.setText("Select Input File");

        jButton3.setText("Browse");
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "TF", "TF-IDF", "TF-IDF2" }));
        jComboBox1.setMaximumSize(new java.awt.Dimension(67, 23));
        jComboBox1.setMinimumSize(new java.awt.Dimension(67, 23));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel11.setText("Select Similarity Based on");

        jLabel12.setText("Select Distance Function");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Cosine Similarity", "Euclidian Function" }));
        jComboBox2.setMaximumSize(new java.awt.Dimension(67, 23));
        jComboBox2.setMinimumSize(new java.awt.Dimension(67, 23));
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable2);

        jLabel13.setText("Matching Documents ");

        jToggleButton2.setText("Find Matching Series");
        jToggleButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton2ActionPerformed(evt);
            }
        });

        jLabel14.setText("Select Dictionary to Compare Against");

        jComboBox3.setLightWeightPopupEnabled(false);
        jComboBox3.setMaximumSize(new java.awt.Dimension(67, 23));
        jComboBox3.setMinimumSize(new java.awt.Dimension(67, 23));
        jComboBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11)
                            .addComponent(jLabel12))
                        .addGap(115, 115, 115)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel13)
                            .addComponent(jLabel14))
                        .addGap(175, 175, 175)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jToggleButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))))
                .addGap(148, 148, 148))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(jLabel12))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 62, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jToggleButton2))
                .addGap(17, 17, 17)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 355, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane2.addTab("Task 3", jPanel3);

        jLabel1.setText("Enter Word Length");

        jButton1.setText("Browse");
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Select Input Directory");

        jLabel3.setText("Enter Band Value (r)");

        jLabel4.setText("Enter Shift Length");

        jLabel5.setText("Enter Mean Value");

        jLabel6.setText("Enter Standard Deviation");

        jTextField1.setText("10");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jTextField2.setText("2");
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jTextField3.setText("3");
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });

        jTextField4.setText("0");
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jTextField5.setText("0.25");
        jTextField5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField5ActionPerformed(evt);
            }
        });

        jLabel7.setText("2r Bands ");

        jButton2.setText("Build Dictionary");
        jButton2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton2MouseClicked(evt);
            }
        });
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jToggleButton1.setText("Open Output Folder");
        jToggleButton1.setEnabled(false);
        jToggleButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jToggleButton1MouseClicked(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jLabel8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel8.setText("Progress");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addComponent(jToggleButton1)
                            .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 238, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(125, 125, 125)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                                    .addComponent(jTextField1)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
                                    .addComponent(jTextField3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))))))
                .addContainerGap(22, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(38, 38, 38)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                    .addComponent(jTextField4))
                .addGap(32, 32, 32)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7)
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton2)
                        .addGap(45, 45, 45)
                        .addComponent(jToggleButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 215, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(91, Short.MAX_VALUE))
        );

        jTabbedPane2.addTab("Task1", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 844, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane2)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField5ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        
        jToggleButton1.setEnabled(false);
        listOfDirectories = null; 
        try {
            // TODO add your handling code here:
            //Check all input values
            if(dialog!=null)
            {
                inputDirectoryPath = dialog.getjFileChooser1().getSelectedFile().getAbsolutePath();
                File[] list = new File(inputDirectoryPath).listFiles();
                if(list.length==0)
                {
                    JOptionPane.showMessageDialog(this,"Given directory is empty !");
                    return;
                }
                listOfDirectories = findDirectories(list);
                System.out.println(listOfDirectories.toArray());
            }else
            {
                JOptionPane.showMessageDialog(this,"Please Select Input Directory ");
                return;
            }
           
            
            
            try{
            bandValue = Double.parseDouble(jTextField1.getText());
            mean = Double.parseDouble(jTextField4.getText());
            wordLength = Double.parseDouble(jTextField3.getText());
            shiftLength = Double.parseDouble(jTextField2.getText());
            standardDeviation = Double.parseDouble(jTextField5.getText());
            }catch(Exception exception){
                JOptionPane.showMessageDialog(this,"Please Enter Valid Inputs"+exception.getMessage());
                return;
            }
         //all good  
           

           progressBar.setValue(10);
           
           MatlabObject matlabObject = new MatlabObject();
           proxy = matlabObject.getMatlabProxy();
           

            // Setup the file system
            ss = null;
            double rBandValueRange[][] = null;
            ss = new CreateFileStructure(inputDirectoryPath,listOfDirectories);

            String path = "cd(\'" + ss.matlabScriptLocation + "')";

            System.out.println("Path is "+path);
            
            
            proxy.eval(path);
                   
            new DataNormalizer(proxy, ss.matlabScriptLocation, ss.inputDataLocation,listOfDirectories);

            CreateGaussianBands gb = new CreateGaussianBands();
             
            
            rBandValueRange = gb.getGaussianBands(ss.inputDataLocation,
                     bandValue, mean,
                     standardDeviation);
            

            AssignLetter.assignToGaussianCurve(proxy, ss.matlabScriptLocation,
                                    ss.inputDataLocation+"/OUTPUTP1",rBandValueRange,listOfDirectories);
            
            
            progressBar.setValue(75);
            
            Object object[][] = new Object[rBandValueRange.length][rBandValueRange[0].length];
            for (int i = 0; i < rBandValueRange.length; i++) {
                for (int j = 0; j < rBandValueRange[i].length; j++) {
                    object[i][j]=(Object)rBandValueRange[i][j];
                }
            }
           
            progressBar.setValue(90);
            
             
            Object header[] = {"Lower","Higher"};
          DefaultTableModel defaultTablePanel = new DefaultTableModel(object, header);
          jTable1.setModel(defaultTablePanel);
          defaultTablePanel.fireTableDataChanged();
          
           //Depending on Number of Directories call construct gesture words N times
            for (int i = 0; i < listOfDirectories.size(); i++) {
                ConstructGestureWords cGestureWords = new ConstructGestureWords();
                System.out.println(inputDirectoryPath+"\\OUTPUTP1\\letter\\"+listOfDirectories.get(i).getName()); 
                cGestureWords.constructGestureWords(wordLength.intValue(), shiftLength.intValue(), inputDirectoryPath+"\\OUTPUTP1\\letter\\"+listOfDirectories.get(i).getName());
                constructGestureWords.add(cGestureWords);
                jComboBox3.addItem(listOfDirectories.get(i).getName()); 
            }

            
            /** Phase 2 Code ***/
            
            //assume only one component right now, hence, index 0 below
            
             SerializeData.serialize("data/test.obj", constructGestureWords.get(0).getTfMapArrayIDF());
            
            Map<Integer, Set<String>> variable1 = createWordsPerSensor(constructGestureWords.get(0).getTfMapArrayIDF());
            List<Map<String, Double[]>> computedScores = createSensorWordScores(variable1, constructGestureWords.get(0).getTfMapArrayIDF());
            
            System.out.println("Scores of phase 2"+computedScores.size());
            
            
            /** Phase 2 Code ***/
            
            
            
            progressBar.setValue(100);
            JOptionPane.showMessageDialog(this,"Processing Complete! Click on Open Output Directory ");
            progressBar.setValue(0); 
            jToggleButton1.setEnabled(true);
            
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MatlabInvocationException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }catch (MatlabConnectionException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            progressBar.setValue(0);
            //try {
                // Disconnect the proxy from MATLAB
                //proxy.exit();
            //} catch (MatlabInvocationException ex) {
//                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            //}
            //proxy.disconnect();
        }
       
        
        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       // TODO add your handling code here:
       dialog = new FilePicker(this, true, JFileChooser.DIRECTORIES_ONLY);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
               });
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton2MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2MouseClicked

    private void jToggleButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton1MouseClicked
        try {
            // TODO add your handling code here:
            Runtime.getRuntime().exec("explorer.exe   "+inputDirectoryPath);
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Problem with Explorer, Please check manually");
        }
    }//GEN-LAST:event_jToggleButton1MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
       
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        
             // TODO add your handling code here:
         dialog = new FilePicker(this, true,JFileChooser.FILES_ONLY); 
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
               });
        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        
            if(constructGestureWords == null){
                JOptionPane.showMessageDialog(this,"Empty Dictionary, Please build dictionary using Task1");
                return;
            }
        
            // TODO add your handling code here:            
            String fileChooser = null;
            try {
            // TODO add your handling code here:
            //Check all input values
            if(dialog!=null)
            {
                fileChooser = dialog.getjFileChooser1().getSelectedFile().getAbsolutePath();  
            }else
            {
                JOptionPane.showMessageDialog(this,"Please Select Input Directory ");
                return;
            }
            
            //normalize, assign letters
            
            File normalizeOutputFile = dialog.getjFileChooser1().getSelectedFile().getParentFile();
            String normalAxisWFile = normalizeOutputFile.getAbsolutePath()+"/nm123.csv";
            String letterAxisWFile = normalizeOutputFile.getAbsolutePath()+"/letter123.csv";
            
           try {
                    proxy.eval("normalize('" + fileChooser + "','" + normalAxisWFile+ "')");
                } catch (MatlabInvocationException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            CreateGaussianBands gb = new CreateGaussianBands();
            double[][] rBandValueRange = gb.getGaussianBands(normalAxisWFile,bandValue, mean,standardDeviation);
            

            AssignLetter.assignLetter(normalAxisWFile, letterAxisWFile, rBandValueRange);
 
            //normalize assign letters
                        
            DistanceFunction distanceFunction = jComboBox2.getSelectedIndex()==0?DistanceFunction.CosineFunction:DistanceFunction.Mahanolobis;
            Entity entity = jComboBox1.getSelectedIndex()==0?Entity.TF:(jComboBox1.getSelectedIndex()==1?Entity.TFIDF:Entity.TFIDF2);
            

            Integer index  = jComboBox3.getSelectedIndex();
            ConstructGestureWords dictionary = constructGestureWords.get(index);
            
            Task3FindSimilarData task3FindSimilarData =  new Task3FindSimilarData(dictionary.getTfIDFMapGlobal(),dictionary.getTfMapArrayIDF(),dictionary.getTfMapArrayIDF2(),wordLength.intValue(),shiftLength.intValue(),letterAxisWFile,dictionary, distanceFunction,entity);
            //constructGestureWordsX.getFileNames() for file Names
            HashMap<Integer, Double> similarityScore = task3FindSimilarData.getSimilarityScore();
            Object[][] object = new Object[similarityScore.entrySet().size()][2];
            int i = 0;
            for (Map.Entry<Integer, Double> entry : similarityScore.entrySet()) { 
		    Integer key = entry.getKey();
		    Double value = entry.getValue();
		    object[i][0]=(Object)dictionary.getFileNames()[key];
                    object[i++][1]=(Object)value;
		}
           Object[] columnNames = {"Multivariate Series Name","Matching Score"};
           
            DefaultTableModel defaultTableModel = new DefaultTableModel(object,columnNames);
            jTable2.setModel(defaultTableModel);
            defaultTableModel.fireTableDataChanged();
           
            //Delete recently created file
            CreateFileStructure.delete(new File(normalAxisWFile));
            CreateFileStructure.delete(new File(letterAxisWFile));
            
        } catch (IOException ex) {
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Something wrong the file score calculations !");
        }
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jComboBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox3ActionPerformed

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
          dialog = new FilePicker(this, true,JFileChooser.FILES_ONLY); 
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
               });
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void jToggleButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton3ActionPerformed
        // TODO add your handling code here:
         if(dialog==null){
             JOptionPane.showMessageDialog(null, "Please Select Input File");
             //return;
         }
         
         
         
         String inputFile = null;
         try      {
            inputFile = dialog.getjFileChooser1().getSelectedFile().getAbsolutePath();
         }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Please select input file");
             return;
         }

         File file = new File(inputFile);
         /*
          * This codes expects input file to be from same input sample location 
         */ 
         File parent = file.getParentFile();
         String normalizedFileName = parent.getParentFile()+"/OUTPUTP1/normalize/"+parent.getName()+"/"+file.getName();
         String task2FilePath = parent.getParentFile()+"/OUTPUTP1/letter/"+parent.getName()+"/task1OutputAll/"+file.getName();
         String letterFileName = parent.getParentFile()+"/OUTPUTP1/letter/"+parent.getName()+"/"+file.getName();
         
         String userChoice = jComboBox4.getSelectedIndex()==0?"TF":(jComboBox4.getSelectedIndex()==1?"IDF":(jComboBox4.getSelectedIndex()==2?"IDF2":jComboBox4.getSelectedIndex()==3?"TF-IDF":"TF-IDF2"));
         
         boolean checkboxticked = checkbox1.getState();
         
         heatMapVisualize = new ShowHeatpMap();
        try {
            image = heatMapVisualize.drawHeatMap(normalizedFileName,task2FilePath,letterFileName,wordLength==null?3:wordLength.intValue(),shiftLength==null?2:shiftLength.intValue(),userChoice,checkboxticked);
             
             //jScrollPane3.setPreferredSize(jScrollPane3.getParent().getMaximumSize());
             boolean drawImage = jScrollPane3.getGraphics().drawImage(image, 0, 0,jScrollPane3.getWidth(),jScrollPane3.getHeight(), null); 
        } catch (IOException ex) {
            
            JOptionPane.showMessageDialog(null, "Something wrong with heatmap generation");
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }

         
         
         
    }//GEN-LAST:event_jToggleButton3ActionPerformed

    private void jToggleButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jToggleButton3MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton3MouseClicked

    private void jToggleButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton4ActionPerformed
        // TODO add your handling code here:
        
               if(dialog==null){
             JOptionPane.showMessageDialog(null, "Please Select Input File");
             return;
         } 

         String inputFile = null;
         try      {
            inputFile = dialog.getjFileChooser1().getSelectedFile().getAbsolutePath();
         }catch(Exception e){
                JOptionPane.showMessageDialog(null, "Please select input file");
             return;
         }

         File file = new File(inputFile);
         /*
          * This codes expects input file to be from same input sample location 
         */ 
         File parent = file.getParentFile();
         String normalizedFileName = parent.getParentFile()+"/OUTPUTP1/normalize/"+parent.getName()+"/"+file.getName();
         String task2FilePath = parent.getParentFile()+"/OUTPUTP1/letter/"+parent.getName()+"/task1OutputAll/"+file.getName();
         String letterFileName = parent.getParentFile()+"/OUTPUTP1/letter/"+parent.getName()+"/"+file.getName();
         
         String userChoice = jComboBox4.getSelectedIndex()==0?"TF":(jComboBox4.getSelectedIndex()==1?"IDF":(jComboBox4.getSelectedIndex()==2?"IDF2":jComboBox4.getSelectedIndex()==3?"TF-IDF":"TF-IDF2"));
         
         boolean checkboxticked = checkbox1.getState();
        ShowHeatpMap heatMapVisualize1 = new ShowHeatpMap();
        try {
            image = heatMapVisualize1.drawHeatMap(normalizedFileName,task2FilePath,letterFileName,wordLength==null?3:wordLength.intValue(),shiftLength==null?2:shiftLength.intValue(),userChoice,checkboxticked);
             
             //jScrollPane3.setPreferredSize(jScrollPane3.getParent().getMaximumSize());
             boolean drawImage = jScrollPane3.getGraphics().drawImage(image, 0, 0, null); 
        } catch (IOException ex) {
            
            JOptionPane.showMessageDialog(null, "Something wrong with heatmap generation");
            Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
        
        //first create heatmap
        
        
        
        Integer[][] boundry = heatMapVisualize1.getBoundry();
             for (int i = 0; i < boundry.length && i <10; i++) {

                int starty = boundry[i][2]*20;  
                int startx = boundry[i][0]*20+80;   //80 x offset
                
                System.out.println("Startx "+startx+"    "+"Starty "+starty);
                int wordl = wordLength==null?3:wordLength.intValue();
                 Graphics2D graphics = (Graphics2D) image.getGraphics();
                                  
                 if(i < 4)
                    graphics.setPaint(Color.RED); 
                 else if(i < 6)
                    graphics.setPaint(Color.GREEN);
                 else if(i < 8)
                    graphics.setPaint(Color.YELLOW);
                 else
                    graphics.setPaint(Color.BLUE);
                 
                 graphics.setFont(new Font( "SansSerif", Font.BOLD, 14 ));
                 graphics.drawString(String.valueOf(i+1), startx, starty); 
                 graphics.drawRect(startx, starty, (boundry[i][1]-boundry[i][0]+1)*20, 20);  //drawing on image 
                jScrollPane3.getGraphics().drawImage(image, 0, 0,jScrollPane3.getWidth(),jScrollPane3.getHeight(), null);
            }
        
    }//GEN-LAST:event_jToggleButton4ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setLocationRelativeTo(null);
                mainWindow.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Checkbox checkbox1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JToggleButton jToggleButton2;
    private javax.swing.JToggleButton jToggleButton3;
    private javax.swing.JToggleButton jToggleButton4;
    // End of variables declaration//GEN-END:variables

    private List<File> findDirectories(File[] listOfDirectories) {
        List<File> fileList = new ArrayList<File>();
        for (int i = 0; i < listOfDirectories.length; i++) {
            if(listOfDirectories[i].isDirectory() && !listOfDirectories[i].isHidden() && !listOfDirectories[i].getName().equalsIgnoreCase("OUTPUTP1") )
                fileList.add(listOfDirectories[i]);
        }
        return fileList;
    }
}
