package kr.ac.mju.mjuapp.main;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.banner.Banner;
import kr.ac.mju.mjuapp.banner.BannerImageThread;
import kr.ac.mju.mjuapp.banner.BannerTextThread;
import kr.ac.mju.mjuapp.banner.MainBannerViewPagerAdapter;
import kr.ac.mju.mjuapp.campusmap.CampusmapActivity;
import kr.ac.mju.mjuapp.cipher.CipherManager;
import kr.ac.mju.mjuapp.common.CustomViewPager;
import kr.ac.mju.mjuapp.community.CommunityListActivity;
import kr.ac.mju.mjuapp.complaint.ComplaintListActivity;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUAlertDialog;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.food.FoodActivity;
import kr.ac.mju.mjuapp.green.GreencampusMainActivity;
import kr.ac.mju.mjuapp.introduce.IntroduceActivity;
import kr.ac.mju.mjuapp.login.LoginActivity;
import kr.ac.mju.mjuapp.login.LoginManager;
import kr.ac.mju.mjuapp.myiweb.MyiwebActivity;
import kr.ac.mju.mjuapp.network.NetworkManager;
import kr.ac.mju.mjuapp.notice.NoticeListActivity;
import kr.ac.mju.mjuapp.notice.NoticeViewActivity;
import kr.ac.mju.mjuapp.photosns.MJUPhoto;
import kr.ac.mju.mjuapp.photosns.MainPhotoImageThread;
import kr.ac.mju.mjuapp.photosns.MainPhotoViewPagerAdapter;
import kr.ac.mju.mjuapp.photosns.PhotoTextThread;
import kr.ac.mju.mjuapp.traffic.TrafficActivity;
import kr.ac.mju.mjuapp.util.PixelConverter;
import kr.ac.mju.mjuapp.weather.WeatherInfo;
import kr.ac.mju.mjuapp.weather.WeatherThread;
import kr.ac.mju.mjuapp.web.WebViewActivity;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * 퍼포먼스를 위해서 최대한 메소드 호출과 모듈화 안하고 그냥 처리함 코드 가독성, 라인수 신경안씀
 * 
 * @author hs
 */
