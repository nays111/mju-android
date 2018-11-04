package kr.ac.mju.mjuapp.login;

import java.io.*;
import java.util.*;

import kr.ac.mju.mjuapp.*;
import kr.ac.mju.mjuapp.http.*;
import net.htmlparser.jericho.*;

import org.apache.http.*;
import org.apache.http.client.*;

import android.content.*;
import android.os.*;
import android.util.*;
import android.webkit.*;

/**
 * @author davidkim
 * 
 */
public class LoginStdNameThread extends Thread {

	private Handler handler;
	private Context context;

	public LoginStdNameThread(Context context, Handler handler) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.handler = handler;
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
		// set httpPost
		httpManager.setHttpPost(context.getString(R.string.url_home_mypage));
		// set cookies
		httpManager.setCookieHeader(getCookies());
		// httpResponse
		HttpResponse response = null;
		// httpEntity
		HttpEntity entity = null;
		// parsing tag vector
		Vector<String> tagNames = new Vector<String>();
		tagNames.add(HTMLElementName.TBODY);
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
				List<Element> tbodyList = elementMap.get(HTMLElementName.TBODY);
				if (tbodyList.size() > 0) {
					String stdName = parsingStdName(tbodyList);
					if (stdName != null) {
						Message msg = new Message();
						msg.what = LoginManager.GET_STD_NAME_SUCCESS;
						msg.obj = stdName;

						handler.sendMessage(msg);
						// debug
						long end = System.currentTimeMillis();
						Log.d("MDC", "LoginStdNameThread : "
								+ (double) (end - start) / 1000.);
					} else
						handler.sendEmptyMessage(LoginManager.GET_STD_NAME_FAIL);
				} else
					handler.sendEmptyMessage(LoginManager.GET_STD_NAME_FAIL);
			} else
				handler.sendEmptyMessage(LoginManager.GET_STD_NAME_FAIL);
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
	 * @param elementsList
	 * @return
	 */
	private String parsingStdName(List<Element> elementsList) {
		String stdName = null;
		Element tbody = elementsList.get(0);
		Element div = tbody.getFirstElement(HTMLElementName.DIV);
		stdName = div.getContent().toString().trim();
		return stdName;
	}

	/**
	 * @return
	 */
	private String getCookies() {
		// CookieSyncManager
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		String cookieMobile01 = cookieManager.getCookie(context
				.getString(R.string.url_cookie_mobile_01));
		String cookieMobile02 = cookieManager.getCookie(context
				.getString(R.string.url_cookie_mobile_02));
		String cookieMain = cookieManager.getCookie(context
				.getString(R.string.url_cookie_main));
		String cookieMyiweb = cookieManager.getCookie(context
				.getString(R.string.url_cookie_myiweb));
		return cookieMobile01 + "; " + cookieMobile02 + "; " + cookieMain
				+ "; " + cookieMyiweb;
		// myiweb main 도메인 네임 같음
	}
}
/* end of file */
