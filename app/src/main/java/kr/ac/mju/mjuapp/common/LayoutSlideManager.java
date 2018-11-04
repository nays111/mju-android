package kr.ac.mju.mjuapp.common;

import java.util.Timer;
import java.util.TimerTask;

import kr.ac.mju.mjuapp.constants.MJUConstants;

import android.os.Handler;
import android.view.View;

/**
 * <pre>
 * kr.ac.mju.mjuapp.common 
 *    |_ LayoutSlideManager.java
 * </pre>
 * 
 * Desc 레이아웃을 슬라이드 되게 해주는 클래스
 * 
 * @author hs
 * @date 2014. 1. 27. 오전 1:40:55
 * @version
 */
public class LayoutSlideManager {
	private int slidingVelocity;
	private int slidingLevel;
	private int fromXOfView;
	private int toXOfView;
	private float newTouchX;
	private float oldTouchX;
	private int direction;
	private int layoutSlidingState;
	private View view;
	private Handler handler;
	private Timer slidingTimer;
	private TimerTask slidingTimerTask;
	private boolean isActionAfterLeftSliding;

	public LayoutSlideManager(View view, Handler handler) {
		// TODO Auto-generated constructor stub
		this.view = view;
		this.handler = handler;
		this.fromXOfView = view.getLeft();
	}

	/**
	 * Desc 변수를 초기화 해주는 메소드. 어디까지 슬라이드 되게 할 것인지 전달받음.
	 * 
	 * @Method Name init
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param toXOfView
	 */
	public void init(int toXOfView) {
		oldTouchX = 0;
		slidingVelocity = 1;
		slidingLevel = 5;
		this.toXOfView = toXOfView;
		layoutSlidingState = MJUConstants.LAYOUT_CLOSED;
	}

	/**
	 * Desc 레이아웃 터치시 해당 레이아웃의 원래 위치 정보로 초기화.
	 * 
	 * @Method Name initXPostion
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param rawX
	 */
	public void initXPostion(float rawX) {
		// TODO Auto-generated method stub
		newTouchX = rawX;
		oldTouchX = newTouchX;
	}

	/**
	 * Desc 레이아웃의 슬라이드 방향을 결정하는 메소드. 주어진 파타미터를 이용해서 어느방향으로 터치했는지 판단하고 그에 따라서 방향이
	 * 결정된다.
	 * 
	 * @Method Name setDirection
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param newTouchX
	 * @param oldTouchX
	 */
	public void setDirection(float newTouchX, float oldTouchX) {
		if (oldTouchX > newTouchX) {
			direction = MJUConstants.LEFT_DIRECTION;
		} else if (oldTouchX < newTouchX) {
			direction = MJUConstants.RIGHT_DIRECTION;
		}
	}

	/**
	 * Desc 레이아웃의 슬라이드 방향을 세팅하는 메소드.
	 * 
	 * @Method Name setDirection
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param flag
	 */
	public void setDirection(int flag) {
		direction = flag;
	}

