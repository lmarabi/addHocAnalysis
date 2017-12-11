package org.umn.AdaptiveIndex;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

public class AQPriorityQueue implements Serializable{
	static final int size = 5;
	private LinkedBlockingQueue<AQkeywords> keywordList;
	
	public AQPriorityQueue() {
		keywordList = new LinkedBlockingQueue<AQkeywords>(size);
		
	}
	
	
	/**
	 * This method add based on the priority of the keywords
	 * The grater priority is the more it will stay in the index 
	 * the less priority will be removed sooner. 
	 * @param word
	 */
	public void add(AQkeywords word){
		if(keywordList.size() < size){
			keywordList.add(word);
		}else{
			try {
				keywordList.take();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			keywordList.add(word);
		}
	}
	
	/**
	 * This method check if the keyword exist in the 
	 * Priority queue 
	 * @param word
	 * @return
	 */
	public boolean contains(String word){
		Iterator<AQkeywords> it = keywordList.iterator();
		while(it.hasNext()){
			if(it.next().keyword.equals(word)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * This method retrieve the priority queue based on keyword.
	 * @param word
	 * @return
	 */
	public AQkeywords getEntry(String word){
		Iterator<AQkeywords> it = keywordList.iterator();
		while(it.hasNext()){
			AQkeywords temp = it.next();
			if(temp.keyword.equals(word)){
				return temp;
			}
		}
		return null;
	}
	
	/**
	 * This method update the value and the priority of a keywords.
	 * @param word
	 */
	public void updateAQKeywords(AQkeywords oldWord, AQkeywords newWord){
		keywordList.remove(oldWord);
		add(newWord);
	}

	
	
}



