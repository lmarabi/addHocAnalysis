package org.umn.keyword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.umn.AdaptiveIndex.AQuadTree;
import org.umn.conf.Common;
import org.umn.index.RectangleQ;

public class LuceneInvertedIndex {

	private static List<String> listf(String directoryName) {
		File directory = new File(directoryName + "/inverted/");

		List<String> resultList = new ArrayList<String>();

		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				resultList.add(file.getName());
			}
		}
		// System.out.println(fList);
		return resultList;
	}

	private static int readInvertedIndex(String directory, int dayofYear,
			String keyword, RectangleQ mbr) {
		double starttime = System.currentTimeMillis();
		int outputResult = 0;

		IndexReader reader = null;
		try {
			reader = DirectoryReader
					.open(FSDirectory.open(new File(directory
							+ "/inverted/" + dayofYear + "=" + mbr.x1 + "," + mbr.y1
							+ "," + mbr.x2 + "," + mbr.y2)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

		QueryParser parser = new QueryParser(Version.LUCENE_46, "keyword",
				analyzer);
		while (true) {
			try {
				String line = keyword;
				line = line.trim();
				if (line.length() == 0) {
					break;
				}
				Query query = parser.parse(line);

				TopDocs results = searcher.search(query, 1);
				System.out.println("** " + results.totalHits);
				// PrintWriter printWriter = new PrintWriter(new
				// BufferedWriter(new
				// FileWriter("SearchResultPointsArabicSimplified.csv", true)));
				for (int i = 0; i < results.scoreDocs.length; ++i) {
					Document doc = searcher.doc(results.scoreDocs[i].doc);
					outputResult = Integer.parseInt(doc.get("count"));
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}

		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		double endtime = System.currentTimeMillis();
		System.out.println("Execution time in milliseconds: "
				+ (endtime - starttime));
		return outputResult;
	}
	
	
	/**
	 * Search inverted
	 * 
	 * @param keyword
	 * @return
	 * @throws Exception
	 */
	public static HashMap<RectangleQ, Integer> searchKeyword(String keyword,
			int dayofYear, List<RectangleQ> queryleavesNode) throws Exception {
		// ///////////
		HashMap<RectangleQ, Integer> result = new HashMap<RectangleQ, Integer>();
		Common conf = new Common();
		conf.loadConfigFile();
		long start = System.currentTimeMillis();
		int count = 0;
		for (RectangleQ mbr : queryleavesNode) {
			System.out.println("Read inverted from the disk "+ mbr.toString());
			count = readInvertedIndex(conf.invertedIndexDir, dayofYear,
					keyword,mbr);
				result.put(mbr, count);
		}
		
		// }
		// ///////////
		return result;
	}

	/**
	 * Search the inverted index. 
	 * @param queryMbr
	 * @param existMBr
	 * @param keyword
	 * @param dayofyear
	 * @return
	 * @throws IOException
	 */
	public static HashMap<RectangleQ, Integer> searchInvertedIndex(
			RectangleQ queryMbr, List<RectangleQ> existMBr, String keyword,
			int dayofyear) throws IOException {

		HashMap<RectangleQ, Integer> result = new HashMap<RectangleQ, Integer>();
		Common conf = new Common();
		conf.loadConfigFile();

		RectangleQ test = null;
		int count = 0;
		List<String> mbrs = listf(conf.invertedIndexDir);
		long start = System.currentTimeMillis();
		for (String filename : mbrs) {
			String[] file = filename.split("=");
			if (file[0].equals(Integer.toString(dayofyear))) {
				String[] xy = file[1].split(",");
				test = new RectangleQ(Double.parseDouble(xy[0]),
						Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
						Double.parseDouble(xy[3]));
				if ((!existMBr.contains(test)) && queryMbr.isIntersected(test)) {
					count = readInvertedIndex(conf.invertedIndexDir, dayofyear,
							keyword,test);
					result.put(test, count);
				}
			}

		}
		return result;
	}



	private static int getDayYearNumber(String day) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFormat.parse(day)); // Give your own date
		return (cal.get(Calendar.DAY_OF_YEAR) - 1);
	}

	/**
	 * Build the inverted index.
	 * @param file
	 * @param outputDir
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	public static boolean buildIndex(String file, String outputDir,AQuadTree tree)
			throws ParseException, Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(file))));
		String line;
		int count = 0; 
		RectangleQ mbr = null;
		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			count++;
			String[] xy = line.substring(0, line.indexOf("\t")).split(",");
			mbr = new RectangleQ(Double.parseDouble(xy[0]),
					Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
					Double.parseDouble(xy[3]));
			String[] list = line.split("\t");
			String time = list[1];
			mbr = tree.getMaximumLeafNode(mbr.getCenterPoint());
			if(mbr == null ){
				continue;
			}
			System.out.println("Building inverted Index for: "+count);
			Directory dir = FSDirectory.open(new File(outputDir + "/inverted/"
					+ getDayYearNumber(time) + "=" + +mbr.x1 + "," + mbr.y1
					+ "," + mbr.x2 + "," + mbr.y2));
			// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46,
					analyzer);
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			iwc.setRAMBufferSizeMB(2048.0);
			IndexWriter writer = new IndexWriter(dir, iwc);
			for (int j = 3; j < list.length; j++) {
				String[] keyvalue = list[j].split(",");
				try {
					addDoc(writer, keyvalue[0], keyvalue[1]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			writer.close();
			analyzer.close();
			dir.close();
			
		}
		long end = System.currentTimeMillis();
		System.out.println("Execution time for Building index in ms:"
				+ (end - start));
		br.close();
		return true;

	}

	private static void addDoc(IndexWriter w, String keyword, String count)
			throws IOException, ParseException {
		Document doc = new Document();
		doc.add(new StringField("keyword", keyword, Store.YES));
		doc.add(new StringField("count", count, Store.YES));
		w.addDocument(doc);
	}

}
