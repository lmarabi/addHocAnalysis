package org.umn.ServerHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import org.umn.index.PointQ;
import org.umn.index.QuadTree;
import org.umn.index.RectangleQ;

import com.google.gson.stream.JsonWriter;

public class HomeServer extends AbstractHandler {
	static QuadTree quadtree;
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
			String startDate = "2015-01-01";
			String endDate = "2015-12-31";
			
			long startTime, endTime,queryExec_time;
			startTime = System.currentTimeMillis();
			ArrayList<PointQ> result = new ArrayList<PointQ>();
			try {
				quadtree.get(new RectangleQ(Double.parseDouble(minLong),Double.parseDouble(minLat)
						, Double.parseDouble(maxLong),Double.parseDouble(maxLat))
				,startDate,endDate,Integer.parseInt(level), result);
				
				quadtree.get(new RectangleQ(Double.parseDouble(minLong),Double.parseDouble(minLat)
						, Double.parseDouble(maxLong),Double.parseDouble(maxLat))
				,startDate,endDate,Integer.parseInt(level)+1, result);
				
			} catch (ParseException e) {
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
				writer.beginObject();
				writer.name("x").value(p.x);
				writer.name("y").value(p.y);
				writer.name("value").value(p.value);
				writer.endObject();
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

	public static void main(String[] args) throws Exception {
		quadtree = new QuadTree(new RectangleQ(-180, -90, 180, 90),
				1000);
		File file = new File(System.getProperty("user.dir") + "/quadtree.dat");
		boolean loadQuadToMemory = quadtree.loadQuadToMemory(file);
		if (loadQuadToMemory) {
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