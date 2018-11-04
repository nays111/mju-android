package kr.ac.mju.mjuapp.notice;

/**
 * @author davidkim
 *
 */
public class Notice {
	private String a_num;				//湲�踰덊샇
	private String a_subject;		//湲��쒕ぉ
	private String a_date;			//�묒꽦 �좎쭨
	private String a_hits;		//議고쉶��	
	private boolean a_file;		//泥⑤��뚯씪�좊Т
	private String a_url;				//留곹겕 url
	
	public String getA_num() {
		return a_num;
	}
	public void setA_num(String a_num) {
		this.a_num = a_num;
	}
	public String getA_subject() {
		return a_subject;
	}
	public void setA_subject(String a_subject) {
		this.a_subject = a_subject;
	}
	public String getA_date() {
		return a_date;
	}
	public void setA_date(String a_date) {
		this.a_date = a_date;
	}
	public String getA_hits() {
		return a_hits;
	}
	public void setA_hits(String a_hits) {
		this.a_hits = a_hits;
	}
	public boolean isA_file() {
		return a_file;
	}
	public void setA_file(boolean a_file) {
		this.a_file = a_file;
	}
	public String getA_url() {
		return a_url;
	}
	public void setA_url(String a_url) {
		this.a_url = a_url;
	}	
}
/* end of file */
