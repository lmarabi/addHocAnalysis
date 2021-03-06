package org.umn.AdaptiveIndex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.umn.index.PointQ;
import org.umn.index.RectangleQ;
import org.umn.keyword.DayIndex;

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
	private static final long serialVersionUID = Long
			.parseLong("-5265544215767626938");

	public AQuadTree(RectangleQ mbr) {
		spaceMbr = mbr;
		int level = 0;
		this.bucket = null;
		this.elements = null;
		this.hasChild = false;
	}

	// Split the tree into 4 quadrants
	private void split(PointQ p) {
		// System.out.println("Split  call from "+this.level +" with MBR "+
		// this.spaceMbr+" elementSize is "+this.elements.size());
		double subWidth = (this.spaceMbr.getWidth() / 2);
		double subHeight = (this.spaceMbr.getHeight() / 2);
		PointQ midWidth;
		PointQ midHeight;
		midWidth = new PointQ((this.spaceMbr.x1 + subWidth), this.spaceMbr.y1);
		midHeight = new PointQ(this.spaceMbr.x1, (this.spaceMbr.y1 + subHeight));

		RectangleQ swMbr = new RectangleQ(this.spaceMbr.x1, this.spaceMbr.y1,
				midWidth.x, midHeight.y);
		this.SW = new AQuadTree(swMbr);
		this.SW.level = this.level + 1;

		RectangleQ nwMbr = new RectangleQ(midHeight.x, midHeight.y, midWidth.x,
				this.spaceMbr.y2);

		this.NW = new AQuadTree(nwMbr);
		this.NW.level = this.level + 1;

		RectangleQ neMbr = new RectangleQ(midWidth.x, midHeight.y,
				this.spaceMbr.x2, this.spaceMbr.y2);

		this.NE = new AQuadTree(neMbr);
		this.NE.level = this.level + 1;

		RectangleQ seMbr = new RectangleQ(midWidth.x, midWidth.y,
				this.spaceMbr.x2, midHeight.y);

		this.SE = new AQuadTree(seMbr);
		this.SE.level = this.level + 1;

	}

	/***
	 * This method should update the list of keywords
	 * 
	 * @param node
	 * @param mapLevel
	 * @throws ParseException
	 */
	public void InsertKeywords(String keyword, int day, PointQ point)
			throws ParseException {
		// check if there is a child or not before insert
		// First case if node doesn't have child
		if (!this.spaceMbr.contains(point)) {
			return;
		}
		if (this.bucket != null) {
			if (this.bucket.keywordsCount[day] == null) {
				this.bucket.initilizeKeywordbucket(day);
			}
			this.bucket.setVersionKeywords(day, keyword);
			// System.out.println("Insert to level:"+this.level+" with MBR:"+this.spaceMbr);
			if (!this.hasChild) {
				return;
			}
			if (this.SW != null)
				this.SW.InsertKeywords(keyword, day, point);
			if (this.NW != null)
				this.NW.InsertKeywords(keyword, day, point);
			if (this.NE != null)
				this.NE.InsertKeywords(keyword, day, point);
			if (this.SE != null)
				this.SE.InsertKeywords(keyword, day, point);
		}
		/*
		 * Else Case if the node has child we need to trace the place where the
		 * point belong to
		 */

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
				this.bucket = new AQuadBucket();
				this.bucket.incrementtVersionCount(p.date, p.value);
				// System.out.println("insert "+p+" call from "+this.level
				// +" with MBR "+
				// this.spaceMbr+" elementSize is "+this.elements.size());
				return;
			} else {
				// Number of node exceed the capacity split and then reqrrange
				// the Points
				if (this.level < 16) {// 16
					this.split(p);
					this.bucket.incrementtVersionCount(p.date, p.value);
					this.hasChild = true;
					reArrangePointsinChildren(p);
					return;
				} else {
					// change only statistics of the bucket
					this.bucket.incrementtVersionCount(p.date, p.value);
					return;
				}
			}
		}
		/*
		 * Else Case if the node has child we need to trace the place where the
		 * point belong to
		 */
		else {
			this.bucket.incrementtVersionCount(p.date, p.value);
			this.SW.insert(p);
			this.NW.insert(p);
			this.NE.insert(p);
			this.SE.insert(p);

		}

	}

	/***
	 * This is the fist step for the query
	 * 
	 * @param queryMBR
	 * @param fromDate
	 * @param toDate
	 * @param mapLevel
	 * @param keywords
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public ArrayList<PointQ> QueryExecuter(RectangleQ queryMBR,
			String fromDate, String toDate, int mapLevel, String keywords,
			ArrayList<PointQ> values) throws Exception {
		int from = AQuadBucket.getDayYearNumber(fromDate);
		int to = AQuadBucket.getDayYearNumber(toDate);
		int swap; 
		if(from > to){
			swap = from; 
			from = to; 
			to = swap;
		}
		if (keywords == null || keywords.equals("")) {
			this.get(queryMBR, from, to, mapLevel, keywords, values);
		} else {
			
			List<PointQ> result = null;
			for (int i = from; i <= to; i++) {
				AQkeywords temp;
				if (this.bucket.keywordsCount[i] == null || this.bucket.keywordsCount[i].contains(keywords) == false) {
					result = DayIndex.searchKeyword(keywords, i);
					if (result != null) {
						for (PointQ p : result) {
							this.InsertKeywords(keywords, i, p);
						}
					}
				}
			}
			this.get(queryMBR, from, to, mapLevel, keywords, values);
		}
		return values;

	}

	/**
	 * This method get the visualize buckets
	 * 
	 * @param queryMBR
	 * @param values
	 * @return
	 * @throws Exception
	 */
	public ArrayList<PointQ> get(RectangleQ queryMBR, int fromDate,
			int toDate, int mapLevel, String keywords,
			ArrayList<PointQ> values) throws Exception {
		if (this.level == mapLevel && this.bucket != null) {
			// System.out.println("Intersected MBR " + this.spaceMbr + " Level"
			// + this.level);
			PointQ p = this.spaceMbr.getCenterPoint();
			if (keywords == null || keywords.equals("")) {
				p.value = this.bucket.getVersionCount(fromDate, toDate);
			} else {

				p.value = this.bucket.getKeywordCount(fromDate, toDate,
						keywords, this);
			}// end if there is a keywords
			values.add(p);
		} else if (this.hasChild) {
			if (this.NW != null && this.NW.spaceMbr.isIntersected(queryMBR)) {
				this.NW.get(queryMBR, fromDate, toDate, mapLevel, keywords,
						values);
			}
			if (this.NE != null && this.NE.spaceMbr.isIntersected(queryMBR)) {
				this.NE.get(queryMBR, fromDate, toDate, mapLevel, keywords,
						values);
			}
			if (this.SE != null && this.SE.spaceMbr.isIntersected(queryMBR)) {
				this.SE.get(queryMBR, fromDate, toDate, mapLevel, keywords,
						values);
			}
			if (this.SW != null && this.SW.spaceMbr.isIntersected(queryMBR)) {
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

	/***
	 * This method return all the leaves level of a quadrants.
	 * 
	 * @param point
	 * @return
	 */
	public List<RectangleQ> getAllInteresectedLeafs(RectangleQ mbr,
			List<RectangleQ> interesectedLeaves) {
		RectangleQ result = null;
		if (!this.hasChild && this.bucket != null
				&& this.spaceMbr.isIntersected(mbr)) {
			// found the maximum leaf node.
			// if(this.bucket.getTotalCount() > 0){
			interesectedLeaves.add(this.spaceMbr);
			// }
		} else if (this.hasChild) {
			if (this.NW != null) {
				this.NW.getAllInteresectedLeafs(mbr, interesectedLeaves);
			}
			if (this.NE != null) {
				this.NE.getAllInteresectedLeafs(mbr, interesectedLeaves);
			}
			if (this.SE != null) {
				this.SE.getAllInteresectedLeafs(mbr, interesectedLeaves);
			}
			if (this.SW != null) {
				this.SW.getAllInteresectedLeafs(mbr, interesectedLeaves);
			}
		}
		return interesectedLeaves;
	}

	/**
	 * This method will return exist rectangles with keyword value.
	 * 
	 * @param day
	 * @param word
	 * @param existMbrs
	 * @return
	 */
	public ExistRectangls getCountFromChilds(int day, String word,
			ExistRectangls existMbrs) {

		if (this.bucket.keywordsCount[day].getEntry(word) != null) {
			existMbrs.count += this.bucket.keywordsCount[day].getEntry(word).count;
			System.out.println("Children " + this.spaceMbr);
			existMbrs.mbrs.add(this.spaceMbr);
		}

		if (this.SW != null && this.SW.bucket != null
				&& this.SW.bucket.keywordsCount[day] != null) {
			this.SW.getCountFromChilds(day, word, existMbrs);
		}
		if (this.SE != null && this.SE.bucket != null
				&& this.SE.bucket.keywordsCount[day] != null) {
			this.SE.getCountFromChilds(day, word, existMbrs);
		}
		if (this.NW != null && this.NW.bucket != null
				&& this.NW.bucket.keywordsCount[day] != null) {
			this.NW.getCountFromChilds(day, word, existMbrs);
		}
		if (this.NE != null && this.NE.bucket != null
				&& this.NE.bucket.keywordsCount[day] != null) {
			this.NE.getCountFromChilds(day, word, existMbrs);
		}
		return existMbrs;
	}

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
			if (node.bucket != null) {
				if (node.bucket.getTotalCount() > 0) {
					if (isWKT) {

						writer.write(toWKT(node.spaceMbr) + "\t" + node.level
								+ "\t" + node.bucket.getTotalCount() + "\n");

					} else {
						writer.write(node.spaceMbr.x1 + "," + node.spaceMbr.y1
								+ "," + node.spaceMbr.x2 + ","
								+ node.spaceMbr.y2 + "\t");
						for (int i = 0; i < 366; i++) {

							writer.write("\t"
									+ this.bucket.getdateFromDayofYer(i) + ","
									+ node.bucket.versionCount[i]);

						}
						writer.write("\n");
					}
				}
			}

			// System.out.println(counter + "\t" + node.spaceMbr.toString());
		} else {
			printLeafNodes(node.SW, writer, isWKT);
			printLeafNodes(node.NW, writer, isWKT);
			printLeafNodes(node.NE, writer, isWKT);
			printLeafNodes(node.SE, writer, isWKT);

		}
	}

	private void printLeafNodesMBRS(AQuadTree node, OutputStreamWriter writer)
			throws IOException {
		if (!node.hasChild) {
			if (node.bucket != null) {
				if (node.bucket.getTotalCount() > 0) {
					writer.write(node.spaceMbr.x1 + "," + node.spaceMbr.y1
							+ "," + node.spaceMbr.x2 + "," + node.spaceMbr.y2
							+ "\n");
				}
			}
		} else {
			printLeafNodesMBRS(node.SW, writer);
			printLeafNodesMBRS(node.NW, writer);
			printLeafNodesMBRS(node.NE, writer);
			printLeafNodesMBRS(node.SE, writer);
			writer.flush();

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

	/***
	 * This method return the maximum leaf quadrant in the tree that contains a
	 * given points. This method is used to build the inverted index for each
	 * quadrants.
	 * 
	 * @param point
	 * @return
	 */
	public RectangleQ getMaximumLeafNode(PointQ point) {
		RectangleQ result = null;
		if (!this.hasChild && this.bucket != null) {
			// found the maximum leaf node.
			if (this.bucket.getTotalCount() > 0) {
				result = this.spaceMbr;
				return result;
			}
		} else if (this.hasChild) {
			if (this.NW != null && this.NW.spaceMbr.contains(point)) {
				return this.NW.getMaximumLeafNode(point);
			}
			if (this.NE != null && this.NE.spaceMbr.contains(point)) {
				return this.NE.getMaximumLeafNode(point);
			}
			if (this.SE != null && this.SE.spaceMbr.contains(point)) {
				return this.SE.getMaximumLeafNode(point);
			}
			if (this.SW != null && this.SW.spaceMbr.contains(point)) {
				return this.SW.getMaximumLeafNode(point);
			}
		}
		return result;
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
			this.SW.removeStatistics();
			this.NE.removeStatistics();
			this.NW.removeStatistics();
			this.SE.removeStatistics();
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
	public boolean storeQuadToDisk(String outputDir) throws IOException {
		try {
			File f = new File(outputDir + "/AQuadtree.dat");
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
	public boolean loadQuadToMemory(String outputDir) {
		try {
			File f = new File(outputDir + "/AQuadtree.dat");
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

	public void StoreRectanglesWKT(String outputdir) throws IOException {
		// for (int i = 0; i < LevelNumbers; i++) {
		// this.insert(1);
		// }
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(outputdir + "/AQuadtree.WKT", false),
				"UTF-8");
		// printAllNodes(this);
		writer.write("Id\tMBR\tDepth\tCounts\n");
		printLeafNodes(this, writer, true);
		writer.close();
		// System.out.println("number of buckets in the leaves:"+counter+
		// "estimated Size = "+((1.47*counter)/1024)+" MB");
	}

	public void StoreRectanglesToArrayText(String outputdir) throws IOException {
		// for (int i = 0; i < LevelNumbers; i++) {
		// this.insert(1);
		// }
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(outputdir + "/AQuadtree_mbrsCounts.txt",
						false), "UTF-8");
		// printAllNodes(this);
		// writer.write("{");
		printLeafNodes(this, writer, false);
		// writer.write("}");
		writer.close();
		// System.out.println("number of buckets in the leaves:"+counter+
		// "estimated Size = "+((1.47*counter)/1024)+" MB");
	}

	public void StoreLeafsQuadrantsOnly(String outputdir) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(
				new FileOutputStream(outputdir + "/AQuadtree_mbrs.txt", false),
				"UTF-8");
		printLeafNodesMBRS(this, writer);
		writer.close();
	}

}