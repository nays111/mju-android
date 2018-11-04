package kr.ac.mju.mjuapp.traffic;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author davidkim, hs
 */
public class TrafficActivity extends FragmentActivity implements
		OnClickListener, OnTouchListener {
	private WebView wv;
	private String url;
	private String title;
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private LayoutSlideManager layoutSlideManager;
	private PixelConverter converter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.traffic_layout);
		// init
		init();
		// initlayout
		initLayout();

		if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
			openLayout();
		}

		setListeners();
	}

	/**
	 * Desc 핸들러를 통해서 전달되는 메세지를 처리하는 메소드
	 * 
	 * @Method Name handleMessage
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param msg
	 */
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.traffic_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.traffic_left_slidingbar).setClickable(true);
			break;
		case MJUConstants.EXECUTE_ACTION:
			Log.i("notice", "traffic_recive_msg");
			if (!url.equals("") && !title.equals("")) {
				((WebView) findViewById(R.id.traffic_webview)).loadUrl(url);
				((TextView) findViewById(R.id.traffic_title)).setText(title);
			}
			url = "";
			title = "";
			break;
		}
	}

	/**
	 * Desc 초기화
	 * 
	 * @Method Name init
	 * @Date 2014. 1. 27.
	 * @author hs
	 */
	private void init() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		converter = new PixelConverter(this);

		wv = (WebView) findViewById(R.id.traffic_webview);
		wv.setWebViewClient(new MJUWebClient());

		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();

		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(
					findViewById(R.id.traffic_content), new TrafficHandler(
							TrafficActivity.this));
			layoutSlideManager
					.init((int) ((float) displaymetrics.widthPixels - converter
							.getWidth(135)));
		}

		url = "";
		title = "";
	}

	/**
	 * 
	 */
	private void initLayout() {
		RelativeLayout.LayoutParams rParams = null;
		rParams = (LayoutParams) findViewById(R.id.traffic_sub_layout)
				.getLayoutParams();
		rParams.rightMargin = converter.getWidth(135);
		findViewById(R.id.traffic_sub_layout).setLayoutParams(rParams);

		rParams = (LayoutParams) findViewById(R.id.traffic_sliding_btn)
				.getLayoutParams();
		rParams.width = converter.getWidth(50);
		rParams.height = converter.getHeight(50);

		rParams = (LayoutParams) findViewById(R.id.traffic_header_icon)
				.getLayoutParams();
		rParams.width = converter.getWidth(30);
		rParams.height = converter.getHeight(30);
		rParams.setMargins(0, 0, converter.getWidth(15), 0);

		View view = findViewById(R.id.traffic_submenu_title);
		LinearLayout.LayoutParams linearlayoutParams = (LinearLayout.LayoutParams) view
				.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));

		view = findViewById(R.id.traffic_submenu_humanity_title);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0,
				converter.getHeight(5));

		view = findViewById(R.id.traffic_liberalarts_shuttlebus);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);

		view = findViewById(R.id.traffic_submenu_science_title);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0,
				converter.getHeight(5));

		view = findViewById(R.id.traffic_science_schoolbus_gotoschool);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
	}

	/**
	 * Desc 리스너 등록 메소드
	 * 
	 * @Method Name setListeners
	 * @Date 2014. 1. 27.
	 * @author hs
	 */
	private void setListeners() {
		// TODO Auto-generated method stub
		findViewById(R.id.traffic_liberalarts_shuttlebus).setOnClickListener(
				this);
		findViewById(R.id.traffic_liberalarts_urbanbus)
				.setOnClickListener(this);
		findViewById(R.id.traffic_liberalarts_subway).setOnClickListener(this);
		findViewById(R.id.traffic_science_schoolbus_gotoschool)
				.setOnClickListener(this);
		findViewById(R.id.traffic_science_schoolbus_walkhome)
				.setOnClickListener(this);
		findViewById(R.id.traffic_science_shuttle_semester_weekday)
				.setOnClickListener(this);
		findViewById(R.id.traffic_science_shuttle_semester_weekend)
				.setOnClickListener(this);
		findViewById(R.id.traffic_science_shuttle_vacation).setOnClickListener(
				this);
		findViewById(R.id.traffic_between_liberalarts_and_science_gotoschool)
				.setOnClickListener(this);
		findViewById(R.id.traffic_between_liberalarts_and_science_walkhome)
				.setOnClickListener(this);
		findViewById(R.id.traffic_sliding_btn).setOnTouchListener(this);
		findViewById(R.id.traffic_left_slidingbar).setOnTouchListener(this);
	}

	private void openLayout() {
		// TODO Auto-generated method stub
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				layoutSlideManager.slideLayoutToRightAutomatically();
			}
		};
		timer.schedule(task, 500);
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
			url = "";
			switch (id) {
			case R.id.traffic_liberalarts_shuttlebus:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040103000000";
				title = getString(R.string.schoolbus);
				break;
			case R.id.traffic_liberalarts_urbanbus:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040101000000";
				title = getString(R.string.urbanbusbus);
				break;
			case R.id.traffic_liberalarts_subway:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040102000000";
				title = getString(R.string.subway);
				break;
			case R.id.traffic_science_schoolbus_gotoschool:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040201000000";
				title = getString(R.string.schoolbus_to_school);
				break;
			case R.id.traffic_science_schoolbus_walkhome:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040202000000";
				title = getString(R.string.schoolbus_to_home);
				break;
			case R.id.traffic_science_shuttle_semester_weekday:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040203000000";
				title = getString(R.string.schoolbus_semester_weekday);
				break;
			case R.id.traffic_science_shuttle_semester_weekend:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040204000000";
				title = getString(R.string.schoolbus_semester_weekend);
				break;
			case R.id.traffic_science_shuttle_vacation:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040205000000";
				title = getString(R.string.schoolbus_vacation);
				break;
			case R.id.traffic_between_liberalarts_and_science_gotoschool:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040206000000";
				title = getString(R.string.humanity_and_science_to_school);
				break;
			case R.id.traffic_between_liberalarts_and_science_walkhome:
				url = "http://www.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_040207000000";
				title = getString(R.string.humanity_and_science_to_home);
				break;
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

	/**
	 * Desc static 헨들러 클래스
	 * 
	 * @author hs
	 * @date 2014. 1. 27. 오후 3:38:22
	 * @version
	 */
	static class TrafficHandler extends Handler {
		private final WeakReference<TrafficActivity> trafficAcivity;

		public TrafficHandler(TrafficActivity activity) {
			// TODO Auto-generated constructor stub
			trafficAcivity = new WeakReference<TrafficActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			TrafficActivity activity = trafficAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

}
/* end of file */
