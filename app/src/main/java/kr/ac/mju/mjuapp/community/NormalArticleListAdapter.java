package kr.ac.mju.mjuapp.community;

import java.util.*;

import kr.ac.mju.mjuapp.*;
import android.content.*;
import android.view.*;
import android.widget.*;

public class NormalArticleListAdapter extends BaseAdapter {
	private LayoutInflater inflator;
	private ArrayList<NormalArticle> communityList;
	
	public NormalArticleListAdapter(Context context, ArrayList<NormalArticle> communityList) {
		this.inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			view = inflator.inflate(R.layout.community_list_row_layout, parent, false);
		}
		
		if (communityList.get(position).isReplyimg()) {
			((ImageView)view.findViewById(R.id.community_list_row_reply_img)).setVisibility(View.VISIBLE);
			
		} else {
			((ImageView)view.findViewById(R.id.community_list_row_reply_img)).setVisibility(View.GONE);
		}
		
		((TextView)view.findViewById(R.id.community_list_row_subject)).setText(communityList.
				get(position).getTitle());
		
		String count = communityList.get(position).getReplyCount();
		if (count != null) {
			((TextView)view.findViewById(R.id.community_list_row_reply_count)).setText(count);
			((TextView)view.findViewById(R.id.community_list_row_reply_count)).setVisibility(View.VISIBLE);
		} else {
			((TextView)view.findViewById(R.id.community_list_row_reply_count)).setVisibility(View.INVISIBLE);
		}
		
		((TextView)view.findViewById(R.id.community_list_row_writer)).setText(communityList.
				get(position).getName());
		((TextView)view.findViewById(R.id.community_list_row_date)).setText(communityList.
				get(position).getDate());
		((TextView)view.findViewById(R.id.community_list_row_hidden_url)).setText(communityList.
				get(position).getUrl());
		
		if (communityList.get(position).getFile() == true) {
			((ImageView)view.findViewById(R.id.community_list_row_attachedfile))
			.setVisibility(View.VISIBLE);
		} else {
			((ImageView)view.findViewById(R.id.community_list_row_attachedfile))
				.setVisibility(View.INVISIBLE);
		}
		
		return view;
	}
}
