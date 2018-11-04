package kr.ac.mju.mjuapp.photosns;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PhotoViewPagerAdapter extends PagerAdapter {

	private Context context;
	private int orientation;
	private ArrayList<Bitmap> bitmapList;

	public PhotoViewPagerAdapter(Context context, int orientation) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.orientation = orientation;
		bitmapList = new ArrayList<Bitmap>();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return bitmapList.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		// TODO Auto-generated method stub
		return view == obj;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		ImageView imageView = new ImageView(context);
		
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			imageView.setImageBitmap(bitmapList.get(position));
			imageView.setScaleType(ScaleType.FIT_XY);
		} else {	//portrait일때 가로에 꽉차게만..
			imageView.setImageBitmap(bitmapList.get(position));
			imageView.setScaleType(ScaleType.FIT_START);
		}
		((ViewPager) container).addView(imageView, 0);
		
		return imageView;
	}

	@Override
	public void destroyItem(ViewGroup pager, int position, Object view) {
		// TODO Auto-generated method stub
		((ViewPager) pager).removeView((View) view);
	}

	public void addView(Bitmap bmp) {
		bitmapList.add(bmp);
		notifyDataSetChanged();
	}
	
	public ArrayList<Bitmap> getBitmapList() {
		return bitmapList;
	}

	public void clear() {
		for (Bitmap b : bitmapList) {
			b.recycle();
		}
		bitmapList.clear();
		notifyDataSetChanged();
	}
}