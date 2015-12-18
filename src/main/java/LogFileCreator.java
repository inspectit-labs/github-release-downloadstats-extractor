import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class creates a config file of which lists all
 * 
 * @author Tobias Angerstein
 */
public class LogFileCreator {

	/**
	 * The given url
	 */
	private static String URL = "https://api.github.com/repos/inspectit/inspectit/releases";

	/**
	 * Downloads the JSON
	 * 
	 * @return JSONArray
	 */
	private static JSONArray getJSONArray() {
		ResteasyClient client = new ResteasyClientBuilder().build();
		WebTarget target = client.target(URL);
		Response response = target.request().header("User-Agent", "DownloadCounter").get();
		String jsonString = response.readEntity(String.class);
		response.close();
		try {
			return new JSONArray(jsonString);
		} catch (JSONException e) {
			System.err.println(jsonString);
		}
		return null;
	}

	/**
	 * Creates the resultList of "browser_download_url" and "download_count"
	 * 
	 * @param jsonArray
	 *            the downloaded jsonArray
	 * @return the resultList
	 */
	private static ArrayList<String[]> getResultList(JSONArray jsonArray) {
		ArrayList<String[]> resultList = new ArrayList<String[]>();
		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONArray assets = jsonArray.getJSONObject(i).getJSONArray("assets");
				for (int z = 0; z < assets.length(); z++) {
					resultList.add(new String[] {
							assets.getJSONObject(z).get("browser_download_url").toString()
									.replace("https://github.com/inspectIT/inspectIT/releases/download/", "RELEASE."),
							assets.getJSONObject(z).get("download_count").toString() });
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return resultList;
	}

	/**
	 * Writes results in textfile
	 * 
	 * @param resultList
	 *            the results
	 */
	private static void createLogfile(ArrayList<String[]> resultList) {
		String currentMonth = new SimpleDateFormat("yyyy-MM").format(Calendar.getInstance().getTime());
		File logFile = new File(currentMonth + ".txt");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFile));
			for (String[] resultTupel : resultList) {
				writer.write(resultTupel[0] + " " + resultTupel[1]);
				writer.newLine();
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Main Method
	 * 
	 * @param args
	 *            clientID clientSecret
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// GET request without ID's will be executed
			System.out.println("Downloading without Authentification ...");
			createLogfile(getResultList(getJSONArray()));

		} else if (args.length != 0 && args.length != 2) {
			System.err.println(
					"Wrong number of arguments! Please type java -jar Jar.jar :yourClientID :yourClientSecret");
		} else {
			// The Id's will be added
			URL += "?client_id=" + args[0] + "&client_secret=" + args[1];
			System.out.println("Use given Authentification ...");
			createLogfile(getResultList(getJSONArray()));
		}
	}
}