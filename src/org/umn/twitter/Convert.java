package org.umn.twitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import com.org.json.JSONArray;
import com.org.json.JSONException;
import com.org.json.JSONObject;

/**
 * 
 * @author louai
 *
 */

public class Convert {
//	static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
//	final static String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
//	static SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
	
	static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
	final static String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
	static SimpleDateFormat sf = new SimpleDateFormat(TWITTER);


	public static String json2csvHashtag(String jsonLine) throws ParseException {

		String csvOutput = "";
		List<String> hashes = new ArrayList<String>();

		double[] coords = new double[2];
		try {
			JSONObject post = new JSONObject(jsonLine);
			String created_at = fixDate(post.getString("created_at"));
			JSONObject geo = post.getJSONObject("geo");
			JSONArray coordsJSON = (JSONArray) geo.get("coordinates");
			JSONObject entities = post.getJSONObject("entities");
			JSONArray hashtags1 = (JSONArray) entities.get("hashtags");
			coords[0] = coordsJSON.getDouble(0);
			coords[1] = coordsJSON.getDouble(1);
			JSONObject hashObj;
			for (int i = 0; i < hashtags1.length(); i++) {
				hashObj = hashtags1.getJSONObject(i);
				String hashtagName = hashObj.getString("text");
				hashes.add(hashtagName);
			}
			if (hashes.size() > 0) {
				for (String hash : hashes) {
					csvOutput = created_at + "," + coords[0] + "," + coords[1]
							+ "," + hash + "\n";
				}
			}

		} catch (JSONException e) {

		}

		return csvOutput;
	}

	public static String json2csv(String jsonLine, String shortname)
			throws ParseException {

		String csvOutput = "";
		double[] coords = new double[2];

		try {
			JSONObject post = new JSONObject(jsonLine);
			JSONObject user = post.getJSONObject("user");
			String created_at = fixDate(post.getString("created_at"));
//			String tweetID = post.getString("id_str");
//			String user_id = user.getString("id_str");
//			String user_screen_name = user.getString("screen_name");
//			String tweetText = post.getString("text");
//			String language = user.getString("lang").replace(",", ".");
//			String os = getOperatingSystem(post.getString("source"));
//			String fixedTweetText = tweetText.replace('\n', ' ');
//			String anotherFixedTweetText = fixedTweetText.replace(",", ".");
//			String yetAnotherFixedTweeet = anotherFixedTweetText.replaceAll(
//					"[\\t\\n\\r]", " ");
//			String followers_count = Integer.toString(user
//					.getInt("followers_count"));
			JSONObject geo = post.getJSONObject("geo");
			JSONArray coordsJSON = (JSONArray) geo.get("coordinates");
			coords[0] = coordsJSON.getDouble(0);
			coords[1] = coordsJSON.getDouble(1);

//			csvOutput = created_at + "," + tweetID + "," + user_id + ","
//					+ user_screen_name + "," + yetAnotherFixedTweeet.trim()
//					+ "," + followers_count + "," + language + "," + os + ","
//					+ coords[0] + "," + coords[1] + "\n";
			
			csvOutput = created_at + ","+ coords[0] + "," + coords[1] + "\n";

		} catch (JSONException e) {
			csvOutput = "";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return csvOutput;
	}

	public static String getOperatingSystem(String os) {
		String result = "";
		if (os.contains("BlackBerry")) {
			result = "BlackBerry";
		} else if (os.contains("Android")) {
			result = "Android";
		} else if (os.contains("Mac")) {
			result = "Mac";
		} else if (os.contains("iPhone")) {
			result = "iPhone";
		} else if (os.contains("Nokia")) {
			result = "Nokia";
		} else if (os.contains("Instagram")) {
			result = "Instagram";
		} else if (os.contains("iOS")) {
			result = "iOS";
		} else if (os.contains("FourSquare")) {
			result = "FourSquare";
		} else if (os.contains("Windows")) {
			result = "Windows";
		} else if (os.contains("iPad")) {
			result = "iPad";
		} else {
			result = "Other";
		}
		return result;
	}

	public static Date getTwitterDate(String date) throws ParseException {

//		final String TWITTER = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
//		SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
		sf.setLenient(true);

		return sf.parse(date);
	}

	public static String fixDate(String date) throws ParseException {
		Date dates = getTwitterDate(date);
//		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
		return sdf1.format(dates);
	}

	public static File[] getOuputFiles(String directoryName) {
		File[] temp = new File(directoryName).listFiles();
		Arrays.sort(temp);
		return temp;

	}

	public static List<File> listf(String directoryName) {
		File directory = new File(directoryName);

		List<File> resultList = new ArrayList<File>();

		// get all the files from a directory
		File[] fList = directory.listFiles();
		resultList.addAll(Arrays.asList(fList));
		for (File file : fList) {
			if (file.isFile()) {

			} else if (file.isDirectory()) {
				resultList.addAll(listf(file.getAbsolutePath()));
			}
		}
		// System.out.println(fList);
		return resultList;
	}


	// tweet datafolder output folder
	public static void main(String[] args) throws FileNotFoundException,
			ParseException, Exception {
		double currenttime = System.currentTimeMillis();
		//File[] inputfiles = getOuputFiles(args[1]);
		File[] inputfiles = getOuputFiles(System.getProperty("user.dir")+"/dataset/");
		for (int days = 0; days < inputfiles.length; days++) {
			File file = inputfiles[days];
			if (file.isFile() || file.getName().equals(".DS_Store")
					|| file.getName().equals("some.txt")) {
				// Do nothing
			} else {
				List<File> innerFiles = listf(file.getAbsolutePath());
				for (File inn : innerFiles) {
					writeCSVFile(inn.getAbsolutePath(), inn.getName());
				}

			}
		}
		double endtime = System.currentTimeMillis();
		System.out.println("*************************\n"
				+ "Processing time in seconds = "
				+ ((endtime - currenttime) * 0.001));

	}



	private static void writeCSVFile(String fileName, String shortName) throws IOException {
		System.out.println(fileName);
		String LastLine = "";

		BufferedReader br = null;

		try {
			if (!fileName.contains(".gzip")) {
				br = new BufferedReader(new FileReader(fileName));
			} else {
				// This is extecuted if the dataset is compressed using tar.gz
				br = new BufferedReader(new InputStreamReader(
						new GZIPInputStream(new FileInputStream(fileName))));
			}

			String fileNameFixed = shortName.substring(0, 10);

			String line;

			while ((line = br.readLine()) != null) {
				if (line.equals("")) {

				} else {
					LastLine = json2csv(line, fileNameFixed);
					System.out.println(LastLine);
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
