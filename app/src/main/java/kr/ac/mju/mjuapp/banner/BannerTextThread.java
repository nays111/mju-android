package kr.ac.mju.mjuapp.banner;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.http.HttpManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BannerTextThread extends Thread {

	private Handler handler;

	private HttpManager httpManager;
	private HttpResponse response = null;
	private HttpEntity entity = null;
	private Vector<String> tagNames;
	private StatusLine status;
	HashMap<String, List<Element>> elementMap;
	
	public BannerTextThread(Handler handler) {
		// TODO Auto-generated constructor stub
		this.handler = handler;

		httpManager = new HttpManager();
		httpManager.init();
		tagNames = new Vector<String>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();

		tagNames.add(HTMLElementName.DIV);
		try {
			httpManager.setHttpPost(MJUConstants.MJU_IMG_URL); //.MJU_INDEX_URL);
			response = httpManager.executeHttpPost();
			status = response.getStatusLine();

			if (status.getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				elementMap = httpManager.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
				List<Element> liList = elementMap.get(HTMLElementName.DIV);

				String bannerUrl;
				Banner banner = null;
				Message msg;

				for (int i = 0; i < 5; i++) {
					Log.d("[banner]", "start");

					Log.d("[liList]", liList.get(i).toString());

					banner = new Banner();
					bannerUrl = liList.get(i).getAllElements(HTMLElementName.A).get(0).getAttributeValue("href").trim();

					Log.d("[banner] url = ", bannerUrl);

					banner.setUrl(bannerUrl);
					banner.setTitle(liList.get(i).getAllElements(HTMLElementName.A).get(1).getContent().toString().trim());
					Log.d("[banner] url = ", liList.get(i).getAllElements(HTMLElementName.A).get(1).getContent().toString().trim());

					banner.setWirter(liList.get(i).getAllElements(HTMLElementName.A).get(2).getAttributeValue("href").trim());
					banner.setDate(liList.get(i).getAllElements(HTMLElementName.A).get(3).getContent().toString().trim());

					msg = handler.obtainMessage();
					msg.obj = banner;
					msg.what = MJUConstants.MAIN_BANNER_TEXTS_COMPLETE;
					handler.sendMessage(msg);
				}
				/*
				List<Element> divList = elementMap.get(HTMLElementName.DIV);

				int index1 = 36;
				int index2 = 43;
				String bannerUrl;
				Banner banner = null;
				Message msg;

				for (int i = 0; i < 5; i++) {
					banner = new Banner();
					bannerUrl = divList.get(index1 + i).getAllElements(HTMLElementName.A).get(0).
							getAttributeValue("href").trim();
					banner.setUrl(bannerUrl);
					banner.setTitle(divList.get(index2 + i).getContent().toString().trim());

					// 각 배너 url 타고들어가서 조회수, 작성자, 등록일 정보 파싱
					List<Element> tdList = getEachBannerInfo(bannerUrl);
					if (tdList != null) {
						banner.setWirter(tdList.get(2).getContent().toString().trim());
						banner.setDate(tdList.get(3).getContent().toString().trim());
					} else {
						handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_TEXTS_FAIL);
					}
					msg = handler.obtainMessage();
					msg.obj = banner;
					msg.what = MJUConstants.MAIN_BANNER_TEXTS_COMPLETE;
					handler.sendMessage(msg);

				}*/
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("bannaer error : "+e);
			handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_TEXTS_FAIL);
		}
	}

	private List<Element> getEachBannerInfo(String bannerUrl) {
		// TODO Auto-generated method stub
		tagNames.add(HTMLElementName.TD);
		
		try {
			httpManager.setHttpPost(bannerUrl);
			response = httpManager.executeHttpPost();
			status = response.getStatusLine();
			
			if (status.getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				elementMap = httpManager.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
				
				return elementMap.get(HTMLElementName.TD);
			}
		} catch (Exception e) {
			// TODO: handle exception
			handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_TEXTS_FAIL);
		}
		
		handler.sendEmptyMessage(MJUConstants.MAIN_BANNER_TEXTS_FAIL);
		return null;
	}
}
