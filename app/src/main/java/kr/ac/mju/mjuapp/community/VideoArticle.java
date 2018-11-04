package kr.ac.mju.mjuapp.community;

import android.graphics.Bitmap;

/**
 * Article 클래스를 상속받은 클래스 Video 게시판 정보를 저장하는 클래스
 * 
 * @author Hs
 * 
 */

public class VideoArticle extends Article {
	private String date;
	private Bitmap bitmap;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
}
