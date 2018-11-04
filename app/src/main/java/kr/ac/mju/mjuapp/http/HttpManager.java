package kr.ac.mju.mjuapp.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

/**
 * @author davidkim
 * 
 */
public class HttpManager {

	public static final int UTF_8 = 0;
	public static final int EUC_KR = 1;

	private HttpClient httpClient;
	private HttpPost httpPost;
	private HttpGet httpGet;

	public HttpManager() {
	}

	/**
	 * 
	 */
	public void init() {
		httpClient = getClient();
		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.BROWSER_COMPATIBILITY);
	}

	/**
	 * 
	 */
	public void initSSL() {
		httpClient = wrapClient(httpClient);
	}

	/**
	 * 
	 */
	public void initFilePost() {
		if (httpPost != null) {
			httpPost.setHeader("Connection", "Keep-Alive");
			httpPost.setHeader("Accept-Charset", "UTF-8");
			httpPost.setHeader("ENCTYPE", "multipart/form-data");
		}
	}

	/**
	 * @param entity
	 */
	public void setEntity(HttpEntity entity) {
		httpPost.setEntity(entity);
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public HttpResponse executeHttpPost() throws ClientProtocolException,
			IOException {
		if (httpClient != null && httpPost != null) {
			return httpClient.execute(httpPost);
		}
		return null;
	}

	/**
	 * 
	 */
	public void shutdown() {
		if (httpClient != null)
			httpClient.getConnectionManager().shutdown();
	}

	/**
	 * @return
	 */
	public HttpClient getHttpClient() {
		return this.httpClient != null ? this.httpClient : null;
	}

	/**
	 * 
	 * myiweb의 EUC-KR인코딩에 맞추기 위해서 flag파라미터 추가 - Hs
	 * 
	 * @param entity
	 * @param tagNames
	 * @param flag
	 * @return
	 */
	public HashMap<String, List<Element>> getHttpElementsMap(HttpEntity entity,
			Vector<String> tagNames, int flag) {
		try {
			// declare Stream
			InputStream is = null;
			BufferedReader br = null;
			// get stream
			is = entity.getContent();

			// flag값에 따라서 인코딩 설정
			if (flag == UTF_8) {
				br = new BufferedReader(new InputStreamReader(is, HTTP.UTF_8),
						8);
			} else if (flag == EUC_KR) {
				br = new BufferedReader(new InputStreamReader(is, "EUC-KR"), 8);
			}

			// HashMap init
			HashMap<String, List<Element>> elementMap = new HashMap<String, List<Element>>();
			// set Element list from source
			Source s = new Source(br);
			s.fullSequentialParse();
			List<Element> element = null;
			for (int i = 0; i < tagNames.size(); i++) {
				String tag = (String) tagNames.get(i);
				element = s.getAllElements(tag);
				if (element != null) {
					elementMap.put(tag, element);
				}
			}
			return elementMap;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * myiweb의 EUC-KR인코딩에 맞추기 위해서 flag파라미터 추가 - Hs
	 * 
	 * @param paramsMap
	 * @param url
	 * @param flag
	 */
	public void setHttpPost(HashMap<String, String> paramsMap, String url,
			int flag) {
		httpPost = new HttpPost(url);

		// flag값에 따라서 인코딩 설정
		if (flag == EUC_KR) {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			Set<String> keySet = paramsMap.keySet();
			for (String key : keySet) {
				String value = paramsMap.get(key);
				nameValuePairs.add(new BasicNameValuePair(key, value));
			}

			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs,
						"EUC-KR"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		} else if (flag == UTF_8) {
			Vector<NameValuePair> nameValue = new Vector<NameValuePair>();
			Set<String> keySet = paramsMap.keySet();
			for (String key : keySet) {
				String value = paramsMap.get(key);
				nameValue.add(new BasicNameValuePair(key, value));
			}

			try {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValue,
						HTTP.UTF_8));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * @param url
	 */
	public void setHttpPost(String url) {
		httpPost = new HttpPost(url);
	}

	/**
	 * @param base
	 * @return
	 */
	private HttpClient wrapClient(HttpClient base) {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				@Override
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}
			};
			X509HostnameVerifier verifier = new X509HostnameVerifier() {

				@Override
				public void verify(String string, SSLSocket ssls)
						throws IOException {
				}

				@Override
				public void verify(String string, String[] strings,
						String[] strings1) throws SSLException {
				}

				@Override
				public boolean verify(String string, SSLSession ssls) {
					return true;
				}

				@Override
				public void verify(String host, X509Certificate cert)
						throws SSLException {
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
			ssf.setHostnameVerifier(verifier);
			ClientConnectionManager ccm = base.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", ssf, 443));

			return new DefaultHttpClient(ccm, base.getParams());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * @return
	 */
	private DefaultHttpClient getClient() {
		DefaultHttpClient ret = null;

		// SETS UP PARAMETERS
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		HttpConnectionParams.setConnectionTimeout(params, 5000);
		HttpConnectionParams.setSoTimeout(params, 5000);
		params.setBooleanParameter("http.protocol.expect-continue", false);

		// REGISTERS SCHEMES FOR BOTH HTTP AND HTTPS
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		final SSLSocketFactory sslSocketFactory = SSLSocketFactory
				.getSocketFactory();
		sslSocketFactory
				.setHostnameVerifier(SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		registry.register(new Scheme("https", sslSocketFactory, 443));

		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(
				params, registry);
		ret = new DefaultHttpClient(manager, params);
		return ret;
	}

	/**
	 * @param uri
	 */
	public void setHttpGet(String uri) {
		this.httpGet = new HttpGet(uri);
	}

	/**
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public HttpResponse executeHttpGet() throws ClientProtocolException,
			IOException {
		if (httpClient != null && httpGet != null)
			return httpClient.execute(httpGet);
		return null;
	}

	/**
	 * @param cookie
	 */
	public void setCookieHeader(String cookie) {
		if (httpPost != null)
			httpPost.setHeader("Cookie", cookie);
	}
}
/* end of file */
