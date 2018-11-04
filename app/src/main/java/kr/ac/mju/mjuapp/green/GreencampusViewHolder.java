package kr.ac.mju.mjuapp.green;

import android.widget.TextView;

/**
 * @author davidkim
 * 
 */
public class GreencampusViewHolder {
	private TextView numView; // 湲�쾲��
	private TextView subjectView; // 湲�젣紐�
	private TextView writerView; // �묒꽦��
	private TextView dateView; // �묒꽦 �쇱옄
	private TextView hiddenUrlView; // 留곹겕 url
	private TextView statusView; // 泥섎━ �곹깭

	public TextView getStatusView() {
		return statusView;
	}

	public void setStatusView(TextView statusView) {
		this.statusView = statusView;
	}

	public TextView getNumView() {
		return numView;
	}

	public void setNumView(TextView numView) {
		this.numView = numView;
	}

	public TextView getSubjectView() {
		return subjectView;
	}

	public void setSubjectView(TextView subjectView) {
		this.subjectView = subjectView;
	}

	public TextView getWriterView() {
		return writerView;
	}

	public void setWriterView(TextView writerView) {
		this.writerView = writerView;
	}

	public TextView getDateView() {
		return dateView;
	}

	public void setDateView(TextView dateView) {
		this.dateView = dateView;
	}

	public TextView getHiddenUrlView() {
		return hiddenUrlView;
	}

	public void setHiddenUrlView(TextView hiddenUrlView) {
		this.hiddenUrlView = hiddenUrlView;
	}
}
/* end of file */
