package kr.ac.mju.mjuapp.campusmap;

import kr.ac.mju.mjuapp.*;
import android.content.*;
import android.database.*;
import android.view.*;
import android.widget.*;

/**
 * @author davidkim
 *
 */
public class CampusmapDialogAdapter extends CursorAdapter {

	@SuppressWarnings("deprecation")
	public CampusmapDialogAdapter(Context context, Cursor c) {
		super(context, c);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		// TODO Auto-generated method stub
		CampusmapDialogViewHolder vh = (CampusmapDialogViewHolder) view.getTag();
		if (vh == null) {
			vh = new CampusmapDialogViewHolder();
			// set
			vh.setOfficeName1((TextView) view.findViewById(R.id.campusmap_dialog_row_office_name1));
			vh.setOfficeName2((TextView) view.findViewById(R.id.campusmap_dialog_row_office_name2));
			vh.setOfficeName3((TextView) view.findViewById(R.id.campusmap_dialog_row_office_name3));
			vh.setOfficePhone((TextView) view.findViewById(R.id.campusmap_dialog_row_office_phone));
			// set tag
			view.setTag(vh);
		}
		// set data
		vh.getOfficeName1().setText(cursor.getString(cursor.getColumnIndex(CampusmapContentProvider.COL_OFFICE_NAME)));
		vh.getOfficeName2().setText(cursor.getString(cursor.getColumnIndex(CampusmapContentProvider.COL_OFFICE_NAME2)));
		vh.getOfficeName3().setText(cursor.getString(cursor.getColumnIndex(CampusmapContentProvider.COL_OFFICE_NAME3)));
		vh.getOfficePhone().setText(cursor.getString(cursor.getColumnIndex(CampusmapContentProvider.COL_OFFICE_PHONE)));
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.campusmap_dialog_row_layout, parent, false);

		return view;
	}

}
/* end of file */
