package kr.ac.mju.mjuapp.introduce;

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
 * @author davidkim
 *
 */
public class IntroduceActivity extends FragmentActivity implements OnClickListener, OnTouchListener {
	private WebView wv;
	private String url;
	private String title;
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private LayoutSlideManager layoutSlideManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.introduce_layout);
		//init
		init();
		//init layout
		initLayout();
		
		if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
			openLayout();
		}
		
		findViewById(R.id.introduce_cheering_song).setOnClickListener(this);
		findViewById(R.id.introduce_foundation_spirit).setOnClickListener(this);
		findViewById(R.id.introduce_president_message).setOnClickListener(this);
		findViewById(R.id.introduce_school_song).setOnClickListener(this);
		findViewById(R.id.introduce_symbol).setOnClickListener(this);
		findViewById(R.id.introduce_symbol_animal).setOnClickListener(this);
		findViewById(R.id.introduce_left_slidingbar).setOnTouchListener(this);
		findViewById(R.id.introduce_sliding_btn).setOnTouchListener(this);
	}

	/**
	 * Desc				핸들러를 통해서 전달되는 메세지를 처리하는 메소드				
	 * @Method Name		handleMessage
	 * @Date			2014. 1. 27. 
	 * @author			hs
	 * @param 			msg
	 */
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.introduce_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.introduce_left_slidingbar).setClickable(true);
			break;
		case MJUConstants.EXECUTE_ACTION:
			if (!url.equals("") && !title.equals("")) {
				((WebView) findViewById(R.id.introduce_webview)).loadUrl(url);
				((TextView) findViewById(R.id.introduce_title)).setText(title);
			}
			break;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (NetworkManager.checkNetwork(getApplicationContext())) {
			int id = v.getId();
			switch (id) {
			case R.id.introduce_cheering_song:
				url = "http://m.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_020600000000";
				title = getString(R.string.cheering_song);
				break;
			case R.id.introduce_foundation_spirit:
				url = "http://m.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_020200000000";
				title = getString(R.string.foundation_spirit);
				break;
			case R.id.introduce_president_message:
				url = "http://m.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_020100000000";
				title = getString(R.string.president_message);
				break;
			case R.id.introduce_school_song:
				url = "http://m.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_020500000000";
				title = getString(R.string.school_song);
				break;
			case R.id.introduce_symbol:
				url = "http://m.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_020300000000";
				title = getString(R.string.symbol);
				break;
			case R.id.introduce_symbol_animal:
				url = "http://m.mju.ac.kr/mbs/mjumob2/subview.jsp?id=mjumob_020400000000";
				title = getString(R.string.symbol_animal);
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
	 * 
	 */
	private void init() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		PixelConverter converter = new PixelConverter(this);
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		
		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(findViewById(R.id.introduce_content), 
					new IntroduceHandler(IntroduceActivity.this));
			layoutSlideManager.init((int) ((float)displaymetrics.widthPixels - converter.getWidth(135)));
		}
		
		wv = (WebView)findViewById(R.id.introduce_webview);
		wv.setWebViewClient(new MJUWebClient());
	}
	/**
	 * 
	 */
	private void initLayout(){
		PixelConverter converter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;
		rParams = (LayoutParams) findViewById(R.id.introduce_sub_layout).getLayoutParams();
		rParams.rightMargin = converter.getWidth(135);
		findViewById(R.id.introduce_sub_layout).setLayoutParams(rParams);
		
		rParams = (LayoutParams)findViewById(R.id.introduce_sliding_btn).getLayoutParams();
		rParams.width = converter.getWidth(50);
		rParams.height = converter.getHeight(50);
		
		rParams = (LayoutParams)findViewById(R.id.introduce_header_icon).getLayoutParams();
		rParams.width = converter.getWidth(30);
		rParams.height = converter.getHeight(30);
		rParams.setMargins(0, 0, converter.getWidth(15), 0);
		
		View view = findViewById(R.id.menu_introduce_title);
		LinearLayout.LayoutParams linearlayoutParams =  (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));
		
		view = findViewById(R.id.introduce_president_message);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
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
	
	/**
	 * Desc			WebViewclient			
	 * @author		hs
	 * @date		2014. 1. 27. 오후 3:37:52
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
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
			Toast.makeText(getBaseContext(), "onReceivedError : " + errorCode + ", " + description, Toast.LENGTH_LONG)
					.show();
			// error 시 white page 출력
			String blankPage = "<html><body></body></html>";
			view.loadData(blankPage, "text/html", "utf-8");
		}
	}
	
	/**
	 * Desc			static 헨들러 클래스			
	 * @author		hs
	 * @date		2014. 1. 27. 오후 3:38:22
	 * @version
	 */
	static class IntroduceHandler extends Handler {
		private final WeakReference<IntroduceActivity> introduceAcivity;
		
		public IntroduceHandler(IntroduceActivity activity) {
			// TODO Auto-generated constructor stub
			introduceAcivity = new WeakReference<IntroduceActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			IntroduceActivity activity = introduceAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */
