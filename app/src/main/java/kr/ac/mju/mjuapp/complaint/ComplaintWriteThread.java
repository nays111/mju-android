package kr.ac.mju.mjuapp.complaint;

import java.io.*;
import java.util.*;

import kr.ac.mju.mjuapp.*;
import kr.ac.mju.mjuapp.http.*;

import org.apache.http.*;
import org.apache.http.client.*;

import android.content.*;
import android.os.*;
import android.webkit.*;

/**
 * @author davidkim
 *
 */
public class ComplaintWriteThread extends Thread {
	
	private Handler handler;
	private Context context;
	private HashMap<String, String> paramsMap;

	public ComplaintWriteThread(Handler handler, Context context, HashMap<String, String> hashMap) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.context = context;
		this.paramsMap = hashMap;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		// create httpManager
		HttpManager httpManager = new HttpManager();
		// init
		httpManager.init();
		// initSSL
		httpManager.initSSL();
		try {
			/**************************
			 * write board
			 **************************/
			// set httpPost
			httpManager.setHttpPost(paramsMap, context.getString(R.string.board_write_url), 
					HttpManager.UTF_8);
			// set cookies
			httpManager.setCookieHeader(getCookies());
			// httpResponse
			HttpResponse boardResponse = null;
			// execute
			boardResponse = httpManager.executeHttpPost();
			// get response
			HttpEntity entity = boardResponse.getEntity();
			// upload success
			if (entity != null)
				handler.sendEmptyMessage(ComplaintWriteActivity.UPLOAD_SUCCESS);
			// upload fail
			else
				handler.sendEmptyMessage(ComplaintWriteActivity.UPLOAD_FAIL);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			handler.sendEmptyMessage(ComplaintWriteActivity.UPLOAD_FAIL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			handler.sendEmptyMessage(ComplaintWriteActivity.UPLOAD_FAIL);
		} finally {
			httpManager.shutdown();
		}
	}
	/**
	 * @return
	 */
	private String getCookies() {
		// CookieSyncManager
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		String cookieMobile01 = cookieManager.getCookie(context.getString(R.string.url_cookie_mobile_01));
		String cookieMobile02 = cookieManager.getCookie(context.getString(R.string.url_cookie_mobile_02));
		String cookieMain = cookieManager.getCookie(context.getString(R.string.url_cookie_main));
		return cookieMobile01 + "; " + cookieMobile02 + "; " + cookieMain;
	}
}
/* end of file */
