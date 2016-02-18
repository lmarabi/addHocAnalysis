package org.umn.AdaptiveIndex;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
//import java.util.HashMap;

public class AQuadBucket implements Serializable{
	static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	//Hash keywords
	private HashMap<String, Integer>[] keywordsCount;
	// cardinality 
	private int[] versionCount; 
	
	public AQuadBucket() {
		// TODO Auto-generated constructor stub
		HashMap<String, Integer>[] hashMaps = (HashMap<String, Integer>[]) new HashMap<?,?>[366];
		keywordsCount =  hashMaps;
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
		return result;
	}
	
	/**
	 * This function search for the keywords in the buckets. 
	 * @param fromdate
	 * @param todate
	 * @param word
	 * @return 0 if doesn't exist , otherwise return >  0
	 * @throws ParseException
	 */
	public int getKeywordCount(String fromdate, String todate,String word) throws ParseException{
		int result = 0; 
		int from = this.getDayYearNumber(fromdate);
		int to = this.getDayYearNumber(todate);
		for(int i = from; i<=to;i++){
			if(keywordsCount[i].containsKey(word)){
				result += keywordsCount[i].get(word);
			}
		}
		return result;
	}
	
	
	public void setVersionCount(String day, int count) throws ParseException {
		this.versionCount[getDayYearNumber(day)] += count;
	}
	
	public void incrementtVersionCount(String day,int count ) throws ParseException {
		this.versionCount[getDayYearNumber(day)] += count;
	}
	
	public void setVersionKeywords(String day, String keyword, int count) throws ParseException{
		this.keywordsCount[getDayYearNumber(day)].put(keyword, count);
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
		AQuadBucket temp  = new AQuadBucket();
		
	}

}
