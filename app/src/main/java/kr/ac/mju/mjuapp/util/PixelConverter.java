package kr.ac.mju.mjuapp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * @author davidkim
 * 
 */
public class PixelConverter {
	private float displayWidth;
	private float displayHeight;
	private float widthRatio;
	private float heightRatio;

	public PixelConverter(Context context) {
		Display display = ((WindowManager) context
				.getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();

		// getWidth(), getHeight()媛�deprecated�섏뿀��
		/*
		 * displayWidth = display.getWidth(); displayHeight =
		 * display.getHeight();
		 */

		// 2014.1.8 �섏젙 by Hs
		Point point = new Point();
		display.getSize(point);
		displayWidth = point.x;
		displayHeight = point.y;
		// �섏젙 ��
		widthRatio = displayWidth / 480.0f;
		heightRatio = displayHeight / 800.0f;
	}

	public int getWidth(int pixel) {
		return (int) (pixel * widthRatio);
	}

	public int getHeight(int pixel) {
		return (int) (pixel * heightRatio);
	}
}
/* end of file */