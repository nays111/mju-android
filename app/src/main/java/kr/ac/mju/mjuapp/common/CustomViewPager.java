package kr.ac.mju.mjuapp.common;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
	
	private boolean ispagingEnbaled;

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		ispagingEnbaled = true;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (ispagingEnbaled) {
			return super.onTouchEvent(event);
		}
		return false;
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if (ispagingEnbaled) {
			return super.onInterceptTouchEvent(event);
		}
		return false;
	}
	
	public void setPagingEnbaled(boolean ispagingEnbaled) {
		this.ispagingEnbaled = ispagingEnbaled;
	}
}
