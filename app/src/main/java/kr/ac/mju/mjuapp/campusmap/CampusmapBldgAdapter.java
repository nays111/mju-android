package kr.ac.mju.mjuapp.campusmap;

import kr.ac.mju.mjuapp.R;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * @author davidkim
 *
 */
public class CampusmapBldgAdapter extends CursorAdapter {

	@SuppressWarnings("deprecation")
	public CampusmapBldgAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		CampusmapBldgViewHolder vh = (CampusmapBldgViewHolder) view.getTag();
		if (vh == null) {
			// get
			vh = new CampusmapBldgViewHolder();
			// set viewholder
			vh.setBldgNameView((TextView) view.findViewById(R.id.campusmap_row_bldg_name));
			vh.setLatitudeView((TextView) view.findViewById(R.id.campusmap_row_bldg_latitude));
			vh.setLongitudeView((TextView) view.findViewById(R.id.campusmap_row_bldg_longitude));
			// set tag
			view.setTag(vh);
		}
		// set data
		vh.getBldgNameView().setText(cursor.getString(cursor.getColumnIndex(CampusmapContentProvider.COL_BLDG_NAME)));
		vh.getLatitudeView().setText(cursor.getString(cursor.getColumnIndex(CampusmapContentProvider.COL_BLDG_LATITUDE)));
		vh.getLongitudeView().setText(cursor.getString(cursor.getColumnIndex(CampusmapContentProvider.COL_BLDG_LONGITUDE)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.campusmap_building_row_layout,	parent, false);

		return view;
	}

}
