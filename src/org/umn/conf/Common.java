package org.umn.conf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Common {

	public static String quadtreeDir;
	public static String quadtreeinputFile;
	public static String quadtree_mbrFile;
	public static String invertedIndexDir;
	public static String invertedIndexinputFile;
	

	public static void loadConfigFile() throws IOException {

		Properties prop = new Properties();
		prop.load(new FileInputStream("config.properties"));
		Common.quadtreeDir = prop.getProperty("quadtreeDir");
		Common.quadtreeinputFile = prop.getProperty("quadtreeinputFile");
		Common.quadtree_mbrFile = prop.getProperty("quadtree_mbrFile");
		Common.invertedIndexDir = prop
				.getProperty("invertedIndexDir");
		Common.invertedIndexinputFile = prop
				.getProperty("invertedIndexinputFile");
		System.out.println("Config file Loaded");

	}

}
