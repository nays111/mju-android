package kr.ac.mju.mjuapp.notice;

import java.util.*;

import kr.ac.mju.mjuapp.*;
import kr.ac.mju.mjuapp.common.*;
import android.content.*;
import android.view.*;
import android.widget.*;

/**
 * @author davidkim
 * 
 */
public class NoticeAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<Notice> mNoticeList;

	public NoticeAdapter(Context _context, ArrayList<Notice> _noticeList) {
		// TODO Auto-generated constructor stub
		this.mInflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mNoticeList = _noticeList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mNoticeList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mNoticeList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		NoticeViewHolder vh;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.notice_list_row_layout,
					parent, false);
			vh = new NoticeViewHolder();

			// set viewholder
			vh.setA_subjectView((CustomTextView) convertView
					.findViewById(R.id.notice_list_row_subject));
			vh.setA_numView((TextView) convertView
					.findViewById(R.id.notice_list_row_num));
			vh.setA_dateView((TextView) convertView
					.findViewById(R.id.notice_list_row_date));
			vh.setA_hiddenUrlView((TextView) convertView
					.findViewById(R.id.notice_list_row_hidden_url));
			vh.setA_attachedFileView((ImageView) convertView
					.findViewById(R.id.notice_list_row_attachedfile));
			// set viewholder to convertview
			convertView.setTag(vh);
		} else
			vh = (NoticeViewHolder) convertView.getTag();

		// set data
		((TextView) vh.getA_subjectView()).setText(mNoticeList.get(position)
				.getA_subject());
		((TextView) vh.getA_numView()).setText(mNoticeList.get(position)
				.getA_num());
		((TextView) vh.getA_dateView()).setText(mNoticeList.get(position)
				.getA_date());
		((TextView) vh.getA_hiddenUrlView()).setText(mNoticeList.get(position)
				.getA_url());
		if (mNoticeList.get(position).isA_file())
			((ImageView) vh.getA_attachedFileView())
					.setVisibility(View.VISIBLE);
		else
			((ImageView) vh.getA_attachedFileView())
					.setVisibility(View.INVISIBLE);
		return convertView;
	}
}
/* end of file */