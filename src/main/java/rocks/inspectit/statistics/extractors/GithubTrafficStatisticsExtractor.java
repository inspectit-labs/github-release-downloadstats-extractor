package rocks.inspectit.statistics.extractors;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;
import org.influxdb.InfluxDB;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import rocks.inspectit.statistics.entities.GithubTrafficStatisticsEntity;

public class GithubTrafficStatisticsExtractor extends AbstractExtractor<GithubTrafficStatisticsEntity> {
	private static final String URL_KEY = "github.traffic.url";
	private static final String TOP_LIST_URL_KEY = "github.traffic.url.toplist";
	private static final String GITHUB_USERNAME_KEY = "github.username";
	private static final String GITHUB_PASSWORD_KEY = "github.password";

	private static final String GITHUB_LOGIN_URL = "https://github.com/login";
	private static final String GITHUB_SESSION_URL = "https://github.com/session";
	private static final String SEARCH_PATTERN_AUTHEN_TOKEN = "<input name=\"authenticity_token\" type=\"hidden\" value=\"";

	public GithubTrafficStatisticsExtractor(Properties properties, InfluxDB influxDB) {
		super(properties);
		init(GithubTrafficStatisticsEntity.getTemplate(), influxDB);
	}

	@Override
	public List<GithubTrafficStatisticsEntity> getResultList() {
		System.out.println("Retrieving Github Traffic statistics...");
		String jsonString = getJSONString(getProperties().getProperty(URL_KEY));
		List<GithubTrafficStatisticsEntity> statistics = new ArrayList<GithubTrafficStatisticsEntity>();
		try {

			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray counts = jsonObject.getJSONArray("counts");
			for (int z = 0; z < counts.length(); z++) {
				long time = Long.parseLong(counts.getJSONObject(z).get("bucket").toString()) * 1000L;
				int total = Integer.parseInt(counts.getJSONObject(z).get("total").toString());
				int unique = Integer.parseInt(counts.getJSONObject(z).get("unique").toString());

				GithubTrafficStatisticsEntity entity = new GithubTrafficStatisticsEntity(time, "all", total, unique, 100.0, 100.0);
				statistics.add(entity);
			}
			int overallTotal = Integer.parseInt(jsonObject.getJSONObject("summary").get("total").toString());
			int overallUnique = Integer.parseInt(jsonObject.getJSONObject("summary").get("unique").toString());

			Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
			long timestamp = DateUtils.truncate(cal, Calendar.DATE).getTimeInMillis();
			JSONArray referrer = jsonObject.getJSONArray("referrer");
			for (int z = 0; z < referrer.length(); z++) {
				String site = referrer.getJSONObject(z).get("site").toString();
				int total = Integer.parseInt(referrer.getJSONObject(z).get("total").toString());
				int unique = Integer.parseInt(referrer.getJSONObject(z).get("unique").toString());

				GithubTrafficStatisticsEntity entity = new GithubTrafficStatisticsEntity(timestamp, site, total, unique, 100.0 * ((double) total) / ((double) overallTotal), 100.0 * ((double) unique)
						/ ((double) overallUnique));
				statistics.add(entity);
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully retrieved Github Traffic statistics.");
		return statistics;
	}

	@Override
	protected boolean needsRelativationOfValues() {
		return false;
	}

	@Override
	protected void initProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY) && System.getenv(URL_KEY) != null) {
			properties.setProperty(URL_KEY, System.getenv(URL_KEY));
		}
		if (!properties.containsKey(GITHUB_USERNAME_KEY) && System.getenv(GITHUB_USERNAME_KEY) != null) {
			properties.setProperty(GITHUB_USERNAME_KEY, System.getenv(GITHUB_USERNAME_KEY));
		}

		if (!properties.containsKey(GITHUB_PASSWORD_KEY) && System.getenv(GITHUB_PASSWORD_KEY) != null) {
			properties.setProperty(GITHUB_PASSWORD_KEY, System.getenv(GITHUB_PASSWORD_KEY));
		}

