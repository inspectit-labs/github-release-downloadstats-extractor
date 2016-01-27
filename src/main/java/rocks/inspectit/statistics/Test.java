package rocks.inspectit.statistics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
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
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecFactory;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.http.util.TextUtils;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
	}

public static void main(String[] args) throws ClientProtocolException, IOException {
//	CloseableHttpClient client = HttpClients.createDefault();
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
	
	HttpGet httpGet = new HttpGet("https://github.com/login");
	CloseableHttpResponse response = client.execute(httpGet, httpContext);
	HttpEntity httpEntity = response.getEntity();

	String body = IOUtils.toString(	httpEntity.getContent(), "UTF-8");
	String pattern_1 = "<input name=\"authenticity_token\" type=\"hidden\" value=\"";
	int startIndex = body.indexOf(pattern_1) + pattern_1.length();
	int stopIndex = body.indexOf("\"", startIndex);
	String authenticityToken = body.substring(startIndex,stopIndex);

	EntityUtils.consume(httpEntity);
	
	HttpPost httpPost = new HttpPost("https://github.com/session");

	List <NameValuePair> paramList = new ArrayList <NameValuePair>();
	paramList.add(new BasicNameValuePair("commit", "Sign in"));
//	paramList.add(new BasicNameValuePair("utf-8", "✓"));
	paramList.add(new BasicNameValuePair("authenticity_token", authenticityToken));
	paramList.add(new BasicNameValuePair("login", "AlexanderWert"));
	paramList.add(new BasicNameValuePair("password", "290727wxela.-"));

	httpPost.setEntity(new UrlEncodedFormEntity(paramList));
	
	
	
	 response = client.execute(httpPost, httpContext);
	 httpEntity = response.getEntity();
	 body = IOUtils.toString(	httpEntity.getContent(), "UTF-8");

	 EntityUtils.consume(httpEntity);
	 
	  httpGet = new HttpGet("https://github.com/AlexanderWert/inspectIT/graphs/traffic-data");
	  httpGet.addHeader("Accept", "application/json");
	  response = client.execute(httpGet, httpContext);
		 httpEntity = response.getEntity();

		 body = IOUtils.toString(	httpEntity.getContent(), "UTF-8");
		 System.out.println(body);
		 EntityUtils.consume(httpEntity);
	 client.close();
}

private static class LenientCookieSpec extends BrowserCompatSpec {
    public LenientCookieSpec() {
        super();
        registerAttribHandler(ClientCookie.EXPIRES_ATTR, new BasicExpiresHandler(new String[]{"EEE, d MMM yyyy HH:mm:ss Z"}) {
            @Override public void parse(SetCookie cookie, String value) throws MalformedCookieException {
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
