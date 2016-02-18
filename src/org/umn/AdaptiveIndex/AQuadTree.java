package org.umn.AdaptiveIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.Serializable;

import org.umn.index.PointQ;
import org.umn.index.QuadBucket;
import org.umn.index.RectangleQ;
import org.umn.keyword.InvertedIndex;

/***
 * This class Build the quadtree after the hadoop analysis phase. The
 * functionality supported in this Quadtree as following: 1) Construct the
 * quadtree from the output of hadoop analysis "time method" 2) Has the ability
 * to incrementally add keywords to the quadtree.
 * 
 * @author louai
 *
 */
public class AQuadTree implements Serializable {
	RectangleQ spaceMbr;
	int level;
	AQuadBucket bucket;
	PointQ elements;
	boolean hasChild;
	AQuadTree NW, NE, SE, SW, parent; // four subtrees
	// OutputStreamWriter writer;
	int counter = 0;

	public AQuadTree(RectangleQ mbr, AQuadTree parent) {
		spaceMbr = mbr;
		int level = 0;
		this.bucket = new AQuadBucket();
		this.elements = null;
		this.hasChild = false;
		this.parent = parent;
	}

	// Split the tree into 4 quadrants
	private void split() {
		// System.out.println("Split  call from "+this.level +" with MBR "+
		// this.spaceMbr+" elementSize is "+this.elements.size());
		double subWidth = (this.spaceMbr.getWidth() / 2);
		double subHeight = (this.spaceMbr.getHeight() / 2);
		PointQ midWidth;
		PointQ midHeight;
		midWidth = new PointQ((this.spaceMbr.x1 + subWidth), this.spaceMbr.y1);
		midHeight = new PointQ(this.spaceMbr.x1, (this.spaceMbr.y1 + subHeight));

		this.SW = new AQuadTree(new RectangleQ(this.spaceMbr.x1,
				this.spaceMbr.y1, midWidth.x, midHeight.y), this);
		this.SW.level = this.level + 1;
		this.NW = new AQuadTree(new RectangleQ(midHeight.x, midHeight.y,
				midWidth.x, this.spaceMbr.y2), this);
		this.NW.level = this.level + 1;
		this.NE = new AQuadTree(new RectangleQ(midWidth.x, midHeight.y,
				this.spaceMbr.x2, this.spaceMbr.y2), this);
		this.NE.level = this.level + 1;
		this.SE = new AQuadTree(new RectangleQ(midWidth.x, midWidth.y,
				this.spaceMbr.x2, midHeight.y), this);
		this.SE.level = this.level + 1;
	}

	/**
	 * Insert an object into this tree
	 * 
	 * @throws ParseException
	 */
	public void insert(PointQ p) throws ParseException {
		// check if there is a child or not before insert
		// First case if node doesn't have child
		if (!this.spaceMbr.contains(p))
			return;
		if (!this.hasChild) {
			/*
			 * if the elements in the node less than the capacity insert
			 * otherwise split the node and redistribute the nodes between the
			 * children.
			 */
			if (this.elements == null) {
				this.elements = p;
				this.bucket.incrementtVersionCount(p.date,p.value);
				// System.out.println("insert "+p+" call from "+this.level
				// +" with MBR "+
				// this.spaceMbr+" elementSize is "+this.elements.size());
				return;
			} else {
				// Number of node exceed the capacity split and then reqrrange
				// the Points
				if (this.level < 16) {
					this.split();
					this.bucket.incrementtVersionCount(p.date,p.value);
					this.hasChild = true;
					reArrangePointsinChildren(p);
					return;
				} else {
					// change only statistics of the bucket
					this.bucket.incrementtVersionCount(p.date,p.value);
					return;
				}
			}
		}
		/*
		 * Else Case if the node has child we need to trace the place where the
		 * point belong to
		 */
		else {
			this.bucket.incrementtVersionCount(p.date,p.value);
			// if (this.SW.spaceMbr.contains(p)) {
			this.SW.insert(p);
			// return;
			// } else if (this.NW.spaceMbr.contains(p)) {
			this.NW.insert(p);
			// return;
			// } else if (this.NE.spaceMbr.contains(p)) {
			this.NE.insert(p);
			// return;
			// } else if (this.SE.spaceMbr.contains(p)) {
			this.SE.insert(p);
			// return;
			// }
		}

	}

//	/***
//	 * This method should update the list of keywords
//	 * 
//	 * @param node
//	 * @param mapLevel
//	 */
//	public void adaptiveUpdate(AQuadTree node, int mapLevel) {
//		if (node.level <= mapLevel) {
//
//		}
//
//	}
	

