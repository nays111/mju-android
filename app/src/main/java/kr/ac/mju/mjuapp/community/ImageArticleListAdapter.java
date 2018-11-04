package kr.ac.mju.mjuapp.community;

import java.util.*;

import kr.ac.mju.mjuapp.*;
import android.content.*;
import android.view.*;
import android.widget.*;

public class ImageArticleListAdapter extends BaseAdapter {
	private LayoutInflater inflator;
	private ArrayList<ImageArticle> communityList;

	public ImageArticleListAdapter(Context context,
			ArrayList<ImageArticle> communityList) {
		this.inflator = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.communityList = communityList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return communityList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return communityList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub

		if (view == null) {
			view = inflator.inflate(R.layout.community_list_row_img_layout,
					parent, false);
		}

		if (communityList.get(position).getBitmap() != null) {
			((ImageView) view.findViewById(R.id.community_list_row_img))
					.setImageBitmap(communityList.get(position).getBitmap());
		} else {
			((ImageView) view.findViewById(R.id.community_list_row_img))
					.setImageResource(R.drawable.icon_logo);
		}

		((TextView) view.findViewById(R.id.community_list_row_subject))
				.setText(communityList.get(position).getTitle());
		((TextView) view.findViewById(R.id.community_list_row_name))
				.setText(communityList.get(position).getName());

		String count = communityList.get(position).getCount();

		if (count == null) {
			((TextView) view.findViewById(R.id.community_list_row_count))
					.setVisibility(View.INVISIBLE);
		} else {
			((TextView) view.findViewById(R.id.community_list_row_count))
					.setVisibility(View.VISIBLE);
			((TextView) view.findViewById(R.id.community_list_row_count))
					.setText(communityList.get(position).getCount());
		}

		((TextView) view.findViewById(R.id.community_list_row_hidden_url))
				.setText(communityList.get(position).getUrl());

		return view;
	}
}
