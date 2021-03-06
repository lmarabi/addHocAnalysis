package org.umn.AdaptiveIndex;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.umn.index.RectangleQ;
//import java.util.HashMap;

public class AQuadBucket implements Serializable {
	// Hash keywords
	public AQPriorityQueue[] keywordsCount;
	// cardinality
	public int[] versionCount;
	private static final long serialVersionUID = Long.parseLong("-3143011700429869092");

	public AQuadBucket() {
		// TODO Auto-generated constructor stub
		// HashMap<String, Integer>[] hashMaps = (HashMap<String, Integer>[])
		// new HashMap<?,?>[366];
		keywordsCount = new AQPriorityQueue[366];
		// for (int i = 0; i < 366; i++) {
		// keywordsCount[i] = null;
		// }
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
	public int getVersionCount(int fromdate, int todate)
			throws ParseException {
		int result = 0;
		for (int i = fromdate; i <= todate; i++) {
			result += versionCount[i];
		}
		return result;
	}

	public void initilizeKeywordbucket(int i) {
		if (keywordsCount[i] == null) {
			keywordsCount[i] = new AQPriorityQueue();
		}

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
	public int getKeywordCount(int fromdate, int todate, String word,
			AQuadTree node) throws Exception {
		int result = 0;
		ExistRectangls exist = new ExistRectangls();
		for (int i = fromdate; i <= todate; i++) {
			initilizeKeywordbucket(i);
			AQkeywords temp;
			if ((temp = keywordsCount[i].getEntry(word)) != null) {
				System.out.println("Already Exist in the cash");
				result += temp.count;
				// this update so we can put the the keyword object in the
				// proper place in the queue.
				keywordsCount[i].updateAQKeywords(temp, temp);
			}
//			if (!inCash) {
//				// Try to finds in children for keywords.
//				node.getCountFromChilds(i, word, exist);
//				if ((exist.count > 0) && (!keywordsCount[i].contains(word))) {
//					// update from existing children.
//					System.out.println(" Read From the cash vlaues.");
//					keywordsCount[i].add(new AQkeywords(word, exist.count));
//					// finds out rectangles outside the children boundaries.
//					result += exist.count;
//				}
//				// Usually check for the uncovered rectangles and not existed
//				// keywords.
//				List<RectangleQ> leaves = new ArrayList<RectangleQ>();
//				node.getAllInteresectedLeafs(node.spaceMbr, leaves);
//				List<RectangleQ> leavesTobeRead = new ArrayList<RectangleQ>();
//				missedRectangles(leaves, exist.mbrs, leavesTobeRead);
//				List<PointQ> locations = DayIndex
//						.searchKeyword(word, i);
//				// update the sum of these recatngles to this buckets
//				for(PointQ point : locations){
//					node.InsertKeywords(word, i, point);
//				}
//				
//					
//					
//				
//			}

		}
		return result;
	}

	/**
	 * Return only the missing rectangles to read from the disk.
	 * 
	 * @param intersected
	 * @param existed
	 * @param result
	 * @return
	 */
	private List<RectangleQ> missedRectangles(List<RectangleQ> intersected,
			List<RectangleQ> existed, List<RectangleQ> result) {
		for (RectangleQ mbr : intersected) {
			if (!existed.contains(mbr)) {
				result.add(mbr);
			}
		}
		return result;
	}

	

	public void incrementtVersionCount(String day, int count)
			throws ParseException {
		this.versionCount[getDayYearNumber(day)] += count;
	}

	public int getChildkeywords(int day, String keyword) {
		int result = 0;

		return result;
	}

	public void setVersionKeywords(int day, String keyword) throws ParseException {
		AQkeywords temp;
		if ((temp = this.keywordsCount[day].getEntry(keyword)) == null) {
			this.keywordsCount[day].add(new AQkeywords(keyword, 1));
		} else {
			AQkeywords newvalue = new AQkeywords(keyword, (temp.count + 1));
			keywordsCount[day].updateAQKeywords(temp, newvalue);
		}

	}

	public int getTotalCount() {
		int result = 0;
		for (int i : versionCount) {
			result += i;
		}
		return result;
	}

	public static int getDayYearNumber(String day) throws ParseException {
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateFormat.parse(day)); // Give your own date
			return (cal.get(Calendar.DAY_OF_YEAR) - 1);
		} catch (Exception e) {

		}
		return 0;
	}

	public String getdateFromDayofYer(int dayofYear) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_YEAR, dayofYear);
		String formatDates = dateFormat.format(calendar.getTime());
		return formatDates;
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
