package kr.ac.mju.mjuapp.complaint;

/**
 * @author davidkim
 * 
 */
public class Complaint {
	private String c_subject; // �쒕ぉ
	private String c_date; // �좎껌�좎쭨
	private String c_writer; // �좎껌�� private String c_status; // 泥섎━�곹깭
	private String c_hidden_url;// 湲��댁슜 url
	private String c_status;

	public String getC_subject() {
		return c_subject;
	}

	public void setC_subject(String c_subject) {
		this.c_subject = c_subject;
	}

	public String getC_date() {
		return c_date;
	}

	public void setC_date(String c_date) {
		this.c_date = c_date;
	}

	public String getC_writer() {
		return c_writer;
	}

	public void setC_writer(String c_writer) {
		this.c_writer = c_writer;
	}

	public String getC_status() {
		return c_status;
	}

	public void setC_status(String c_status) {
		this.c_status = c_status;
	}

	public String getC_hidden_url() {
		return c_hidden_url;
	}

	public void setC_hidden_url(String c_hidden_url) {
		this.c_hidden_url = c_hidden_url;
	}
}
/* end of file */
