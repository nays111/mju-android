package kr.ac.mju.mjuapp.banner;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.http.HttpManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.HttpStatus;

public class BannerImageThread extends Thread {
	
	private Handler handler;
	private Context context;
	private HttpManager httpManager;
	private HttpResponse response = null;
	private HttpEntity entity = null;
	private Vector<String> tagNames;
	private StatusLine status;
	HashMap<String, List<Element>> elementMap;

	public BannerImageThread(Handler handler, Context context) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.context = context;

		httpManager = new HttpManager();
		httpManager.init();
		tagNames = new Vector<String>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		URL imageUrl = null;
		BitmapFactory.Options opt = new BitmapFactory.Options();
		Bitmap tempBitmap;
		Bitmap finalBitmap;
		Message msg; 
		BufferedInputStream bis;
		Resources r = context.getResources();

		//kihin add 2016.01.12================================================
		tagNames.add(HTMLElementName.DIV);
		try {
			httpManager.setHttpPost(MJUConstants.MJU_IMG_URL); //.MJU_INDEX_URL);
			response = httpManager.executeHttpPost();
			status = response.getStatusLine();

			if (status.getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				elementMap = httpManager.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
				List<Element> liList = elementMap.get(HTMLElementName.DIV);

				Banner banner = null;

				for (int i = 0; i < 5; i++) {
					Log.d("[banner]", "start");

					Log.d("[liList]", liList.get(i).toString());

					banner = new Banner();
					String imgpathUrl=liList.get(i).getAllElements(HTMLElementName.A).get(2).getAttributeValue("href").trim();

					Log.d("[imgpathUrl] url = ", imgpathUrl);

					try {
						imageUrl = new URL("http://www.mju.ac.kr/upload/banner/3562/" + imgpathUrl);
						//imageUrl = new URL(MJUConstants.MAIN_BANNER_IMAGE_URLS[i]);
						bis = executeConnect(imageUrl);
						opt.inPreferredConfig = Config.RGB_565;
						tempBitmap = BitmapFactory.decodeStream(bis, null, opt);
						finalBitmap = Bitmap.createScaledBitmap(tempBitmap,
								(int) r.getDimension(R.dimen.viewpager_width),
								(int) r.getDimension(R.dimen.viewpager_height), false);
						tempBitmap.recycle();
						msg = new Message();
						msg.what = MJUConstants.MAIN_BANNER_IMAGE_COMPLETE;
						msg.obj = finalBitmap;
						handler.sendMessage(msg);
						bis.close();
					} catch (OutOfMemoryError e) {
						handler.sendEmptyMessage(MJUConstants.OUT_OF_MEMORY);
					} catch (MalformedURLException e) {
						handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_IMAGE_FAIL);
					} catch (IOException e) {
						handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_IMAGE_FAIL);
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("bannaer error : "+e);
			handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_TEXTS_FAIL);
		}

	/*	for (int i = 0; i < MJUConstants.MAIN_BANNER_IMAGE_URLS.length; i++) {
			try {
				Banner l_ban=l_bannerList.get(i);
				String imgpathUrl=l_ban.getWirter();

				imageUrl = new URL("http://www.mju.ac.kr/upload/banner/3562/"+imgpathUrl);
				//imageUrl = new URL(MJUConstants.MAIN_BANNER_IMAGE_URLS[i]);
				bis = executeConnect(imageUrl);
				opt.inPreferredConfig = Config.RGB_565;
				tempBitmap = BitmapFactory.decodeStream(bis, null, opt);
				finalBitmap = Bitmap.createScaledBitmap(tempBitmap, 
						(int)r.getDimension(R.dimen.viewpager_width), 
						(int)r.getDimension(R.dimen.viewpager_height), false);
				tempBitmap.recycle();
				msg = new Message();
				msg.what = MJUConstants.MAIN_BANNER_IMAGE_COMPLETE;
				msg.obj = finalBitmap;
				handler.sendMessage(msg); 
				bis.close();
			} catch (OutOfMemoryError e) {
				handler.sendEmptyMessage(MJUConstants.OUT_OF_MEMORY);
			} catch (MalformedURLException e) {
				handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_IMAGE_FAIL);
			} catch (IOException e) {
				handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_IMAGE_FAIL);
			}
		}*/
	}
	
	private BufferedInputStream executeConnect(URL imageUrl) {
		// TODO Auto-generated method stub 
		try {
			if (imageUrl == null) {
				throw new Exception(); 
			}
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setDefaultUseCaches(true);
			conn.connect();
			int size = conn.getContentLength();
			
			if (size > 0) {
				return new BufferedInputStream(conn.getInputStream(), size);
			}
		} catch (IOException e) {
			handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_IMAGE_FAIL);
		} catch (Exception e) {
			handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_IMAGE_FAIL);
		}
		return null;
	}
}