	/**
	 * This method get the visualize buckets
	 * 
	 * @param queryMBR
	 * @param values
	 * @return
	 * @throws Exception 
	 */
	public ArrayList<PointQ> get(RectangleQ queryMBR, String fromDate,
			String toDate, int mapLevel, String keywords,
			ArrayList<PointQ> values) throws Exception {
		if (this.level == mapLevel) {
			System.out.println("Intersected MBR " + this.spaceMbr + " Level"
					+ this.level);
			PointQ p = this.spaceMbr.getCenterPoint();
			if (keywords == null) {
				p.value = this.bucket.getVersionCount(fromDate, toDate);
			} else {
				p.value = this.bucket.getKeywordCount(fromDate, toDate,
						keywords);
				if (p.value == 0) {
					// Need to query form the inverted index and update the
					// quadtree.
//					HashMap<RectangleQ,Integer> mbrKeywordCount = InvertedIndex.searchKeyword(keywords, this.spaceMbr);
//					Iterator it = mbrKeywordCount.entrySet().iterator();
//					while(it.hasNext()){
//						Map.Entry<RectangleQ,Integer> pair = (Map.Entry<RectangleQ,Integer>)it.next();
//				        System.out.println(pair.getKey().getCenterPoint() + " = " + pair.getValue()); 
//					}
					
				}
			}//end if there is a keywords 
			values.add(p);
		} else if (this.hasChild) {
			if (this.NW.spaceMbr.isIntersected(queryMBR)) {
				this.NW.get(queryMBR, fromDate, toDate, mapLevel, keywords,
						values);
			}
			if (this.NE.spaceMbr.isIntersected(queryMBR)) {
				this.NE.get(queryMBR, fromDate, toDate, mapLevel, keywords,
						values);
			}
			if (this.SE.spaceMbr.isIntersected(queryMBR)) {
				this.SE.get(queryMBR, fromDate, toDate, mapLevel, keywords,
						values);
			}
			if (this.SW.spaceMbr.isIntersected(queryMBR)) {
				this.SW.get(queryMBR, fromDate, toDate, mapLevel, keywords,
						values);
			}
			return values;
		}
		// if(this.level == mapLevel){
		// System.out.println("Intersected MBR "+this.spaceMbr+" Level"+this.level);
		// PointQ p = this.spaceMbr.getCenterPoint();
		// p.value = this.bucket.getTweetCount(fromDate, toDate);
		// values.add(p);
		// }
		return values;
	}

	// public void aggregateStatistics(QuadTree node){
	// if(!node.hasChild){
	// if(node.elements.size() >0){
	//
	// }
	// }else{// node has children
	// List<VisoBucket> temp = new ArrayList<VisoBucket>();
	// int[] aggregate = new int[365];
	// for(int i =0; i< 365;i++){
	// node.SE
	// }
	// }
	// }

	/**
	 * This method redistribute the points between the 4 new quadrant child
	 * 
	 * @param list
	 * @throws ParseException
	 */
	private void reArrangePointsinChildren(PointQ p) throws ParseException {
		this.SW.insert(p);
		this.NW.insert(p);
		this.NE.insert(p);
		this.SE.insert(p);
	}

	private void printLeafNodes(AQuadTree node, OutputStreamWriter writer,
			boolean isWKT) throws IOException {
		if (!node.hasChild) {
			if (isWKT) {
				writer.write(toWKT(node.spaceMbr) + "\t" + node.level + "\t"
						+ node.bucket.getTotalCount() + "\n");
			} else {

				// writer.write(", new RectangleQ("+node.spaceMbr.toString()+")");
				writer.write(node.spaceMbr.x1 + "," + node.spaceMbr.y1 + ","
						+ node.spaceMbr.x2 + "," + node.spaceMbr.y2 + "\n");
			}

			// System.out.println(counter + "\t" + node.spaceMbr.toString());
		} else {
			printLeafNodes(node.SW, writer, isWKT);
			printLeafNodes(node.NW, writer, isWKT);
			printLeafNodes(node.NE, writer, isWKT);
			printLeafNodes(node.SE, writer, isWKT);
		}
	}

	private void printAllNodes(AQuadTree node, OutputStreamWriter writer,
			boolean isWKT) throws IOException {
		writer.write(toWKT(node.spaceMbr) + "\n");
		// System.out.println(counter + "\t" + node.spaceMbr.toString());
		printLeafNodes(node.SW, writer, isWKT);
		printLeafNodes(node.NW, writer, isWKT);
		printLeafNodes(node.NE, writer, isWKT);
		printLeafNodes(node.SE, writer, isWKT);
	}

