package org.umn.index;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
//import java.util.HashMap;

public class QuadBucket implements Serializable{
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//	private static final long serialVersionUID = Long.parseLong("6370864004145947726");


	//Hash keywords
//	private HashMap<String, Integer> keywords;
	// cardinality 
	private int[] versionCount; 
	private static final long serialVersionUID = Long.parseLong("6370864004145947726");
	
	public QuadBucket() {
		// TODO Auto-generated constructor stub
//		keywords = new HashMap<String, Integer>();
		versionCount = new int[366];
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
//		result = versionCount[Integer.parseInt(fromdate)];
		return result;
	}
	
	public String getdateFromDayofYer(int dayofYear){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_YEAR, dayofYear);
		String formatDates = dateFormat.format(calendar.getTime());
		return formatDates;
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
