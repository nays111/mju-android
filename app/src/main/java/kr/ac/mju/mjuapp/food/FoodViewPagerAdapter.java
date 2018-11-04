package kr.ac.mju.mjuapp.food;

import java.util.ArrayList;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.util.PixelConverter;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FoodViewPagerAdapter extends PagerAdapter {

	private Context context;
	private ArrayList<Food> foodList;
	private View v;
	private PixelConverter converter;
	private LinearLayout.LayoutParams linearLayoutParams;
	private ArrayList<String> foodItemList;

	public FoodViewPagerAdapter(Context context, ArrayList<Food> foodList) {
		this.context = context;
		this.foodList = foodList;

		converter = new PixelConverter(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return foodList.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object obj) {
		// TODO Auto-generated method stub
		return view == obj;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		// TODO Auto-generated method stub
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public Object instantiateItem(ViewGroup pager, int position) {
		// TODO Auto-generated method stub
		//
		View view = null;
		// layout inflater
		LayoutInflater inflater = LayoutInflater.from(context);
		// set view

		foodItemList = foodList.get(position).getMenuList();

		if (foodItemList.size() == 3) { // size 3
			view = inflater.inflate(R.layout.food_3menu_layout, null);
			init3MenuLayout(view);
		} else if (foodItemList.size() == 5) { // size 5
			view = inflater.inflate(R.layout.food_5menu_layout, null);
			init5MenuLayout(view);
		} else if (foodItemList.size() == 6) {
			view = inflater.inflate(R.layout.food_6menu_layout, null);
			init6MenuLayout(view);
		} else if (foodItemList.size() == 7) {
			view = inflater.inflate(R.layout.food_7menu_layout, null);
			init7MenuLayout(view);
		}

		pager.addView(view);
		return view;
	}

	private void init3MenuLayout(View view) {
		// TODO Auto-generated method stub

		v = view.findViewById(R.id.food_3_menu_linearlayout);
		FrameLayout.LayoutParams frameLayoutParams = (FrameLayout.LayoutParams) v
				.getLayoutParams();
		frameLayoutParams.setMargins(converter.getWidth(25), 0,
				converter.getWidth(25), 0);

		v = view.findViewById(R.id.food_breakfast_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(0).split(","),
				view.findViewById(R.id.tv_food_breakfast));

		v = view.findViewById(R.id.food_lunch_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(1).split(","),
				view.findViewById(R.id.tv_food_lunch));

		v = view.findViewById(R.id.food_dinner_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(2).split(","),
				view.findViewById(R.id.tv_food_dinner));
	}

	private void init5MenuLayout(View view) {
		// TODO Auto-generated method stub

		v = view.findViewById(R.id.food_5_menu_linearlayout);
		FrameLayout.LayoutParams frameLayoutParams = (FrameLayout.LayoutParams) v
				.getLayoutParams();
		frameLayoutParams.setMargins(converter.getWidth(25), 0,
				converter.getWidth(25), 0);

		v = view.findViewById(R.id.food_korean_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(0).split(","),
				view.findViewById(R.id.tv_food_korean));

		v = view.findViewById(R.id.food_western_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(1).split(","),
				view.findViewById(R.id.tv_food_western));

		v = view.findViewById(R.id.food_special_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(2).split(","),
				view.findViewById(R.id.tv_food_special));

		v = view.findViewById(R.id.food_chinese_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(3).split(","),
				view.findViewById(R.id.tv_food_chinese));

		v = view.findViewById(R.id.food_100ban_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(4).split(","),
				view.findViewById(R.id.tv_food_100ban));
	}

	private void init6MenuLayout(View view) {
		// TODO Auto-generated method stub

		v = view.findViewById(R.id.food_6_menu_linearlayout);
		FrameLayout.LayoutParams frameLayoutParams = (FrameLayout.LayoutParams) v
				.getLayoutParams();
		frameLayoutParams.setMargins(converter.getWidth(25), 0,
				converter.getWidth(25), 0);

		v = view.findViewById(R.id.food_korean_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(0).split(","),
				view.findViewById(R.id.tv_food_korean));

		v = view.findViewById(R.id.food_western_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(1).split(","),
				view.findViewById(R.id.tv_food_western));

		v = view.findViewById(R.id.food_special_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(2).split(","),
				view.findViewById(R.id.tv_food_special));

		v = view.findViewById(R.id.food_ramen_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(3).split(","),
				view.findViewById(R.id.tv_food_ramen));

		v = view.findViewById(R.id.food_bunsik_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(4).split(","),
				view.findViewById(R.id.tv_food_bunsik));

		v = view.findViewById(R.id.food_100ban_title);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(5).split(","),
				view.findViewById(R.id.tv_food_100ban));
	}

	private void init7MenuLayout(View view) {
		// TODO Auto-generated method stub

		v = view.findViewById(R.id.food_7_menu_linearlayout);
		FrameLayout.LayoutParams frameLayoutParams = (FrameLayout.LayoutParams) v
				.getLayoutParams();
		frameLayoutParams.setMargins(converter.getWidth(25), 0,
				converter.getWidth(25), 0);

		v = view.findViewById(R.id.food_morning);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(0).split(","),
				view.findViewById(R.id.tv_food_morning));

		v = view.findViewById(R.id.food_lunch);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(1).split(","),
				view.findViewById(R.id.tv_food_lunch));

		v = view.findViewById(R.id.food_dinner);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(2).split(","),
				view.findViewById(R.id.tv_food_dinner));

		v = view.findViewById(R.id.food_gasya);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(3).split(","),
				view.findViewById(R.id.tv_food_gasya));

		v = view.findViewById(R.id.food_sarirang);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(4).split(","),
				view.findViewById(R.id.tv_food_sarirang));

		v = view.findViewById(R.id.food_chammiso);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(5).split(","),
				view.findViewById(R.id.tv_food_chammiso));

		v = view.findViewById(R.id.food_takeOut);
		linearLayoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
		linearLayoutParams.setMargins(0, converter.getHeight(20), 0,
				converter.getHeight(5));
		((TextView) v).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));

		addMenuItems(foodItemList.get(6).split(","),
				view.findViewById(R.id.tv_food_takeOut));
	}

	private void addMenuItems(String[] menuItems, View view) {
		// TODO Auto-generated method stub
		linearLayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearLayoutParams.setMargins(converter.getWidth(20),
				converter.getHeight(20), 0, 0);

		((TextView) view).setTypeface(Typeface.createFromAsset(
				context.getAssets(), "SDGTL.TTF"));
		String str = "";
		for (int i = 0; i < menuItems.length; i++) {
			str += menuItems[i].trim() + "\n";
		}
		((TextView) view).setText(str);
	}
}
