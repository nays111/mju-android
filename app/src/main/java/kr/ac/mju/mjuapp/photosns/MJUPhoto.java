package kr.ac.mju.mjuapp.photosns;

public class MJUPhoto {
	private String title;
	private String wirter;
	private String date;
	private String hit;

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

	public String getHit() {
		return hit;
	}

	public void setHit(String hit) {
		this.hit = hit;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "제목: " + title + "\n작성자: " + wirter + "\n날짜: " + date
				+ "\n조회수: " + hit;
	}
}
