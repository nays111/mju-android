package kr.ac.mju.mjuapp.photosns;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MainPhotoViewPagerAdapter extends PagerAdapter {

	private Context context;
	private ArrayList<Bitmap> drawableList;
	private ArrayList<String> pictureTitleList;		// 다른 액티비티로 넘기기 위해서 사진 타이틀을 가지고 있음

	public MainPhotoViewPagerAdapter(Context context, ArrayList<MJUPhoto> pictureList) {
		this.context = context;
		drawableList = new ArrayList<Bitmap>();
		pictureTitleList = new ArrayList<String>();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return drawableList.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		// TODO Auto-generated method stub
		return view == obj;
	}

	@Override
	public Object instantiateItem(View container, final int position) {
		// TODO Auto-generated method stub
		ImageView imgView = new ImageView(context);
		imgView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		imgView.setScaleType(ScaleType.FIT_XY);
		imgView.setImageBitmap(drawableList.get(position));
		
		imgView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, PhotoActivity.class);
				intent.putStringArrayListExtra("pictureTitleList", pictureTitleList);
				intent.putExtra("position", position);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}
		});

		((ViewPager) container).addView(imgView, 0);
		return imgView;
	}

	@Override
	public void destroyItem(ViewGroup pager, int position, Object view) {
		// TODO Auto-generated method stub
		((ViewPager) pager).removeView((View) view);
	}

	public void addView(Bitmap bmp) {
		drawableList.add(bmp);
		notifyDataSetChanged();
	}

	public void clear() {
		for (Bitmap b : drawableList) {
			b.recycle();
		}
		drawableList.clear();
		notifyDataSetChanged();
	}

	public void addTitle(String title) {
		pictureTitleList.add(title);
	}
}
