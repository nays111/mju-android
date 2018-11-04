package kr.ac.mju.mjuapp.community;

import android.graphics.Bitmap;

/**
 * Article 클래스를 상속받은 클래스 Image 게시판 정보를 저장하는 클래스
 * 
 * @author Hs
 * 
 */

public class ImageArticle extends Article {
	private String name;
	private Bitmap bitmap;
	private String count;

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCount() {
		return count;
	}

	public void setCount(String count) {
		this.count = count;
	}

}
