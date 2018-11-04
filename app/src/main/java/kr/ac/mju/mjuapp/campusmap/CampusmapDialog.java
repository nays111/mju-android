package kr.ac.mju.mjuapp.campusmap;

import kr.ac.mju.mjuapp.*;
import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author davidkim
 *
 */
public class CampusmapDialog extends Dialog {
	private Context context;
	private Cursor cursor;

	public CampusmapDialog(Context context, Cursor cursor) {
		// TODO Auto-generated constructor stub
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		this.context = context;
		this.cursor = cursor;
		setContentView(R.layout.campusmap_dialog_layout);
		WindowManager.LayoutParams lParams = getWindow().getAttributes();
		lParams.dimAmount = 0.5f;
		getWindow().setAttributes(lParams);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		init();
	}

	/**
	 * 
	 */
	/**
	 * 
	 */
	private void init() {
		// set adapter
		CampusmapDialogAdapter adapter = new CampusmapDialogAdapter(context, cursor);
		adapter.notifyDataSetChanged();
		// set listview
		((ListView) findViewById(R.id.campusmap_dialog_listview)).setAdapter(adapter);
		((ListView) findViewById(R.id.campusmap_dialog_listview)).setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				String phone = ((TextView) view.findViewById(R.id.campusmap_dialog_row_office_phone)).getText().toString();
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + phone));
				context.startActivity(intent);
			}
		});
	}
	/**
	 * @param title
	 */
	public void setTitle(String title){
		((TextView)findViewById(R.id.campusmap_dialog_title)).setText(title);
	}
}
/* end of file */
