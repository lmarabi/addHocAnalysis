package org.umn.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.umn.twitter.Convert;

public class Test {

	public static void buildQuadTRee(String path) throws FileNotFoundException,
			IOException, ParseException, InterruptedException {
		QuadTree quadtree = new QuadTree(new RectangleQ(-180, -90, 180, 90),
				5);
		File quadfile = new File(System.getProperty("user.dir") + "/../dataset/addHoc/quadtree.dat");
//		boolean loadQuadToMemory = quadtree.loadQuadToMemory(quadfile);
//		if (loadQuadToMemory) {
//			System.out.println("loaded to memory successfully");
//		}else{
//			System.out.println("Could not load ");
//		}
		File dataFolder = new File(path);
		if(!dataFolder.exists()){
			System.out.println("Data folder doesn't exist");
			return;
		}
		File[] inputfiles = Convert.getOuputFiles(path);
		//"/media/louai/My Book/Twitter_Data_2015/GeotaggedSample/"
				//System.getProperty("user.dir") + "/dataset/");
		//"/media/louai/My Book/Twitter_Data_2014/Twitter_Data/GeotaggedSample"
//		for (int days = 0; days < 3; days++) {
		for (int days = 0; days < inputfiles.length; days++) {			
			File file = inputfiles[days];
			if (file.isFile() || file.getName().equals(".DS_Store")
					|| file.getName().equals("some.txt")) {
				// Do nothing
			} else {
//				int itmes =0; 
				List<File> innerFiles = Convert.listf(file.getAbsolutePath());
				for (File inn : innerFiles) {
//					if(itmes >=1)
//						   break;
//					itmes++;
					String fileName = inn.getAbsolutePath();
					String shortName = inn.getName();
					System.out.println(fileName);
					String LastLine = "";

					BufferedReader br = null;

					try {
						if (!fileName.contains(".gzip") || !fileName.contains(".gz")) {
							br = new BufferedReader(new FileReader(fileName));
						} else {
							// This is extecuted if the dataset is compressed
							// using tar.gz
							br = new BufferedReader(new InputStreamReader(
									new GZIPInputStream(new FileInputStream(
											fileName))));
						}

						String fileNameFixed = shortName.substring(0, 10);

						String line;

						while ((line = br.readLine()) != null) {
							if (line.equals("")) {

							} else {
								LastLine = Convert
										.json2csv(line, fileNameFixed);
								// System.out.println(LastLine);
								if (LastLine != "") {
//									System.out.println(LastLine);
									String[] tweet = LastLine.split(",");
//									System.out.println("added "+(++item));
									PointQ point = new PointQ(
											Double.parseDouble(tweet[tweet.length - 1]),
											Double.parseDouble(tweet[tweet.length - 2]));
									point.date = tweet[0];
										quadtree.insert(point);
								}
							}
						}
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ParseException e) {
						e.printStackTrace();
					}

				}
			}
		}

		

	
		boolean stored = quadtree.storeQuadToDisk(quadfile);
		if (stored) {
			System.out.println("Stored Successfully");
		} else {
			System.out.println("Error while Storing ");
		}
		
		try{
			quadtree.StoreRectanglesWKT();
			quadtree.StoreRectanglesToArrayText();
			quadtree.removeStatistics();
		}catch(Exception e){
			System.err.println("Error Happend while export to WKT");
		}

	}

	public static void QueryQuadTree() throws IOException, ParseException {
		QuadTree quadtree = new QuadTree(new RectangleQ(-180, -90, 180, 90),
				1);
		long startTime, endTime,queryExec_time;
		File file = new File(System.getProperty("user.dir") + "/../dataset/addHoc/versionofQuadTree/2015/quadtree.dat");
		startTime = System.currentTimeMillis();
		boolean loadQuadToMemory = quadtree.loadQuadToMemory(file);
		endTime = System.currentTimeMillis();
		if (loadQuadToMemory) {
			System.out.println("loaded to memory successfully "+" Execution time "+ (endTime - startTime));
//			startTime = System.currentTimeMillis();
//			quadtree.StoreRectanglesWKT();
//			endTime = System.currentTimeMillis();
//			System.out.println("stored WKT successfully to /dataset/addHoc/versionofQuadTree/2015/"+" Execution time "+ (endTime - startTime));
			startTime = System.currentTimeMillis();
			quadtree.StoreRectanglesToArrayText();
			endTime = System.currentTimeMillis();
			System.out.println("stored MBRs successfully /dataset/addHoc/versionofQuadTree/2015/"+" Execution time "+ (endTime - startTime));


//			ArrayList<PointQ> result = new ArrayList<PointQ>();
//			quadtree.get(new RectangleQ(-180, -90, 180, 90),"2014-10-01","2014-12-31",1, result);
//			endTime = System.currentTimeMillis();
//			System.out.println("Result size = " + result.size()+" Execution time "+ (endTime - startTime));
//			for(PointQ p : result){
//				System.out.println(p.value);
//			}
//			System.out.println("remove statistics from quadtree");
//			quadtree.removeStatistics();
//			if(quadtree.storeQuadToDisk(file)){
//				System.out.println("Stored successfully");
//				result.clear();
//				quadtree.StoreRectanglesWKT();
//				quadtree.get(new RectangleQ(-180, -90, 180, 90),"2013-10-01","2013-10-16",1, result);

//				for(PointQ p : result){
//					System.out.println(p.value);
//				}
//			}else{
//				System.out.println("problem occure when saving quadtree to disk");
//			}
			
			
		} else {
			System.out.println("Could not load to memory");
		}
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException, ParseException, InterruptedException {
		//String path = args[0];
		//buildQuadTRee(path);
		QueryQuadTree();

	}

}
