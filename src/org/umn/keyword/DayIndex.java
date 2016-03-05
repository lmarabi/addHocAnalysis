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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
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
import org.umn.index.PointQ;
import org.umn.index.RectangleQ;

public class DayIndex {

	

	/**
	 * Build the inverted index.
	 * 
	 * @param file
	 * @param outputDir
	 * @return
	 * @throws ParseException
	 * @throws Exception
	 */
	public static boolean buildIndex(String file, String outputDir)
			throws ParseException, Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				(new FileInputStream(file))));
		String line;
		int count = 0;
		RectangleQ mbr = null;
		String time;
		PointQ point;
		String keyword;
		HashMap<Integer, Directory> directory = new HashMap<Integer, Directory>();
		HashMap<Integer, IndexWriter> writer = new HashMap<Integer, IndexWriter>();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46,
				analyzer);
		long start = System.currentTimeMillis();
		while ((line = br.readLine()) != null) {
			count++;
			String[] list = line.split("\t");
			keyword = list[0];
			time = list[1];
			System.out.println("Building inverted Index for: " + count);
			int dayofYear = getDayYearNumber(time); 
			if(directory.containsKey(dayofYear) == false){
				directory.put(dayofYear, FSDirectory.open(new File(outputDir + "/inverted/"
				+ getDayYearNumber(time))));
			}
			
			// Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
			
			iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			iwc.setRAMBufferSizeMB(2048.0);
			if(writer.containsKey(dayofYear) == false){
				writer.put(dayofYear, new IndexWriter(directory.get(dayofYear), new IndexWriterConfig(Version.LUCENE_46,
						analyzer)));
			}
			StringBuilder locations = new StringBuilder();
			for (int j = 2; j < list.length; j++) {
				if (!list[j].equals("")) {
					locations.append(list[j] + "\t");

				}
			}

			try {
				addDoc(writer.get(dayofYear), keyword, locations.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			

		}
		long end = System.currentTimeMillis();
		System.out.println("Execution time for Building index in ms:"
				+ (end - start));
		br.close();
		Iterator it = writer.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer,IndexWriter> wr = (Entry<Integer, IndexWriter>) it.next();
			wr.getValue().close();
			
		}
		analyzer.close();
		it = directory.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer,Directory> dir = (Entry<Integer, Directory>) it.next();
			dir.getValue().close();
			
		}
		return true;

	}

	public static List<PointQ> searchKeyword(String keyword,int dayofYear) {
	    Common conf = new Common();
		double starttime = System.currentTimeMillis();
		List<PointQ> outputResult = new ArrayList<PointQ>();

		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(FSDirectory.open(new File(conf.invertedIndexDir
					+ "/inverted/" + dayofYear)));
		} catch (IOException e1) {
			//e1.printStackTrace();
			System.out.println("Inverted Index not found for this day: "+ conf.invertedIndexDir
					+ "/inverted/" + dayofYear);
			return null;
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
					String[] locations = doc.get("count").split("\t");
					for (String p : locations) {
						if (!p.equals("")) {
							outputResult.add(new PointQ(Double.parseDouble( p.substring(0,
									p.indexOf(","))), Double
									.parseDouble(p.substring(
											p.indexOf(",")+1, p.length()-1))));
						}
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
				return null;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
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

	private static int getDayYearNumber(String day) throws ParseException {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateFormat.parse(day)); // Give your own date
		return (cal.get(Calendar.DAY_OF_YEAR) - 1);
	}

	private static void addDoc(IndexWriter w, String keyword, String locations)
			throws IOException, ParseException {
		Document doc = new Document();
		doc.add(new StringField("keyword", keyword, Store.YES));
		doc.add(new StringField("count", locations, Store.YES));
		w.addDocument(doc);
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
//		DayIndex.buildIndex(
//				"/export/scratch/louai/scratch1/workspace/dataset/addHoc/dataset/demokeywordDay/part-00000",
//				"/export/scratch/louai/scratch1/workspace/dataset/addHoc/dataset/demokeywordDay/");
		DayIndex.searchKeyword("I'm",0);

	}

}
