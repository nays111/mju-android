package kr.ac.mju.mjuapp.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class DateManager {

	public static String getDate(int offset) {
		String date = "";
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE,MM,dd,yyyy", Locale.KOREA);
		Calendar cal = Calendar.getInstance();
		
		cal.add(Calendar.DATE, offset - (getIndexForToday() - 2));
		
		String[] tokens = dateFormat.format(cal.getTime()).split(",");
		date += tokens[3] + ". " + removeFirtZero(tokens[1]) + ". " + removeFirtZero(tokens[2]) + ". " + tokens[0];
		
		return date;
	}
	
	public static String getCurrentMonth() {
		Date day = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM", Locale.ENGLISH);
		
		return removeFirtZero(dateFormat.format(day));
	}
	
	public static String getCurrentDate() {
		Date day = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd", Locale.ENGLISH);
		
		return removeFirtZero(dateFormat.format(day));
	}

	public static String getDayInEnglish() {
		Date day = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
		
		return dateFormat.format(day);
	}
	
	public static String getDayInKorean() {
		Date day = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE", Locale.KOREA);
		
		return dateFormat.format(day);
	}
	
	public static int getIndexForToday() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(Calendar.DAY_OF_WEEK);
	}
	
	private static String removeFirtZero(String str) {
		// TODO Auto-generated method stub
		if (str.startsWith("0")) {
			str = str.substring(1);
		}
		return str;
	}
}
