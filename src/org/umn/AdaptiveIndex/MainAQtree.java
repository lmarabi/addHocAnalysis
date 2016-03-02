package org.umn.AdaptiveIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;

import org.umn.conf.Common;
import org.umn.index.PointQ;
import org.umn.index.QuadTree;
import org.umn.index.RectangleQ;
import org.umn.keyword.LuceneInvertedIndex;

public class MainAQtree {
	static AQuadTree quadtree;

	public static void main(String[] args) throws Exception {
		
		ConstructAQtree();
//		BuildKeywords();
	}

	public static void BuildKeywords() throws Exception {
		Common conf = new Common();
		Common.loadConfigFile();
		quadtree = new AQuadTree(new RectangleQ(-180, -60, 180, 70));
		long startTime, endTime, queryExec_time;
		startTime = System.currentTimeMillis();
		boolean loadQuadToMemory = quadtree.loadQuadToMemory(conf.quadtreeDir);
		if (loadQuadToMemory) {
			endTime = System.currentTimeMillis();
			System.out.println("loaded to memory successfully time to import: "+ + (endTime - startTime));
//			ArrayList<PointQ> result = new ArrayList<PointQ>();
//			quadtree.get(new RectangleQ(102.27193199999994,-42.81557490220856,38.63911949999988,65.58343616151522), "2015-01-01",
//					"2015-12-30", 3, "", result);
//			
//			System.out.println("Result size = " + result.size()
//					+ " Execution time " + (endTime - startTime));
//			for (PointQ p : result) {
//				System.out.println(p.value);
//			}
			
			
			// Build the keyword index for each quadrant in the tree. 
			LuceneInvertedIndex.buildIndex(conf.invertedIndexinputFile, conf.invertedIndexDir,quadtree);

		} else {
			System.out.println("Could not load to memory");
		}
	}

	public static void ConstructAQtree()
			throws Exception {
		Common conf = new Common();
		conf.loadConfigFile();
		long start = System.currentTimeMillis();
		quadtree = new AQuadTree(new RectangleQ(-180, -60, 180, 70));

		File data = new File(conf.quadtreeinputFile);
		if (!data.exists()) {
			System.out.println("Data folder doesn't exist");
			return;
		}
//		int count = 0;

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(data.getAbsoluteFile()))));
		String line;
		RectangleQ mbr = null;
		long starttime = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
				String[] xy = line.substring(0, line.indexOf("\t")).split(",");
				mbr = new RectangleQ(Double.parseDouble(xy[0]),
						Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
						Double.parseDouble(xy[3]));
				String[] list = line.split("\t");
				for (int j = 2; j < list.length; j++) {
					String[] pointCount = list[j].split(",");
					try {
						PointQ point = mbr.getCenterPoint();
						point.date = pointCount[0];
						point.value = Integer.parseInt(pointCount[1]);
						if(point.value > 0){
							quadtree.insert(point);
						}
					} catch (IndexOutOfBoundsException e) {

					}
				}
		}
		System.out.println("Building index took in ms: "+ (System.currentTimeMillis()-starttime));
		br.close();
		starttime = System.currentTimeMillis();
		boolean stored = quadtree.storeQuadToDisk(conf.quadtreeDir);
		System.out.println("Storing index took in ms: "+ (System.currentTimeMillis()-starttime));
		if (stored) {
			starttime = System.currentTimeMillis();
			quadtree.StoreRectanglesWKT(conf.quadtreeDir);
			System.out.println("Storing WKT took in ms: "+ (System.currentTimeMillis()-starttime));
			starttime = System.currentTimeMillis();
			LuceneInvertedIndex.buildIndex(conf.invertedIndexinputFile, conf.invertedIndexDir,quadtree);
			System.out.println("build inverted index took in ms: "+ (System.currentTimeMillis()-starttime));
			starttime = System.currentTimeMillis();
			quadtree.StoreLeafsQuadrantsOnly(conf.quadtreeDir);
			System.out.println("Storing index took in ms: "+ (System.currentTimeMillis()-starttime));
			
		} else {
			System.out.println("Error while Storing ");
		}

	}

}
