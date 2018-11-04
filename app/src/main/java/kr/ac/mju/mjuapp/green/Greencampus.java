package kr.ac.mju.mjuapp.green;

/**
 * @author davidkim
 * 
 */
public class Greencampus {
	private String p_num; // 글 번호
	private String p_subject; // 글 제목
	private String p_date; // 작성 날짜
	private String p_writer; // 작성자
	private String p_url; // 링크 url
	private String p_status; // 처리상태

	public String getP_status() {
		return p_status;
	}

	public void setP_status(String p_status) {
		this.p_status = p_status;
	}

	public String getP_num() {
		return p_num;
	}

	public void setP_num(String p_num) {
		this.p_num = p_num;
	}

	public String getP_subject() {
		return p_subject;
	}

	public void setP_subject(String p_subject) {
		this.p_subject = p_subject;
	}

	public String getP_date() {
		return p_date;
	}

	public void setP_date(String p_date) {
		this.p_date = p_date;
	}

	public String getP_writer() {
		return p_writer;
	}

	public void setP_writer(String p_writer) {
		this.p_writer = p_writer;
	}

	public String getP_url() {
		return p_url;
	}

	public void setP_url(String p_url) {
		this.p_url = p_url;
	}
}
/* end of file */
