package kr.ac.mju.mjuapp.login;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.http.HttpManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * @author davidkim, Hs
 * 
 */
public class LoginThread extends Thread {

	private Handler handler;
	private Context context;

	private String paramUserId;
	private String paramUserPw;
	private String urlLogin;
	private String msgLoginSucess;

	private String id;
	private String pwd;

	private int type;

	public LoginThread(Context context, Handler handler, int type, String id,
			String pwd) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.context = context;

		this.id = id;
		this.pwd = pwd;
		this.type = type;

		// type = LOGIN_TYPE_MOBILE
		// type = LOGIN_TYPE_WEB

		if (type == LoginManager.LOGIN_TYPE_MOBILE) {
			paramUserId = context
					.getString(R.string.html_param_login_mobile_user_id);
			paramUserPw = context
					.getString(R.string.html_param_login_mobile_user_pw);
			urlLogin = context.getString(R.string.url_login_mobile);
			msgLoginSucess = context
					.getString(R.string.html_login_success_mobile);

		} else if (type == LoginManager.LOGIN_TYPE_WEB) {
			paramUserId = context
					.getString(R.string.html_param_login_main_user_id);
			paramUserPw = context
					.getString(R.string.html_param_login_main_user_pw);
			urlLogin = context.getString(R.string.url_login_main);
			msgLoginSucess = context
					.getString(R.string.html_login_success_main);

		} else if (type == LoginManager.LOGIN_TYPE_MYIWEB) {
			paramUserId = context
					.getString(R.string.html_param_login_main_user_id);
			paramUserPw = context
					.getString(R.string.html_param_login_main_user_pw);
			urlLogin = context.getString(R.string.url_login_myiweb);
			msgLoginSucess = context
					.getString(R.string.html_login_success_myiweb);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		// debug
		long start = System.currentTimeMillis();
		// create httpManager
		HttpManager httpManager = new HttpManager();
		// init
		httpManager.init();
		// init SSL
		httpManager.initSSL();
		// set parameter map
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put(paramUserId, id);
		paramsMap.put(paramUserPw, pwd);
		if (type == LoginManager.LOGIN_TYPE_WEB) {
			paramsMap.put("RSP", "www.mju.ac.kr");
			paramsMap.put("RelayState", "/mbs/mjukr/popup_sso_success.jsp");
		} else if (type == LoginManager.LOGIN_TYPE_MYIWEB) {
			paramsMap.put("RSP", "myiweb.mju.ac.kr");
			paramsMap.put("RelayState", "index.html");
			paramsMap.put("INIpluginData", "");
		}
		// set httpPost
		httpManager.setHttpPost(paramsMap, urlLogin, HttpManager.UTF_8);
		// httpResponse
		HttpResponse response = null;
		// httpEntity
		HttpEntity entity = null;
		// parsing tag vector
		Vector<String> tagNames = new Vector<String>();
		tagNames.add(HTMLElementName.SCRIPT);

		try {
			// execute login
			response = httpManager.executeHttpPost();
			// get Status
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() == HttpStatus.SC_OK) {
				// html response result
				entity = response.getEntity();
				// get response elementsMap
				HashMap<String, List<Element>> elementMap = httpManager
						.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
				// parsing login result

				if (LoginManager.parseLoginHTML(context,
						elementMap.get(HTMLElementName.SCRIPT), msgLoginSucess)) {
					// saveCookies
					saveCookies(httpManager.getHttpClient());
					handler.sendEmptyMessage(LoginManager.LOGIN_SUCCESS);
					// debug
					long end = System.currentTimeMillis();
					Log.d("MDC", "LoginThread : " + (double) (end - start)
							/ 1000.);
				}
				// login fail
				else
					handler.sendEmptyMessage(LoginManager.LOGIN_FAIL);
			} else
				handler.sendEmptyMessage(LoginManager.LOGIN_FAIL);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			handler.sendEmptyMessage(LoginManager.NETWORK_FAIL);
			Log.d("MDC", "ClientProtocolException : " + e.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			handler.sendEmptyMessage(LoginManager.NETWORK_FAIL);
			Log.d("MDC", "IOException : " + e.toString());
		} finally {
			httpManager.shutdown();
		}
	}

	/**
	 * @param client
	 */
	private void saveCookies(HttpClient client) {
		// cookie
		List<Cookie> cookies = ((DefaultHttpClient) client).getCookieStore()
				.getCookies();

		if (cookies.isEmpty())
			Log.d("MDC", "Cookies is empty");
		else {
			// CookieSyncManager
			CookieSyncManager.createInstance(context);
			CookieManager cookieManager = CookieManager.getInstance();
			// save cookie
			for (int i = 0; i < cookies.size(); i++) {
				cookieManager.setCookie(cookies.get(i).getDomain(), cookies
						.get(i).getName() + "=" + cookies.get(i).getValue());
				// Log.i("MDC", "(Domain)" + cookies.get(i).getDomain() +
				// ": (Name)" + cookies.get(i).getName() + "=(value) " +
				// cookies.get(i).getValue());
			}
			// Cookie sync
			CookieSyncManager.getInstance().sync();
			// cookie sync time
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				Log.d("MDC",
						"LoginThread.java InterruptedException : "
								+ e.toString());
			}
		}
	}
}
/* end of file */
