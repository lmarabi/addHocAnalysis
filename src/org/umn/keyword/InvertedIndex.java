package org.umn.keyword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.umn.index.RectangleQ;

public class InvertedIndex implements Serializable {
	Map<String, Integer> keywords;

	public InvertedIndex() {
		this.keywords = new HashMap<String, Integer>();
	}

	/**
	 * Search inverted
	 * 
	 * @param keyword
	 * @return
	 * @throws Exception 
	 */
	public static HashMap<RectangleQ,Integer> searchKeyword(String keyword,RectangleQ queryMbr) throws Exception {
		/////////////
		HashMap<RectangleQ,Integer> result = new HashMap<RectangleQ, Integer>();
		String fileName = System.getProperty("user.dir")
				+ "/../dataset/addHoc/dataset/quadtree_mbrs.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(fileName))));
		String line;
		RectangleQ test = null;
		int count = 0;
		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] xy = line.split(",");
			test = new RectangleQ(Double.parseDouble(xy[0]),
					Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
					Double.parseDouble(xy[3]));
			if(queryMbr.isIntersected(test)){
				InvertedIndex index = new InvertedIndex();
				index.ReadFromDisk(test);
				if(index.keywords.containsKey(keyword)){
					result.put(test, index.keywords.get(keyword));
				}
			}
		}
		/////////////
		return result;
	}

	/**
	 * This method takes an input and then Build the inverted index.
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static boolean build(String file) throws Exception {
		String fileName = "";
		if (file == null) {
			fileName = System.getProperty("user.dir") + "/dataset/part-r-00000";
		} else {
			fileName = file;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(fileName))));
		String line;
		RectangleQ mbr = null;
		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] xy = line.substring(0, line.indexOf("\t")).split(",");
			mbr = new RectangleQ(Double.parseDouble(xy[0]),
					Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
					Double.parseDouble(xy[3]));
			String[] list = line.split("\t");
			InvertedIndex index = new InvertedIndex();
			for (int j = 2; j < list.length; j++) {
				String[] keyvalue = list[j].split(",");
				try {
					index.keywords.put(keyvalue[0],
							Integer.parseInt(keyvalue[1]));
				} catch (IndexOutOfBoundsException e) {

				}
			}
			index.storeToDisk(mbr);
		}
		long end = System.currentTimeMillis();
		System.out.println("Execution time for Building index in ms:"
				+ (end - start));
		br.close();
		return true;
	}

	/**
	 * This method stores an inverted index to the disk
	 * 
	 * @param mbr
	 * @return
	 */
	public boolean storeToDisk(RectangleQ mbr) {
		FileOutputStream fos;
		try {
			File folder = new File(System.getProperty("user.dir")
					+ "/inverted/");
			if (!folder.exists()) {
				folder.mkdir();
			}
			fos = new FileOutputStream(new File(System.getProperty("user.dir")
					+ "/inverted/" + mbr.x1 + "," + mbr.y1 + "," + mbr.x2 + ","
					+ mbr.y2),true);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(keywords);
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * This method Read inverted index from the disk
	 * 
	 * @param mbr
	 * @return
	 */
	public boolean ReadFromDisk(RectangleQ mbr) {

		try {
			FileInputStream fis = new FileInputStream(new File(
					System.getProperty("user.dir")
					+ "/inverted/" + mbr.x1 + "," + mbr.y1 + "," + mbr.x2 + ","
					+ mbr.y2));
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.keywords = (HashMap<String, Integer>) ois.readObject();
			ois.close();
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
