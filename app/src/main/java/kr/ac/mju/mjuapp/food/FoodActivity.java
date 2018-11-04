package kr.ac.mju.mjuapp.food;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.common.DateManager;
import kr.ac.mju.mjuapp.common.LayoutSlideManager;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.network.NetworkManager;
import kr.ac.mju.mjuapp.util.PixelConverter;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FoodActivity extends FragmentActivity implements OnClickListener,
		OnTouchListener {

	public static final int SCIENCE = 5;
	public static final int HUMANITY_STUDENT = 6;
	public static final int HUMANITY_STAFF = 7;
	private static final int HUMANITY_NAVI_LAYOUT = 8;
	private static final int SCIENCE_NAVI_LAYOUT = 9;

	private View view;
	private PixelConverter converter;
	private LinearLayout.LayoutParams linearLayoutParams;
	private RelativeLayout.LayoutParams relativeLayoutParams;
	private LayoutInflater inflator;

	private int currentPage;

	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private FoodHandler foodHandler;
	private LayoutSlideManager layoutSlideManager;

	private FoodThread thread;
	private String campusTitle;
	private String cafeteriaTitle;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		setContentView(R.layout.food_main_layout);

		init();
		initLayout();

		if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
			openLayout();
		}
	}

	@SuppressWarnings("unchecked")
	public void handleMessage(Message msg) {
		int what = msg.what;
		switch (what) {
		case MJUConstants.FOOD_SUCCESS:
			initViewPager((ArrayList<Food>) msg.obj);
			progressDialog.dismiss();
			break;
		case MJUConstants.FOOD_FAIL:
			Toast.makeText(getBaseContext(),
					getString(R.string.getting_info_fail), Toast.LENGTH_SHORT)
					.show();
			progressDialog.dismiss();
			break;
		case MJUConstants.FOOD_EMPTY:
			Toast.makeText(getBaseContext(), getString(R.string.no_food_info),
					Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
			break;
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.food_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.food_left_slidingbar).setClickable(true);
			break;
		case MJUConstants.EXECUTE_ACTION:
			if (thread != null) {
				thread.start();
				((ViewPager) findViewById(R.id.food_menu_viewpager))
						.removeAllViews();
				((TextView) findViewById(R.id.food_date)).setText("");
				progressDialog.show(fragmentManager, "");

				((TextView) findViewById(R.id.food_campus_title))
						.setText(campusTitle);
				((TextView) findViewById(R.id.food_cafeteria_title))
						.setText(cafeteriaTitle);
			}
			break;
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		DisplayMetrics displaymetrics = new DisplayMetrics();
		converter = new PixelConverter(getApplicationContext());
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		inflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		foodHandler = new FoodHandler(FoodActivity.this);

		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(
					findViewById(R.id.food_content), foodHandler);
			layoutSlideManager
					.init((int) ((float) displaymetrics.widthPixels - converter
							.getWidth(135)));
		}
	}

	private void initLayout() {
		// TODO Auto-generated method stub
		view = findViewById(R.id.food_sub_layout);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.rightMargin = converter.getWidth(135);

		// food title part
		view = findViewById(R.id.food_sliding_btn);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(50);
		relativeLayoutParams.height = converter.getHeight(50);
		view.setOnTouchListener(this);

		view = findViewById(R.id.food_header_icon);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(30);
		relativeLayoutParams.height = converter.getHeight(30);
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(15), 0);

		// slide menu
		view = findViewById(R.id.menu_food_title);
		LinearLayout.LayoutParams linearlayoutParams = (LinearLayout.LayoutParams) view
				.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));

		view = findViewById(R.id.food_seoul_campus_title);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0,
				converter.getHeight(5));

		view = findViewById(R.id.food_humanity_student_cafeteria);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		view.setOnClickListener(this);
		view = findViewById(R.id.food_humanity_staff_cafeteria);
		view.setOnClickListener(this);

		view = findViewById(R.id.food_yongin_campus_title);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0,
				converter.getHeight(5));

		view = findViewById(R.id.food_science_student_cefeteria);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.food_science_library_cefeteria);
		view.setOnClickListener(this);
		view = findViewById(R.id.food_science_staff_cefeteria);
		view.setOnClickListener(this);

		// food contents part
		view = findViewById(R.id.food_campus_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(25),
				converter.getHeight(20), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.food_cafeteria_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(10), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.food_date);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(140), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.food_navigator_layout);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.height = converter.getHeight(40);
		relativeLayoutParams.setMargins(converter.getWidth(25),
				converter.getHeight(30), converter.getWidth(25),
				converter.getHeight(20));

		// add food navi layout
		changeNaviLayout(HUMANITY_NAVI_LAYOUT);

		findViewById(R.id.food_left_slidingbar).setOnTouchListener(this);
	}

	private void changeNaviLayout(int flag) {
		// TODO Auto-generated method stub
		View view = findViewById(R.id.food_navigator_layout);
		((LinearLayout) view).removeAllViews();

		if (flag == HUMANITY_NAVI_LAYOUT) {
			((LinearLayout) view).addView((LinearLayout) inflator.inflate(
					R.layout.food_humanity_navi_layout, null));
			initHumanityNaviLayout();
		} else {
			((LinearLayout) view).addView((LinearLayout) inflator.inflate(
					R.layout.food_science_navi_layout, null));
			initScienceNaviLayout();
		}
	}

	private void initScienceNaviLayout() {
		// TODO Auto-generated method stub
		view = findViewById(R.id.btn_food_science_student_cafeteria);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(120);
		linearLayoutParams.height = converter.getHeight(40);
		linearLayoutParams.setMargins(0, 0, 0, 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.btn_food_science_library_cafeteria);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(120);
		linearLayoutParams.height = converter.getHeight(40);
		linearLayoutParams.setMargins(converter.getWidth(30), 0, 0, 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.btn_food_science_staff_cafeteria);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(120);
		linearLayoutParams.height = converter.getHeight(40);
		linearLayoutParams.setMargins(converter.getWidth(30), 0, 0, 0);
		view.setOnClickListener(this);

	}

	private void initHumanityNaviLayout() {
		// TODO Auto-generated method stub
		view = findViewById(R.id.btn_food_humanity_student_cafeteria);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(120);
		linearLayoutParams.height = converter.getHeight(40);
		linearLayoutParams.setMargins(0, 0, 0, 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.btn_food_humanity_staff_cafeteria);
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.width = converter.getWidth(120);
		linearLayoutParams.height = converter.getHeight(40);
		linearLayoutParams.setMargins(converter.getWidth(30), 0, 0, 0);
		view.setOnClickListener(this);
	}

	protected void initViewPager(final ArrayList<Food> foodList) {
		// TODO Auto-generated method stub
		ViewPager foodViewPager = (ViewPager) findViewById(R.id.food_menu_viewpager);
		FoodViewPagerAdapter fAdapter = new FoodViewPagerAdapter(
				getApplicationContext(), foodList);
		foodViewPager.setOffscreenPageLimit(foodList.size());
		foodViewPager.setAdapter(fAdapter);
		foodViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				setPagerMarker(position, foodList.size());
				setDate(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});

		int todayIndex = DateManager.getIndexForToday();
		if (todayIndex == 1) {
			todayIndex++; // 일요일이면 월요일로 보여주기 위해서 +1
		} else if (todayIndex == 7) {
			if (currentPage == HUMANITY_STUDENT) { // 토요일인 경우 인문 학생식당
				// 그냥 내비둬
			} else { // 토요일인 경우 인문 학생식당이 아니면 메뉴 없으니깐.. 월요일로..
				todayIndex = 2;
			}
		}
		foodViewPager.setCurrentItem(todayIndex - 2); // 일요일 1, 월 2, 화 3, 수 4, 목
														// 5, 금 6, 토 7 이므로
		setPagerMarker(todayIndex - 2, foodList.size());
		setDate(todayIndex - 2);
	}

	private void setDate(int position) {
		// TODO Auto-generated method stub
		((TextView) findViewById(R.id.food_date)).setText(DateManager
				.getDate(position));
	}

	private void setPagerMarker(int position, int length) {
		// TODO Auto-generated method stub
		LinearLayout markerlayout;
		markerlayout = (LinearLayout) findViewById(R.id.food_viewpager_marker_layout);
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
				mark.setBackgroundResource(R.drawable.food_viewpager_makrer_selected);
			} else {
				mark.setBackgroundResource(R.drawable.food_viewpager_makrer);
			}
			markerlayout.addView(mark);
		}
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
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch (id) {
		case MJUConstants.PROGRESS_DIALOG:
			Dialog progressDialog = new Dialog(this,
					android.R.style.Theme_Translucent_NoTitleBar);
			progressDialog.setContentView(R.layout.loading_progress);
			progressDialog.setCancelable(false);
			return progressDialog;
		default:
			return null;
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (NetworkManager.checkNetwork(getApplicationContext())) {
			switch (v.getId()) {
			case R.id.food_humanity_student_cafeteria:
			case R.id.btn_food_humanity_student_cafeteria:
				thread = new FoodThread(foodHandler,
						MJUConstants.HUMANITYCAMPUS_STUDENT_CAFETERIA_URL,
						FoodThread.HUMANITY_STUDENT);
				campusTitle = getResources().getString(
						R.string.main_weather_seoul_campus);
				cafeteriaTitle = "# "
						+ getResources().getString(
								R.string.food_student_cefeteria);
				if (currentPage == SCIENCE) {
					changeNaviLayout(HUMANITY_NAVI_LAYOUT);
				}
				currentPage = HUMANITY_STUDENT;
				break;
			case R.id.food_humanity_staff_cafeteria:
			case R.id.btn_food_humanity_staff_cafeteria:
				thread = new FoodThread(foodHandler,
						MJUConstants.HUMANITYCAMPUS_STAFF_CAFETEREIA_URL,
						FoodThread.HUMANITY_STAFF);
				campusTitle = getResources().getString(
						R.string.main_weather_seoul_campus);
				cafeteriaTitle = "# "
						+ getResources().getString(
								R.string.food_staff_cefeteria);
				if (currentPage == SCIENCE) {
					changeNaviLayout(HUMANITY_NAVI_LAYOUT);
				}
				currentPage = HUMANITY_STAFF;
				break;
			case R.id.food_science_student_cefeteria:
			case R.id.btn_food_science_student_cafeteria:
				thread = new FoodThread(foodHandler,
						MJUConstants.SCIENCECAMPUS_STUDENT_CAFETERIA_URL,
						FoodThread.SCIENCE_STUDENT);
				campusTitle = getResources().getString(
						R.string.main_weather_yongin_campus);
				cafeteriaTitle = "# "
						+ getResources().getString(
								R.string.food_student_cefeteria);
				if (currentPage != SCIENCE) {
					changeNaviLayout(SCIENCE_NAVI_LAYOUT);
				}
				currentPage = SCIENCE;
				break;
			case R.id.food_science_library_cefeteria:
			case R.id.btn_food_science_library_cafeteria:
				thread = new FoodThread(foodHandler,
						MJUConstants.SCIENCECAMPUS_LIBRARY_CAFETEREIA_URL,
						FoodThread.SCIENCE_LIBRARY);
				campusTitle = getResources().getString(
						R.string.main_weather_yongin_campus);
				cafeteriaTitle = "# "
						+ getResources().getString(
								R.string.food_library_cefeteria);
				if (currentPage != SCIENCE) {
					changeNaviLayout(SCIENCE_NAVI_LAYOUT);
				}
				currentPage = SCIENCE;
				break;
			case R.id.food_science_staff_cefeteria:
			case R.id.btn_food_science_staff_cafeteria:
				thread = new FoodThread(foodHandler,
						MJUConstants.SCIENCECAMPUS_STAFF_CAFETERIA_URL,
						FoodThread.SCIENCE_STAFF);
				campusTitle = getResources().getString(
						R.string.main_weather_yongin_campus);
				cafeteriaTitle = "# "
						+ getResources().getString(
								R.string.food_staff_cefeteria);
				if (currentPage != SCIENCE) {
					changeNaviLayout(SCIENCE_NAVI_LAYOUT);
				}
				currentPage = SCIENCE;
				break;
			}
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(true);
			} else {
				thread.start();
				progressDialog.show(fragmentManager, "");
				((ViewPager) findViewById(R.id.food_menu_viewpager))
						.removeAllViews();
				((TextView) findViewById(R.id.food_date)).setText("");
				((TextView) findViewById(R.id.food_campus_title))
						.setText(campusTitle);
				((TextView) findViewById(R.id.food_cafeteria_title))
						.setText(cafeteriaTitle);
			}
		}
	}

	static class FoodHandler extends Handler {
		private final WeakReference<FoodActivity> foodAcivity;

		public FoodHandler(FoodActivity activity) {
			// TODO Auto-generated constructor stub
			foodAcivity = new WeakReference<FoodActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			FoodActivity activity = foodAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
