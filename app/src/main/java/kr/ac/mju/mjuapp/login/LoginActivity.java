package kr.ac.mju.mjuapp.login;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.cipher.CipherManager;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUAlertDialog;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.network.NetworkManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * @author davidkim
 * 
 */
public class LoginActivity extends FragmentActivity implements OnClickListener {

	private AtomicInteger loadCount = new AtomicInteger();
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MJUAlertDialog alertDialog;
	private LoginHandler loginHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		init();

		// password Edittext
		((EditText) findViewById(R.id.login_passwd))
				.setOnEditorActionListener(new OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// TODO Auto-generated method stub
						if (actionId == EditorInfo.IME_ACTION_DONE)
							((ImageButton) findViewById(R.id.login_login_btn))
									.performClick();
						return true;
					}
				});
		((ImageButton) findViewById(R.id.login_login_btn))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.login_textview_autologin))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.login_textview_save_user_id))
				.setOnClickListener(this);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// initLayout
		initLayout();
	}

	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		//
		switch (what) {
			case LoginManager.LOGIN_SUCCESS:
				/*if (loadCount.decrementAndGet() == LoginManager.LOGIN_LOAD_COUNT - 1) {
					login(LoginManager.LOGIN_TYPE_WEB);
				} else if (loadCount.decrementAndGet() == LoginManager.LOGIN_LOAD_COUNT  - 2) {
					login(LoginManager.LOGIN_TYPE_MYIWEB);
				} else {
					//get student name;
					getStdName();
				}*/
				getStdName();
			break;
		case LoginManager.LOGIN_FAIL:
			// dimiss Dialog
			progressDialog.dismiss();
			alertDialog = MJUAlertDialog.newInstance(
					MJUConstants.NORMAL_ALERT_DIALOG, R.string.app_name,
					R.string.msg_login_fail, 0);
			alertDialog.show(fragmentManager, "");
			((EditText) findViewById(R.id.login_passwd)).setText("");
			break;
		case LoginManager.NETWORK_FAIL:
			// dimiss Dialog
			progressDialog.dismiss();
			// error weak signal msg
			Toast.makeText(
					getBaseContext(),
					getResources().getString(
							R.string.msg_network_error_weak_signal),
					Toast.LENGTH_SHORT).show();
			break;
		case LoginManager.GET_STD_NAME_SUCCESS:
			// set pref
			setPreferences((String) msg.obj);
			// dimiss Dialog
			progressDialog.dismiss();
			Toast.makeText(getBaseContext(), getString(R.string.login_done),
					Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			// finish
			finish();
			break;
		case LoginManager.GET_STD_NAME_FAIL:
			// cookie remove
			CookieManager.getInstance().removeAllCookie();
			// dimiss Dialog
			progressDialog.dismiss();
			Toast.makeText(getBaseContext(),
					getString(R.string.getting_info_fail), Toast.LENGTH_SHORT)
					.show();
			break;
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		loginHandler = new LoginHandler(LoginActivity.this);
	}

	/**
	 * 
	 */
	private void login(int type) {
		String id = ((EditText) findViewById(R.id.login_id)).getText()
				.toString();
		String pw = ((EditText) findViewById(R.id.login_passwd)).getText()
				.toString();
		// type = LOGIN_TYPE_MOBILE
		LoginThread loginThread = new LoginThread(getApplicationContext(),
				loginHandler, type, id, pw);
		loginThread.start();
	}

	/**
	 * 
	 */
	private void getStdName() {
		LoginStdNameThread stdNameThread = new LoginStdNameThread(
				getApplicationContext(), loginHandler);
		stdNameThread.start();
	}

	/**
	 * @return
	 */
	private boolean checkIdPw() {
		String id = ((EditText) findViewById(R.id.login_id)).getText()
				.toString();
		String passwd = ((EditText) findViewById(R.id.login_passwd)).getText()
				.toString();
		if (id.equals("")) {
			Toast.makeText(
					getBaseContext(),
					getResources()
							.getString(R.string.msg_login_id_insufficient),
					Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.login_id)).requestFocus();
			return false;
		} else if (passwd.equals("")) {
			Toast.makeText(
					getBaseContext(),
					getResources()
							.getString(R.string.msg_login_pw_insufficient),
					Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.login_passwd)).requestFocus();
			return false;
		}
		return true;
	}

	/**
	 * @param login
	 */
	private boolean setPreferences(String stdName) {
		// get pref
		SharedPreferences pref = getSharedPreferences(
				getString(R.string.pref_name), MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		// get userId, checkAutoLogin, checkSaveUserId
		String userId = ((EditText) findViewById(R.id.login_id)).getText()
				.toString();
		boolean checkAutoLogin = ((CheckBox) findViewById(R.id.login_chk_autologin))
				.isChecked();
		boolean checkSaveUserId = ((CheckBox) findViewById(R.id.login_chk_save_user_id))
				.isChecked();
		// set autoLogin
		if (checkAutoLogin) {
			String userPw = ((EditText) findViewById(R.id.login_passwd))
					.getText().toString();
			userPw = CipherManager.encryptDES(userPw, getApplicationContext());
			if (userPw != null)
				editor.putString(getString(R.string.pref_key_user_pw), userPw);
			else {
				// Encryption failed.
				loginHandler.sendEmptyMessage(LoginManager.LOGIN_FAIL);
				return false;
			}
		}
		// set saveUserId
		editor.putString(getString(R.string.pref_key_user_id), userId);

		// save autologin, saveuserid
		editor.putBoolean(getString(R.string.pref_key_auto_login),
				checkAutoLogin);
		editor.putBoolean(getString(R.string.pref_key_save_user_id),
				checkSaveUserId);
		// save stdName
		if (stdName != null)
			editor.putString(getString(R.string.pref_std_name), stdName);
		// commit
		editor.commit();
		return true;
	}

	/**
	 * 
	 */
	private void initLayout() {
		// get inputmanager
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		// get preference
		SharedPreferences pref = getSharedPreferences(
				getString(R.string.pref_name), MODE_PRIVATE);
		boolean checkSaveUserId = pref.getBoolean(
				getString(R.string.pref_key_save_user_id), false);
		EditText idEditText = ((EditText) findViewById(R.id.login_id));
		EditText pwEdittext = ((EditText) findViewById(R.id.login_passwd));
		// if set saving user_id
		if (checkSaveUserId) {
			String userId = pref.getString(
					getString(R.string.pref_key_user_id), null);
			idEditText.setText(userId);
			((CheckBox) findViewById(R.id.login_chk_save_user_id))
					.setChecked(checkSaveUserId);
			pwEdittext.requestFocus();
			imm.showSoftInput(pwEdittext, InputMethodManager.SHOW_FORCED);
		} else {
			idEditText.setText("");
			idEditText.requestFocus();
			imm.showSoftInput(idEditText, InputMethodManager.SHOW_FORCED);
		}
		pwEdittext.setText("");
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.login_login_btn:
			// login
			if (NetworkManager.checkNetwork(LoginActivity.this)) {
				if (checkIdPw()) {
					progressDialog.show(fragmentManager, "");
					loadCount.set(LoginManager.LOGIN_LOAD_COUNT);
					login(LoginManager.LOGIN_TYPE_MOBILE);
				}
			}
			break;
		case R.id.login_textview_autologin:
			((CheckBox) findViewById(R.id.login_chk_autologin)).performClick();
			break;
		case R.id.login_textview_save_user_id:
			((CheckBox) findViewById(R.id.login_chk_save_user_id))
					.performClick();
			break;
		}
	}

	static class LoginHandler extends Handler {
		private final WeakReference<LoginActivity> loginAcivity;

		public LoginHandler(LoginActivity activity) {
			// TODO Auto-generated constructor stub
			loginAcivity = new WeakReference<LoginActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			LoginActivity activity = loginAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */