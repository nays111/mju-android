package kr.ac.mju.mjuapp.photosns;

import java.io.File;
import java.util.ArrayList;

import kr.ac.mju.mjuapp.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoAllViewActivity extends Activity implements OnClickListener,
		OnTouchListener {

	private String baseFilePath;
	private String baseFileName;
	private String extName;
	private ArrayList<String> titleList;
	private int[] imgViewIds = { R.id.picture_gird_img1,
			R.id.picture_gird_img2, R.id.picture_gird_img3,
			R.id.picture_gird_img4, R.id.picture_gird_img5,
			R.id.picture_gird_img6, R.id.picture_gird_img7, };
	public static Activity photoAllViewActivity;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			setContentView(R.layout.picture_all_view_layout);

			init();
			setBitmapImages();

			setClickListeners();
		} else {
			Toast.makeText(getBaseContext(),
					getString(R.string.not_available_storage),
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		Bitmap bitmap;
		Drawable d;

		for (int i = 0; i < imgViewIds.length; i++) {
			d = ((ImageView) findViewById(imgViewIds[i])).getDrawable();
			if (d instanceof BitmapDrawable) {
				bitmap = ((BitmapDrawable) d).getBitmap();
				bitmap.recycle();
			}
			d.setCallback(null);
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		baseFilePath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ getResources().getString(R.string.uri_file_save_directory)
				+ getResources().getString(R.string.picture_folder_name);

		baseFileName = getResources().getString(R.string.picture_file_name);
		extName = getResources().getString(R.string.picture_file_extension);

		titleList = getIntent().getStringArrayListExtra("titleList");

		photoAllViewActivity = PhotoAllViewActivity.this;
	}

	private void setBitmapImages() {
		// TODO Auto-generated method stub

		if (isFilesExists()) {
			try {
				Bitmap bitmapImage;
				ImageView imgView;
				for (int i = 0; i < imgViewIds.length; i++) {
					bitmapImage = BitmapFactory.decodeFile(baseFilePath
							+ baseFileName + i + extName);
					imgView = (ImageView) findViewById(imgViewIds[i]);
					imgView.setImageBitmap(bitmapImage);
					imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				}
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
				Toast.makeText(getBaseContext(),
						getString(R.string.main_picure_get_info_fail),
						Toast.LENGTH_SHORT).show();
				finish();
			}
		} else {
			Toast.makeText(getBaseContext(),
					getString(R.string.main_picure_get_info_fail),
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	private boolean isFilesExists() {
		// TODO Auto-generated method stub
		String fileName;

		for (int i = 0; i < 7; i++) {
			fileName = baseFilePath + baseFileName + i + extName;
			File imgFile = new File(fileName);
			if (!imgFile.exists()) {
				return false;
			}
		}
		return true;
	}

	private void setClickListeners() {
		// TODO Auto-generated method stub
		((ImageView) findViewById(R.id.picture_gird_img1))
				.setOnClickListener(this);
		((ImageView) findViewById(R.id.picture_gird_img2))
				.setOnClickListener(this);
		((ImageView) findViewById(R.id.picture_gird_img3))
				.setOnClickListener(this);
		((ImageView) findViewById(R.id.picture_gird_img4))
				.setOnClickListener(this);
		((ImageView) findViewById(R.id.picture_gird_img5))
				.setOnClickListener(this);
		((ImageView) findViewById(R.id.picture_gird_img6))
				.setOnClickListener(this);
		((ImageView) findViewById(R.id.picture_gird_img7))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.tv_picture_gird_goback))
				.setOnClickListener(this);

		((ImageView) findViewById(R.id.picture_gird_img1))
				.setOnTouchListener(this);
		((ImageView) findViewById(R.id.picture_gird_img2))
				.setOnTouchListener(this);
		((ImageView) findViewById(R.id.picture_gird_img3))
				.setOnTouchListener(this);
		((ImageView) findViewById(R.id.picture_gird_img4))
				.setOnTouchListener(this);
		((ImageView) findViewById(R.id.picture_gird_img5))
				.setOnTouchListener(this);
		((ImageView) findViewById(R.id.picture_gird_img6))
				.setOnTouchListener(this);
		((ImageView) findViewById(R.id.picture_gird_img7))
				.setOnTouchListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = null;
		switch (v.getId()) {
		case R.id.tv_picture_gird_goback:
			finish();
			return;
		case R.id.picture_gird_img1:
			intent = new Intent(PhotoAllViewActivity.this, PhotoActivity.class);
			intent.putExtra("position", 0);
			break;
		case R.id.picture_gird_img2:
			intent = new Intent(PhotoAllViewActivity.this, PhotoActivity.class);
			intent.putExtra("position", 1);
			break;
		case R.id.picture_gird_img3:
			intent = new Intent(PhotoAllViewActivity.this, PhotoActivity.class);
			intent.putExtra("position", 2);
			break;
		case R.id.picture_gird_img4:
			intent = new Intent(PhotoAllViewActivity.this, PhotoActivity.class);
			intent.putExtra("position", 3);
			break;
		case R.id.picture_gird_img5:
			intent = new Intent(PhotoAllViewActivity.this, PhotoActivity.class);
			intent.putExtra("position", 4);
			break;
		case R.id.picture_gird_img6:
			intent = new Intent(PhotoAllViewActivity.this, PhotoActivity.class);
			intent.putExtra("position", 5);
			break;
		case R.id.picture_gird_img7:
			intent = new Intent(PhotoAllViewActivity.this, PhotoActivity.class);
			intent.putExtra("position", 6);
			break;
		}

		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.putStringArrayListExtra("pictureTitleList", titleList);
			startActivity(intent);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		ImageView iv = (ImageView) v;

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			iv.setColorFilter(
					getResources().getColor(R.color.imageview_selected_color),
					Mode.SRC_OVER);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			iv.setColorFilter(0x00000000, Mode.SRC_OVER);
		}
		return false;
	}
}