package kr.ac.mju.mjuapp.login;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.htmlparser.jericho.Element;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.cipher.CipherManager;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

/**
 * @author davidkim, Hs
 * 
 */

public class LoginManager {

	private Context context;
	private Handler handler;
	private String id;
	private String pw;
	private boolean isAutoLoginChecked;
	private int flag;
	// private Handler myHandler; //kihin add 2015.12.30

	public static final int LOGIN_LOAD_COUNT = 3;
	public static final int LOGIN_SUCCESS = 4;
	public static final int LOGIN_FAIL = 5;
	public static final int LOGIN_TYPE_WEB = 6;
	public static final int LOGIN_TYPE_MOBILE = 7;
	public static final int LOGIN_TYPE_MYIWEB = 8;
	public static final int NETWORK_FAIL = 9;
	public static final int GET_STD_NAME_SUCCESS = 10;
	public static final int GET_STD_NAME_FAIL = 11;
	public static final int BTN_CLICK_LOGIN = 12;
	public static final int AUTO_LOGIN = 13;
	public static final int LOGIN_FAIL_DIALOG = 14;

	private AtomicInteger loginLoadCount;

	public LoginManager(Context context, Handler handler, String id, String pw,
			boolean isAutoLoginChecked, int flag) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.handler = handler;
		this.id = id;
		this.pw = pw;
		this.isAutoLoginChecked = isAutoLoginChecked;
		this.flag = flag;
		loginLoadCount = new AtomicInteger(3);
		// myHandler = new MyHandler();
	}

	private Handler loginHandler = new Handler() {
		public void handleMessage(Message msg) {
			int what = msg.what;
			switch (what) {
			case LOGIN_SUCCESS:
				/*
				 * if (loginLoadCount.decrementAndGet() == LOGIN_LOAD_COUNT - 1)
				 * { login(LOGIN_TYPE_WEB); } else if
				 * (loginLoadCount.decrementAndGet() == LOGIN_LOAD_COUNT - 2) {
				 * login(LOGIN_TYPE_MYIWEB); } else { getStdName(); }
				 */// kihin add 2015.12.30
				setPreferences((String) msg.obj);
				if (flag == BTN_CLICK_LOGIN) {
					handler.sendEmptyMessage(MJUConstants.LOGIN_COMPLETE);
				} else {
					handler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
				}
				break;
			case GET_STD_NAME_SUCCESS:
				setPreferences((String) msg.obj);
				if (flag == BTN_CLICK_LOGIN) {
					handler.sendEmptyMessage(MJUConstants.LOGIN_COMPLETE);
				} else {
					handler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
				}
				break;
			case LOGIN_FAIL:
				handler.sendEmptyMessage(MJUConstants.LOGIN_FAILED);
				break;
			case NETWORK_FAIL:
				Toast.makeText(
						context,
						context.getResources().getString(
								R.string.msg_network_error_weak_signal),
						Toast.LENGTH_SHORT).show();
				handler.sendEmptyMessage(MJUConstants.NETWORK_FAILED);
				break;
			case GET_STD_NAME_FAIL:
				// cookie remove
				CookieManager.getInstance().removeAllCookie();
				Toast.makeText(context,
						context.getString(R.string.getting_info_fail),
						Toast.LENGTH_SHORT).show();
				handler.sendEmptyMessage(MJUConstants.GET_STD_NAME_FAILED);
				break;
			}
		}
	};

	private boolean setPreferences(String stdName) {
		// get pref
		SharedPreferences pref = context.getSharedPreferences(
				context.getString(R.string.pref_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		// save id
		editor.putString(context.getString(R.string.pref_key_user_id), id);
		// set autoLogin
		if (isAutoLoginChecked) {
			pw = CipherManager.encryptDES(pw, context);
			if (pw != null)
				editor.putString(context.getString(R.string.pref_key_user_pw),
						pw);
			else {
				// Encryption failed.
				handler.sendEmptyMessage(LOGIN_FAIL);
				return false;
			}
		}

		// save autologin, saveuserid
		editor.putBoolean(context.getString(R.string.pref_key_auto_login),
				isAutoLoginChecked);
		// save stdName
		if (stdName != null)
			editor.putString(context.getString(R.string.pref_std_name), stdName);
		// commit
		editor.commit();
		return true;
	}

	public void executeLogin() {
		// TODO Auto-generated method stub
		login(LOGIN_TYPE_MOBILE);
	}

	private void login(int type) {
		// TODO Auto-generated method stub
		LoginThread loginThread = new LoginThread(context, loginHandler, type,
				id, pw);
		loginThread.start();
	}

	private void getStdName() {
		// TODO Auto-generated method stub
		LoginStdNameThread stdNameThread = new LoginStdNameThread(context,
				loginHandler);
		stdNameThread.start();
	}

	/**
	 * @param loginThread
	 * @param entity
	 * @return
	 */
	public static boolean parseLoginHTML(Context context,
			List<Element> elementsList, String loginSuccess) {
		try {
			// element check
			if (elementsList != null) {
				for (Element element : elementsList) {
					String parseResult = element.getContent().toString();
					// tag check
					if (parseResult.contains(loginSuccess))
						return true;
					// 2013년 7월 말??? 8월부터 비밀번호 변경하라는 요구 예외처리
					else if (parseResult.contains(context.getResources()
							.getString(
									R.string.html_login_success_main_pwchange)))
						return true;
					else
						return false;
				}
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @return
	 */
	public static boolean checkLogin(Context context) {
		// CookieSyncManager
		CookieSyncManager.createInstance(context);
		if (CookieManager.getInstance().hasCookies()) {
			if ((CookieManager.getInstance().getCookie(
					context.getString(R.string.url_cookie_mobile_01)) != null)
					&& (CookieManager.getInstance().getCookie(
							context.getString(R.string.url_cookie_mobile_02)) != null)
					&& (CookieManager.getInstance().getCookie(
							context.getString(R.string.url_cookie_main)) != null)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static String getCookies(Context context) {
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
		// myiweb, main 도메인 네임 같음
	}

}