public class MainActivity extends FragmentActivity implements OnClickListener,
		OnEditorActionListener, OnTouchListener {
	private static final int LOGIN_LAYOUT = 5;
	private static final int QUICK_MENU_LAYOUT = 6;
	private static final int MYIWEB_LAYOUT = 7;
	private static final int LOAD_COUNT = 10;
	private static final int LEFT_MARGIN = 35;

	private int notLoadedPagePositionForBanner;
	private int notLoadedPagePositionForPicture;
	private int currentQuickMenuLayout;
	private boolean isAutoLoginChecked;
	private boolean isPictureFisrtImageLoaded;
	private boolean isBannerFisrtImageLoaded;
	private boolean isOnCreate;
	private boolean isBackBtnPressed;
	private LayoutInflater inflator;
	private PixelConverter converter;
	private View view;
	private LinearLayout firstLayout;
	private LinearLayout secondLayout;
	private LinearLayout thirdLayout;
	private LinearLayout.LayoutParams linearLayoutParams;
	private RelativeLayout.LayoutParams relativeLayoutParams;
	private AtomicInteger loadCount;
	private TextView currentNoticeNaviTextView;
	private ArrayList<MainNotice> mainNoticeList;
	private ArrayList<Banner> bannerList;
	private ArrayList<MJUPhoto> pictureList;
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MJUAlertDialog alertDialog;
	private MainHandler mainHandler;
	private MainLayoutSlideManager firstLayoutSlideManager;
	private MainLayoutSlideManager secondLayoutSlideManager;
	private AtomicInteger bannerTextFailCounter;
	private AtomicInteger picnnerTextFailCounter;
	private AtomicInteger bannerImgFailCounter;
	private AtomicInteger picnnerImgFailCounter;
	private Timer photoScrollTimer;
	private Timer bannerScrollTimer;
	private TimerTask photoSrollTimerTask;
	private TimerTask bannerSrollTimerTask;
	private static Toast mToast;
	private int LayoutFlag = 0;

	/** Called when the activity is first created. */
	// 처음 화면 창조.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		setContentView(R.layout.main_layout);
		firstLayout = (LinearLayout) findViewById(R.id.main_first_layout_container);
		secondLayout = (LinearLayout) findViewById(R.id.main_second_layout_container);
		thirdLayout = (LinearLayout) findViewById(R.id.main_third_layout_container);
		init();
		// 날씨, 사진, 배너 네트워크로부터 받아옴.
		if (NetworkManager.checkNetwork(getApplicationContext())) {
			getWeather();
			getMainNotice();

			getPictureImg();
			getPictureText();

			getBannerImg();
			getBannerText();
		}
		// 초기 메인화면..
		initLayout();
		initSecondLayout();
		initThirdLayout();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		if (LayoutFlag == 2) {

			onKeyDown(4,
					new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));

			LayoutFlag = 0;

		} else if (LayoutFlag == 3) {

			onKeyDown(4,
					new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
			thirdLayout.setVisibility(View.INVISIBLE);

			onKeyDown(4,
					new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
			thirdLayout.setVisibility(View.VISIBLE);
			LayoutFlag = 0;
		}// ㅎㅎㅎㅎㅎㅎ 백버튼 넣었습니당

		if (!isOnCreate) {
			// 만약 firstlayout이 닫혀있따면...
			// if (firstLayoutSlideManager.getLayoutState() ==
			// MJUConstants.LAYOUT_CLOSED) {
			// 처음 화면 시작할때 명지대 마크 나오는 부분.
			startImageBoardTimer();
			// }
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();

		stopImageBoardTimer();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();

		if (LoginManager.checkLogin(getApplicationContext())) {
			SharedPreferences pref = getSharedPreferences(
					getString(R.string.pref_name), MODE_PRIVATE);
			boolean checkAutoLogin = pref.getBoolean(
					getString(R.string.pref_key_auto_login), false);

			if (!checkAutoLogin) {
				logout();
			}
		}
		// delete temp picture files
		deleteTempFiles();

		CustomViewPager pictureViewPager = ((CustomViewPager) findViewById(R.id.main_picture_viewPager));
		pictureViewPager.removeAllViews();
		if (((MainPhotoViewPagerAdapter) pictureViewPager.getAdapter()) != null) {
			((MainPhotoViewPagerAdapter) pictureViewPager.getAdapter()).clear();
		}
		CustomViewPager bannerViewPager = ((CustomViewPager) findViewById(R.id.main_banner_viewPager));
		bannerViewPager.removeAllViews();
		if (((MainBannerViewPagerAdapter) bannerViewPager.getAdapter()) != null) {
			((MainBannerViewPagerAdapter) bannerViewPager.getAdapter()).clear();
		}

		mToast.cancel();
	}

	private void deleteTempFiles() {
		// TODO Auto-generated method stub
		String basePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ getResources().getString(R.string.uri_file_save_directory);
		String folderName = getResources().getString(
				R.string.picture_folder_name);
		File file = new File(basePath + folderName);

		/**
		 * 2014.06.22 �섏젙 /mjudownload/tmp_pictures/ 폴더가 있는지 없는지 여부 체크 후 처리.
		 */
		if (file.exists()) { // 폴더가 존재하면 삭제.
			if (file.listFiles().length > 0) {
				for (File data : file.listFiles()) {
					data.delete();
				}
			}
		} else {
			// 폴더가 존재하지 않으면 그냥 끝.
		}
	}

	private void getPictureImg() {
		// TODO Auto-generated method stub
		MainPhotoImageThread phtoThread = new MainPhotoImageThread(mainHandler,
				this);
		phtoThread.start();
	}

	private void getPictureText() {
		// TODO Auto-generated method stub
		PhotoTextThread pictureTextThread = new PhotoTextThread(mainHandler);
		pictureTextThread.start();
	}

	private void getBannerText() {
		// TODO Auto-generated method stub
		BannerTextThread bannerTextThread = new BannerTextThread(mainHandler);
		bannerTextThread.start();
	}

	private void getBannerImg() {
		// TODO Auto-generated method stub
		BannerImageThread bannerImgThread = new BannerImageThread(mainHandler,
				this);
		bannerImgThread.start();
	}

	@SuppressWarnings("unchecked")
	public void handleMessage(Message msg) {
		// main화면에 사진있는 부분. 핸들.
		CustomViewPager pictureViewPager = ((CustomViewPager) findViewById(R.id.main_picture_viewPager));
		switch (msg.what) {
		case MJUConstants.LOAD_COMPLETE:
			if (loadCount.decrementAndGet() == 0) {
				if (isOnCreate) {
					mainHandler.sendEmptyMessageDelayed(
							MJUConstants.SPLASH_DISMISS, 500);
				}
			}
			break;
		// 서울 날씨 정보 가져오기 성공
		case MJUConstants.WEATHER_SEOUL_COMPLETE:
			mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
			setWeatherInfo((WeatherInfo) msg.obj, WeatherThread.TYPE_SEOUL);
			break;
		// 용인 날씨 정보 가져오기 성공.
		case MJUConstants.WEATHER_YONGIN_COMPLETE:
			mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
			setWeatherInfo((WeatherInfo) msg.obj, WeatherThread.TYPE_YONGIN);
			break;
		// 날씨 정보 가져오기 실패.
		case MJUConstants.WEATHER_FAIL:
			mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
			Toast.makeText(getBaseContext(),
					getString(R.string.main_weather_get_info_fail),
					Toast.LENGTH_SHORT).show();
			break;
		// 메인 공지 정보 가져오기 성공.
		case MJUConstants.MAIN_NOTICE_SUCCESS:
			mainNoticeList = (ArrayList<MainNotice>) msg.obj;
			if (mainNoticeList != null) {
				setMainNotice();
				if (isOnCreate) {
					mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
				}
			}
			break;
		// 공지 가져오기 실패.
		case MJUConstants.MAIN_NOTICE_FAIL:
			if (isOnCreate) {
				mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
			}
			Toast.makeText(getBaseContext(),
					getString(R.string.main_notice_get_info_fail),
					Toast.LENGTH_SHORT).show();
			break;
		// 패너 이미지 가져오기 성공
		case MJUConstants.MAIN_BANNER_IMAGE_COMPLETE:
			Bitmap bannerBitmap = (Bitmap) msg.obj;
			CustomViewPager bannerViewPager = ((CustomViewPager) findViewById(R.id.main_banner_viewPager));
			((MainBannerViewPagerAdapter) bannerViewPager.getAdapter())
					.addView(bannerBitmap);
			if (!isBannerFisrtImageLoaded && isOnCreate) {
				mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
				isBannerFisrtImageLoaded = true;
			}
			break;
		// 배너 이미지 가져오기 실패
		case MJUConstants.MAIN_BANNER_IMAGE_FAIL:
			if (!isBannerFisrtImageLoaded && isOnCreate) {
				if (bannerImgFailCounter.get() == 1) {
					mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
				}
			}
			if (bannerImgFailCounter.getAndDecrement() == 1) {
				Toast.makeText(getBaseContext(),
						getString(R.string.main_banner_get_info_fail),
						Toast.LENGTH_SHORT).show();
			}
			break;
		// 배너 텍스트가져오기 성공
		case MJUConstants.MAIN_BANNER_TEXTS_COMPLETE:
			Banner tempBanner = (Banner) msg.obj;
			bannerList.add(tempBanner);
			int bannerSize = bannerList.size();

			if (bannerSize == 1) { // 泥�諛곕꼫 濡쒕뱶��
				updateBannerInfo(0);
				mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
			}

			// 배너 정보 로드가 다 안되었을때 사용자가 보려고 하는 경우, (프로그래스 다이얼로그 띄운상태)
			// 이상황에서 배너 정보 로드가 되었고 배너리스트가 add가 된경우.
			if (bannerSize - 1 == notLoadedPagePositionForBanner) {
				updateBannerInfo(notLoadedPagePositionForBanner);
				setPageMarker(MJUConstants.MAIN_BANNER,
						notLoadedPagePositionForBanner);
				progressDialog.dismiss();
				startImageBoardTimer();
			}
			break;
		// 메인 베너 텍스트 실패.
		case MJUConstants.MAIN_BANNER_TEXTS_FAIL:
			if (bannerTextFailCounter.getAndDecrement() == 1) {
				Toast.makeText(getBaseContext(),
						getString(R.string.main_banner_get_info_fail),
						Toast.LENGTH_SHORT).show();
			}
			break;
		// 메인 사진 성공.
		case MJUConstants.MAIN_PICTURE_IAMGE_COMPLETE:
			Bitmap pictureBitmap = (Bitmap) msg.obj;
			((MainPhotoViewPagerAdapter) pictureViewPager.getAdapter())
					.addView(pictureBitmap);
			if (!isPictureFisrtImageLoaded && isOnCreate) {
				mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
				isPictureFisrtImageLoaded = true;
			}
			break;
		// 메인 사진 실패.
		case MJUConstants.MAIN_PICTURE_IMAGE_FAIL:
			if (!isPictureFisrtImageLoaded && isOnCreate) {
				if (picnnerImgFailCounter.get() == 1) {
					mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
				}
			}
			if (picnnerImgFailCounter.getAndDecrement() == 1) {
				Toast.makeText(getBaseContext(),
						getString(R.string.main_picure_get_info_fail),
						Toast.LENGTH_SHORT).show();
			}
			break;
		// 메인 사진 성공
		case MJUConstants.MAIN_PICTURE_TEXTS_COMPLETE:
			MJUPhoto picture = (MJUPhoto) msg.obj;
			pictureList.add(picture);
			int pictureSize = pictureList.size();

			if (pictureSize == 1) {
				updatePictureInfo(0);
				mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
			}

			if (pictureSize - 1 == notLoadedPagePositionForPicture) {
				updatePictureInfo(notLoadedPagePositionForPicture);
				setPageMarker(MJUConstants.MAIN_PICTURE,
						notLoadedPagePositionForPicture);
				progressDialog.dismiss();
				startImageBoardTimer();
			}
			((MainPhotoViewPagerAdapter) pictureViewPager.getAdapter())
					.addTitle(picture.getTitle());
			break;
		// 메인 사진 텍스트 실패
		case MJUConstants.MAIN_PICTURE_TEXTS_FAIL:
			if (picnnerTextFailCounter.getAndDecrement() == 1) {
				Toast.makeText(getBaseContext(),
						getString(R.string.main_picure_get_info_fail),
						Toast.LENGTH_SHORT).show();
			}
			break;
		// login 성공.
		case MJUConstants.LOGIN_COMPLETE:
			Toast.makeText(getBaseContext(), getString(R.string.login_done),
					Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
			changeLayout(MYIWEB_LAYOUT);
			break;
		// login 실패
		case MJUConstants.LOGIN_FAILED:
			progressDialog.dismiss();
			alertDialog = MJUAlertDialog.newInstance(
					MJUConstants.NORMAL_ALERT_DIALOG, R.string.app_name,
					R.string.msg_login_fail, 0);
			alertDialog.show(fragmentManager, "");
			((EditText) findViewById(R.id.pw_input)).setText("");
		case MJUConstants.NETWORK_FAILED:
			progressDialog.dismiss();
		case MJUConstants.GET_STD_NAME_FAILED:
			progressDialog.dismiss();
			break;
		case MJUConstants.SPLASH_DISMISS:
			startBeginAnimation();
			break;
		case MJUConstants.FIRST_LAYOUT_OPENED:
			((CustomViewPager) findViewById(R.id.main_banner_viewPager))
					.setPagingEnbaled(false);
			((CustomViewPager) findViewById(R.id.main_picture_viewPager))
					.setPagingEnbaled(false);
			findViewById(R.id.main_first_invisible_view).setClickable(true);
			break;
		case MJUConstants.FIRST_LAYOUT_CLOSED:
			((CustomViewPager) findViewById(R.id.main_banner_viewPager))
					.setPagingEnbaled(true);
			((CustomViewPager) findViewById(R.id.main_picture_viewPager))
					.setPagingEnbaled(true);
			findViewById(R.id.main_first_invisible_view).setClickable(false);
			startImageBoardTimer();

			// 메인 페이지에서 로그인 안하고 다른 곳에서 로그인한 후 메인 페이지로 왔는데 로그인 레이아웃이 inflate된
			// 상태라면 myiweb 레이아웃으로 변경.
			if (currentQuickMenuLayout == LOGIN_LAYOUT) {
				if (LoginManager.checkLogin(getApplicationContext())) {
					changeLayout(MYIWEB_LAYOUT);
				}
			}
			break;
		case MJUConstants.SECOND_LAYOUT_OPENED:
			break;
		case MJUConstants.SECOND_LAYOUT_CLOSED:
			break;
		case MJUConstants.MAKE_BACK_BTN_FALSE:
			isBackBtnPressed = false;
		case MJUConstants.IMAGE_BOARD_START_TIMER:
			startImageBoardTimer();
			break;
		// 포토 사진 바까주는거.
		case MJUConstants.PHOTO_NEXT_PAGE:
			changeImageViewPage(MJUConstants.PHOTO_NEXT_PAGE);
			break;
		// 배너 바까주는 거.
		case MJUConstants.BANNER_NEXT_PAGE:
			changeImageViewPage(MJUConstants.BANNER_NEXT_PAGE);
			break;
		case MJUConstants.OUT_OF_MEMORY:
			Toast.makeText(getBaseContext(), R.string.getting_info_fail,
					Toast.LENGTH_SHORT).show();
			break;
		}
	}

	/**
	 * 
	 * Desc 날씨 정보를 가져오는 Thread를 실행시키는 역할.
	 * 
	 * @MethodName getWeather()
	 * @author Hs
	 */
	// 날씨 받아옴
	private void getWeather() {
		// TODO Auto-generated method stub
		WeatherThread seoulWeatherTrhead = new WeatherThread(mainHandler, this,
				WeatherThread.TYPE_SEOUL);
		seoulWeatherTrhead.start();

		WeatherThread yonginWeatherThread = new WeatherThread(mainHandler,
				this, WeatherThread.TYPE_YONGIN);
		yonginWeatherThread.start();
	}

	public void setPageMarker(int flag, int position) {
		// TODO Auto-generated method stub
		int length;
		LinearLayout markerlayout;
		if (flag == MJUConstants.MAIN_PICTURE) {
			markerlayout = (LinearLayout) findViewById(R.id.picture_viewpager_marker_layout);
			length = 7;
		} else {
			markerlayout = (LinearLayout) findViewById(R.id.banner_viewpager_marker_layout);
			length = 5;
		}
		markerlayout.removeAllViews();

		View mark = null;
		for (int i = 0; i < length; i++) {
			mark = new View(getApplicationContext());
			linearLayoutParams = new LinearLayout.LayoutParams(
					converter.getWidth(7), converter.getHeight(7));
			linearLayoutParams.setMargins(converter.getWidth(5), 0,
					converter.getWidth(5), converter.getHeight(2));
			mark.setLayoutParams(linearLayoutParams);
			if (i == position) {
				mark.setBackgroundResource(R.drawable.viewpager_makrer_selected);
			} else {
				mark.setBackgroundResource(R.drawable.viewpager_makrer);
			}
			markerlayout.addView(mark);
		}
	}

	private void startBeginAnimation() {
		// TODO Auto-generated method stub
		Animation fadeOutAnim = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.splash_fade_out);
		fadeOutAnim.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				isOnCreate = false;
				// 처음에 명지대 마크 화면 사라지게 하는 부분.
				findViewById(R.id.main_splash_image).setVisibility(
						View.INVISIBLE);
				findViewById(R.id.main_first_invisible_view)
						.setClickable(false);
				secondLayout.setVisibility(View.GONE);
				thirdLayout.setVisibility(View.GONE);

				// 레이아웃이 닫혀있을때만 이지미지 롤링 기능 시작.
				// if (firstLayoutSlideManager.getLayoutState() ==
				// MJUConstants.LAYOUT_CLOSED) {
				mainHandler
						.sendEmptyMessage(MJUConstants.IMAGE_BOARD_START_TIMER);
				// }

				SharedPreferences pref = getSharedPreferences(
						getString(R.string.pref_name), Activity.MODE_PRIVATE);
				boolean ischecked = pref.getBoolean(
						getString(R.string.pref_app_notice_show_again), false);

				if (!ischecked) {
					alertDialog = MJUAlertDialog.newInstance(
							MJUConstants.APP_NOTICE_DIALOG, R.string.app_name,
							R.string.main_notice_dialog_str, 0);
					alertDialog.show(fragmentManager, "");
				}
			}
		});
		// fade out 으로 화면 사라지게 하는 부분.
		findViewById(R.id.main_splash_image).startAnimation(fadeOutAnim);
	}

	public void saveNoticeCheckBoxState(boolean isChecked) {
		SharedPreferences pref = getSharedPreferences(
				getString(R.string.pref_name), Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(getString(R.string.pref_app_notice_show_again),
				isChecked);
		editor.commit();
	}

	/**
	 * Desc 날씨정보를 메인 액티비티 날씨 레이아웃에 뿌려주는 메소드.
	 * 
	 * @MethodName setWeatherInfo()
	 * @author Hs
	 * @param weatherInfo
	 * @param flag
	 */
	private void setWeatherInfo(WeatherInfo weatherInfo, int flag) {
		// TODO Auto-generated method stub
		String temperature = weatherInfo.getTemp();
		String state = weatherInfo.getText();

		TextView seoulTextView = (TextView) findViewById(R.id.weather_seoul_title);
		TextView seoulCampusTextView = (TextView) findViewById(R.id.weather_seoul_campus);
		TextView seoulWeatherTextView = (TextView) findViewById(R.id.weather_seoul_contents);
		TextView seoulDegreeTextView = (TextView) findViewById(R.id.weather_seoul_degree);
		TextView seoulDegreeSymbolTextView = (TextView) findViewById(R.id.weather_seoul_degree_symbol);

		TextView yonginTextView = (TextView) findViewById(R.id.weather_yongin_title);
		TextView yonginCampusTextView = (TextView) findViewById(R.id.weather_yongin_campus);
		TextView yonginWeatherTextView = (TextView) findViewById(R.id.weather_yongin_contents);
		TextView yonginDegreeTextView = (TextView) findViewById(R.id.weather_yongin_degree);
		TextView yonginDegreeSymbolTextView = (TextView) findViewById(R.id.weather_yongin_degree_symbol);

		/*
		 * 날씨 textview 레이아웃 조정.
		 */
		// 서울 title
		relativeLayoutParams = (RelativeLayout.LayoutParams) seoulTextView
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(15),
				converter.getHeight(3), 0, 0);
		seoulTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));
		// 서울 캠퍼스
		seoulCampusTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// 서울 날씨 내용
		seoulWeatherTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// 서울 온도
		relativeLayoutParams = (RelativeLayout.LayoutParams) seoulDegreeTextView
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(45), 0, 0, 0);
		seoulDegreeTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_ultlt_otf)));
		seoulDegreeSymbolTextView
				.setTypeface(Typeface.createFromAsset(getAssets(),
						getString(R.string.helvetica_neueltstd_ultlt_otf)));

		// 용인 title
		relativeLayoutParams = (RelativeLayout.LayoutParams) yonginTextView
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(15), 0, 0, 0);
		yonginTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));
		// 용인 캠퍼스
		yonginCampusTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// 용인 날씨 내용
		yonginWeatherTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// 용인 온도
		relativeLayoutParams = (RelativeLayout.LayoutParams) yonginDegreeTextView
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(45), 0, 0, 0);
		yonginDegreeTextView.setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_ultlt_otf)));
		yonginDegreeSymbolTextView
				.setTypeface(Typeface.createFromAsset(getAssets(),
						getString(R.string.helvetica_neueltstd_ultlt_otf)));

		/*
		 * 날씨 정보 셋팅
		 */
		if (flag == WeatherThread.TYPE_SEOUL) {
			seoulWeatherTextView.setText(state);
			seoulDegreeTextView.setText(temperature);
		} else {
			yonginWeatherTextView.setText(state);
			yonginDegreeTextView.setText(temperature);
		}
	}

	/**
	 * 
	 * Desc 명지대 홈페이지를 파싱해서 공지사항 데이터를 가져오는 메소드 가져온 결과는 핸들러를 통해서 공지.
	 * 
	 * @MethodName getNotice()
	 * @author Hs
	 */
	private void getMainNotice() {
		// TODO Auto-generated method stub
		MainNoticeThread noticeThread = new MainNoticeThread(mainHandler,
				MJUConstants.MAIN_NOTICE_URLS[0]);
		noticeThread.start();
	}

	// 화면 공지사항 네줄 보여주는것.
	private void setMainNotice() {
		// TODO Auto-generated method stub
		if (isOnCreate) {
			// main notice contents divider
			view = findViewById(R.id.main_notice_text4_underline);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(0, 0, converter.getWidth(35),
					converter.getHeight(15));
			view = findViewById(R.id.main_notice_text3_underline);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(0, 0, converter.getWidth(35), 0);
			view = findViewById(R.id.main_notice_text2_underline);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(0, 0, converter.getWidth(35), 0);
			view = findViewById(R.id.main_notice_text1_underline);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(0, 0, converter.getWidth(35), 0);

			// main notice contents 4
			view = findViewById(R.id.main_notice_text4);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(
					converter.getWidth(LEFT_MARGIN - 3),
					converter.getHeight(5), converter.getWidth(35), 0);
			((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
					getString(R.string.sdgtl_ttf)));
			((TextView) view).setText(mainNoticeList.get(3).getTitle());
			view.setOnClickListener(this);
			// main notice contents 3
			view = findViewById(R.id.main_notice_text3);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(0, converter.getHeight(5),
					converter.getWidth(35), 0);
			((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
					getString(R.string.sdgtl_ttf)));
			((TextView) view).setText(mainNoticeList.get(2).getTitle());
			view.setOnClickListener(this);
			// main notice contents 2
			view = findViewById(R.id.main_notice_text2);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(0, converter.getHeight(5),
					converter.getWidth(35), 0);
			((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
					getString(R.string.sdgtl_ttf)));
			((TextView) view).setText(mainNoticeList.get(1).getTitle());
			view.setOnClickListener(this);
			// main notice contents 1
			view = findViewById(R.id.main_notice_text1);
			relativeLayoutParams = (RelativeLayout.LayoutParams) view
					.getLayoutParams();
			relativeLayoutParams.setMargins(0, 0, converter.getWidth(35), 0);
			((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
					getString(R.string.sdgtl_ttf)));
			((TextView) view).setText(mainNoticeList.get(0).getTitle());
			view.setOnClickListener(this);
		} else {
			view = findViewById(R.id.main_notice_text1);
			((TextView) view).setText(mainNoticeList.get(0).getTitle());
			view = findViewById(R.id.main_notice_text2);
			((TextView) view).setText(mainNoticeList.get(1).getTitle());
			view = findViewById(R.id.main_notice_text3);
			((TextView) view).setText(mainNoticeList.get(2).getTitle());
			view = findViewById(R.id.main_notice_text4);
			((TextView) view).setText(mainNoticeList.get(3).getTitle());
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		isOnCreate = true;
		loadCount = new AtomicInteger();
		bannerTextFailCounter = new AtomicInteger(1);
		picnnerTextFailCounter = new AtomicInteger(1);
		bannerImgFailCounter = new AtomicInteger(1);
		picnnerImgFailCounter = new AtomicInteger(1);

		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		mainHandler = new MainHandler(MainActivity.this);

		converter = new PixelConverter(getApplicationContext());
		inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		bannerList = new ArrayList<Banner>();
		pictureList = new ArrayList<MJUPhoto>();
		notLoadedPagePositionForBanner = -1;
		notLoadedPagePositionForPicture = -1;

		isPictureFisrtImageLoaded = false;
		isBannerFisrtImageLoaded = false;

		/* 메인 레이아웃을 더해줌. */
		((LinearLayout) findViewById(R.id.main_first_layout_container))
				.addView((RelativeLayout) inflator.inflate(
						R.layout.main_first_layout, null));
		((LinearLayout) findViewById(R.id.main_second_layout_container))
				.addView((LinearLayout) inflator.inflate(
						R.layout.main_second_layout, null));
		((LinearLayout) findViewById(R.id.main_third_layout_container))
				.addView((LinearLayout) inflator.inflate(
						R.layout.main_third_layout, null));

		if (NetworkManager.checkNetwork(getApplicationContext())) {
			if (checkAutoLogin()) {
				loadCount.set(LOAD_COUNT + 1);
				getLogin();
			} else {
				loadCount.set(LOAD_COUNT);
			}
		}
	}

	private void initViewPager(int flag) {
		// TODO Auto-generated method stub
		CustomViewPager viewPager = null;
		switch (flag) {
		case MJUConstants.MAIN_BANNER:
			viewPager = (CustomViewPager) findViewById(R.id.main_banner_viewPager);
			viewPager.setAdapter(new MainBannerViewPagerAdapter(
					getApplicationContext(), bannerList));
			viewPager.setOnPageChangeListener(new MyPageChangeListener(
					MJUConstants.MAIN_BANNER));
			setPageMarker(MJUConstants.MAIN_BANNER, 0);
			viewPager.setOffscreenPageLimit(1);
			break;
		case MJUConstants.MAIN_PICTURE:
			viewPager = (CustomViewPager) findViewById(R.id.main_picture_viewPager);
			viewPager.setAdapter(new MainPhotoViewPagerAdapter(
					getApplicationContext(), pictureList));
			viewPager.setOnPageChangeListener(new MyPageChangeListener(
					MJUConstants.MAIN_PICTURE));
			setPageMarker(MJUConstants.MAIN_PICTURE, 0);
			viewPager.setOffscreenPageLimit(1);
			break;
		}
	}

	private void initLayout() {
		// TODO Auto-generated method stub

		// main first layout 오른쪽 여백 설정.
		view = findViewById(R.id.main_first_layout_container);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(0), 0);

		// main second layout 오른쪽 여백 설정.
		view = findViewById(R.id.main_second_layout_container);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(0), 0);

		/* 레이아웃의 크기를 화면의 크기에 맞게 조정 */
		// ////////////////////////////////////////////////////////////////////////////////////
		// / main first layout ////
		// ////////////////////////////////////////////////////////////////////////////////////
		// main first header layout
		view = findViewById(R.id.main_first_header_layout_container);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.height = converter.getHeight(95);

		// main logo image 크기 조정.
		view = findViewById(R.id.header_logo_img);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(120);
		relativeLayoutParams.height = converter.getHeight(33);
		relativeLayoutParams.setMargins(converter.getWidth(LEFT_MARGIN),
				converter.getHeight(5), 0, converter.getHeight(3));

		// to second textview
		view = findViewById(R.id.category_button);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(120);
		relativeLayoutParams.height = converter.getWidth(45);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		// weather
		view = findViewById(R.id.weather_divider1);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(10), 0,
				converter.getHeight(10));
		view = findViewById(R.id.weather_divider2);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(110),
				converter.getHeight(10), 0, converter.getHeight(10));

		// main quick(myiweb, login) layout
		view = findViewById(R.id.main_first_quick_layout_container);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.height = converter.getHeight(100);
		// quick menu layout
		initQuickMenuLayout();
		currentQuickMenuLayout = QUICK_MENU_LAYOUT;

		// main notice layout
		view = findViewById(R.id.main_first_notice_layout_container);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.height = converter.getHeight(200);
		// notice navi textviews
		// 일반
		view = findViewById(R.id.main_notice_notice_tv);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(LEFT_MARGIN - 5),
				converter.getHeight(8), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);
		currentNoticeNaviTextView = (TextView) view;
		// 행사
		view = findViewById(R.id.main_notice_ceremony_tv);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(37), 0, 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);
		// 학사
		view = findViewById(R.id.main_notice_school_affairs_tv);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(37), 0, 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);
		// 장학
		view = findViewById(R.id.main_notice_schalorship_tv);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(37), 0, 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);
		// 경력
		view = findViewById(R.id.main_notice_career_tv);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(37), 0, 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		// main picture layout
		view = findViewById(R.id.main_first_picture_layout_container);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.height = converter.getHeight(187);
		// main banner viewpager
		initViewPager(MJUConstants.MAIN_PICTURE);
		// main picture textviews
		view = findViewById(R.id.main_picture_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(LEFT_MARGIN),
				converter.getHeight(33), converter.getWidth(35), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.main_picture_date);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(65), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.main_picture_writer);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(10), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		// main banner layout
		view = findViewById(R.id.main_first_banner_layout_container);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.height = converter.getHeight(187);
		// main banner viewpager
		initViewPager(MJUConstants.MAIN_BANNER);
		// main banner textviews
		view = findViewById(R.id.main_banner_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(LEFT_MARGIN),
				converter.getHeight(33), converter.getWidth(35), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.main_banner_date);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(75), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		/*view = findViewById(R.id.main_banner_writer);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(10), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));*/

		findViewById(R.id.main_first_invisible_view).setOnTouchListener(this);

		mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);

	}

	private void initSecondLayout() {
		// TODO Auto-generated method stub
		view = findViewById(R.id.main_second_contents_layout);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(0), 0, 0, 0);

		// to first textview
		view = findViewById(R.id.main_move_button);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(120);
		relativeLayoutParams.height = converter.getWidth(45);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		// to third textview
		view = findViewById(R.id.develop_move_button);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(120);
		relativeLayoutParams.height = converter.getWidth(45);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		// main second layout title (category)
		view = findViewById(R.id.main_second_layout_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(40),
				converter.getHeight(45), 0, converter.getHeight(10));
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));

		// main second layout divider line
		view = findViewById(R.id.main_second_layout_divider1);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(40), 0,
				converter.getWidth(40), 0);

		view = findViewById(R.id.main_second_layout_divider2);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(40), 0,
				converter.getWidth(40), 0);
		view = findViewById(R.id.main_second_layout_divider3);

		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(40), 0,
				converter.getWidth(40), 0);

		// main second layout section1 title (guide)
		view = findViewById(R.id.section1_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(40),
				converter.getHeight(20), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));

		view = findViewById(R.id.menu_food);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(80), 0, 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_library);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(40), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_campusmap);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(30), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_traffic);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_entrance);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(30), 0,
				converter.getHeight(35));
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_phone);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		// main second layout section2 title (community)
		view = findViewById(R.id.section2_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(40),
				converter.getHeight(20), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));

		view = findViewById(R.id.menu_community);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_notice);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(30), 0,
				converter.getHeight(35));
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		// main second layout section3 title (etc)
		view = findViewById(R.id.section3_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(40),
				converter.getHeight(20), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));

		view = findViewById(R.id.menu_timetable);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, 0, 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_qna);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_introduce);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(30), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.menu_energy);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(30), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
		mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
	}

	private void initThirdLayout() {
		// TODO Auto-generated method stub
		view = findViewById(R.id.developer_layout);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		// linearLayoutParams.setMargins(converter.getWidth(100), 0,
		// converter.getWidth(100), 0);

		// to second textview
		view = findViewById(R.id.third_layout_to_main_textview);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(120);
		linearLayoutParams.height = converter.getWidth(45);
		relativeLayoutParams.setMargins(converter.getWidth(10), 0, 0, 0);
		((TextView) view).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) { // #4
				// TODO Auto-generated method stub
				secondLayoutSlideManager
						.setDirection(MJUConstants.RIGHT_DIRECTION);
				secondLayoutSlideManager.keepSlidingLayout();
				firstLayoutSlideManager.setMainDirection(2);
			}
		});
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.app_version);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(70), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_ultlt_otf)));

		view = findViewById(R.id.developer_info_title);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(40), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.developer_info_department1);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(25), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.developer_info_department2);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(10), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.developer_info_hello1);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(25), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.developer_info_hello2);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(10), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.feedback);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(80), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.feedback_email_btn);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(300);
		linearLayoutParams.height = converter.getHeight(65);
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(25), converter.getWidth(100), 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.developer_info_hello3);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(100),
				converter.getHeight(40), converter.getWidth(100), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		mainHandler.sendEmptyMessage(MJUConstants.LOAD_COMPLETE);
	}

	// quick 메뉴 마이아이웹 아이콘있는 부분..
	private void initQuickMenuLayout() {
		// TODO Auto-generated method stub

		// myiweb
		view = findViewById(R.id.quick_menu_myiweb_btn_layout);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.setMargins(converter.getWidth(LEFT_MARGIN), 0, 0, 0);
		// myiweb img btn
		view = findViewById(R.id.btn_quickmenu_myiweb);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.height = converter.getHeight(69);
		linearLayoutParams.setMargins(0, converter.getHeight(7), 0,
				converter.getHeight(4));
		view.setOnClickListener(this);
		// myiweb textview
		view = findViewById(R.id.quickmenu_myiweb_textview);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// library
		view = findViewById(R.id.quick_menu_library_btn_layout);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.setMargins(converter.getWidth(45), 0, 0, 0);
		// library img btn
		view = findViewById(R.id.btn_quickmenu_library);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.height = converter.getHeight(69);
		linearLayoutParams.setMargins(0, converter.getHeight(7), 0,
				converter.getHeight(4));
		view.setOnClickListener(this);
		// library textview
		view = findViewById(R.id.quickmenu_library_textview);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// phone
		view = findViewById(R.id.quick_menu_phone_btn_layout);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.setMargins(converter.getWidth(45), 0, 0, 0);
		// phone img btn
		view = findViewById(R.id.btn_quickmenu_phone);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.height = converter.getHeight(69);
		linearLayoutParams.setMargins(0, converter.getHeight(7), 0,
				converter.getHeight(4));
		view.setOnClickListener(this);
		// phone textview
		view = findViewById(R.id.quickmenu_phone_textview);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// food
		view = findViewById(R.id.quick_menu_food_btn_layout);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.setMargins(converter.getWidth(45), 0, 0, 0);
		// food img btn
		view = findViewById(R.id.btn_quickmenu_food);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(69);
		linearLayoutParams.height = converter.getHeight(69);
		linearLayoutParams.setMargins(0, converter.getHeight(7), 0,
				converter.getHeight(4));
		view.setOnClickListener(this);
		// food textview
		view = findViewById(R.id.quickmenu_food_textview);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
	}

	/**
	 * 
	 * Desc 로그인
	 * 
	 * @MethodName login()
	 * @author Hs
	 */
	private void login() {
		// TODO Auto-generated method stub
		String id = ((TextView) findViewById(R.id.id_input)).getText()
				.toString();
		String pw = ((TextView) findViewById(R.id.pw_input)).getText()
				.toString();

		if (NetworkManager.checkNetwork(MainActivity.this)) {
			if (checkIdPw(id, pw)) {
				LoginManager loginManageer = new LoginManager(getApplication(),
						mainHandler, id, pw, isAutoLoginChecked,
						LoginManager.BTN_CLICK_LOGIN);
				loginManageer.executeLogin();
				progressDialog.show(fragmentManager, "");
			}
		}
	}

	private boolean checkIdPw(String id, String pw) {
		// TODO Auto-generated method stub

		if (id.equals("")) {
			Toast.makeText(
					getBaseContext(),
					getResources()
							.getString(R.string.msg_login_id_insufficient),
					Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.id_input)).requestFocus();
			return false;
		} else if (pw.equals("")) {
			Toast.makeText(
					getBaseContext(),
					getResources()
							.getString(R.string.msg_login_pw_insufficient),
					Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.pw_input)).requestFocus();
			return false;
		}
		return true;
	}

	private boolean checkAutoLogin() {
		// get
		SharedPreferences pref = getSharedPreferences(
				getString(R.string.pref_name), MODE_PRIVATE);
		boolean checkAutoLogin = pref.getBoolean(
				getString(R.string.pref_key_auto_login), false);
		return checkAutoLogin;
	}

	private void getLogin() {
		// get Preferences
		SharedPreferences pref = getSharedPreferences(
				getString(R.string.pref_name), MODE_PRIVATE);
		String id = pref.getString(getString(R.string.pref_key_user_id), null);
		String pw = pref.getString(getString(R.string.pref_key_user_pw), null);
		// decrypt password
		pw = CipherManager.decryptDES(pw, getApplicationContext());

		LoginManager loginManageer = new LoginManager(getApplication(),
				mainHandler, id, pw, true, LoginManager.AUTO_LOGIN);
		loginManageer.executeLogin();
	}

	private void logout() {
		// cookie remove
		CookieManager.getInstance().removeAllCookie();

		Toast.makeText(getBaseContext(), getString(R.string.logout_done),
				Toast.LENGTH_SHORT).show();
		// get prefeditor
		SharedPreferences pref = getSharedPreferences(
				getString(R.string.pref_name), MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		// set pref
		editor.putBoolean(getString(R.string.pref_key_auto_login), false);
		editor.putString(getString(R.string.pref_key_user_pw), null);
		editor.commit();
	}

	private String getSTDNumber() {
		SharedPreferences pref = getSharedPreferences(
				getString(R.string.pref_name), MODE_PRIVATE);
		return pref.getString(getString(R.string.pref_key_user_id), null);
	}

	private void changeLayout(int flag) {
		// TODO Auto-generated method stub
		((LinearLayout) findViewById(R.id.main_first_quick_layout_container))
				.removeAllViews();
		currentQuickMenuLayout = flag;
		switch (flag) {
		case LOGIN_LAYOUT:
			((LinearLayout) findViewById(R.id.main_first_quick_layout_container))
					.addView((RelativeLayout) inflator.inflate(
							R.layout.main_first_login_layout, null));
			initLoginLayout();
			break;
		case MYIWEB_LAYOUT:
			((LinearLayout) findViewById(R.id.main_first_quick_layout_container))
					.addView((RelativeLayout) inflator.inflate(
							R.layout.main_first_myiweb_layout, null));
			initMyiwebLayout();
			break;
		case QUICK_MENU_LAYOUT:
			((LinearLayout) findViewById(R.id.main_first_quick_layout_container))
					.addView((LinearLayout) inflator.inflate(
							R.layout.main_first_quickmenu_layout, null));
			initQuickMenuLayout();
			break;
		}
	}

	// myiweb 아이콘 클릭시 나타나는 레이아웃.
	private void initMyiwebLayout() {
		// TODO Auto-generated method stub
		// to quick menu btn
		view = findViewById(R.id.btn_myiweb_to_quickmenu);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(60);
		relativeLayoutParams.height = converter.getHeight(30);
		relativeLayoutParams.setMargins(converter.getWidth(LEFT_MARGIN),
				converter.getHeight(30), 0, 0);
		view.setOnClickListener(this);
		// quick menu title
		view = findViewById(R.id.myiweb_quick_menu_textview);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(46),
				converter.getHeight(7), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// titmetable btn
		view = findViewById(R.id.btn_myiweb_timetable);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(65);
		relativeLayoutParams.height = converter.getHeight(65);
		relativeLayoutParams.setMargins(converter.getWidth(15),
				converter.getHeight(15), 0, 0);
		view.setOnClickListener(this);
		// grade btn
		view = findViewById(R.id.btn_myiweb_grade);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(65);
		relativeLayoutParams.height = converter.getHeight(65);
		relativeLayoutParams.setMargins(converter.getWidth(15), 0, 0, 0);
		view.setOnClickListener(this);
		// graduate btn
		view = findViewById(R.id.btn_myiweb_graduate);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(65);
		relativeLayoutParams.height = converter.getHeight(65);
		relativeLayoutParams.setMargins(converter.getWidth(15), 0, 0, 0);
		view.setOnClickListener(this);
		// logout btn
		view = findViewById(R.id.temp_logout_btn);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(70);
		relativeLayoutParams.height = converter.getHeight(30);
		relativeLayoutParams.setMargins(converter.getWidth(15),
				converter.getHeight(30), 0, 0);
		view.setOnClickListener(this);
	}

	// login시 바뀌는 화면.
	private void initLoginLayout() {
		// TODO Auto-generated method stub
		// to quick menu btn
		view = findViewById(R.id.btn_login_to_quickmenu);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(60);
		relativeLayoutParams.height = converter.getHeight(30);
		relativeLayoutParams.setMargins(converter.getWidth(LEFT_MARGIN),
				converter.getHeight(30), 0, 0);
		view.setOnClickListener(this);
		// quick menu title
		view = findViewById(R.id.login_quick_menu_textview);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(46),
				converter.getHeight(7), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		// id title
		view = findViewById(R.id.id_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(35),
				converter.getHeight(17), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));
		// pw title
		view = findViewById(R.id.pw_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(10), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.helvetica_neueltstd_th_otf)));
		// id input
		view = findViewById(R.id.id_input);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(120);
		relativeLayoutParams.height = converter.getHeight(23);
		relativeLayoutParams.setMargins(converter.getWidth(35),
				converter.getHeight(20), 0, 0);
		((EditText) view).requestFocus();
		showKeyBoard(view);

		// pw input
		view = findViewById(R.id.pw_input);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(120);
		relativeLayoutParams.height = converter.getHeight(23);
		relativeLayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		((EditText) view).setOnEditorActionListener(this);
		// login btn
		view = findViewById(R.id.login_btn);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(78);
		relativeLayoutParams.height = converter.getHeight(30);
		relativeLayoutParams.setMargins(converter.getWidth(30),
				converter.getHeight(18), 0, 0);
		view.setOnClickListener(this);
		// auto login textview
		view = findViewById(R.id.auto_login_textview);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(33),
				converter.getHeight(16), 0, 0);
		view.setOnClickListener(this);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
	}

	// login 이나 pwd 텍스트 공간 클릭하면 키보드 보여지게하기.
	private void showKeyBoard(View view) {
		// TODO Auto-generated method stub
		EditText editText = (EditText) view;
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.RESULT_HIDDEN);
	}

	// 키보드 숨기기.
	private void hideKeyBoard() {
		// TODO Auto-generated method stub
		EditText edit = (EditText) findViewById(R.id.id_input);
		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
	}

	private void updateBannerInfo(int postion) {
		// TODO Auto-generated method stub
		if (bannerList.size() <= 0) {
			return;
		}
		Banner banner = bannerList.get(postion);

		((TextView) findViewById(R.id.main_banner_title)).setText(banner
				.getTitle());
		((TextView) findViewById(R.id.main_banner_date)).setText(banner
				.getDate());
		//((TextView) findViewById(R.id.main_banner_writer)).setText(banner.getWirter());
	}

	private void updatePictureInfo(int position) {
		// TODO Auto-generated method stub
		if (pictureList.size() <= 0) {
			return;
		}
		MJUPhoto picture = pictureList.get(position);

		((TextView) findViewById(R.id.main_picture_title)).setText(picture
				.getTitle());
		((TextView) findViewById(R.id.main_picture_date)).setText(picture
				.getDate());
		((TextView) findViewById(R.id.main_picture_writer)).setText(picture
				.getWirter());
	}

	private boolean isInstalled(String appUrl) {
		List<PackageInfo> appinfo = getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < appinfo.size(); i++) {
			PackageInfo pi = appinfo.get(i);
			if (pi.packageName.equals(appUrl))
				return true;
		}
		return false;
	}

	// 도서관 앱 으로 이동 하는 부분.
	private void goToLibraryApp() {
		// TODO Auto-generated method stub
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setData(Uri
					.parse(getString(R.string.url_library_app_pkg_name)
							+ "?apid=" + getSTDNumber()));
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			startActivity(new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.url_library_app_market))));
		}
	}

	private void closeSecondAndFirstLayout() {
		// TODO Auto-generated method stub
		 /*firstLayoutSlideManager.setDirection(MJUConstants.RIGHT_DIRECTION);
		 secondLayoutSlideManager.setDirection(MJUConstants.RIGHT_DIRECTION);

		 secondLayoutSlideManager.keepSlidingLayout();
		 firstLayoutSlideManager.keepSlidingLayout();*/
	}

	// 공지사항 서브 제목.
	private String getNoticeSubTitle() {
		switch (currentNoticeNaviTextView.getId()) {
		case R.id.main_notice_notice_tv:
			return "일반 공지";
		case R.id.main_notice_ceremony_tv:
			return "행사 공지";
		case R.id.main_notice_school_affairs_tv:
			return "학사 공지";
		case R.id.main_notice_schalorship_tv:
			return "장학 공지";
		case R.id.main_notice_career_tv:
			return "경력 공지";
		}
		return "공지";
	}

	private void changeImageViewPage(int flag) {
		// TODO Auto-generated method stub
		CustomViewPager viewPager;
		if (flag == MJUConstants.PHOTO_NEXT_PAGE) { // photo
			viewPager = (CustomViewPager) findViewById(R.id.main_picture_viewPager);
		} else { // banner
			viewPager = (CustomViewPager) findViewById(R.id.main_banner_viewPager);
		}

		int currentPage = viewPager.getCurrentItem();
		int currentSize = viewPager.getAdapter().getCount();

		if (currentSize != 0) {
			int nextPage = (currentPage + 1) % currentSize;

			if (viewPager.getChildCount() > 0) {
				viewPager.setCurrentItem(nextPage, true);
			}
		}
	}

	// 처음에 명지대 마크화면에서 메인 화면으로 넘어가게 하는 부분. 시간도 설정.
	private void startImageBoardTimer() {
		// TODO Auto-generated method stub
		if (photoScrollTimer == null) {
			photoScrollTimer = new Timer();
			photoSrollTimerTask = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mainHandler.sendEmptyMessage(MJUConstants.PHOTO_NEXT_PAGE);
				}
			};
			photoScrollTimer.scheduleAtFixedRate(photoSrollTimerTask, 4000,
					4000);
		}

		if (bannerScrollTimer == null) {
			bannerScrollTimer = new Timer();
			bannerSrollTimerTask = new TimerTask() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mainHandler.sendEmptyMessage(MJUConstants.BANNER_NEXT_PAGE);
				}
			};
			bannerScrollTimer.scheduleAtFixedRate(bannerSrollTimerTask, 6000,
					4000);
		}
		if (thirdLayout.getVisibility() == View.VISIBLE) {
			thirdLayout.setVisibility(View.GONE);
			secondLayout.setVisibility(View.GONE);
		} else if (secondLayout.getVisibility() == View.VISIBLE) {
			secondLayout.setVisibility(View.GONE);
		}
		// if(firstLayout.getVisibility()==View.GONE) {
		// firstLayout.setVisibility(View.VISIBLE);
		// }
	}

	// 액티비티가 화면에서 사라지면 실행됨.
	private void stopImageBoardTimer() {
		// TODO Auto-generated method stub
		if (photoSrollTimerTask != null) {
			photoSrollTimerTask.cancel();
			photoSrollTimerTask = null;
		}
		if (photoScrollTimer != null) {
			photoScrollTimer.cancel();
			photoScrollTimer.purge();
			photoScrollTimer = null;
		}

		if (bannerSrollTimerTask != null) {
			bannerSrollTimerTask.cancel();
			bannerSrollTimerTask = null;
		}
		if (bannerScrollTimer != null) {
			bannerScrollTimer.cancel();
			bannerScrollTimer.purge();
			bannerScrollTimer = null;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		// slide manager에 추가하는것.
		// initiate firstLayout

		if (firstLayoutSlideManager == null) {
			firstLayoutSlideManager = new MainLayoutSlideManager(
					MJUConstants.FIRST_MAIN_LAYOUT,
					findViewById(R.id.main_first_layout_container), mainHandler);
			firstLayout.setClickable(true);
			firstLayout.setOnTouchListener(this);

		}

		// initiate secondLayout
		if (secondLayoutSlideManager == null) {
			secondLayoutSlideManager = new MainLayoutSlideManager(
					MJUConstants.SECOND_MAIN_LAYOUT,
					findViewById(R.id.main_second_layout_container),
					mainHandler);
			secondLayout.setClickable(true);
			secondLayout.setOnTouchListener(this);
			thirdLayout.setVisibility(View.GONE);
		}

		TextView categoryButton = (TextView) findViewById(R.id.category_button);
		TextView mainMoveButton = (TextView) findViewById(R.id.main_move_button);
		TextView developButton = (TextView) findViewById(R.id.develop_move_button);

		categoryButton.setOnClickListener(new OnClickListener() { // #1

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						LayoutFlag = 2;
						stopImageBoardTimer();
						firstLayoutSlideManager
								.setDirection(MJUConstants.LEFT_DIRECTION);
						firstLayoutSlideManager.keepSlidingLayout();
						secondLayout.setVisibility(View.VISIBLE);
						thirdLayout.setVisibility(View.VISIBLE);
						firstLayoutSlideManager.setMainDirection(2);
					}
				});

		mainMoveButton.setOnClickListener(new OnClickListener() { // #2
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						LayoutFlag = 1;
						if (thirdLayout.getVisibility() == View.VISIBLE) {

							firstLayoutSlideManager
									.setDirection(MJUConstants.RIGHT_DIRECTION);
							secondLayoutSlideManager
									.setDirection(MJUConstants.RIGHT_DIRECTION);

							secondLayoutSlideManager.keepSlidingLayout();
							firstLayoutSlideManager.keepSlidingLayout();
							firstLayoutSlideManager.setMainDirection(1);
						} else {

							firstLayoutSlideManager
									.setDirection(MJUConstants.RIGHT_DIRECTION);
							firstLayoutSlideManager.keepSlidingLayout();
							firstLayoutSlideManager.setMainDirection(1);
						}
					}
				});

		developButton.setOnClickListener(new OnClickListener() { // #3
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						LayoutFlag = 3;
						secondLayoutSlideManager
								.setDirection(MJUConstants.LEFT_DIRECTION);
						secondLayoutSlideManager.keepSlidingLayout();
						firstLayoutSlideManager.setMainDirection(3);
					}
				});

		// }
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		// TODO Auto-generated method stub
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			((ImageButton) findViewById(R.id.login_btn)).performClick();
		}
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// 만약 앱 구독중이면 터치 무시.
		if (isOnCreate) {
			return false;
		}
		/*
		 * int viewId = v.getId(); switch (event.getAction()) { case
		 * MotionEvent.ACTION_DOWN: if (viewId ==
		 * R.id.main_first_layout_container || viewId ==
		 * R.id.main_first_invisible_view) {
		 * firstLayoutSlideManager.initXPostion(event.getRawX());
		 * 
		 * // 둘다 열려 있을때 첫번째 레이아웃을 터치해서 닫으려고 하는 경우 첫, 두번째 레이아웃을 동시에 닫기 위해서 두번째
		 * 레이아웃도 초기화. if (firstLayoutSlideManager.getLayoutState() ==
		 * MJUConstants.LAYOUT_OPENED &&
		 * secondLayoutSlideManager.getLayoutState() ==
		 * MJUConstants.LAYOUT_OPENED) {
		 * secondLayoutSlideManager.initXPostion(event.getRawX()); } } else {
		 * secondLayoutSlideManager.initXPostion(event.getRawX()); } break; case
		 * MotionEvent.ACTION_MOVE: if (viewId ==
		 * R.id.main_first_layout_container || viewId ==
		 * R.id.main_first_invisible_view) { stopImageBoardTimer();
		 * firstLayoutSlideManager.slideLayout(event.getRawX());
		 * 
		 * // 둘다 열려 있을때 첫번째 레이아웃을 터치해서 닫을려고 하는 경우 첫, 두번째 레이아웃을 동시에 닫기 위해서 두번째
		 * 레이아웃도 움직임 if (firstLayoutSlideManager.getLayoutState() ==
		 * MJUConstants.LAYOUT_OPENED &&
		 * secondLayoutSlideManager.getLayoutState() ==
		 * MJUConstants.LAYOUT_OPENED) {
		 * secondLayoutSlideManager.slideLayout(event.getRawX()); } } else {
		 * secondLayoutSlideManager.slideLayout(event.getRawX()); } break; case
		 * MotionEvent.ACTION_UP: if (viewId == R.id.main_first_layout_container
		 * || viewId == R.id.main_first_invisible_view) {
		 * firstLayoutSlideManager.keepSlidingLayout();
		 * 
		 * // 둘다 열려있을때 첫번째 레이아웃을 터치해서 닫을려고 하는 경우 첫, 두번째 레이아웃을 동시에 닫기 위해서 두번째
		 * 레이아웃도 움직임. if (firstLayoutSlideManager.getLayoutState() ==
		 * MJUConstants.LAYOUT_OPENED &&
		 * secondLayoutSlideManager.getLayoutState() ==
		 * MJUConstants.LAYOUT_OPENED) {
		 * secondLayoutSlideManager.keepSlidingLayout(); } } else {
		 * secondLayoutSlideManager.keepSlidingLayout();
		 * 
		 * } break; }
		 */
		return false;
	}

	// second layout이 나온다?
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == RESULT_OK) {
			if (requestCode == MJUConstants.RQ_COMMUNITY_AFTER_LOGIN) {
				findViewById(R.id.menu_community).performClick();
			} else if (requestCode == MJUConstants.RQ_TIMETABLE_AFTER_LOGIN) {
				findViewById(R.id.menu_timetable).performClick();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		Log.e("[de]", "OnClick On");

		// 만약 앱구동중이면 터치 무시
		if (isOnCreate) {
			return;
		}
		MainNoticeThread noticeThread;
		Intent intent = null;
		MainNotice notice = null;

		switch (v.getId()) {
		case R.id.btn_quickmenu_myiweb:
			if (LoginManager.checkLogin(getApplicationContext())) {
				changeLayout(MYIWEB_LAYOUT);
			} else {
				changeLayout(LOGIN_LAYOUT);
			}
			break;
		case R.id.btn_quickmenu_library:
		case R.id.menu_library:
			LayoutFlag = 2;
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				if (LoginManager.checkLogin(getApplicationContext())) {
					// 로그인 되어있을때

					goToLibraryApp();
				} else {
					if (isInstalled("kr.ac.mjlib.library")) { // 로그인 x, 앱설치 o
						Log.e("[de]", "lib install");

						Toast.makeText(getBaseContext(),
								R.string.msg_login_alert, Toast.LENGTH_SHORT)
								.show();

						Log.e("[de]", "lib install end");
					} else { // 로그인 x, 앱설치 x

						Log.e("[de]", "lib not install");

						startActivity(new Intent(
								Intent.ACTION_VIEW,
								Uri.parse(getString(R.string.url_library_app_market))));

						Log.e("[de]", "lib not install end");

					}
				}
			}
			return;
		case R.id.btn_quickmenu_phone:
		case R.id.menu_phone:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				intent = new Intent(MainActivity.this, WebViewActivity.class);
				intent.putExtra("rooturl",
						getResources().getString(R.string.url_phone));
			}
			break;
		case R.id.btn_quickmenu_food:
		case R.id.menu_food:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				intent = new Intent(MainActivity.this, FoodActivity.class);
			}
			break;
		case R.id.btn_login_to_quickmenu:
			hideKeyBoard();
			changeLayout(QUICK_MENU_LAYOUT);
			break;
		case R.id.btn_myiweb_to_quickmenu:
			changeLayout(QUICK_MENU_LAYOUT);
			break;
		case R.id.login_btn:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				hideKeyBoard();
				login();
			}
			return;
		case R.id.temp_logout_btn:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				logout();
				changeLayout(QUICK_MENU_LAYOUT);
			}
			return;
		case R.id.btn_myiweb_timetable:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				intent = new Intent(MainActivity.this, MyiwebActivity.class);
				intent.putExtra(MJUConstants.MYIWEB_FLAG,
						MJUConstants.MYIWEB_TIMETABLE);
			}
			break;
		case R.id.btn_myiweb_grade:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				intent = new Intent(MainActivity.this, MyiwebActivity.class);
				intent.putExtra(MJUConstants.MYIWEB_FLAG,
						MJUConstants.MYIWEB_GRADE);
			}
			break;
		case R.id.btn_myiweb_graduate:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				intent = new Intent(MainActivity.this, MyiwebActivity.class);
				intent.putExtra(MJUConstants.MYIWEB_FLAG,
						MJUConstants.MYIWEB_GRADUATE);
			}
			break;
		case R.id.auto_login_textview:
			if (isAutoLoginChecked) {
				((TextView) v).setTextColor(getResources().getColor(
						R.color.auto_login_textview));
				isAutoLoginChecked = false;
			} else {
				((TextView) v).setTextColor(getResources().getColor(
						R.color.auto_login_textview_checked));
				isAutoLoginChecked = true;
			}
			return;
		case R.id.main_notice_notice_tv:
			if (currentNoticeNaviTextView.getId() != v.getId()) {
				noticeThread = new MainNoticeThread(mainHandler,
						MJUConstants.MAIN_NOTICE_URLS[0]);
				noticeThread.start();

				// 전 텍스트 색 변경.
				currentNoticeNaviTextView
						.setTextSize(
								TypedValue.COMPLEX_UNIT_PX,
								getResources()
										.getDimension(
												R.dimen.main_notice_navi_text_size_not_selected));
				// 선택된 텍스트 색 변경
				currentNoticeNaviTextView = (TextView) v;
				currentNoticeNaviTextView.setTextSize(
						TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(
								R.dimen.main_notice_navi_text_size_selected));
			}
			break;
		case R.id.main_notice_ceremony_tv:
			if (currentNoticeNaviTextView.getId() != v.getId()) {
				noticeThread = new MainNoticeThread(mainHandler,
						MJUConstants.MAIN_NOTICE_URLS[1]);
				noticeThread.start();

				// 전 텍스트 색 변경
				currentNoticeNaviTextView
						.setTextSize(
								TypedValue.COMPLEX_UNIT_PX,
								getResources()
										.getDimension(
												R.dimen.main_notice_navi_text_size_not_selected));
				// 선택된 텍스트 색 변경
				currentNoticeNaviTextView = (TextView) v;
				currentNoticeNaviTextView.setTextSize(
						TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(
								R.dimen.main_notice_navi_text_size_selected));
			}
			break;
		case R.id.main_notice_school_affairs_tv:
			if (currentNoticeNaviTextView.getId() != v.getId()) {
				noticeThread = new MainNoticeThread(mainHandler,
						MJUConstants.MAIN_NOTICE_URLS[2]);
				noticeThread.start();

				// 전 텍스트 색 변경
				currentNoticeNaviTextView
						.setTextSize(
								TypedValue.COMPLEX_UNIT_PX,
								getResources()
										.getDimension(
												R.dimen.main_notice_navi_text_size_not_selected));
				// 선택된 텍스트 색 변경
				currentNoticeNaviTextView = (TextView) v;
				currentNoticeNaviTextView.setTextSize(
						TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(
								R.dimen.main_notice_navi_text_size_selected));
			}
			break;
		case R.id.main_notice_schalorship_tv:
			if (currentNoticeNaviTextView.getId() != v.getId()) {
				noticeThread = new MainNoticeThread(mainHandler,
						MJUConstants.MAIN_NOTICE_URLS[3]);
				noticeThread.start();

				// 전 텍스트 색 변경
				currentNoticeNaviTextView
						.setTextSize(
								TypedValue.COMPLEX_UNIT_PX,
								getResources()
										.getDimension(
												R.dimen.main_notice_navi_text_size_not_selected));
				// 선택된 텍스트 색 변경
				currentNoticeNaviTextView = (TextView) v;
				currentNoticeNaviTextView.setTextSize(
						TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(
								R.dimen.main_notice_navi_text_size_selected));
			}
			break;
		case R.id.main_notice_career_tv:
			if (currentNoticeNaviTextView.getId() != v.getId()) {
				noticeThread = new MainNoticeThread(mainHandler,
						MJUConstants.MAIN_NOTICE_URLS[4]);
				noticeThread.start();

				// 전 텍스트 색 변경
				currentNoticeNaviTextView
						.setTextSize(
								TypedValue.COMPLEX_UNIT_PX,
								getResources()
										.getDimension(
												R.dimen.main_notice_navi_text_size_not_selected));
				// 선택된 텍스트 색 변경
				currentNoticeNaviTextView = (TextView) v;
				currentNoticeNaviTextView.setTextSize(
						TypedValue.COMPLEX_UNIT_PX,
						getResources().getDimension(
								R.dimen.main_notice_navi_text_size_selected));
			}
			break;
		case R.id.main_notice_text1:
			notice = mainNoticeList.get(0);
			intent = new Intent(MainActivity.this, NoticeViewActivity.class);
			intent.putExtra("url", notice.getUrl());
			intent.putExtra("subtitle", getNoticeSubTitle());
			break;
		case R.id.main_notice_text2:
			notice = mainNoticeList.get(1);
			intent = new Intent(MainActivity.this, NoticeViewActivity.class);
			intent.putExtra("url", notice.getUrl());
			intent.putExtra("subtitle", getNoticeSubTitle());
			break;
		case R.id.main_notice_text3:
			notice = mainNoticeList.get(2);
			intent = new Intent(MainActivity.this, NoticeViewActivity.class);
			intent.putExtra("url", notice.getUrl());
			intent.putExtra("subtitle", getNoticeSubTitle());
			break;
		case R.id.main_notice_text4:
			notice = mainNoticeList.get(3);
			intent = new Intent(MainActivity.this, NoticeViewActivity.class);
			intent.putExtra("url", notice.getUrl());
			intent.putExtra("subtitle", getNoticeSubTitle());
			break;
		case R.id.menu_campusmap:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				intent = new Intent(MainActivity.this, CampusmapActivity.class);
			}
			break;
		case R.id.menu_entrance:
			if (isInstalled(getString(R.string.url_entrance_app_pkg_name))) { // �낇븰
				LayoutFlag = 2; // �깆씠
				// �ㅼ튂�섏뼱�덉쑝硫�
				// start AdmissionApp
				intent = getPackageManager().getLaunchIntentForPackage(
						getString(R.string.url_entrance_app_pkg_name));
			} else { // �ㅼ튂 �덈릺�댁엳�쇰㈃
				// start market
				Uri uri = Uri
						.parse(getString(R.string.url_entrance_app_market));
				intent = new Intent(Intent.ACTION_VIEW, uri);
			}
			break;
		case R.id.menu_traffic:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				intent = new Intent(MainActivity.this, TrafficActivity.class);
			}
			break;
		case R.id.menu_community:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				if (LoginManager.checkLogin(getApplicationContext())) {
					intent = new Intent(MainActivity.this,
							CommunityListActivity.class);
					break;
				} else {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent,
							MJUConstants.RQ_COMMUNITY_AFTER_LOGIN);
					return;
				}
			}
			break;
		case R.id.menu_notice:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				intent = new Intent(MainActivity.this, NoticeListActivity.class);
			}
			break;
		case R.id.menu_timetable:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				if (LoginManager.checkLogin(getApplicationContext())) {
					intent = new Intent(MainActivity.this, MyiwebActivity.class);
					intent.putExtra(MJUConstants.MYIWEB_FLAG,
							MJUConstants.MYIWEB_TIMETABLE);
				} else {
					intent = new Intent(MainActivity.this, LoginActivity.class);
					startActivityForResult(intent,
							MJUConstants.RQ_TIMETABLE_AFTER_LOGIN);
					return;
				}
			}
			break;
		case R.id.menu_qna:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				intent = new Intent(MainActivity.this,
						ComplaintListActivity.class);
			}
			break;
		case R.id.menu_introduce:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				LayoutFlag = 2;
				intent = new Intent(MainActivity.this, IntroduceActivity.class);
			}
			break;
		case R.id.menu_energy:
			LayoutFlag = 2;
			intent = new Intent(MainActivity.this,
					GreencampusMainActivity.class);
			break;
		case R.id.feedback_email_btn:
			LayoutFlag = 3;
			intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"
					+ MJUConstants.FEEDBACK_EMAIL_ADDR));
			break;
		case R.id.third_layout_to_main_textview:
			closeSecondAndFirstLayout();
			LayoutFlag = 2;
			//break;
			return;
		}

		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}

	}

	private class MyPageChangeListener implements OnPageChangeListener {

		int flag;

		public MyPageChangeListener(int flag) {
			// TODO Auto-generated constructor stub
			this.flag = flag;
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageSelected(int position) {
			// TODO Auto-generated method stub
			if (flag == MJUConstants.MAIN_BANNER) { // 배너
				if (bannerList.size() - 1 < position) {
					progressDialog.show(fragmentManager, "");
					notLoadedPagePositionForBanner = position;
					stopImageBoardTimer();
				} else {
					setPageMarker(MJUConstants.MAIN_BANNER, position);
					updateBannerInfo(position);
				}
			} else { // 사진
				if (pictureList.size() - 1 < position) {
					progressDialog.show(fragmentManager, "");
					notLoadedPagePositionForPicture = position;
					stopImageBoardTimer();
				} else {
					setPageMarker(MJUConstants.MAIN_PICTURE, position);
					updatePictureInfo(position);
				}
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		int firstLayoutState = firstLayoutSlideManager.getLayoutState();
		int secondLyaoutState = secondLayoutSlideManager.getLayoutState();

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (firstLayoutState == MJUConstants.SLIDING
					|| secondLyaoutState == MJUConstants.SLIDING) {
				return false;
			}
			if (firstLayoutSlideManager.getMainDirection() == 1) {
				// 첫번재 레이아웃 닫혀있는 경우, 메뉴(2번째 레이아웃) 보여줌

				stopImageBoardTimer();
				firstLayoutSlideManager
						.setDirection(MJUConstants.LEFT_DIRECTION);
				firstLayoutSlideManager.keepSlidingLayout();
				secondLayout.setVisibility(View.VISIBLE);
				thirdLayout.setVisibility(View.VISIBLE);
				firstLayoutSlideManager.setMainDirection(2);

			} else {
				if (firstLayoutSlideManager.getMainDirection() == 2) {
					if (thirdLayout.getVisibility() == View.VISIBLE) {
						firstLayoutSlideManager
								.setDirection(MJUConstants.RIGHT_DIRECTION);
						secondLayoutSlideManager
								.setDirection(MJUConstants.RIGHT_DIRECTION);

						secondLayoutSlideManager.keepSlidingLayout();
						firstLayoutSlideManager.keepSlidingLayout();
						firstLayoutSlideManager.setMainDirection(1);
					} else {
						firstLayoutSlideManager
								.setDirection(MJUConstants.RIGHT_DIRECTION);
						firstLayoutSlideManager.keepSlidingLayout();
						firstLayoutSlideManager.setMainDirection(1);
					}
				} else {
					secondLayoutSlideManager
							.setDirection(MJUConstants.RIGHT_DIRECTION);
					secondLayoutSlideManager.keepSlidingLayout();
					firstLayoutSlideManager.setMainDirection(2);
				}
			}
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 슬라이드 중이면 무시.

			if (firstLayoutState == MJUConstants.SLIDING
					|| secondLyaoutState == MJUConstants.SLIDING) {

				return false;
			}
			if (firstLayoutSlideManager.getMainDirection() == 1) {
				if (isBackBtnPressed) {
					finish();
				} else {
					isBackBtnPressed = true;
					mToast = Toast.makeText(getBaseContext(),
							getString(R.string.back_btn_again_to_quit),
							Toast.LENGTH_SHORT);
					mToast.show();
					mainHandler.sendEmptyMessageDelayed(
							MJUConstants.MAKE_BACK_BTN_FALSE,
							MJUConstants.APP_FINISH_TIME);
				}
			} else if (firstLayoutSlideManager.getMainDirection() == 2) {

				if (firstLayoutState == MJUConstants.SLIDING
						|| secondLyaoutState == MJUConstants.SLIDING) {
					return false;
				} // 슬라이딩중인지 ㅎㅎ
				if (thirdLayout.getVisibility() == View.VISIBLE) {

					firstLayoutSlideManager
							.setDirection(MJUConstants.RIGHT_DIRECTION);
					secondLayoutSlideManager
							.setDirection(MJUConstants.RIGHT_DIRECTION);

					secondLayoutSlideManager.keepSlidingLayout();
					firstLayoutSlideManager.keepSlidingLayout();
					firstLayoutSlideManager.setMainDirection(1);
				} else {

					firstLayoutSlideManager
							.setDirection(MJUConstants.RIGHT_DIRECTION);
					firstLayoutSlideManager.keepSlidingLayout();
					firstLayoutSlideManager.setMainDirection(1);
				}
			} else {

				secondLayoutSlideManager
						.setDirection(MJUConstants.RIGHT_DIRECTION);
				secondLayoutSlideManager.keepSlidingLayout();
				firstLayoutSlideManager.setMainDirection(2);
			}
		}
		return true;
	}

	/**
	 * Desc static 헨들러 클래스
	 * 
	 * @author hs
	 * @date 2014. 1. 27. 오후 3:38:22
	 * @version
	 */
	static class MainHandler extends Handler {
		private final WeakReference<MainActivity> mainAcivity;

		public MainHandler(MainActivity activity) {
			// TODO Auto-generated constructor stub
			mainAcivity = new WeakReference<MainActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			MainActivity activity = mainAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}