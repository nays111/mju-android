package kr.ac.mju.mjuapp.myiweb;

import java.lang.ref.WeakReference;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.common.LayoutSlideManager;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.network.NetworkManager;
import kr.ac.mju.mjuapp.util.PixelConverter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author davidkim
 * 
 */
public class MyiwebActivity extends FragmentActivity implements
		OnClickListener, OnTouchListener {

	private String url = null;
	private String title = null;
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MyiwebHandler myiwebHandler;
	private LayoutSlideManager layoutSlideManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myiweb_layout);
		// init
		init();
		// init layout
		initLayout();

		setPageUrls();
		if (url == null || url.equals("")) {
			Toast.makeText(getBaseContext(),
					getString(R.string.getting_page_info_fail),
					Toast.LENGTH_SHORT).show();
		}

		// set title
		((TextView) findViewById(R.id.myiweb_title)).setText(title);
		if (NetworkManager.checkNetwork(getApplicationContext())) {
			WebView wv = (WebView) findViewById(R.id.myiweb_webview);
			wv.loadUrl(url);
			wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			wv.setWebViewClient(new MJUWebClient());
		}

		findViewById(R.id.myiweb_credits).setOnClickListener(this);
		findViewById(R.id.myiweb_credits_to_graduation)
				.setOnClickListener(this);
		findViewById(R.id.myiweb_timetable).setOnClickListener(this);
		/*
		 * 모바일 웹 미준비로 인해서 주석처리
		 * findViewById(R.id.myiweb_student_card).setOnClickListener(this);
		 * findViewById(R.id.myiweb_check_scholar).setOnClickListener(this);
		 * findViewById(R.id.myiweb_check_register).setOnClickListener(this);
		 */
		findViewById(R.id.myiweb_left_slidingbar).setOnTouchListener(this);
		findViewById(R.id.myiweb_sliding_btn).setOnTouchListener(this);
	}

	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.myiweb_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.myiweb_left_slidingbar).setClickable(true);
			break;
		case MJUConstants.EXECUTE_ACTION:
			if (!url.equals("") && !title.equals("")) {
				WebView wv = (WebView) findViewById(R.id.myiweb_webview);
				wv.loadUrl(url);
			}
			break;
		}
	}

	/**
	 * 
	 */
	private void initLayout() {
		PixelConverter converter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;
		rParams = (LayoutParams) findViewById(R.id.myiweb_sub_layout)
				.getLayoutParams();
		rParams.rightMargin = converter.getWidth(135);
		findViewById(R.id.myiweb_sub_layout).setLayoutParams(rParams);

		rParams = (LayoutParams) findViewById(R.id.myiweb_sliding_btn)
				.getLayoutParams();
		rParams.width = converter.getWidth(50);
		rParams.height = converter.getHeight(50);

		rParams = (LayoutParams) findViewById(R.id.myiweb_header_icon)
				.getLayoutParams();
		rParams.width = converter.getWidth(30);
		rParams.height = converter.getHeight(30);
		rParams.setMargins(0, 0, converter.getWidth(15), 0);

		View view = findViewById(R.id.myiweb_submene_title);
		LinearLayout.LayoutParams linearlayoutParams = (LinearLayout.LayoutParams) view
				.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));

		view = findViewById(R.id.myiweb_timetable);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
	}

	private void setPageUrls() {
		// TODO Auto-generated method stub
		int whichPageUrl = getIntent()
				.getIntExtra(MJUConstants.MYIWEB_FLAG, -1);

		if (whichPageUrl == -1) {
			return;
		}

		if (whichPageUrl == MJUConstants.MYIWEB_TIMETABLE) {
			url = "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/timetable.jsp?id=mjumob_030100000000";
			title = getResources()
					.getString(R.string.myiweb_sub_menu_timetable);
		} else if (whichPageUrl == MJUConstants.MYIWEB_GRADE) {
			url = "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/grade.jsp?id=mjumob_030200000000";
			title = getResources().getString(R.string.myiweb_sub_menu_credit);
		} else {
			url = "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/graduate.jsp?id=mjumob_030300000000";
			title = getResources().getString(
					R.string.myiweb_sub_menu_check_grade);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (layoutSlideManager.getLayoutState() == MJUConstants.SLIDING) {
				return false;
			} else if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
				layoutSlideManager.slideLayoutToRightAutomatically();
			} else {
				layoutSlideManager.slideLayoutToLeftAutomatically(false);
			}
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(false);
			} else {
				super.onBackPressed();
			}
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (NetworkManager.checkNetwork(getApplicationContext())) {
			int id = v.getId();
			switch (id) {
			case R.id.myiweb_credits:
				url = "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/grade.jsp?id=mjumob_030200000000";
				title = getResources().getString(
						R.string.myiweb_sub_menu_credit);
				break;
			case R.id.myiweb_credits_to_graduation:
				url = "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/graduate.jsp?id=mjumob_030300000000";
				title = getResources().getString(
						R.string.myiweb_sub_menu_check_grade);
				break;
			case R.id.myiweb_timetable:
				url = "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/timetable.jsp?id=mjumob_030100000000";
				title = getResources().getString(
						R.string.myiweb_sub_menu_timetable);
				break;

			// 모바일 페이지 미 준비로 인해서 주석처리
			/*
			 * case R.id.myiweb_student_card: url =
			 * "http://m.mjuac.kr/mbs/mjumob2/jsp/myiweb/studentCard.jsp?id=mjumob_030100000000"
			 * ; titleView.setText(getResources().getString(R.string.
			 * myiweb_sub_menu_check_student_card)); break; case
			 * R.id.myiweb_check_scholar: url =
			 * "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/checkScholar.jsp?id=mjumob_030100000000"
			 * ; titleView.setText(getResources().getString(R.string.
			 * myiweb_sub_menu_check_scholar)); break; case
			 * R.id.myiweb_check_register: url =
			 * "http://m.mju.ac.kr/mbs/mjumob2/jsp/myiweb/checkSchoolRegister.jsp?id=mjumob_030100000000"
			 * ; titleView.setText(getResources().getString(R.string.
			 * myiweb_sub_menu_check_register)); break;
			 */
			}

			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(true);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			layoutSlideManager.initXPostion(event.getRawX());
			break;
		case MotionEvent.ACTION_MOVE:
			layoutSlideManager.slideLayout(event.getRawX());
			break;
		case MotionEvent.ACTION_UP:
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
				layoutSlideManager.slideLayoutToRightAutomatically();
			} else if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(false);
			} else {
				layoutSlideManager.keepSlidingLayout();
			}
			break;
		}
		return false;
	}

	/**
	 * 
	 */
	private void init() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		PixelConverter converter = new PixelConverter(this);

		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		myiwebHandler = new MyiwebHandler(MyiwebActivity.this);

		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(
					findViewById(R.id.myiweb_content), myiwebHandler);
			layoutSlideManager
					.init((int) ((float) displaymetrics.widthPixels - converter
							.getWidth(135)));
		}
	}

	static class MyiwebHandler extends Handler {
		private final WeakReference<MyiwebActivity> myiwebAcivity;

		public MyiwebHandler(MyiwebActivity activity) {
			// TODO Auto-generated constructor stub
			myiwebAcivity = new WeakReference<MyiwebActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			MyiwebActivity activity = myiwebAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

	/**
	 * Desc WebViewclient
	 * 
	 * @author hs
	 * @date 2014. 1. 27. 오후 3:37:52
	 * @version
	 */
	private class MJUWebClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			progressDialog.show(fragmentManager, "");
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			progressDialog.dismiss();
			((TextView) findViewById(R.id.myiweb_title)).setText(title);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
			Toast.makeText(getBaseContext(),
					"onReceivedError : " + errorCode + ", " + description,
					Toast.LENGTH_LONG).show();
			// error 시 white page 출력
			String blankPage = "<html><body></body></html>";
			view.loadData(blankPage, "text/html", "utf-8");
		}
	}
}
/* end of file */
