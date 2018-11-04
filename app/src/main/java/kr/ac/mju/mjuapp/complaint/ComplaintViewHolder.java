package kr.ac.mju.mjuapp.complaint;

import android.widget.TextView;

/**
 * @author davidkim
 * 
 */
public class ComplaintViewHolder {
	private TextView subjectView;
	private TextView writerView;
	private TextView dateView;
	private TextView statusView;
	private TextView hiddenUrlView;

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

	public TextView getStatusView() {
		return statusView;
	}

	public void setStatusView(TextView statusView) {
		this.statusView = statusView;
	}

	public TextView getHiddenUrlView() {
		return hiddenUrlView;
	}

	public void setHiddenUrlView(TextView hiddenUrlView) {
		this.hiddenUrlView = hiddenUrlView;
	}
}
/* end of file */
