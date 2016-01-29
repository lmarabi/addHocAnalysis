package org.umn.index;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
//import java.util.HashMap;

public class QuadBucket implements Serializable{
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	//Hash keywords
//	private HashMap<String, Integer> keywords;
	// cardinality 
	private int[] versionCount; 
	
	public QuadBucket() {
		// TODO Auto-generated constructor stub
//		keywords = new HashMap<String, Integer>();
		versionCount = new int[366];
//		for (int j = 0; j < versionCount.length; j++) {
//			versionCount[j] = 0;
//		}
	}
	
	/**
	 * This return the
	 * @param from
	 * @param to
	 * @return
	 * @throws ParseException 
	 */
	public int getVersionCount(String fromdate,String todate) throws ParseException {
		int result = 0; 
		int from = this.getDayYearNumber(fromdate);
		int to = this.getDayYearNumber(todate);
		for(int i = from; i<=to;i++){
			result += versionCount[i];
		}
		return result;
	}
	
	
	public void setVersionCount(String day, int count) throws ParseException {
		this.versionCount[getDayYearNumber(day)] += count;
	}
	
	public void incrementtVersionCount(String day) throws ParseException {
		this.versionCount[getDayYearNumber(day)] += 1;
	}
	
	public int getTotalCount(){
		int result = 0;
		for(int i : versionCount){
			result += i;
		}
		return result;
	}
	
	private int getDayYearNumber(String day) throws ParseException{
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFormat.parse(day)); // Give your own date
		return (cal.get(Calendar.DAY_OF_YEAR) -1);
	}
	
//	public HashMap<String, Integer> getKeywords() {
//		return keywords;
//	}
//	
//	public void setKeywords(HashMap<String, Integer> keywords) {
//		this.keywords = keywords;
//	}
	
	public static void main(String[] args) throws ParseException {
		QuadBucket temp  = new QuadBucket();
		
	}

}
