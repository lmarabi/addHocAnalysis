package org.umn.ServerHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.umn.AdaptiveIndex.AQuadTree;
import org.umn.conf.Common;
import org.umn.index.PointQ;
import org.umn.index.RectangleQ;

import com.google.gson.stream.JsonWriter;

public class HomeServer extends AbstractHandler {
	static AQuadTree quadtree;
	static OutputStreamWriter outputWriter;


	@Override
	public void handle(String s, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, FileNotFoundException,
			UnsupportedEncodingException {
		System.out.println("request received");
		
		try {
			executeRequest(s, baseRequest, request, response);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public synchronized void executeRequest(String s, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, FileNotFoundException,
			UnsupportedEncodingException, InterruptedException {
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("text/html;charset=utf-8");
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Content-Encoding", "gzip");
		response.addHeader("Access-Control-Allow-Credentials", "true");
		baseRequest.setHandled(true);
		String path = request.getPathInfo();
		String fileString = System.getProperty("user.dir")
				+ "/addhocServer.log";
		this.outputWriter = new OutputStreamWriter(new FileOutputStream(
				fileString, true), "UTF-8");

		if (path.equals("/query")) {
			String minLong = baseRequest.getParameter("x1");//min_long
			String minLat = baseRequest.getParameter("y1");//min_lat
			String maxLong = baseRequest.getParameter("x2");//max_long
			String maxLat = baseRequest.getParameter("y2");//min_lat
			String level = baseRequest.getParameter("level");//min_lat
//			String startDate = baseRequest.getParameter("startDate");
//			String endDate = baseRequest.getParameter("endDate");
			String startDate = baseRequest.getParameter("start");
			String endDate = baseRequest.getParameter("end");
			String keyword = baseRequest.getParameter("keyword");
			RectangleQ queryMBR = new RectangleQ(Double.parseDouble(minLong),Double.parseDouble(minLat)
					, Double.parseDouble(maxLong),Double.parseDouble(maxLat));
			long startTime, endTime,queryExec_time;
			startTime = System.currentTimeMillis();
			ArrayList<PointQ> result = new ArrayList<PointQ>();
			System.out.println("Query from "+queryMBR.toString()+"  Dates:"+startDate+"-"+endDate+" Level"
			+level+" Keyword"+ keyword);
			try {
				quadtree.get(queryMBR, startDate, endDate, Integer.parseInt(level), keyword, result);
				quadtree.get(queryMBR, startDate, endDate, (Integer.parseInt(level)+1), keyword, result);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			endTime = System.currentTimeMillis() - startTime;
			System.out.println("Result size = " + result.size()+" Execution time "+ (endTime));
			
			JsonWriter writer = new JsonWriter(new OutputStreamWriter(
					new GZIPOutputStream(response.getOutputStream()),
					"UTF-8"));
			writer.setLenient(true);
			writer.beginObject();
			writer.name("locations");
			writer.beginArray();
			for(PointQ p : result){
				if (p.value > 0) {
					writer.beginObject();
					writer.name("x").value(p.x);
					writer.name("y").value(p.y);
					writer.name("value").value(p.value);
					writer.endObject();
				}
			}
			writer.endArray();
			writer.endObject();
			writer.close();
			this.outputWriter.close();

		} else {
			System.out.println("parameter error");
			response.getWriter()
					.print("<h2> Welcome to this tutorial </h2> <br /><br />"
							+ "These are the main functionalties that are implemeneted in this server <br /><br />"
							+ "1 - Key word Search : (input : MBR + query) <br /><br />"
							+ "<a href='http://10.14.22.10:8085/keywordSearch?q=dubai&max_lat=180&max_long=180&min_lat=-180&min_long=-180'> http://10.14.22.10:8085/keywordSearch?q=dubai&max_lat=180&max_long=180&min_lat=-180&min_long=-180 </a> <br /><br />"
							+ "2- Temporal Search (input : Start Date + End Date + MBR) <br / ><br />"
							+ "<a href='http://10.14.22.10:8085/temporalSearch?startDate=2013-10-12&endDate=2013-10-15&max_lat=180&max_long=180&min_lat=-180&min_long=-180'>http://10.14.22.10:8085/temporalSearch?startDate=2013-10-12&endDate=2013-10-15&max_lat=180&max_long=180&min_lat=-180&min_long=-180</a><br /><br />"
							+ "3- Spatial Search (input : MBR) <br / ><br />"
							+ "<a href='http://10.14.22.10:8085/spatialSearch?max_lat=180&max_long=180&min_lat=-180&min_long=-180'>http://10.14.22.10:8085/spatialSearch?max_lat=180&max_long=180&min_lat=-180&min_long=-180</a><br /><br />"
							+ "4- Tweets per day (input : Start Date + End Date + MBR) <br / ><br />"
							+ "<a href='http://10.14.22.10:8085/dayVolume?startDate=2013-10-12&endDate=2013-10-15&max_lat=180&max_long=180&min_lat=-180&min_long=-180'>http://10.14.22.10:8085/dayVolume?startDate=2013-10-12&endDate=2013-10-15&max_lat=180&max_long=180&min_lat=-180&min_long=-180</a><br /><br />"
							+ "5- Hashtag Count (input : MBR) <br / ><br />"
							+ "<a href='http://10.14.22.10:8085/hashtagcount?max_lat=180&max_long=180&min_lat=-180&min_long=-180'>http://10.14.22.10:8085/hashtagcount?max_lat=180&max_long=180&min_lat=-180&min_long=-180</a><br /><br />"
							+ "6- Popular People (input : MBR) <br / ><br />"
							+ "<a href='http://10.14.22.10:8085/popularPerson?max_lat=180&max_long=180&min_lat=-180&min_long=-180'>http://10.14.22.10:8085/popularPerson?max_lat=180&max_long=180&min_lat=-180&min_long=-180</a><br /><br />"
							+ "7- Active users (input : MBR) <br / ><br />"
							+ "<a href='http://10.14.22.10:8085/activePerson?max_lat=180&max_long=180&min_lat=-180&min_long=-180'>http://10.14.22.10:8085/activePerson?max_lat=180&max_long=180&min_lat=-180&min_long=-180</a><br /><br />"
							+ "<h1> Thank You </h1>");
		}

	}
	
	
	private static void buildQuadtree(String inputdir,String outputdir) throws NumberFormatException, IOException, ParseException{
		long start = System.currentTimeMillis();
		quadtree = new AQuadTree(new RectangleQ(-180, -79, 180, 83));

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
//		quadtree.StoreRectanglesWKT(outputdir);
	}

	public static void main(String[] args) throws Exception {
		Common conf = new Common();
		conf.loadConfigFile();
		buildQuadtree(conf.quadtreeinputFile,conf.quadtreeDir);
		if (quadtree != null) {
			System.out.println("loaded to memory successfully");
		} else {
			System.out.println("Could not load to memory");
		}
		Server server = new Server(8095);
		server.setHandler(new HomeServer());
		server.start();
		server.join();
	}
	


}