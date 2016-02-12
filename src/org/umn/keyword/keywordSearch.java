package org.umn.keyword;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.umn.index.RectangleQ;

public class keywordSearch {
	int[] count; 
	RectangleQ[] mbrs;
	
	public keywordSearch(int size) {
		 this.count = new int[size] ;
		 this.mbrs = new RectangleQ[size]; 
	}
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		RectangleQ[] mbr = {new RectangleQ(-180,-90,180,90)};
		keywordSearch temp = keywordSearch.match(mbr);
		
	}
	
	/**
	 * This method takes an array of mbrs and find keywords, that match a given keyword
	 * @param mbr
	 * @return
	 * @throws IOException
	 */
	public static keywordSearch match(RectangleQ[] mbr) throws IOException{
		keywordSearch result = new keywordSearch(mbr.length);
		String fileName = System.getProperty("user.dir")+"/dataset/part-r-00000";
		BufferedReader br = new BufferedReader(new InputStreamReader((new FileInputStream(fileName))));
		String line;

		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] list = line.split("\t");
			for(int i=0 ; i< mbr.length ; i++){
				//System.out.println(list[0]);
			}
			
		}
		long end = System.currentTimeMillis();
		br.close();
		System.out.println("program end Time: "+(end -start));
		return result;
	}

}
