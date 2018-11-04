package kr.ac.mju.mjuapp.main;

import java.util.Timer;
import java.util.TimerTask;

import kr.ac.mju.mjuapp.constants.MJUConstants;
import android.os.Handler;
import android.view.View;

/**
 * 
 * <pre>
 * kr.ac.mju.mjuapp.main
 *   |_ LayoutSlideManager.java
 * </pre>
 * 
 * Desc 메인 액티비티 레이아웃을 슬라이드 되게 해주는 클래스
 * 
 * @Author Hs
 * @Date 2013. 12. 13. 오후 12:06:16
 * @Version 1.2
 */
public class MainLayoutSlideManager {

	private int slidingVelocity; // 1이 제일 빠름
	private int slidingLevel;
	private int fromXOfView;
	private int toXOfView;
	private float newTouchX;
	private float oldTouchX;
	private int direction;
	private int mainDirection;
	private int layoutSlidingState;
	private View view;
	private Handler handler;
	private Timer slidingTimer;
	private TimerTask slidingTimerTask;
	private int whichLayout;

	/**
	 * Constructor
	 * 
	 * @param whichLayout
	 *            , 레이아웃 종류 (firstLayout or secondLayout)
	 * @param view
	 *            , 레이아웃
	 * @param handler
	 *            , 핸들러
	 * @param main
	 *            view, 현재 열려있있는 뷰 (1,2,3)
	 */
	public MainLayoutSlideManager(int whichLayout, View view, Handler handler) {
		this.view = view;
		this.handler = handler;
		this.fromXOfView = view.getRight();
		this.whichLayout = whichLayout;

		oldTouchX = 0;
		layoutSlidingState = MJUConstants.LAYOUT_CLOSED;

		mainDirection = 1;
		slidingVelocity = 1;
		slidingLevel = 5;
	}

	/**
	 * 
	 * @param newTouchX
	 *            ,
	 * @param oldTouchX
	 *            ,
	 * @author Hs
	 */
	private void setDirection(float newTouchX, float oldTouchX) {
		if (oldTouchX > newTouchX) {
			direction = MJUConstants.LEFT_DIRECTION;
		} else if (oldTouchX < newTouchX) {
			direction = MJUConstants.RIGHT_DIRECTION;
		}
	}

	public void setDirection(int flag) {
		direction = flag;
	}

	public void setMainDirection(int direc) {
		mainDirection = direc;
	}

	public int getMainDirection() {
		return mainDirection;
	}

	public void initXPostion(float rawX) {
		// TODO Auto-generated method stub
		newTouchX = rawX;
		oldTouchX = newTouchX;
	}

	/**
	 * 
	 * @MethodName slideLayout()
	 * @Date 2013. 12. 13.
	 * @author Administrator
	 * 
	 * @param rawX
	 */
	public void slideLayout(float rawX) {
		// TODO Auto-generated method stub

		int currentRightX = view.getRight();
		final int newLeft;
		final int newRight;

		if (layoutSlidingState == MJUConstants.SLIDING) {
			stopSliding(MJUConstants.SLIDING); // 자동으로 슬라이드 되는 중 손으로 터치하는 경우
		}
		newTouchX = rawX;

		if (currentRightX <= fromXOfView && currentRightX >= toXOfView) {
			newRight = (int) (currentRightX - (oldTouchX - newTouchX));
			newLeft = newRight - view.getWidth();

			if (newRight <= fromXOfView && newRight >= toXOfView) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						view.layout(newLeft, view.getTop(), newRight,
								view.getBottom());
					}
				});
				setDirection(newTouchX, oldTouchX);
				oldTouchX = newTouchX;
			}
		}
	}

	public void keepSlidingLayout() {
		// TODO Auto-generated method stub

		if (direction == MJUConstants.LEFT_DIRECTION) {
			slideLayoutToLeftAutomatically();
		} else {
			slideLayoutToRightAutomatically();
		}
	}

	private void slideLayoutToRightAutomatically() {
		// TODO Auto-generated method stub
		slidingTimer = new Timer();
		slidingTimerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(closeLayoutRunnable);
			}
		};
		slidingTimer.schedule(slidingTimerTask, 0, slidingVelocity);
	}

	private void slideLayoutToLeftAutomatically() {
		// TODO Auto-generated method stub

		slidingTimer = new Timer();
		slidingTimerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(OpenLayoutRunnable);
			}
		};

		slidingTimer.schedule(slidingTimerTask, 0, slidingVelocity);
	}

	private void stopSliding(int flag) {
		// TODO Auto-generated method stub

		if (slidingTimerTask != null) {
			slidingTimerTask.cancel();
			slidingTimerTask = null;
		}
		if (slidingTimer != null) {
			slidingTimer.purge();
			slidingTimer.cancel();
			slidingTimer = null;
		}

		if (flag == MJUConstants.LAYOUT_CLOSED) {
			handler.removeCallbacks(closeLayoutRunnable);

			if (whichLayout == MJUConstants.FIRST_MAIN_LAYOUT) { // 첫번째 레이아웃
																	// 닫힌경우
				handler.sendEmptyMessage(MJUConstants.FIRST_LAYOUT_CLOSED);
			} else { // 두번째 레이아웃 닫힌경우
				handler.sendEmptyMessage(MJUConstants.SECOND_LAYOUT_CLOSED);
			}
		} else if (flag == MJUConstants.LAYOUT_OPENED) {
			handler.removeCallbacks(OpenLayoutRunnable);

			if (whichLayout == MJUConstants.FIRST_MAIN_LAYOUT) { // 첫번째 레이아웃
																	// 열린경우
				handler.sendEmptyMessage(MJUConstants.FIRST_LAYOUT_OPENED);
			} else { // 두번째 레이아웃 열린경우
				handler.sendEmptyMessage(MJUConstants.SECOND_LAYOUT_OPENED);
			}
		}
	}

	public int getLayoutState() {
		return layoutSlidingState;
	}

	public void setSlidingLevel(int slidingLevel) {
		// TODO Auto-generated method stub
		this.slidingLevel = slidingLevel;
	}

	public void setSlidingVelocity(int slidingVelocity) {
		// TODO Auto-generated method stub
		this.slidingVelocity = slidingVelocity;
	}

	private Runnable OpenLayoutRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (view.getRight() <= toXOfView) {
				view.layout(toXOfView - view.getWidth(), view.getTop(),
						toXOfView, view.getBottom());
				stopSliding(MJUConstants.LAYOUT_OPENED);
				layoutSlidingState = MJUConstants.LAYOUT_OPENED;

			} else {
				view.layout(view.getLeft() - slidingLevel, view.getTop(),
						view.getRight() - slidingLevel, view.getBottom());
				layoutSlidingState = MJUConstants.SLIDING;
			}
		}
	};

	public void init() {

	}

	private Runnable closeLayoutRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (view.getRight() >= fromXOfView) {
				view.layout(0, view.getTop(), fromXOfView, view.getBottom());
				stopSliding(MJUConstants.LAYOUT_CLOSED);
				layoutSlidingState = MJUConstants.LAYOUT_CLOSED;

			} else {
				view.layout(view.getLeft() + slidingLevel, view.getTop(),
						view.getRight() + slidingLevel, view.getBottom());
				layoutSlidingState = MJUConstants.SLIDING;
			}
		}
	};

}
