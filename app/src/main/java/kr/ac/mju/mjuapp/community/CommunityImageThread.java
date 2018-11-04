package kr.ac.mju.mjuapp.community;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class CommunityImageThread extends Thread {
	private String url;
	private Bitmap bitmap;

	public CommunityImageThread(String url) {
		// TODO Auto-generated constructor stub
		this.url = url;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		try {
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setDefaultUseCaches(true);
			conn.connect();
			int size = conn.getContentLength();
			if (size > 0) {
				BufferedInputStream bis = new BufferedInputStream(
						conn.getInputStream(), size);
				bitmap = BitmapFactory.decodeStream(bis);
				bis.close();
			}
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
		} catch (IOException e) {
			// TODO Auto-generated catch block
		}
	}

	public Bitmap getImageBitmap() {
		return bitmap;
	}

}