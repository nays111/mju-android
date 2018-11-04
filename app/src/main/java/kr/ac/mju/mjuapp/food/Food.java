package kr.ac.mju.mjuapp.food;

import java.util.ArrayList;

public class Food {
	private String day;
	private String date;
	
	private ArrayList<String> menuList;
	
	public Food() {
		// TODO Auto-generated constructor stub
		menuList = new ArrayList<String>();
	}

	public String getDay() {
		return day;
	}

	public void setDay(String day) {
		this.day = day;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public void addMenu(String str) {
		menuList.add(str);
	}
	
	public ArrayList<String> getMenuList() {
		return menuList;
	}
	
	public int getMenuSize() {
		return menuList.size();
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String returnStr = "요일: " + day + "\n날짜: " + date + "\n";

		for (int i = 0; i < menuList.size(); i++) {
			returnStr += "메뉴" + (i+1) + ": " + menuList.get(i) + "\n";
		}
		
		return returnStr;
	}
}
