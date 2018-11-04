package kr.ac.mju.mjuapp.web;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUAlertDialog;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.network.NetworkManager;
import kr.ac.mju.mjuapp.util.PixelConverter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author davidkim
 * 
 */
public class WebViewActivity extends FragmentActivity {
	private String rootUrl = null;
	private String oldRootUrl = null;

	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MJUAlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview_layout);
		// init
		init();
		initLayout();
	}

	private void initLayout() {
		// TODO Auto-generated method stub
		PixelConverter converter = new PixelConverter(getApplicationContext());
		RelativeLayout.LayoutParams relativeLayoutParams;

		View view = findViewById(R.id.webview_header_icon);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(30);
		relativeLayoutParams.height = converter.getHeight(30);
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(15), 0);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// cookie
		CookieSyncManager.createInstance(getApplicationContext());
		CookieSyncManager.getInstance().startSync();
		// get rootUrl
		rootUrl = getIntent().getExtras().getString("rooturl");
		if (rootUrl != null) {
			// check network state
			if (NetworkManager.checkNetwork(this)) {
				// webView set
				WebView webView = (WebView) findViewById(R.id.webview);
				if (oldRootUrl == null) {
					webView.loadUrl(rootUrl);
					oldRootUrl = rootUrl;
				} else if (!oldRootUrl.equals(rootUrl)) {
					webView.loadUrl(rootUrl);
					oldRootUrl = rootUrl;
				}
			}
			// set titlebar
			setTitlebar();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// cookie
		CookieSyncManager.getInstance().stopSync();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		WebView webView = (WebView) findViewById(R.id.webview);

		if (webView.getUrl() != null) {
			if (webView.getUrl().equals(rootUrl) || !webView.canGoBack())
				super.onBackPressed();
			else
				webView.goBack();
		} else
			finish();
	}

	/**
	 * 
	 */
	@SuppressLint("SetJavaScriptEnabled")
	private void init() {
		// webView setting
		WebView webView = (WebView) findViewById(R.id.webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);

				// 罹좏띁�ㅻ㏊��寃쎌슦 �밸툕�쇱슦���뺣� 異뺤냼 媛�뒫
				if (url.equals("http://www.mju.ac.kr/mbs/mjumob2/images/img_map01.png")
						|| url.equals("http://www.mju.ac.kr/mbs/mjumob2/images/img_map02.png"))
					view.getSettings().setBuiltInZoomControls(true);
				else
					view.getSettings().setBuiltInZoomControls(false);

				// ���섏씠吏�吏꾪뻾 �곹솴 �꾨줈洹몃젅�ㅻ컮 異쒕젰
				progressDialog.show(fragmentManager, "");

			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub

				if (url.startsWith("tel:")) {
					// �밸럭�먯꽌 �꾪솕踰덊샇 �대┃ ��
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri
							.parse(url));
					startActivity(intent);
					return true;
				} else if (url.startsWith("mailto:")) {
					// �밸럭�먯꽌 硫붿씪 �대┃ ��
					Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
							.parse(url));
					startActivity(intent);
					return true;
				}
				return false;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				// progressbar dismiss
				progressDialog.dismiss();
				// cookie
				CookieSyncManager.getInstance().sync();
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// TODO Auto-generated method stub
				super.onReceivedError(view, errorCode, description, failingUrl);
				Toast.makeText(getBaseContext(),
						"onReceivedError : " + errorCode + ", " + description,
						Toast.LENGTH_SHORT).show();
				// error ��white page 異쒕젰
				if (errorCode == WebViewClient.ERROR_HOST_LOOKUP) {
					String blankPage = "<html><body></body></html>";
					view.loadData(blankPage, "text/html", "utf-8");
					view.clearHistory();
				}
			}
		});
		webView.setWebChromeClient(new WebChromeClient() {
			// JavaScript Popup handle
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				// TODO Auto-generated method stub
				// JavaScript 泥섎━
				result.confirm();
				// 濡쒓렇���섏� �딆븯��寃쎌슦 JavaScript �앹뾽 李�泥섎━
				if (message.contains(getString(R.string.msg_login_alert))) {
					alertDialog = MJUAlertDialog.newInstance(
							MJUConstants.NORMAL_ALERT_DIALOG,
							R.string.app_name, R.string.msg_login_alert, 0);
					alertDialog.show(fragmentManager, "");
					return true;
				}
				return true;
			}
		});

		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
	}

	/**
	 * 
	 */
	private void setTitlebar() {
		TextView titleTextView = (TextView) findViewById(R.id.webview_title_textview);
		ImageView headerIconView = (ImageView) findViewById(R.id.webview_header_icon);
		if (rootUrl.equals(getResources().getString(R.string.url_phone))) {
			titleTextView.setText(" 전화번호 안내 ");
			headerIconView
					.setBackgroundResource(R.drawable.icon_header_phoneinfo);
		} else if (rootUrl.equals(getResources()
				.getString(R.string.url_library))) {
			titleTextView.setText(" 도서관");
			headerIconView
					.setBackgroundResource(R.drawable.icon_header_library);
		}
	}
}
/* end of file */