	/**
	 * Desc 터치하면서 전달되는 터치 x좌표를 받아서 터치된 x좌표에 따라서 슬라이드 하는 메소드.
	 * 
	 * @Method Name slideLayout
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param rawX
	 */
	public void slideLayout(float rawX) {
		// TODO Auto-generated method stub
		int currentViewLeftX = view.getLeft();
		final int newViewRightX;
		final int newViewLeftX;

		if (layoutSlidingState == MJUConstants.SLIDING) {
			stopSliding(MJUConstants.SLIDING); // 자동으로 슬라이드 되는 중 손으로 터치하는 경우
		}
		newTouchX = rawX;

		if (currentViewLeftX >= fromXOfView && currentViewLeftX <= toXOfView) {
			newViewLeftX = (int) (currentViewLeftX - (oldTouchX - newTouchX));
			newViewRightX = newViewLeftX + view.getWidth();

			if (newViewLeftX >= fromXOfView && newViewLeftX <= toXOfView) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						view.layout(newViewLeftX, view.getTop(), newViewRightX,
								view.getBottom());
					}
				});
				setDirection(newTouchX, oldTouchX);
				oldTouchX = newTouchX;
			}
		}
	}

	/**
	 * Desc 터치하면서 레이아웃을 슬라이딩하다가 중간에 터치 안하는 경우 진행방향으로 계속 슬라이드 되게 해주는 메소드.
	 * 
	 * @Method Name keepSlidingLayout
	 * @Date 2014. 1. 27.
	 * @author hs
	 */
	public void keepSlidingLayout() {
		// TODO Auto-generated method stub
		if (direction == MJUConstants.LEFT_DIRECTION) {
			if (isActionAfterLeftSliding) {
				slideLayoutToLeftAutomatically(true);
			} else {
				slideLayoutToLeftAutomatically(false);
			}
		} else {
			slideLayoutToRightAutomatically();
		}
	}

	/**
	 * Desc 오른쪽으로 자동 슬라이드. 핸들러를 이용해서 일정 시간주기로 레이아웃을 오른쪽으로 이동시키는 Runnable을 메시지큐에
	 * 전달.
	 * 
	 * @Method Name slideLayoutToRightAutomatically
	 * @Date 2014. 1. 27.
	 * @author hs
	 */
	public void slideLayoutToRightAutomatically() {
		// TODO Auto-generated method stub
		slidingTimer = new Timer();
		slidingTimerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(SlideRightRunnable);
			}
		};
		slidingTimer.schedule(slidingTimerTask, 0, slidingVelocity);
	}

	/**
	 * 
	 * Desc 왼쪽으로 자동 슬라이드. 핸들러를 이용해서 일정 시간주기로 레이아웃을 왼쪽으로 이동시키는 Runnable을 메시지큐에
	 * 전달. isActionAfterLeftSliding이 true일 때 슬라이딩이 끝난 후 특정 일을 할수 있도록 헨들러를 통해서
	 * 보내도록 세팅.
	 * 
	 * @Method Name slideLayoutToLeftAutomatically
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param isActionAfterLeftSliding
	 */
	public void slideLayoutToLeftAutomatically(boolean isActionAfterLeftSliding) {
		// TODO Auto-generated method stub
		this.isActionAfterLeftSliding = isActionAfterLeftSliding;

		slidingTimer = new Timer();
		slidingTimerTask = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.post(SlideLeftRunnable);
			}
		};
		slidingTimer.schedule(slidingTimerTask, 0, slidingVelocity);
	}

	/**
	 * Desc 슬라이드를 멈추는 메소드.
	 * 
	 * @Method Name stopSliding
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param flag
	 */
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
			handler.sendEmptyMessage(MJUConstants.LAYOUT_CLOSED);
			handler.removeCallbacks(SlideLeftRunnable);

			if (isActionAfterLeftSliding) {
				handler.sendEmptyMessage(MJUConstants.EXECUTE_ACTION);
			}
		} else if (flag == MJUConstants.LAYOUT_OPENED) {
			handler.sendEmptyMessage(MJUConstants.LAYOUT_OPENED);
			handler.removeCallbacks(SlideRightRunnable);
		}
	}

	/**
	 * Desc 레이아웃 상태를 가져오는 메소드.
	 * 
	 * @Method Name getLayoutState
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @return 레이아웃 상태 플래그값 (int)
	 */
	public int getLayoutState() {
		return layoutSlidingState;
	}

	/**
	 * Desc 슬라이드 정도를 세팅하는 메소드.
	 * 
	 * @Method Name setSlidingLevel
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param slidingLevel
	 */
	public void setSlidingLevel(int slidingLevel) {
		// TODO Auto-generated method stub
		this.slidingLevel = slidingLevel;
	}

	/**
	 * Desc 슬라이드 속도를 세팅하는 메소드. 1이 가장 빠름.(1= 1/1000 초 ~ 1000=1초)
	 * 
	 * @Method Name setSlidingVelocity
	 * @Date 2014. 1. 27.
	 * @author hs
	 * @param slidingVelocity
	 */
	public void setSlidingVelocity(int slidingVelocity) {
		// TODO Auto-generated method stub
		this.slidingVelocity = slidingVelocity;
	}

	/**
	 * Desc 레이아웃을 왼쪽으로 이동시키는 역할을 하는 Runnable
	 * 
	 * @Date 2014. 1. 27.
	 * @author hs
	 */
	private Runnable SlideLeftRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (view.getLeft() <= fromXOfView) {
				view.layout(0, view.getTop(), view.getWidth(), view.getBottom());
				stopSliding(MJUConstants.LAYOUT_CLOSED);
				layoutSlidingState = MJUConstants.LAYOUT_CLOSED;
			} else {
				view.layout(view.getLeft() - slidingLevel, view.getTop(),
						view.getRight() - slidingLevel, view.getBottom());
				layoutSlidingState = MJUConstants.SLIDING;
			}
		}
	};

	/**
	 * Desc 레이아웃을 오른쪽으로 이동시키는 역할을 하는 Runnable
	 * 
	 * @Date 2014. 1. 27.
	 * @author hs
	 */
	private Runnable SlideRightRunnable = new Runnable() {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (view.getLeft() >= toXOfView) {
				view.layout(toXOfView, view.getTop(), view.getWidth()
						+ toXOfView, view.getBottom());
				stopSliding(MJUConstants.LAYOUT_OPENED);
				layoutSlidingState = MJUConstants.LAYOUT_OPENED;
			} else {
				view.layout(view.getLeft() + slidingLevel, view.getTop(),
						view.getRight() + slidingLevel, view.getBottom());
				layoutSlidingState = MJUConstants.SLIDING;
			}
		}
	};
}
