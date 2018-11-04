package kr.ac.mju.mjuapp.photosns;

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

public class PhotoTextThread extends Thread {
	private Handler handler;

	public PhotoTextThread(Handler handler) {
		this.handler = handler;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		HttpManager httpManager = new HttpManager();
		httpManager.init();
		HttpResponse response = null;
		HttpEntity entity = null;
		Vector<String> tagNames = new Vector<String>();
		tagNames.add(HTMLElementName.TBODY);

		try {
			MJUPhoto picture;
			Message msg;
			for (int i = 0; i < 7; i++) {
				httpManager.setHttpPost(MJUConstants.MAIN_PICTURE_TEXT_URLS[i]);
				response = httpManager.executeHttpPost();
				StatusLine status = response.getStatusLine();

				if (status.getStatusCode() == HttpStatus.SC_OK) {
					entity = response.getEntity();
					HashMap<String, List<Element>> elementMap = httpManager
							.getHttpElementsMap(entity, tagNames,
									HttpManager.UTF_8);
					List<Element> tdList = elementMap
							.get(HTMLElementName.TBODY).get(0)
							.getAllElements(HTMLElementName.TD);

					if (tdList != null && tdList.size() != 0) {
						picture = new MJUPhoto();

						picture.setTitle(tdList.get(0)
								.getAllElements(HTMLElementName.STRONG).get(0)
								.getContent().toString().trim());
						picture.setWirter(tdList.get(2).getContent().toString()
								.trim());
						picture.setDate(tdList.get(3).getContent().toString()
								.trim());
						picture.setHit(tdList.get(4).getContent().toString()
								.trim());

						msg = handler.obtainMessage();
						msg.obj = picture;
						msg.what = MJUConstants.MAIN_PICTURE_TEXTS_COMPLETE;
						handler.sendMessage(msg);
					} else {
						handler.sendEmptyMessage(MJUConstants.MAIN_PICTURE_TEXTS_FAIL);
					}
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			handler.sendEmptyMessage(MJUConstants.MAIN_PICTURE_TEXTS_FAIL);
		}
	}
}
