package kr.ac.mju.mjuapp.main;

import java.util.ArrayList;
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

public class MainNoticeThread extends Thread {

	private Handler handler;
	private String noticeUrl;

	public MainNoticeThread(Handler handler, String noticeUrl) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.noticeUrl = noticeUrl;
	}

	public void run() {
		HttpManager httpManager = new HttpManager();
		httpManager.init();
		HttpResponse response = null;
		HttpEntity entity = null;
		Vector<String> tagNames = new Vector<String>();
		tagNames.add(HTMLElementName.UL);

		try {
			ArrayList<MainNotice> mainNoticeList = new ArrayList<MainNotice>();
			
			httpManager.setHttpPost(noticeUrl);

			response = httpManager.executeHttpPost();
			StatusLine status = response.getStatusLine();

			if (status.getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(entity, tagNames,
						HttpManager.UTF_8);
				
				List<Element> liList = elementMap.get(HTMLElementName.UL).get(0).getAllElements(HTMLElementName.LI);
				String title, url;
					
				for (int j = 0; j < liList.size() - 1; j++) {
					Element e = liList.get(j).getAllElements(HTMLElementName.A).get(0);
					title = e.getContent().toString().trim();
					url = e.getAttributeValue("href");
					MainNotice notice = new MainNotice(url, title);
					mainNoticeList.add(notice);
				}
			}
			
			Message msg = new Message();
			msg.obj = mainNoticeList;
			msg.what = MJUConstants.MAIN_NOTICE_SUCCESS;
			handler.sendMessage(msg);
		} catch (Exception e) {
			handler.sendEmptyMessage(MJUConstants.MAIN_NOTICE_FAIL);
		}
	}
}