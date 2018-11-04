package kr.ac.mju.mjuapp.weather;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import kr.ac.mju.mjuapp.constants.MJUConstants;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author davidkim
 *
 */
public class WeatherThread extends Thread {
	private Handler handler;
	private int type;

	public static final int TYPE_SEOUL = 0x01;
	public static final int TYPE_YONGIN = 0x02;

	private final String URL_SEOUL = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=59&gridy=127";
	private final String URL_YONGIN = "http://www.kma.go.kr/wid/queryDFS.jsp?gridx=64&gridy=119";

	public WeatherThread(Handler handler, Context context, int type) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.type = type;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub 
		super.run();
		// debug
		long start = System.currentTimeMillis();
		// params
		HttpURLConnection conn = null;
		WeatherInfo weatherInfo = null;
		URL url = null;
		
		try {
			if (type == TYPE_SEOUL) {
				url = new URL(URL_SEOUL);
			} else if (type == TYPE_YONGIN) {
				url = new URL(URL_YONGIN);
			} 
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(3000);
			conn.setUseCaches(true);
			conn.connect();
			
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				
				// set xmlParser
				XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
				XmlPullParser parser = parserCreator.newPullParser();
				parser.setInput(conn.getInputStream(), "UTF-8");
				// init parse data
				String tag;
				int parseEvent = parser.getEventType();
				boolean isDataStarted = false;
				// parsing
				while (parseEvent != XmlPullParser.END_DOCUMENT) {
					switch (parseEvent) {
					case XmlPullParser.START_TAG:
						tag = parser.getName();
						// <data>
						if (tag.equalsIgnoreCase("data")) {
							weatherInfo = new WeatherInfo();
							isDataStarted = true;
						}
						if (isDataStarted) {
							//<temp>
							if (tag.equalsIgnoreCase("temp")) {
								parseEvent = parser.nextToken();
								weatherInfo.setTemp(parser.getText());
							}
							//<wfKor>
							else if (tag.equalsIgnoreCase("wfKor")) {
								parseEvent = parser.nextToken();
								weatherInfo.setText(parser.getText());
							}
						}
						break;
					case XmlPullParser.END_TAG:
						if (isDataStarted) {
							tag = parser.getName();
							if (tag.equalsIgnoreCase("data")) {
								isDataStarted = false;
								// debug
								long end = System.currentTimeMillis();
								Log.d("MDC", "WeatherThread : " + (double) (end - start) / 1000.);
								// send message
								Message msg = new Message();
								msg.obj = weatherInfo; 
								if (type == TYPE_SEOUL)
									msg.what = MJUConstants.WEATHER_SEOUL_COMPLETE;
								else if (type == TYPE_YONGIN)
									msg.what = MJUConstants.WEATHER_YONGIN_COMPLETE;
								handler.sendMessage(msg);
								return;
							}
						}
						break;
					}
					parseEvent = parser.next();
				}
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			handler.sendEmptyMessage(MJUConstants.WEATHER_FAIL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			handler.sendEmptyMessage(MJUConstants.WEATHER_FAIL);
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			handler.sendEmptyMessage(MJUConstants.WEATHER_FAIL);
		} finally {
			if (conn != null)
				conn.disconnect();
		}
	}
}
/* end of file */
