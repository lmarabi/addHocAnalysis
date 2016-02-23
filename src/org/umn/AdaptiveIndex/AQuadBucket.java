package org.umn.AdaptiveIndex;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
//import java.util.HashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;

import org.umn.index.RectangleQ;
import org.umn.keyword.InvertedIndex;

public class AQuadBucket implements Serializable {
	// Hash keywords
	private AQPriorityQueue[] keywordsCount;
	// cardinality
	private int[] versionCount;

	public AQuadBucket() {
		// TODO Auto-generated constructor stub
		// HashMap<String, Integer>[] hashMaps = (HashMap<String, Integer>[])
		// new HashMap<?,?>[366];
		keywordsCount = new AQPriorityQueue[366];
		for (int i = 0; i < 366; i++) {
			keywordsCount[i] = new AQPriorityQueue();
		}
		versionCount = new int[366];
	}

	/**
	 * This return the
	 * 
	 * @param from
	 * @param to
	 * @return
	 * @throws ParseException
	 */
	public int getVersionCount(String fromdate, String todate)
			throws ParseException {
		int result = 0;
		int from = this.getDayYearNumber(fromdate);
		int to = this.getDayYearNumber(todate);
		for (int i = from; i <= to; i++) {
			result += versionCount[i];
		}
		return result;
	}

	/**
	 * This function search for the keywords in the buckets.
	 * 
	 * @param fromdate
	 * @param todate
	 * @param word
	 * @return 0 if doesn't exist , otherwise return > 0
	 * @throws Exception
	 */
	public int getKeywordCount(String fromdate, String todate, String word,
			AQuadTree node) throws Exception {
		int result = 0;
		int count = 0;
		int from = this.getDayYearNumber(fromdate);
		int to = this.getDayYearNumber(todate);
		for (int i = from; i <= to; i++) {
			AQkeywords temp;
			if ((temp = keywordsCount[i].getEntry(word)) != null) {
				result += temp.count;
				// this update so we can put the the keyword object in the
				// proper place in the queue.
				keywordsCount[i].updateAQKeywords(temp, temp);
			} else {
				// keyword doesn't exist in the AQtree , need to be query from
				// the inverted index and then updates all leaves nodes.
				HashMap<RectangleQ, Integer> keyvalue = InvertedIndex
						.searchKeyword(word, node.spaceMbr, i);
				// update the sum of these recatngles to this buckets
				Iterator<Entry<RectangleQ, Integer>> it = keyvalue.entrySet()
						.iterator();
				while (it.hasNext()) {
					Map.Entry<RectangleQ, Integer> entry = it.next();
					node.InsertKeywords(word, entry.getKey(), i,
							entry.getValue());
					count += entry.getValue();
				}
				result = count;
			}
		}
		return result;
	}

	public void incrementtVersionCount(String day, int count)
			throws ParseException {
		this.versionCount[getDayYearNumber(day)] += count;
	}

	public void setVersionKeywords(int day, String keyword, int count,
			boolean hasChild) throws ParseException {
		AQkeywords temp;
		if ((temp = this.keywordsCount[day].getEntry(keyword)) == null) {
			this.keywordsCount[day].add(new AQkeywords(keyword, count));
		} else {
			if (hasChild) {
				AQkeywords newvalue = new AQkeywords(keyword,
						(temp.count + count));
				keywordsCount[day].updateAQKeywords(temp, newvalue);
			} else {
				// do nothing as they are already inserted in the first outer if
				// statement
			}
		}

	}

	public int getTotalCount() {
		int result = 0;
		for (int i : versionCount) {
			result += i;
		}
		return result;
	}

	private int getDayYearNumber(String day) throws ParseException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateFormat.parse(day)); // Give your own date
			return (cal.get(Calendar.DAY_OF_YEAR) - 1);
		} catch (Exception e) {

		}
		return 0;
	}

	// public HashMap<String, Integer> getKeywords() {
	// return keywords;
	// }
	//
	// public void setKeywords(HashMap<String, Integer> keywords) {
	// this.keywords = keywords;
	// }

	public static void main(String[] args) throws ParseException {
		AQuadBucket temp = new AQuadBucket();

	}

}
