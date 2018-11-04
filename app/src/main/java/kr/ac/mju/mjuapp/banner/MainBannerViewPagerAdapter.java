package kr.ac.mju.mjuapp.banner;

import java.util.ArrayList;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.community.CommunityViewActivity;
import kr.ac.mju.mjuapp.constants.MJUConstants;
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

public class MainBannerViewPagerAdapter extends PagerAdapter {

	private Context context;
	private ArrayList<Bitmap> drawableList;
	private ArrayList<Banner> bannerList;;
	
	public MainBannerViewPagerAdapter(Context context, ArrayList<Banner> bannerList) {
		this.context = context;
		drawableList = new ArrayList<Bitmap>();
		this.bannerList = bannerList;
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
				Intent intent = new Intent(context, CommunityViewActivity.class);
				intent.putExtra("url", bannerList.get(position).getUrl());
				intent.putExtra("main_title", context.getString(R.string.main_banner_in_board_title));
				intent.putExtra("type", MJUConstants.IMG_ARTICLE);
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
}
