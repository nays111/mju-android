package kr.ac.mju.mjuapp.photosns;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.Message;

/**
 * @author davidkim
 * 
 */
public class MainPhotoImageThread extends Thread {
	private Handler handler;
	private Context context;

	public MainPhotoImageThread(Handler handler, Context context) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.context = context;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		URL imageUrl;
		BitmapFactory.Options opt = new BitmapFactory.Options();
		Bitmap tempBitmap;
		Bitmap finalBitmap;
		Message msg;
		BufferedInputStream bis;
		Resources r = context.getResources();

		for (int index = 0; index < MJUConstants.MAIN_PICTURE_IMAGE_URLS.length; index++) {
			try {
				imageUrl = new URL(MJUConstants.MAIN_PICTURE_IMAGE_URLS[index]);
				bis = new BufferedInputStream( executeConnect(imageUrl));
				bis.mark(bis.available());
				bis.reset();
				opt.inPreferredConfig = Config.RGB_565;
				tempBitmap = BitmapFactory.decodeStream(bis, null, opt);
				finalBitmap = Bitmap.createScaledBitmap(tempBitmap,
						(int) r.getDimension(R.dimen.viewpager_width),
						(int) r.getDimension(R.dimen.viewpager_height), false);
				tempBitmap.recycle();
				msg = new Message();
				msg.what = MJUConstants.MAIN_PICTURE_IAMGE_COMPLETE;
				msg.obj = finalBitmap;
				handler.sendMessage(msg);
				bis.close();
			} catch (OutOfMemoryError e) {
				handler.sendEmptyMessage(MJUConstants.OUT_OF_MEMORY);
			} catch (MalformedURLException e) {
				handler.sendEmptyMessage(MJUConstants.MAIN_PICTURE_IMAGE_FAIL);
			} catch (IOException e) {
				handler.sendEmptyMessage(MJUConstants.MAIN_PICTURE_IMAGE_FAIL);
			}
		}
	}

	private BufferedInputStream executeConnect(URL imageUrl) {
		// TODO Auto-generated method stub
		try {
			if (imageUrl == null) {
				throw new Exception();
			}
			HttpURLConnection conn = (HttpURLConnection) imageUrl
					.openConnection();
			conn.setDefaultUseCaches(true);
			conn.connect();
			int size = conn.getContentLength();

			if (size > 0) {
				return new BufferedInputStream(conn.getInputStream(), size);
			}

		} catch (IOException e) {
			handler.sendEmptyMessage(MJUConstants.MAIN_PICTURE_IMAGE_FAIL);
		} catch (Exception e) {
			handler.sendEmptyMessage(MJUConstants.MAIN_PICTURE_IMAGE_FAIL);
		}
		return null;
	}
}
/* end of file */
