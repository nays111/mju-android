package kr.ac.mju.mjuapp.banner;

public class Banner {
	private String url;
	private String title;
	private String wirter;
	private String date;

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

	public String getWirter() {
		return wirter;
	}

	public void setWirter(String wirter) {
		this.wirter = wirter;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "1: " + title + "\n2 " + wirter + "\n3 " + date + "\nURL: " + url;
	}
}
