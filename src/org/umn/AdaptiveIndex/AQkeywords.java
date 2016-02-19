package org.umn.AdaptiveIndex;

import java.io.Serializable;
import java.util.Comparator;

public class AQkeywords implements Comparable<AQkeywords>, Serializable{
	String keyword;
	int count;
	int priority;

	public AQkeywords(String keyword, int count, int priority) {
		this.keyword = keyword;
		this.count = count;
		this.priority = priority;
	}

	public void increamentPriority() {
		this.priority += 1;
	}

	@Override
	public int compareTo(AQkeywords arg0) {
		return arg0.priority - priority;
	}

}
