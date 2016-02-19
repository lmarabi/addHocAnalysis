package org.umn.AdaptiveIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;

import org.umn.conf.Common;
import org.umn.index.PointQ;
import org.umn.index.RectangleQ;

public class MainAQtree {

	public static void main(String[] args) throws NumberFormatException, IOException, ParseException {
		Common conf = new Common();
		conf.loadConfigFile();
		ConstructAQtree(conf.quadtreeinputFile, conf.quadtreeDir);
	}

	public static void ConstructAQtree(String inputdir, String outputdir) throws NumberFormatException,
			IOException, ParseException {
		AQuadTree tree = new AQuadTree(new RectangleQ(-180, -90, 180, 90), null);
		
		File data = new File(inputdir);
		if (!data.exists()) {
			System.out.println("Data folder doesn't exist");
			return;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(data.getAbsoluteFile()))));
		String line;
		RectangleQ mbr = null;
		long start = System.currentTimeMillis();
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
					tree.insert(point);
				} catch (IndexOutOfBoundsException e) {

				}
			}

		}

		boolean stored = tree.storeQuadToDisk(outputdir);
		if (stored) {
			tree.StoreRectanglesWKT(outputdir);
			tree.StoreRectanglesToArrayText(outputdir);
			System.out.println("Stored Successfully");
		} else {
			System.out.println("Error while Storing ");
		}
		
	}

}
