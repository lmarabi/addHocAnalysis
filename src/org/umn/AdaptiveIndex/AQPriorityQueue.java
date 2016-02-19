package org.umn.AdaptiveIndex;

import java.io.Serializable;

public class AQPriorityQueue implements Serializable{
	
	private AQkeywords[] keywordList; 
	
	public AQPriorityQueue() {
		keywordList = new AQkeywords[5];
		for(int i =0; i< 5; i++){
			keywordList[i] = new AQkeywords("", 0, 0);
		}
		
	}
	
	/**
	 * This method add based on the priority of the keywords
	 * The grater priority is the more it will stay in the index 
	 * the less priority will be removed sooner. 
	 * @param word
	 */
	public void add(AQkeywords word){
		int min = keywordList[0].priority;
		int index = 0;
		for(int i =1; i< 5; i++){
			if(keywordList[i].priority < min){
				index = i;
			}
		}
		keywordList[index] = word;
	}
	
	/**
	 * This method check if the keyword exist in the 
	 * Priority queue 
	 * @param word
	 * @return
	 */
	public boolean contains(String word){
		for(int i =0; i< 5; i++){
			if(keywordList[i].keyword.equals(word)){
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
		for(int i =0; i< 5; i++){
			if(keywordList[i].keyword.equals(word)){
				return keywordList[i];
			}
		}
		return null;
	}
	
	/**
	 * This method update the value and the priority of a keywords.
	 * @param word
	 */
	public void updateAQKeywords(AQkeywords word){
		for(int i =0; i< 5; i++){
			if(keywordList[i].keyword.equals(word.keyword)){
				keywordList[i].priority += 1; 
				keywordList[i].count += word.count;
			}
		}
	}

	
	
	
}



