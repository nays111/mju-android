package kr.ac.mju.mjuapp.notice;

import kr.ac.mju.mjuapp.common.*;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author davidkim
 * 
 */
public class NoticeViewHolder {
	private TextView a_numView; // 湲�쾲��
	private TextView a_subjectView; // 湲�젣紐�
	private TextView a_dateView; // �묒꽦�쇱옄
	private TextView a_hiddenUrlView; // 留곹겕 url
	private ImageView a_attachedFileView; // 泥⑤��뚯씪 �대�吏�

	public TextView getA_numView() {
		return a_numView;
	}

	public void setA_numView(TextView a_numView) {
		this.a_numView = a_numView;
	}

	public TextView getA_subjectView() {
		return a_subjectView;
	}

	public void setA_subjectView(CustomTextView a_subjectView) {
		this.a_subjectView = a_subjectView;
	}

	public TextView getA_dateView() {
		return a_dateView;
	}

	public void setA_dateView(TextView a_dateView) {
		this.a_dateView = a_dateView;
	}

	public TextView getA_hiddenUrlView() {
		return a_hiddenUrlView;
	}

	public void setA_hiddenUrlView(TextView a_hiddenUrlView) {
		this.a_hiddenUrlView = a_hiddenUrlView;
	}

	public ImageView getA_attachedFileView() {
		return a_attachedFileView;
	}

	public void setA_attachedFileView(ImageView a_attachedFileView) {
		this.a_attachedFileView = a_attachedFileView;
	}
}
/* end of file */
