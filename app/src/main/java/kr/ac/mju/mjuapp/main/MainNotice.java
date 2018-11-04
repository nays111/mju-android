package kr.ac.mju.mjuapp.main;

/**
 * 
 * <pre>
 * kr.ac.mju.mjuapp.temp
 *   |_ MainNotice.java
 * </pre>
 * 
 * Desc 메인 액티비티의 공지사항 정보를 저장할 클래스
 * 
 * @Author Hs
 * @Date 2013. 12. 17. 오후 9:15:41
 * @Version 1.0
 */
public class MainNotice {
	String url;
	String title;

	public MainNotice(String url, String title) {
		super();
		this.url = "http://www.mju.ac.kr" + url;
		this.title = title;
	}

	public MainNotice() {
		// TODO Auto-generated constructor stub
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "URL : " + url + "\nTitle: : " + title;

	}
}