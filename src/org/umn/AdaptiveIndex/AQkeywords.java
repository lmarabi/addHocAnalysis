package org.umn.AdaptiveIndex;

import java.io.Serializable;

public class AQkeywords implements Serializable{
	 String keyword;
	 int count;

	public AQkeywords(String keyword, int count) {
		this.keyword = keyword;
		this.count = count;
	}

	

}
