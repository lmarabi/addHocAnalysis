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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.umn.conf.Common;
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
	public static HashMap<RectangleQ, Integer> searchKeyword(String keyword,
			RectangleQ queryMbr, int dayofYear) throws Exception {
		// ///////////
		HashMap<RectangleQ, Integer> result = new HashMap<RectangleQ, Integer>();
		Common conf = new Common();
		conf.loadConfigFile();
//		BufferedReader br = new BufferedReader(new InputStreamReader(
//				(new FileInputStream(conf.quadtree_mbrFile))));
//		String line;
		RectangleQ test = null;
		int count = 0;
		List<String> mbrs = listf(conf.invertedIndexDir);
		long start = System.currentTimeMillis();
//		while ((line = br.readLine()) != null) {
		for(String filename : mbrs){
			String[] file = filename.split("=");
		
			String[] xy = file[1].split(",");
			test = new RectangleQ(Double.parseDouble(xy[0]),
					Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
					Double.parseDouble(xy[3]));
			if (queryMbr.isIntersected(test)) {
				InvertedIndex index = new InvertedIndex();
				index.ReadFromDisk(test, dayofYear, conf.invertedIndexDir);
				if (index.keywords.containsKey(keyword)) {
					result.put(test, index.keywords.get(keyword));
				}
			}
		}
//		}
		// ///////////
		return result;
	}
	
	
	public static List<String> listf(String directoryName) {
		File directory = new File(directoryName+"/inverted/");

		List<String> resultList = new ArrayList<String>();

		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				resultList.add(file.getName());
			}
		}
		// System.out.println(fList);
		return resultList;
	}

	/**
	 * This method takes an input and then Build the inverted index.
	 * 
	 * @param inputFile
	 * @return
	 * @throws Exception
	 */
	public static boolean build(String inputFile, String outputdir)
			throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(inputFile))));
		String line;
		RectangleQ mbr = null;
		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] xy = line.substring(0, line.indexOf("\t")).split(",");
			mbr = new RectangleQ(Double.parseDouble(xy[0]),
					Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
					Double.parseDouble(xy[3]));
			String[] list = line.split("\t");
			String time = list[1];
			InvertedIndex index = new InvertedIndex();
			for (int j = 2; j < list.length; j++) {
				String[] keyvalue = list[j].split(",");
				try {
					index.keywords.put(keyvalue[0],
							Integer.parseInt(keyvalue[1]));
				} catch (IndexOutOfBoundsException e) {

				}
			}
			index.storeToDisk(outputdir, mbr, getDayYearNumber(time));
		}
		long end = System.currentTimeMillis();
		System.out.println("Execution time for Building index in ms:"
				+ (end - start));
		br.close();
		return true;
	}

	private static int getDayYearNumber(String day) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFormat.parse(day)); // Give your own date
		return (cal.get(Calendar.DAY_OF_YEAR) - 1);
	}

	/**
	 * This method stores an inverted index to the disk
	 * 
	 * @param mbr
	 * @return
	 * @throws ParseException
	 */
	public boolean storeToDisk(String outputdir, RectangleQ mbr, int dayofYear) {
		FileOutputStream fos;
		try {
			File folder = new File(outputdir + "/inverted/");
			if (!folder.exists()) {
				folder.mkdir();
			}
			fos = new FileOutputStream(new File(outputdir + "/inverted/"
					+ dayofYear + "=" + +mbr.x1 + "," + mbr.y1 + "," + mbr.x2
					+ "," + mbr.y2), true);
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
	public boolean ReadFromDisk(RectangleQ mbr, int dayofYear, String outputdir) {

		try {
			FileInputStream fis = new FileInputStream(new File(outputdir
					+ "/inverted/" + dayofYear + "=" + mbr.x1 + "," + mbr.y1
					+ "," + mbr.x2 + "," + mbr.y2));
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