		if (!properties.containsKey(TOP_LIST_URL_KEY) && System.getenv(TOP_LIST_URL_KEY) != null) {
			properties.setProperty(TOP_LIST_URL_KEY, System.getenv(TOP_LIST_URL_KEY));
		}
	}

	@Override
	protected void checkProperties(Properties properties) {
		if (!properties.containsKey(URL_KEY)) {
			throw new IllegalArgumentException("GitHub API URL not specified: " + URL_KEY);
		}
		if (!properties.containsKey(GITHUB_USERNAME_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + GITHUB_USERNAME_KEY);
		}
		if (!properties.containsKey(GITHUB_PASSWORD_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + GITHUB_PASSWORD_KEY);
		}
		if (!properties.containsKey(TOP_LIST_URL_KEY)) {
			throw new IllegalArgumentException("Property not specified: " + TOP_LIST_URL_KEY);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected String getJSONString(String apiUri) {
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			client.getCookieSpecs().register("lenient", new CookieSpecFactory() {

				@Override
				public CookieSpec newInstance(HttpParams params) {
					return new LenientCookieSpec();
				}
			});
			HttpClientParams.setCookiePolicy(client.getParams(), "lenient");

			CookieStore cookies = new BasicCookieStore();

			HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookies);

			loginToGitHub(client, httpContext);

			String countsStr = retrieveAggregatedCounts(client, httpContext);
			String topReferrerStr = retrieveTopReferrer(client, httpContext);

			String referrerJson = "\"referrer\":[";
			Document doc = Jsoup.parse(topReferrerStr);
			Elements elements = doc.getElementsByTag("table");

			for (Element element : elements) {
				if (!element.getElementsByTag("th").isEmpty() && element.getElementsByTag("th").get(0).text().equals("Site")) {
					Elements rows = element.getElementsByTag("tr");
					for (int i = 1; i < rows.size(); i++) {
						if (i > 1) {
							referrerJson += ",";
						}
						String[] values = rows.get(i).text().split("\\s");
						referrerJson += "{\"site\":\"" + values[0] + "\",\"total\":" + values[1] + ",\"unique\":" + values[2] + "}";
					}
				}
			}
			referrerJson += "]";

			client.close();
			return countsStr.substring(0, countsStr.length() - 1) + "," + referrerJson + "}";
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void loginToGitHub(DefaultHttpClient client, HttpContext httpContext) throws IOException, ClientProtocolException, UnsupportedEncodingException {
		HttpGet httpGet = new HttpGet(GITHUB_LOGIN_URL);
		CloseableHttpResponse response = client.execute(httpGet, httpContext);
		HttpEntity httpEntity = response.getEntity();

		String body = IOUtils.toString(httpEntity.getContent(), "UTF-8");
		int startIndex = body.indexOf(SEARCH_PATTERN_AUTHEN_TOKEN) + SEARCH_PATTERN_AUTHEN_TOKEN.length();
		int stopIndex = body.indexOf("\"", startIndex);
		String authenticityToken = body.substring(startIndex, stopIndex);

		EntityUtils.consume(httpEntity);

		HttpPost httpPost = new HttpPost(GITHUB_SESSION_URL);

		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("commit", "Sign in"));
		paramList.add(new BasicNameValuePair("authenticity_token", authenticityToken));
		paramList.add(new BasicNameValuePair("login", getProperties().getProperty(GITHUB_USERNAME_KEY)));
		paramList.add(new BasicNameValuePair("password", getProperties().getProperty(GITHUB_PASSWORD_KEY)));

		httpPost.setEntity(new UrlEncodedFormEntity(paramList));

		response = client.execute(httpPost, httpContext);
		httpEntity = response.getEntity();
		body = IOUtils.toString(httpEntity.getContent(), "UTF-8");

		EntityUtils.consume(httpEntity);
	}

	private String retrieveAggregatedCounts(DefaultHttpClient client, HttpContext httpContext) throws IOException, ClientProtocolException, UnsupportedEncodingException {
		HttpGet httpGet = new HttpGet(getProperties().getProperty(URL_KEY));
		httpGet.addHeader("Accept", "application/json");
		CloseableHttpResponse response = client.execute(httpGet, httpContext);
		HttpEntity httpEntity = response.getEntity();
		String result = IOUtils.toString(httpEntity.getContent(), "UTF-8");
		EntityUtils.consume(httpEntity);
		return result;
	}

	private String retrieveTopReferrer(DefaultHttpClient client, HttpContext httpContext) throws IOException, ClientProtocolException, UnsupportedEncodingException {
		HttpGet httpGet = new HttpGet(getProperties().getProperty(TOP_LIST_URL_KEY));
		CloseableHttpResponse response = client.execute(httpGet, httpContext);
		HttpEntity httpEntity = response.getEntity();
		String result = IOUtils.toString(httpEntity.getContent(), "UTF-8");
		EntityUtils.consume(httpEntity);
		return result;
	}

	@SuppressWarnings("deprecation")
	private static class LenientCookieSpec extends BrowserCompatSpec {
		public LenientCookieSpec() {
			super();
			registerAttribHandler(ClientCookie.EXPIRES_ATTR, new BasicExpiresHandler(new String[] { "EEE, d MMM yyyy HH:mm:ss Z" }) {
				@Override
				public void parse(SetCookie cookie, String value) throws MalformedCookieException {
					if (TextUtils.isEmpty(value)) {
						// You should set whatever you want in cookie
						cookie.setExpiryDate(null);
					} else {
						super.parse(cookie, value);
					}
				}
			});
		}
	}

}
