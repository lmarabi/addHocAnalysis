package org.umn.keyword;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;

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
import org.umn.index.RectangleQ;

public class LuceneInvertedIndex {

	private static int searchInvertedIndex(String index, String queryString) {
		
		double starttime = System.currentTimeMillis();
		int outputResult = -1;

		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File(index)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		IndexSearcher searcher = new IndexSearcher(reader);
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

		QueryParser parser = new QueryParser(Version.LUCENE_46, "keyword",
				analyzer);
		while (true) {
			try {
				String line = queryString;
				line = line.trim();
				if (line.length() == 0) {
					break;
				}
				Query query = parser.parse(line);

				TopDocs results = searcher.search(query, 100);
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
	public static HashMap<RectangleQ,Integer> searchKeyword(String keyword,RectangleQ queryMbr) throws Exception {
		/////////////
		HashMap<RectangleQ,Integer> result = new HashMap<RectangleQ, Integer>();
		String fileName = System.getProperty("user.dir")
				+ "/dataset/quadtree_mbrs.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(fileName))));
		String line;
		RectangleQ test = null;
		int count = 0;
		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] xy = line.split(",");
			test = new RectangleQ(Double.parseDouble(xy[0]),
					Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
					Double.parseDouble(xy[3]));
			if(queryMbr.isIntersected(test)){
				count = searchInvertedIndex(System.getProperty("user.dir") + "/invertedLucene/"
						+ test.x1 + "," + test.y1 + "," + test.x2
						+ "," + test.y2, keyword);
				if(count > 0){
					result.put(test, count);
				}
			}
		}
		/////////////
		return result;
	}
	
	public static boolean buildIndex(String file,
			boolean create) throws ParseException, Exception {
		String fileName = "";
		if (file == null) {
			fileName = System.getProperty("user.dir") + "/dataset/part-r-00000";
		} else {
			fileName = file;
		}

		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(fileName))));
		String line;
		RectangleQ mbr = null;
		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			String[] xy = line.substring(0, line.indexOf("\t")).split(",");
			mbr = new RectangleQ(Double.parseDouble(xy[0]),
					Double.parseDouble(xy[1]), Double.parseDouble(xy[2]),
					Double.parseDouble(xy[3]));
			String[] list = line.split("\t");
			Directory dir = FSDirectory.open(new File(
					System.getProperty("user.dir") + "/invertedLucene/"
							+ mbr.x1 + "," + mbr.y1 + "," + mbr.x2
							+ "," + mbr.y2));
			// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46,
					analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
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
