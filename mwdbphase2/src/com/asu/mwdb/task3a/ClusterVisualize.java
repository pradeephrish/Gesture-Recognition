package com.asu.mwdb.task3a;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ClusterVisualize {
	static int count = 0;
	public static  void createSetsJS(String inputDirectory) throws IOException{;
		File file = new File(inputDirectory);
		File[] listFiles = file.listFiles();
		
		System.out.println("<script>");
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
			System.out.println(script);
		}
		System.out.println("</script>");
	}
	private static String getScript(List<HashSet<String>> listSet) {
		
		String outputString = "";
		// TODO Auto-generated method stub
		String string = "var sets"+count+" = [";
		String next = "";
		for (int j = 0; j < listSet.size()-1; j++) {
			//var sets = [{label: "G1", size: 10}, {label: "G2", size: 5}];
			next+="{label: 'G"+(j+1)+"', size:"+ listSet.get(j).size()+"},";
		}

		next+="{label: 'G3', size:"+ listSet.get(2).size()+"}];";
		outputString = string+next;
		String overlap = "var overlaps"+count+" = ["; 
		int overlap1 = getIntersectionCount(listSet.get(0), listSet.get(1)); //0,1
		overlap+="{sets: ["+0+","+1+"], size:"+overlap1+"},";
		int overlap2 = getIntersectionCount(listSet.get(0), listSet.get(2)); //0,2
		overlap+="{sets: ["+0+","+2+"], size:"+overlap2+"},";
		int overlap3 = getIntersectionCount(listSet.get(1), listSet.get(2)); //1,2
		overlap+="{sets: ["+1+","+2+"], size:"+overlap3+"}];";
		//var overlaps = [{sets: [0,1], size: 2}];
//		System.out.println(overlap);
		outputString += overlap;
		//sets1 = venn.venn(sets1, overlaps1);
//		System.out.println("sets"+count+" = venn.venn(sets"+count+", overlaps"+count+");");
		outputString+="sets"+count+" = venn.venn(sets"+count+", overlaps"+count+");";
		//venn.drawD3Diagram(d3.select(".dynamic0"), sets, w, h);
//		System.out.println("venn.drawD3Diagram(d3.select(\".dynamic"+count+"\"), sets"+count+", 200, 200);");
		outputString+="venn.drawD3Diagram(d3.select(\".dynamic"+count+"\"), sets"+count+", 200, 200);";
		count++;
		return outputString;
	}
	public static Integer getIntersectionCount(Set<String> s1,Set<String> s2){
		Set<String> intersection = new HashSet<String>(s1); // use the copy constructor
		intersection.retainAll(s2);
		return intersection.size();
	}
	public static void main(String[] args) throws IOException {
		ClusterVisualize.createSetsJS("data/pcagg-clusters");
	}
}
