package kr.ac.mju.mjuapp.green;

import java.util.*;

import kr.ac.mju.mjuapp.*;
import android.content.*;
import android.view.*;
import android.widget.*;

/**
 * @author davidkim
 *
 */
public class GreencampusAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<Greencampus> greencampusList;

	public GreencampusAdapter(Context _context,
			ArrayList<Greencampus> _greencampusList) {
		// TODO Auto-generated constructor stub
		this.mInflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.greencampusList = _greencampusList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return greencampusList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return greencampusList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		GreencampusViewHolder vh;
		if (convertView == null) {
			convertView = mInflater.inflate(
					R.layout.greencampus_list_row_layout, parent, false);
			vh = new GreencampusViewHolder();
			// set viewholder
			vh.setSubjectView((TextView) convertView
					.findViewById(R.id.green_list_row_subject));
			vh.setWriterView((TextView) convertView
					.findViewById(R.id.green_list_row_writer));
			vh.setDateView((TextView) convertView
					.findViewById(R.id.green_list_row_date));
			vh.setHiddenUrlView((TextView) convertView
					.findViewById(R.id.green_list_row_hidden_url));
			vh.setStatusView((TextView) convertView
					.findViewById(R.id.green_list_row_status));
			// set viewholder to convertview
			convertView.setTag(vh);
		} else
			vh = (GreencampusViewHolder) convertView.getTag();
		// set data
		vh.getSubjectView().setText(greencampusList.get(position).getP_subject());
		vh.getWriterView().setText(greencampusList.get(position).getP_writer());
		vh.getDateView().setText(greencampusList.get(position).getP_date());
		vh.getHiddenUrlView().setText(greencampusList.get(position).getP_url());
		vh.getStatusView().setText(greencampusList.get(position).getP_status());
		return convertView;
	}
}
/* end of file */