	/**
	 * This method remove statistics and keep the just the MBRs' boundary of the
	 * index
	 * 
	 * @param node
	 */
	public void removeStatistics() {
		if (!this.hasChild) {
			// clear statistics of this node.
			// this.bucket = new QuadBucket();
			this.elements = null;
		} else {
			// this.bucket = new QuadBucket();
			this.elements = null;
			this.SE.removeStatistics();
			this.NE.removeStatistics();
			this.SW.removeStatistics();
			this.NW.removeStatistics();
		}
	}

	public String toWKT(RectangleQ polygon) {
		return (counter++) + "\tPOLYGON ((" + polygon.x2 + " " + polygon.y1
				+ ", " + polygon.x2 + " " + polygon.y2 + ", " + polygon.x1
				+ " " + polygon.y2 + ", " + polygon.x1 + " " + polygon.y1
				+ ", " + polygon.x2 + " " + polygon.y1 + "))";
	}

	/**
	 * Store quad tree to disk
	 * 
	 * @throws IOException
	 */
	public boolean storeQuadToDisk(File f) throws IOException {
		try {
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(spaceMbr);
			oos.writeObject(level);
			oos.writeObject(bucket);
			oos.writeObject(elements);
			oos.writeObject(hasChild);
			oos.writeObject(NW);
			oos.writeObject(NE);
			oos.writeObject(SE);
			oos.writeObject(SW);
			oos.writeObject(parent);
			oos.close();
			fos.close();
		} catch (IOException ex) {
			return false;
		}
		return true;

	}

	/**
	 * Restore quad tree to memory
	 */
	public boolean loadQuadToMemory(File f) {
		try {
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.spaceMbr = (RectangleQ) ois.readObject();
			this.level = (int) ois.readObject();
			this.bucket = (AQuadBucket) ois.readObject();
			this.elements = (PointQ) ois.readObject();
			this.hasChild = (boolean) ois.readObject();
			this.NW = (AQuadTree) ois.readObject();
			this.NE = (AQuadTree) ois.readObject();
			this.SE = (AQuadTree) ois.readObject();
			this.SW = (AQuadTree) ois.readObject();
			this.parent = (AQuadTree) ois.readObject();
			ois.close();
			fis.close();
		} catch (IOException e) {
			System.err.println("Error IO while loading file "
					+ e.getLocalizedMessage() + "\n**" + e.getMessage());
			return false;
		} catch (ClassNotFoundException e) {
			System.err
					.println("Error ClassNotFoundException while loading file "
							+ e.getLocalizedMessage() + "\n**" + e.getMessage());
			return false;
		}
		return true;
	}

	public void StoreRectanglesWKT() throws IOException {
		// for (int i = 0; i < LevelNumbers; i++) {
		// this.insert(1);
		// }
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(System.getProperty("user.dir") + "/../dataset/addHoc/viso_quad.WKT", false), "UTF-8");
		// printAllNodes(this);
		writer.write("Id\tMBR\tDepth\tCounts\n");
		printLeafNodes(this, writer, true);
		writer.close();
		// System.out.println("number of buckets in the leaves:"+counter+
		// "estimated Size = "+((1.47*counter)/1024)+" MB");
	}

	public void StoreRectanglesToArrayText() throws IOException {
		// for (int i = 0; i < LevelNumbers; i++) {
		// this.insert(1);
		// }
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(System.getProperty("user.dir") + "/../dataset/addHoc/quadtree_mbrs.txt", false), "UTF-8");
		// printAllNodes(this);
		// writer.write("{");
		printLeafNodes(this, writer, false);
		// writer.write("}");
		writer.close();
		// System.out.println("number of buckets in the leaves:"+counter+
		// "estimated Size = "+((1.47*counter)/1024)+" MB");
	}

	public static void main(String[] args) throws IOException, ParseException {
		AQuadTree tree = new AQuadTree(new RectangleQ(-180, -90, 180, 90), null);
		File quadfile = new File(System.getProperty("user.dir") + "/../dataset/addHoc/quadtree.dat");
		File data = new File(System.getProperty("user.dir") + "/../dataset/addHoc/dataset/mbrs/part-r-00000");
		if(!data.exists()){
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
		
		
		boolean stored = tree.storeQuadToDisk(quadfile);
		if (stored) {
			System.out.println("Stored Successfully");
		} else {
			System.out.println("Error while Storing ");
		}
		tree.StoreRectanglesWKT();
		tree.StoreRectanglesToArrayText();

	}

}