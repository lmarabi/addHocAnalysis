package org.umn.keyword;

import org.umn.conf.Common;


public class MainInvertedIndex {

	public static void main(String[] args) throws Exception {
		Common conf = new Common(); 
		System.out.println("Building invertedIndex");
		conf.loadConfigFile();
		long start = System.currentTimeMillis();
//		InvertedIndex.build(conf.invertedIndexinputFile, conf.invertedIndexDir);
		LuceneInvertedIndex.buildIndex(conf.invertedIndexinputFile, conf.invertedIndexDir);
		
		long end = System.currentTimeMillis();
		System.out.println("program end Time: " + (end - start));
	}

}
