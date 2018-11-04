package kr.ac.mju.mjuapp.photosns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.common.DepthPageTransformer;
import kr.ac.mju.mjuapp.community.CommunityListActivity;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.login.LoginActivity;
import kr.ac.mju.mjuapp.login.LoginManager;
import kr.ac.mju.mjuapp.network.NetworkManager;
import kr.ac.mju.mjuapp.util.PixelConverter;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoActivity extends FragmentActivity implements OnClickListener {

	private PixelConverter converter;
	private View view;
	private LinearLayout.LayoutParams linearLayoutParams;
	private RelativeLayout.LayoutParams relativeLayoutParams;
	private int position;
	private ArrayList<String> titleList;
	private PhotoHandler photoHandler;

	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	// private MJUAlertDialog alertDialog;
	private int LAYOUT_ORIENTATION_STATE;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		init();

		Configuration config = getResources().getConfiguration();
		LAYOUT_ORIENTATION_STATE = config.orientation;
		if (LAYOUT_ORIENTATION_STATE == Configuration.ORIENTATION_PORTRAIT) { // fortrait
			setContentView(R.layout.picture_layout);
			initLayout();
		} else { // landscape
			setContentView(R.layout.picture_layout_landscape);
			initLayoutForLandScape();
		}
		initViewPager(LAYOUT_ORIENTATION_STATE);
		getPhotoImg();

		// if (LAYOUT_ORIENTATION_STATE == Configuration.ORIENTATION_PORTRAIT) {
		// SharedPreferences pref =
		// getSharedPreferences(getString(R.string.pref_name),
		// Activity.MODE_PRIVATE);
		// boolean ischecked =
		// pref.getBoolean(getString(R.string.pref_photo_notice_show_again),
		// false);
		// if (!ischecked) {
		// alertDialog =
		// MJUAlertDialog.newInstance(MJUConstants.PHOTO_NOTICE_DIALOG,
		// R.string.app_name, R.string.photo_notice_dialog_str, 0);
		// alertDialog.show(fragmentManager, "");
		// }
		// }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		position = intent.getIntExtra("position", -2);

		if (position == -2) {
			Toast.makeText(getBaseContext(), R.string.getting_page_info_fail,
					Toast.LENGTH_SHORT).show();
			return;
		}

		initViewPager(getResources().getConfiguration().orientation);
		getPhotoImg();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		ViewPager viewPager = (ViewPager) findViewById(R.id.picture_image_viewpager);
		viewPager.removeAllViews();
		if (((PhotoViewPagerAdapter) viewPager.getAdapter()) != null) {
			((PhotoViewPagerAdapter) viewPager.getAdapter()).clear();
		}
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MJUConstants.PICTURE_IAMGE_COMPLETE:
			setBitmapImage((Bitmap) msg.obj);
			break;
		case MJUConstants.PICTURE_IMAGE_FAIL:
			Toast.makeText(getBaseContext(),
					getString(R.string.main_picure_get_info_fail),
					Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
			unsetOnClickListener();
			break;
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		titleList = intent.getStringArrayListExtra("pictureTitleList");
		position = intent.getIntExtra("position", -1);

		photoHandler = new PhotoHandler(PhotoActivity.this);
		converter = new PixelConverter(getApplicationContext());

		progressDialog = new MJUProgressDialog();
		fragmentManager = getSupportFragmentManager();
	}

	private void getPhotoImg() {
		// TODO Auto-generated method stub
		PhotoImageThread phtoThread = new PhotoImageThread(photoHandler, this);
		phtoThread.start();
		progressDialog.show(fragmentManager, "");
	}

	private void initLayout() {
		// TODO Auto-generated method stub
		view = findViewById(R.id.tv_picture_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(200);
		relativeLayoutParams.setMargins(converter.getWidth(30),
				converter.getHeight(80), 0, 0);
		((TextView) view).setText(titleList.get(position));
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.btn_picture_upload);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(65);
		relativeLayoutParams.height = converter.getHeight(65);
		relativeLayoutParams.setMargins(0, converter.getHeight(45),
				converter.getWidth(30), 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.btn_previous_picture);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(40);
		relativeLayoutParams.height = converter.getHeight(60);
		relativeLayoutParams.setMargins(converter.getWidth(15),
				converter.getHeight(220), 0, 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.btn_next_picture);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(40);
		relativeLayoutParams.height = converter.getHeight(60);
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(15), 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.tv_picture_to_main);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(315),
				converter.getWidth(20), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.tv_picture_to_board);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.tv_picture_to_all_picture);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.picture_indicator_layout);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(30), 0,
				converter.getWidth(30), converter.getHeight(40));
	}

	private void initLayoutForLandScape() {
		// TODO Auto-generated method stub
		view = findViewById(R.id.tv_picture_title);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(200);
		relativeLayoutParams.setMargins(converter.getWidth(20),
				converter.getHeight(80), 0, 0);
		((TextView) view).setText(titleList.get(position));
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));

		view = findViewById(R.id.btn_picture_upload);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(40);
		relativeLayoutParams.height = converter.getHeight(100);
		relativeLayoutParams.setMargins(0, converter.getHeight(50),
				converter.getWidth(20), 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.btn_previous_picture);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(25);
		relativeLayoutParams.height = converter.getHeight(80);
		relativeLayoutParams.setMargins(converter.getWidth(15),
				converter.getHeight(200), 0, 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.btn_next_picture);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.width = converter.getWidth(25);
		relativeLayoutParams.height = converter.getHeight(80);
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(15), 0);
		view.setOnClickListener(this);

		view = findViewById(R.id.tv_picture_to_main);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(15),
				converter.getHeight(160), 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.tv_picture_to_board);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, 0, 0, 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.tv_picture_to_all_picture);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(0, 0, converter.getWidth(15), 0);
		((TextView) view).setTypeface(Typeface.createFromAsset(getAssets(),
				getString(R.string.sdgtl_ttf)));
		view.setOnClickListener(this);

		view = findViewById(R.id.picture_indicator_layout);
		relativeLayoutParams = (RelativeLayout.LayoutParams) view
				.getLayoutParams();
		relativeLayoutParams.setMargins(converter.getWidth(20), 0,
				converter.getWidth(20), converter.getHeight(40));
	}

	private void initViewPager(int orientation) {
		// TODO Auto-generated method stub
		ViewPager viewPager = (ViewPager) findViewById(R.id.picture_image_viewpager);
		viewPager.removeAllViews();
		if (((PhotoViewPagerAdapter) viewPager.getAdapter()) != null) {
			((PhotoViewPagerAdapter) viewPager.getAdapter()).clear();
		}

		viewPager.setAdapter(new PhotoViewPagerAdapter(getApplicationContext(),
				orientation));
		viewPager.setPageTransformer(true, new DepthPageTransformer());
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				setPageIndicator(position);
				((TextView) findViewById(R.id.tv_picture_title))
						.setText(titleList.get(position));
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
		viewPager.setOffscreenPageLimit(1);
		viewPager.setVisibility(View.INVISIBLE);
	}

	private void setPageIndicator(int position) {
		// TODO Auto-generated method stub
		LinearLayout containerLayout = (LinearLayout) findViewById(R.id.picture_indicator_layout);
		containerLayout.removeAllViews();
		View view;
		for (int i = 0; i < 7; i++) {
			view = new View(getApplicationContext());
			containerLayout.addView(view);
			linearLayoutParams = (LinearLayout.LayoutParams) view
					.getLayoutParams();
			linearLayoutParams.weight = 1;
			linearLayoutParams.height = LayoutParams.MATCH_PARENT;
			linearLayoutParams.width = 0;
			view.setLayoutParams(linearLayoutParams);
			if (i == position) {
				view.setBackgroundResource(R.drawable.picture_bottom_bar);
			}
		}
	}

	private void setBitmapImage(Bitmap photoBitmap) {
		// TODO Auto-generated method stub
		ViewPager viewPager = (ViewPager) findViewById(R.id.picture_image_viewpager);
		PhotoViewPagerAdapter pAdapter = (PhotoViewPagerAdapter) viewPager
				.getAdapter();
		pAdapter.addView(photoBitmap);

		if (pAdapter.getCount() == 7) {
			viewPager.setCurrentItem(position);
			setPageIndicator(position);
			viewPager.setVisibility(View.VISIBLE);
			progressDialog.dismiss();
		}
	}

	private void unsetOnClickListener() {
		// TODO Auto-generated method stub
		findViewById(R.id.btn_previous_picture).setOnClickListener(null);
		findViewById(R.id.btn_next_picture).setOnClickListener(null);
		findViewById(R.id.tv_picture_to_all_picture).setOnClickListener(null);
		findViewById(R.id.tv_picture_to_board).setOnClickListener(null);
		findViewById(R.id.btn_picture_upload).setOnClickListener(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == RESULT_OK) {
			if (requestCode == MJUConstants.RQ_WRITE_IMG_BRD_AFTER_LOGIN) {
				findViewById(R.id.btn_picture_upload).performClick();
			} else if (requestCode == MJUConstants.RQ_COMMUNITY_AFTER_LOGIN) {
				findViewById(R.id.tv_picture_to_board).performClick();
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int currentPosition;
		ViewPager viewPager = (ViewPager) findViewById(R.id.picture_image_viewpager);
		Intent intent = null;
		switch (v.getId()) {
		case R.id.btn_picture_upload:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				if (LoginManager.checkLogin(getApplicationContext())) {
					intent = new Intent(PhotoActivity.this,
							PhotoSNSWriteActivity.class);
				} else {
					intent = new Intent(PhotoActivity.this, LoginActivity.class);
					startActivityForResult(intent,
							MJUConstants.RQ_WRITE_IMG_BRD_AFTER_LOGIN);
					return;
				}
			}
			break;
		case R.id.btn_previous_picture:
			currentPosition = viewPager.getCurrentItem();

			if (currentPosition == 0) {
				Toast.makeText(getBaseContext(),
						getString(R.string.is_first_page), Toast.LENGTH_SHORT)
						.show();
			} else {
				viewPager.setCurrentItem(currentPosition - 1, true);
			}
			break;
		case R.id.btn_next_picture:
			currentPosition = viewPager.getCurrentItem();

			if (currentPosition == 6) {
				Toast.makeText(getBaseContext(),
						getString(R.string.is_last_page), Toast.LENGTH_SHORT)
						.show();
			} else {
				viewPager.setCurrentItem(currentPosition + 1, true);

			}
			break;
		case R.id.tv_picture_to_main:
			if (PhotoAllViewActivity.photoAllViewActivity != null) {
				PhotoAllViewActivity.photoAllViewActivity.finish();
			}
			finish();
			break;
		case R.id.tv_picture_to_board:
			if (NetworkManager.checkNetwork(getApplicationContext())) {
				if (LoginManager.checkLogin(getApplicationContext())) {
					intent = new Intent(PhotoActivity.this,
							CommunityListActivity.class);
					intent.putExtra(
							"directUrl",
							"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?boardType=02&boardId=2012&listType=02&mcategoryId=&row=4&id=mjukr_060202000000");
				} else {
					intent = new Intent(PhotoActivity.this, LoginActivity.class);
					startActivityForResult(intent,
							MJUConstants.RQ_COMMUNITY_AFTER_LOGIN);
					return;
				}
			}
			break;
		case R.id.tv_picture_to_all_picture:
			if (saveBitmap()) {
				intent = new Intent(PhotoActivity.this,
						PhotoAllViewActivity.class);
				intent.putStringArrayListExtra("titleList", titleList);
			}
			break;
		}

		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
		}
	}

	private boolean saveBitmap() {
		// TODO Auto-generated method stub
		ArrayList<Bitmap> bitmapList = ((PhotoViewPagerAdapter) ((ViewPager) findViewById(R.id.picture_image_viewpager))
				.getAdapter()).getBitmapList();

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			int index = 0;
			// /mnt/sdcard/mjudownload 앱 폴더 기본 경로설정
			String basePath = Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ getResources()
							.getString(R.string.uri_file_save_directory);
			// 기본 폴더 생성
			File baseDir = new File(basePath);
			if (!baseDir.exists()) {
				baseDir.mkdir();
			}

			String baseFileName = getResources().getString(
					R.string.picture_file_name);
			String extName = getResources().getString(
					R.string.picture_file_extension);
			String folderName = getResources().getString(
					R.string.picture_folder_name);

			// 사진 폴더 생성
			File saveDir = new File(basePath + folderName);
			if (!saveDir.exists()) {
				saveDir.mkdir();
			}

			String saveFileName;
			OutputStream out = null;
			for (Bitmap bitmap : bitmapList) {
				saveFileName = basePath + folderName + baseFileName + (index++)
						+ extName;

				// 기존 파일이 있으면 지움
				File file = new File(saveFileName);
				if (file.exists()) {
					file.delete();
				}
				try {
					file.createNewFile();
					out = new FileOutputStream(file);
					bitmap.compress(CompressFormat.JPEG, 70, out);
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(getBaseContext(),
							getString(R.string.save_temp_file_fail),
							Toast.LENGTH_SHORT).show();
					return false;
				}
			}

			try {
				out.close();
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Toast.makeText(getBaseContext(),
						getString(R.string.save_temp_file_fail),
						Toast.LENGTH_SHORT).show();
				return false;
			}
		} else {
			Toast.makeText(getBaseContext(),
					getString(R.string.not_available_storage),
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	// public void saveNoticeCheckBoxState(boolean checked) {
	// // TODO Auto-generated method stub
	// SharedPreferences pref =
	// getSharedPreferences(getString(R.string.pref_name),
	// Activity.MODE_PRIVATE);
	// SharedPreferences.Editor editor = pref.edit();
	// editor.putBoolean(getString(R.string.pref_photo_notice_show_again),
	// checked);
	// editor.commit();
	// }

	static class PhotoHandler extends Handler {
		private final WeakReference<PhotoActivity> photoAcivity;

		public PhotoHandler(PhotoActivity activity) {
			// TODO Auto-generated constructor stub
			photoAcivity = new WeakReference<PhotoActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			PhotoActivity activity = photoAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}