package org.umn.AdaptiveIndex;

import java.io.Serializable;
import java.util.PriorityQueue;

public class AQPriorityQueue extends PriorityQueue<AQkeywords> implements Serializable{
	
	@Override
	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		while(this.iterator().hasNext()){
			if(this.iterator().next().keyword.equals((String)arg0))
				return true;
		}
		return false;
	}
	
	
	public AQkeywords getEntry(String keyword){
		while(this.iterator().hasNext()){
			AQkeywords entry = this.iterator().next();
			if(entry.keyword.equals(keyword))
				return entry;
		}
		return null;
	}
	
	
	
}



