package kr.ac.mju.mjuapp.community;

/**
 * Article 클래스를 상속받은 클래스
 * 일반형태의 게시판 정보를 저장하는 클래스 
 * @author Hs
 *
 */

public class NormalArticle extends Article {
	private String name;
	private String date;
	private String replyCount;
	private boolean file;
	private boolean replyimg;

	public boolean isReplyimg() {
		return replyimg;
	}

	public void setReplyimg(boolean replyimg) {
		this.replyimg = replyimg;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public String getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(String replyCount) {
		this.replyCount = replyCount;
	}
	
	public void setFile(boolean file) {
		this.file = file;
	}
	
	public boolean getFile() {
		return file;
	}
}