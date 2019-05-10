package kr.ac.mju.mjuapp.food;

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

public class FoodThread extends Thread {

	public static final int HUMANITY_STUDENT = 1;
	public static final int HUMANITY_STAFF = 2;
	public static final int SCIENCE_STUDENT = 3;
	public static final int SCIENCE_LIBRARY = 4;
	public static final int SCIENCE_STAFF = 5;

	private Handler handler;
	private String url;
	private int flag;

	public FoodThread(Handler handler, String url, int flag) {
		super();
		this.handler = handler;
		this.url = url;
		this.flag = flag;
	}

	public void run() {
		HttpManager httpManager = new HttpManager();
		httpManager.init();
		HttpResponse response = null;
		HttpEntity entity = null;
		Vector<String> tagNames = new Vector<String>();
		tagNames.add(HTMLElementName.DIV);

		httpManager.setHttpPost(url);

		try {
			response = httpManager.executeHttpPost();
			StatusLine status = response.getStatusLine();

			if (status.getStatusCode() == HttpStatus.SC_OK) {
				entity = response.getEntity();
				HashMap<String, List<Element>> elementMap = httpManager
						.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);

				Element div = elementMap.get(HTMLElementName.DIV).get(12);
				ArrayList<Food> foodList = parseData(div.getAllElements(
						HTMLElementName.TABLE).get(0));

				if (foodList == null) { // 운영 안하는 경우 식단 정보 없는 경우
					handler.sendEmptyMessage(MJUConstants.FOOD_EMPTY);
				} else {
					Message msg = handler.obtainMessage();
					msg.obj = foodList;
					msg.what = MJUConstants.FOOD_SUCCESS;
					handler.sendMessage(msg);
				}
			}
		} catch (Exception e) {
			// error
			handler.sendEmptyMessage(MJUConstants.FOOD_FAIL);
		}
	}

	private ArrayList<Food> parseData(Element table) {
		// TODO Auto-generated method stub
		ArrayList<Food> foodList = new ArrayList<Food>();
		Food food;
		Element foodMenuTableForEachDay;

		int size;
		if (flag == HUMANITY_STUDENT) {
			size = 7; // 6 + 1
		} else {
			size = 6; // 5 + 1
		}

		List<Element> tableList = table.getAllElements(HTMLElementName.TABLE);
		if (tableList.size() == 1) { // 식단정보 테이블이 없을때 (빈테이블 , 사이즈 1) -> 방학 중
										// 운영안하는 경우 null 리턴
			return null;
		}

		for (int i = 1; i < size; i++) {
			food = new Food();
			foodMenuTableForEachDay = tableList.get(i);

			/**
			 * 2014.02.26 학교 홈페이지 식단 html에서 요일을 나타내는 태그가 변경되었음. 그런데 지금 보니깐 날짜
			 * 계산은 DataManager에서 다 해주네? 그래서 삭제.
			 */
			// food.setDay(foodMenuTableForEachDay.getAllElements(HTMLElementName.SPAN).get(0).
			// getContent().toString());
			// List<Element> emList =
			// foodMenuTableForEachDay.getAllElements(HTMLElementName.EM);
			// food.setDate(emList.get(0).getContent().toString() + "/" +
			// emList.get(1).getContent().
			// toString());

			List<Element> trList = foodMenuTableForEachDay
					.getAllElements(HTMLElementName.TR);

			for (int j = 0; j < trList.size(); j++) {
				Element divElement = trList.get(j).getFirstElement(
						HTMLElementName.DIV);
				food.addMenu(clearString(divElement.getContent().toString()));
			}
			foodList.add(food);
		}

		return foodList;
	}

	private String clearString(String str) {
		if (str.contains("&nbsp;")) {
			str = str.replace("&nbsp;", "");
		}

		if (str.contains("\n")) {
			str = str.replace("\n", "");
		}

		if (str.contains("amp;")) {
			str = str.replace("amp;", "");
		}

		if (str.contains("&lt;")) {
			str = str.replace("&lt;", "<");
		}

		if (str.contains("&lt;")) {
			str = str.replace("&lt;", "<");
		}
		//19.05.01 불필요한 html태그가 어플에 계속 등장해 제거했음
		if (str.contains("<br>")) {
			str = str.replace("<br>", " ");
		}

		if (str.contains("<p>")) {
			str = str.substring(str.indexOf("<p>") + 3, str.indexOf("</p>"));
		}

		if (str.contains("<p ")) {
			str = str.substring(str.indexOf("\">") + 2, str.indexOf("</p>"));
		}

		if (str.contains("<span ")) {
			str = str.substring(str.indexOf("\">") + 2, str.indexOf("</span>"));
		}

		return str.trim();
	}
}
