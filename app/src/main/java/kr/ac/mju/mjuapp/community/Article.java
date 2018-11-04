package kr.ac.mju.mjuapp.community;

/**
 * 커뮤니티 게시글 정보를 저장하기 위한 클래스. 
 * 다른 파생클래스의 부모 클래스  
 * 기본적으로 글 제목, 글 url만 저장.
 * @author Hs
 *
 */
public class Article {
	protected String title;
	protected String url;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
