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

public class MainAQtree {
	static AQuadTree quadtree;

	public static void main(String[] args) throws Exception {
		Common conf = new Common();
		conf.loadConfigFile();
		ConstructAQtree(conf.quadtreeinputFile, conf.quadtreeDir);
		 testQuery(conf.quadtreeDir);
	}

	public static void testQuery(String quadtreeDir) throws Exception {
		 quadtree = new AQuadTree(new RectangleQ(-180, -90, 180, 90));
//		boolean loadQuadToMemory = quadtree.loadQuadToMemory(quadtreeDir);
//		if (loadQuadToMemory) {
			System.out.println("loaded to memory successfully");
			long startTime, endTime, queryExec_time;
			startTime = System.currentTimeMillis();
			ArrayList<PointQ> result = new ArrayList<PointQ>();
			quadtree.get(new RectangleQ(102.27193199999994,-42.81557490220856,38.63911949999988,65.58343616151522), "2015-01-01",
					"2015-12-30", 3, "", result);
			endTime = System.currentTimeMillis();
			System.out.println("Result size = " + result.size()
					+ " Execution time " + (endTime - startTime));
			for (PointQ p : result) {
				System.out.println(p.value);
			}
			// System.out.println("remove statistics from quadtree");
			// quadtree.removeStatistics();
			// if(quadtree.storeQuadToDisk(file)){
			// System.out.println("Stored successfully");
			// result.clear();
			// quadtree.StoreRectanglesWKT();
			// quadtree.get(new RectangleQ(-180, -90, 180,
			// 90),"2013-10-01","2013-10-16",1, result);
			// endTime = System.currentTimeMillis();
			// System.out.println("Result size = " +
			// result.size()+" Execution time "+ (endTime - startTime));
			// for(PointQ p : result){
			// System.out.println(p.value);
			// }
			// }else{
			// System.out.println("problem occure when saving quadtree to disk");
			// }

//		} else {
//			System.out.println("Could not load to memory");
//		}
	}

	public static void ConstructAQtree(String inputdir, String outputdir)
			throws NumberFormatException, IOException, ParseException {
		long start = System.currentTimeMillis();
		quadtree = new AQuadTree(new RectangleQ(-180, -90, 180, 90));

		File data = new File(inputdir);
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
						quadtree.insert(point);
					} catch (IndexOutOfBoundsException e) {

					}
				}
		}
		System.out.println("Building index took in ms: "+ (System.currentTimeMillis()-starttime));
//
//		starttime = System.currentTimeMillis();
//		boolean stored = quadtree.storeQuadToDisk(outputdir);
//		System.out.println("Storing index took in ms: "+ (System.currentTimeMillis()-starttime));
//		if (stored) {
//			starttime = System.currentTimeMillis();
//			quadtree.StoreRectanglesWKT(outputdir);
//			quadtree.StoreRectanglesToArrayText(outputdir);
//			System.out.println("Stored Successfully");
//			
//		} else {
//			System.out.println("Error while Storing ");
//		}

	}

}
