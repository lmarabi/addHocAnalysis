package org.umn.keyword;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.umn.index.RectangleQ;

public class keywordSearch {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		RectangleQ mbr = new RectangleQ(-61.3336181640625,89.173583984375,-16.328125,54.17633056640625);
//		keywordSearch.match(mbr, "king");

		//searchKeyQ(mbr,"lfc",true);
		
		//LuceneInvertedIndex.buildIndex(null, true);
		
		
		
//		invertedIndexConstruction();
		invertedIndexSearch(mbr, "lfc");
		

	}
	
	public static void invertedIndexConstruction() throws Exception{
		InvertedIndex.build(null);
	}
	
	public static void invertedIndexSearch(RectangleQ mbr, String keywords) throws Exception{
		HashMap<RectangleQ,Integer> temp = InvertedIndex.searchKeyword(keywords, mbr);
		Iterator it = temp.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
		}
	}
	
	public static void searchKeyQ(RectangleQ mbr , String keywords,boolean lucene) throws Exception{
		
		HashMap<RectangleQ,Integer> temp;
		if(!lucene){
		temp = InvertedIndex.searchKeyword(keywords, mbr);
		}else{
			temp = LuceneInvertedIndex.searchKeyword(keywords, mbr);
		}
		Iterator it = temp.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry pair = (Map.Entry)it.next();
	        System.out.println(pair.getKey() + " = " + pair.getValue());
		}
	}

	/**
	 * This method takes an array of mbrs and find keywords, that match a given
	 * keyword
	 * 
	 * @param mbr
	 * @return
	 * @throws IOException
	 */
	public static int[] match(RectangleQ[] mbr, String keyword)
			throws IOException {
		int[] count = new int[mbr.length];
		String fileName = System.getProperty("user.dir")
				+ "/../dataset/addHoc/dataset/part-r-00000";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(fileName))));
		String line;
		RectangleQ test = null;

		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {

			for (int i = 0; i < mbr.length; i++) {
				String[] xy = line.substring(0, line.indexOf("\t")).split(",");
				test = new RectangleQ(Double.parseDouble(xy[0]),
						Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
						Double.parseDouble(xy[3]));
				if (mbr[i].isIntersected(test)) {
					String[] list = line.split("\t");
					for (int j = 2; j < list.length; j++) {
						if (list[j].contains(keyword)) {
							String[] keyvalue = list[j].split(",");
							if (keyvalue[0].equals(keyword)) {
								//System.out.println(list[j]);
								count[i] = Integer.parseInt(keyvalue[1]);
								break;
							}
						}
					}
				}

			}

		}
		long end = System.currentTimeMillis();
		br.close();
		System.out.println("program end Time: " + (end - start));
		return count;
	}

}
