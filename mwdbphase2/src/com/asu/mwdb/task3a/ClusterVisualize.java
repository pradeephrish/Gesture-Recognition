package com.asu.mwdb.task3a;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;

public class ClusterVisualize {
	static int countScript = 0;
	static int countHtml = 0;
	
	public static String createHTML(String inputDirectory,String covername){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("<div style=\"border:2px solid; border-radius:25px; margin: 20px; float:left;\">");
		File file = new File(inputDirectory);
		File[] listFiles = file.listFiles();
		
		for (int i = 0; i < listFiles.length; i++) {
			String compon = listFiles[i].getAbsolutePath();
			compon = compon.substring(compon.lastIndexOf(File.separator)+1);
			//<div class="dynamic0" style="border:2px solid; border-radius:25px; float:left; margin: 5px">
			stringBuffer.append("<div style=\"border:2px solid; border-radius:25px; margin: 5px; float:left;\">");
			String htmlScript="";
			htmlScript = getHtmlScript("PCA");
			htmlScript+= getHtmlScript("SVD");
			htmlScript+= getHtmlScript("LDA");
			htmlScript+= getHtmlScript("TFIDF");
			htmlScript+= getHtmlScript("TFIDF2");
			stringBuffer.append(htmlScript);
			stringBuffer.append("<center>"+compon+"<center>");
			stringBuffer.append("</div>");
		}
		stringBuffer.append("<center>"+covername+"</center>");
		stringBuffer.append("</div>");
		stringBuffer.append("</div>");
		
		return stringBuffer.toString();
	}
	
	private static String getHtmlScript(String string) {
		// TODO Auto-generated method stub
		String outputString = "";
		outputString += "<div class=\"dynamic"+countHtml+"\" style=\"border:2px solid; border-radius:25px; float:left; margin: 5px\">";
		outputString+="<center>"+string+"<center></div>";
		++countHtml;
		return outputString;
	}

	public static  String createSetsJS(String inputDirectory) throws IOException{;
		File file = new File(inputDirectory);
		File[] listFiles = file.listFiles();
		
		StringBuffer stringBuffer = new StringBuffer();
		
		stringBuffer.append("<script>");
		for (int i = 0; i < listFiles.length; i++) {
			String compon = listFiles[i].getAbsolutePath();
			compon = compon.substring(compon.lastIndexOf(File.separator)+1);
//			System.out.println(compon);
			
			File fileIn = new File(inputDirectory+File.separator+compon);
			File[] fileInList = fileIn.listFiles(new FileFilter() {
				
				@Override
				public boolean accept(File pathname) {
					// TODO Auto-generated method stub
					if(pathname.getAbsolutePath().endsWith(".txt"))
						return true;
					return false;
				}
			});
			List<HashSet<String>> listSetLDA = new ArrayList<HashSet<String>>();
			List<HashSet<String>> listSetPCA = new ArrayList<HashSet<String>>();
			List<HashSet<String>> listSetSVD = new ArrayList<HashSet<String>>();
			List<HashSet<String>> listSetTFIDF = new ArrayList<HashSet<String>>();
			List<HashSet<String>> listSetTFIDF2 = new ArrayList<HashSet<String>>();
			
			for (int j = 0; j < fileInList.length; j++) {
				String path = null;
				String[] list = FileUtils.readFileToString(new File(fileInList[j].getAbsolutePath())).split(" ");
				HashSet<String> set = new HashSet<String>();
				set.addAll(Arrays.asList(list));
				if(fileInList[j].getAbsolutePath().contains("LDA"))
				{
					listSetLDA.add(set);
				}else if(fileInList[j].getAbsolutePath().contains("PCA")){
					listSetPCA.add(set);
				}else if(fileInList[j].getAbsolutePath().contains("SVD")){
					listSetSVD.add(set);
				}else if(fileInList[j].getAbsolutePath().contains("TFIDF.")){
					listSetTFIDF.add(set);
				}else if(fileInList[j].getAbsolutePath().contains("TFIDF2")){
					listSetTFIDF2.add(set);
				}
			}
			String script = getScript(listSetPCA);
			script+=getScript(listSetSVD);
			script+=getScript(listSetLDA);
			script+=getScript(listSetTFIDF);
			script+=getScript(listSetTFIDF2);
			stringBuffer.append(script);
		}
		stringBuffer.append("</script>");
		return stringBuffer.toString();
	}
	private static String getScript(List<HashSet<String>> listSet) {
		
		String outputString = "";
		String string = "var sets"+countScript+" = [";
		String next = "";
		for (int j = 0; j < listSet.size()-1; j++) {
			
			next+="{label: 'G"+(j+1)+"', size:"+ listSet.get(j).size()+"},";
		}

		next+="{label: 'G3', size:"+ listSet.get(2).size()+"}];";
		outputString = string+next;
		String overlap = "var overlaps"+countScript+" = ["; 
		int overlap1 = getIntersectionCount(listSet.get(0), listSet.get(1)); //0,1
		overlap+="{sets: ["+0+","+1+"], size:"+overlap1+"},";
		int overlap2 = getIntersectionCount(listSet.get(0), listSet.get(2)); //0,2
		overlap+="{sets: ["+0+","+2+"], size:"+overlap2+"},";
		int overlap3 = getIntersectionCount(listSet.get(1), listSet.get(2)); //1,2
		overlap+="{sets: ["+1+","+2+"], size:"+overlap3+"}];";
		outputString += overlap;
		outputString+="sets"+countScript+" = venn.venn(sets"+countScript+", overlaps"+countScript+");";
		outputString+="venn.drawD3Diagram(d3.select(\".dynamic"+countScript+"\"), sets"+countScript+", 200, 200);";
		countScript++;
		return outputString;
	}
	public static Integer getIntersectionCount(Set<String> s1,Set<String> s2){
		Set<String> intersection = new HashSet<String>(s1); // use the copy constructor
		intersection.retainAll(s2);
		return intersection.size();
	}
	public static void main(String[] args) throws IOException {
		
		try{
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><title>Clusters</title></head><body></body><script src=\"http://d3js.org/d3.v2.min.js\"></script><script src=\"venn.js\"></script><body>");
		buffer.append(ClusterVisualize.createHTML("data/pcagg-clusters", "PCAGG"));
		buffer.append(ClusterVisualize.createHTML("data/svdgg-clusters", "SVDGG"));
		buffer.append("</body>");
		buffer.append(ClusterVisualize.createSetsJS("data/pcagg-clusters"));
		buffer.append(ClusterVisualize.createSetsJS("data/svdgg-clusters"));
		buffer.append("<html>");
		FileUtils.write(new File("visualizeclusters/graph.html"), buffer.toString());
		}catch(Exception e){
			System.out.println("Please run task 3a, before running visualization");
		}
		
		
	}
}
