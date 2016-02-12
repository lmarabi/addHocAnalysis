package org.umn.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class QuadTree implements Serializable {
	RectangleQ spaceMbr;
	int nodeCapacity;
	int level;
	QuadBucket bucket;
	List<PointQ> elements;
	boolean hasChild;
	boolean fixed;
	QuadTree NW, NE, SE, SW; // four subtrees
	// OutputStreamWriter writer;
	int counter = 0;

	public QuadTree(RectangleQ mbr, int capacity) {
		spaceMbr = mbr;
		int level = 0;
		this.nodeCapacity = capacity;
		this.bucket = new QuadBucket();
		this.elements = new ArrayList<PointQ>();
		this.hasChild = false;
		this.fixed = false;
	}

	public int depth() {
		if (this != null) {
			return this.depth();
		}
		return 0;
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

		this.SW = new QuadTree(new RectangleQ(this.spaceMbr.x1,
				this.spaceMbr.y1, midWidth.x, midHeight.y), this.nodeCapacity);
		this.SW.level = this.level + 1;
		this.NW = new QuadTree(new RectangleQ(midHeight.x, midHeight.y,
				midWidth.x, this.spaceMbr.y2), this.nodeCapacity);
		this.NW.level = this.level + 1;
		this.NE = new QuadTree(new RectangleQ(midWidth.x, midHeight.y,
				this.spaceMbr.x2, this.spaceMbr.y2), this.nodeCapacity);
		this.NE.level = this.level + 1;
		this.SE = new QuadTree(new RectangleQ(midWidth.x, midWidth.y,
				this.spaceMbr.x2, midHeight.y), this.nodeCapacity);
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
			if (this.elements.size() < this.nodeCapacity) {
				if (!this.fixed) {// this added to prevent adding any points and
									// have afixed tree structure.
					this.elements.add(p);
				}
				this.bucket.incrementtVersionCount(p.date);
				// System.out.println("insert "+p+" call from "+this.level
				// +" with MBR "+
				// this.spaceMbr+" elementSize is "+this.elements.size());
				return;
			} else {
				// Number of node exceed the capacity split and then reqrrange
				// the Points
				if (this.level < 16) {
					this.split();
					this.elements.add(p);
					this.bucket.incrementtVersionCount(p.date);
					this.hasChild = true;
					reArrangePointsinChildren(this.elements);
					this.elements.clear();
					return;
				} else {
					// change only statistics of the bucket
					this.bucket.incrementtVersionCount(p.date);
					return;
				}
			}
		}
		/*
		 * Else Case if the node has child we need to trace the place where the
		 * point belong to
		 */
		else {
			this.bucket.incrementtVersionCount(p.date);
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

	/**
	 * This method get the visualize buckets
	 * 
	 * @param queryMBR
	 * @param values
	 * @return
	 * @throws ParseException
	 */
	public ArrayList<PointQ> get(RectangleQ queryMBR, String fromDate,
			String toDate, int mapLevel, ArrayList<PointQ> values)
			throws ParseException {
		if (this.level == mapLevel) {
			System.out.println("Intersected MBR " + this.spaceMbr + " Level"
					+ this.level);
			PointQ p = this.spaceMbr.getCenterPoint();
			p.value = this.bucket.getVersionCount(fromDate, toDate);
			values.add(p);
		} else if (this.hasChild) {
			if (this.NW.spaceMbr.isIntersected(queryMBR)) {
				this.NW.get(queryMBR, fromDate, toDate, mapLevel, values);
			}
			if (this.NE.spaceMbr.isIntersected(queryMBR)) {
				this.NE.get(queryMBR, fromDate, toDate, mapLevel, values);
			}
			if (this.SE.spaceMbr.isIntersected(queryMBR)) {
				this.SE.get(queryMBR, fromDate, toDate, mapLevel, values);
			}
			if (this.SW.spaceMbr.isIntersected(queryMBR)) {
				this.SW.get(queryMBR, fromDate, toDate, mapLevel, values);
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
	private void reArrangePointsinChildren(List<PointQ> list)
			throws ParseException {
		for (PointQ p : list) {
			this.SW.insert(p);
			this.NW.insert(p);
			this.NE.insert(p);
			this.SE.insert(p);
		}

	}

	private void printLeafNodes(QuadTree node, OutputStreamWriter writer, boolean isWKT)
			throws IOException {
		if (!node.hasChild) {
			if(isWKT){
				writer.write(toWKT(node.spaceMbr) + "\t" + node.level + "\t"
						+ node.bucket.getTotalCount() + "\n");
			}else{
				
				//writer.write(", new RectangleQ("+node.spaceMbr.toString()+")");
				writer.write(node.spaceMbr.x1+","+node.spaceMbr.y1+","+node.spaceMbr.x2+","+node.spaceMbr.y2+"\n");
			}
			
			// System.out.println(counter + "\t" + node.spaceMbr.toString());
		} else {
			printLeafNodes(node.SW, writer,isWKT);
			printLeafNodes(node.NW, writer,isWKT);
			printLeafNodes(node.NE, writer,isWKT);
			printLeafNodes(node.SE, writer,isWKT);
		}
	}

	private void printAllNodes(QuadTree node, OutputStreamWriter writer, boolean isWKT)
			throws IOException {
		writer.write(toWKT(node.spaceMbr) + "\n");
		// System.out.println(counter + "\t" + node.spaceMbr.toString());
		printLeafNodes(node.SW, writer,isWKT);
		printLeafNodes(node.NW, writer,isWKT);
		printLeafNodes(node.NE, writer,isWKT);
		printLeafNodes(node.SE, writer,isWKT);
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
			//this.bucket = new QuadBucket();
			this.elements = null;
			this.fixed = true;
		} else {
			//this.bucket = new QuadBucket();
			this.elements = null;
			this.fixed = true;
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
			oos.writeObject(nodeCapacity);
			oos.writeObject(level);
			oos.writeObject(bucket);
			oos.writeObject(elements);
			oos.writeObject(hasChild);
			oos.writeObject(fixed);
			oos.writeObject(NW);
			oos.writeObject(NE);
			oos.writeObject(SE);
			oos.writeObject(SW);
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
			this.nodeCapacity = (int) ois.readObject();
			this.level = (int) ois.readObject();
			this.bucket = (QuadBucket) ois.readObject();
			this.elements = (List<PointQ>) ois.readObject();
			this.hasChild = (boolean) ois.readObject();
			this.fixed = (boolean) ois.readObject();
			this.NW = (QuadTree) ois.readObject();
			this.NE = (QuadTree) ois.readObject();
			this.SE = (QuadTree) ois.readObject();
			this.SW = (QuadTree) ois.readObject();
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
				new FileOutputStream(System.getProperty("user.dir")
						+ "/viso_quad.WKT", false), "UTF-8");
		// printAllNodes(this);
		writer.write("Id\tMBR\tDepth\tCounts\n");
		printLeafNodes(this, writer,true);
		writer.close();
		// System.out.println("number of buckets in the leaves:"+counter+
		// "estimated Size = "+((1.47*counter)/1024)+" MB");
	}
	
	
	public void StoreRectanglesToArrayText() throws IOException {
		// for (int i = 0; i < LevelNumbers; i++) {
		// this.insert(1);
		// }
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(System.getProperty("user.dir")
						+ "/quadtree_mbrs.txt", false), "UTF-8");
		// printAllNodes(this);
		//writer.write("{");
		printLeafNodes(this, writer,false);
		//writer.write("}");
		writer.close();
		// System.out.println("number of buckets in the leaves:"+counter+
		// "estimated Size = "+((1.47*counter)/1024)+" MB");
	}

	public static void main(String[] args) throws IOException {
		QuadTree test = new QuadTree(new RectangleQ(-180, -90, 180, 90), 1);
		// test.packInRectangleQs(17);

	}

}