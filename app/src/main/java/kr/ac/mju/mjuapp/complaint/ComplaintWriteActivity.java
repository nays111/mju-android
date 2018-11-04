package kr.ac.mju.mjuapp.complaint;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.network.NetworkManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * @author davidkim
 *
 */
public class ComplaintWriteActivity extends FragmentActivity {

	public static final int UPLOAD_SUCCESS = 20;
	public static final int UPLOAD_FAIL = 21;
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private ComplaintWriteHandler complaintWriteHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.complaint_write_layout);
		
		init();

		// write board button
		Button writeBtn = (Button) findViewById(R.id.complaint_write_submit_btn);
		writeBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// check contents
				if (checkParam() && NetworkManager.checkNetwork(ComplaintWriteActivity.this)) {
					progressDialog.show(fragmentManager, "");
					writeBoard();
				}
			}
		});
	}
	
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case UPLOAD_SUCCESS:
			progressDialog.dismiss();
			Toast.makeText(getBaseContext(), getString(R.string.write_success), Toast.LENGTH_SHORT).show();
			setResult(RESULT_OK);
			finish();
			break;
		case UPLOAD_FAIL:
			progressDialog.dismiss();
			Toast.makeText(getBaseContext(), getString(R.string.write_fail), Toast.LENGTH_SHORT).show();
			// error weak signal msg
			Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_network_error_weak_signal),
					Toast.LENGTH_SHORT).show();
			finish();
			break;
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		complaintWriteHandler = new ComplaintWriteHandler(ComplaintWriteActivity.this);
		
		// complaint spinner
		Spinner complaint = (Spinner) findViewById(R.id.complaint_write_complaint_spinner);
		ArrayAdapter<CharSequence> complaintAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
			R.array.complaint_write_spinner_arr, R.layout.mjuspinner_item_layout);
		complaintAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
		complaint.setAdapter(complaintAdapter);
		complaint.setSelection(0);
		setSpinnerListener();
		// complaint sub spinner
		Spinner complaintSub = (Spinner) findViewById(R.id.complaint_write_complaint_spinner_sub);
		ArrayAdapter<CharSequence> complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
				R.array.complaint_write_spinner_sub_arr_chapel, R.layout.mjuspinner_item_layout);
		complaintSubAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
		complaintSub.setAdapter(complaintSubAdapter);
		complaintSub.setSelection(0);
	}

	/**
	 * 
	 */
	private void writeBoard() {
		ComplaintWriteThread complaintWriteThread = new ComplaintWriteThread(complaintWriteHandler, 
				getApplicationContext(), getBoardWriteParamMap());
		complaintWriteThread.start();
	}

	/**
	 * @return
	 */
	private HashMap<String, String> getBoardWriteParamMap() {
		// set parameters
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		// get contents
		SharedPreferences pref = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		String name = pref.getString(getString(R.string.pref_std_name), "");
		String stdId = pref.getString(getString(R.string.pref_key_user_id), "");
		String subject = ((EditText) findViewById(R.id.complaint_write_subject_edittext)).getText().toString().trim();
		// e-mail, phonenumber 입력 필수 아니므로, null 체크해야 함
		String email = ((EditText) findViewById(R.id.complaint_write_email_edittext)).getText().toString();
		String phone = ((EditText) findViewById(R.id.complaint_write_phone_edittext)).getText().toString();
		if (phone == null)
			phone = "";
		if (email == null)
			email = "";
		String content = ((EditText) findViewById(R.id.complaint_write_content_edittext)).getText().toString();
		int category1Value = getCategory1Value();
		int category2Value = getCategory2Value(category1Value);
		//
		paramsMap.put("boardRecord.title", subject); // 제목
		paramsMap.put("boardRecord.userName", name); // 사용자 이름
		paramsMap.put("boardRecord.source", email); // e-mail
		paramsMap.put("boardRecord.addContents", phone); // 연락처
		paramsMap.put("boardRecord.contents", content); // 내용
		paramsMap.put("boardRecord.mcategoryId", String.valueOf(category2Value)); // 선택
																					// 카테고리
		paramsMap.put("boardRecord.userId", stdId); // 학번
		//
		paramsMap.put("command", "write");
		paramsMap.put("boardSeq", "");
		paramsMap.put("boardRecord.boardConfig.boardId", "5203");
		paramsMap.put("boardId", "5203");
		paramsMap.put("boardRecord.boardSeq", "");
		paramsMap.put("boardRecord.refSeq", "0");
		paramsMap.put("boardRecord.famSeq", "0");
		paramsMap.put("boardRecord.pos", "0");
		paramsMap.put("boardRecord.depth", "0");
		paramsMap.put("boardRecord.readCnt", "0");
		paramsMap.put("regDate", "");
		paramsMap.put("boardRecord.emailReceive", "");
		paramsMap.put("boardRecord.basketYn", "");
		paramsMap.put("boardRecord.commentCnt", "0");
		paramsMap.put("boardRecord.fileCnt", "0");
		paramsMap.put("boardRecord.boardType", "06");
		paramsMap.put("boardRecord.remoteIp", "");
		paramsMap.put("filesize", "0");
		paramsMap.put("pdsCnt", "5");
		paramsMap.put("pdsSize", "5242880");
		paramsMap.put("attechFile", "5242880");
		paramsMap.put("spage", "1");
		paramsMap.put("boardType", "06");
		paramsMap.put("listType", "06");
		paramsMap.put("viewType", "");
		paramsMap.put("aliasYn", "N");
		paramsMap.put("boardRecord.editorYn", "Y");
		paramsMap.put("delFile", "");
		paramsMap.put("upLoadFileValue", "");
		paramsMap.put("upLoadFileText", "");
		paramsMap.put("chkBoxSeq", "");
		paramsMap.put("imsiDir", "");
		paramsMap.put("boardRecord.boardConfig.boardName", "민원센터");
		paramsMap.put("id", "mjukr_050300000000");
		paramsMap.put("boardRecord.frontYn", "Y");
		paramsMap.put("mcategoryId", String.valueOf(category1Value));
		paramsMap.put("mcategory1", String.valueOf(category1Value));
		paramsMap.put("mcategory2", String.valueOf(category2Value));
		paramsMap.put("size_list", "");

		return paramsMap;
	}

	/**
	 * 
	 */
	private boolean checkParam() {
		// check editText parameters
		// get subject
		String subject = ((EditText) findViewById(R.id.complaint_write_subject_edittext)).getText().toString();
		if (subject.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.write_article_title_hint), Toast.LENGTH_SHORT)
					.show();
			((EditText) findViewById(R.id.complaint_write_subject_edittext)).requestFocus();
			return false;
		}
		return true;
	}

	/**
	 * 
	 */
	private void setSpinnerListener() {

		((Spinner) findViewById(R.id.complaint_write_complaint_spinner)).setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
				// TODO Auto-generated method stub
				// complaintSub spinner
				Spinner complaintSub = (Spinner) findViewById(R.id.complaint_write_complaint_spinner_sub);
				ArrayAdapter<CharSequence> complaintSubAdapter = null;
				switch (position) {
				case 0:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_chapel, R.layout.mjuspinner_item_layout);
					break;
				case 1:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_loan, R.layout.mjuspinner_item_layout);
					break;
				case 2:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_commute, R.layout.mjuspinner_item_layout);
					break;
				case 3:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_amenities, R.layout.mjuspinner_item_layout);
					break;
				case 4:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_handicapped, R.layout.mjuspinner_item_layout);
					break;
				case 5:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_military, R.layout.mjuspinner_item_layout);
					break;
				case 6:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_study_abroad, R.layout.mjuspinner_item_layout);
					break;
				case 7:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_absence, R.layout.mjuspinner_item_layout);
					break;
				case 8:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_bachelor, R.layout.mjuspinner_item_layout);
					break;
				case 9:
					complaintSubAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
							R.array.complaint_write_spinner_sub_arr_suggestion, R.layout.mjuspinner_item_layout);
					break;
				}
				complaintSubAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
				complaintSub.setAdapter(complaintSubAdapter);
				complaintSub.setSelection(0);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	/**
	 * @return
	 */
	private int getCategory1Value() {
		return ((Spinner) findViewById(R.id.complaint_write_complaint_spinner)).getSelectedItemPosition() + 6;
	}

	/**
	 * @param category1Value
	 * @return
	 */
	private int getCategory2Value(int category1Value) {
		// get subSpinnerValue
		int subSpinnerValue = ((Spinner) findViewById(R.id.complaint_write_complaint_spinner_sub)).getSelectedItemPosition();
		// set category2Value
		int category2Value = 0;
		switch (category1Value) {
		case 6:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 16;
				break;
			case 1:
				category2Value = 17;
				break;
			case 2:
				category2Value = 18;
				break;
			case 3:
				category2Value = 19;
				break;
			case 4:
				category2Value = 20;
				break;
			}
			break;
		case 7:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 22;
				break;
			case 1:
				category2Value = 75;
				break;
			case 2:
				category2Value = 21;
				break;
			case 3:
				category2Value = 74;
				break;
			}
			break;
		case 8:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 107;
				break;
			case 1:
				category2Value = 108;
				break;
			}
			break;
		case 9:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 24;
				break;
			case 1:
				category2Value = 77;
				break;
			case 2:
				category2Value = 25;
				break;
			case 3:
				category2Value = 111;
				break;
			case 4:
				category2Value = 26;
				break;
			case 5:
				category2Value = 78;
				break;
			case 6:
				category2Value = 27;
				break;
			case 7:
				category2Value = 30;
				break;
			case 8:
				category2Value = 31;
				break;
			case 9:
				category2Value = 80;
				break;
			case 10:
				category2Value = 79;
				break;
			case 11:
				category2Value = 32;
				break;
			case 12:
				category2Value = 34;
				break;
			case 13:
				category2Value = 33;
				break;
			case 14:
				category2Value = 104;
				break;
			case 15:
				category2Value = 105;
				break;
			case 16:
				category2Value = 109;
				break;
			case 17:
				category2Value = 110;
				break;
			case 18:
				category2Value = 106;
				break;
			case 19:
				category2Value = 35;
				break;
			}
			break;
		case 10:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 81;
				break;
			case 1:
				category2Value = 82;
				break;
			}
			break;
		case 11:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 83;
				break;
			case 1:
				category2Value = 84;
				break;
			}
			break;
		case 12:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 37;
				break;
			case 1:
				category2Value = 38;
				break;
			case 2:
				category2Value = 39;
				break;
			case 3:
				category2Value = 40;
				break;
			case 4:
				category2Value = 41;
				break;
			}
			break;
		case 13:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 43;
				break;
			case 1:
				category2Value = 85;
				break;
			case 2:
				category2Value = 44;
				break;
			case 3:
				category2Value = 86;
				break;
			case 4:
				category2Value = 45;
				break;
			case 5:
				category2Value = 87;
				break;
			case 6:
				category2Value = 46;
				break;
			case 7:
				category2Value = 47;
				break;
			case 8:
				category2Value = 89;
				break;
			case 9:
				category2Value = 48;
				break;
			case 10:
				category2Value = 90;
				break;
			}
			break;
		case 14:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 50;
				break;
			case 1:
				category2Value = 91;
				break;
			case 2:
				category2Value = 51;
				break;
			case 3:
				category2Value = 92;
				break;
			case 4:
				category2Value = 60;
				break;
			case 5:
				category2Value = 98;
				break;
			case 6:
				category2Value = 53;
				break;
			case 7:
				category2Value = 93;
				break;
			case 8:
				category2Value = 55;
				break;
			case 9:
				category2Value = 94;
				break;
			case 10:
				category2Value = 57;
				break;
			case 11:
				category2Value = 101;
				break;
			case 12:
				category2Value = 59;
				break;
			case 13:
				category2Value = 97;
				break;
			case 14:
				category2Value = 63;
				break;
			case 15:
				category2Value = 100;
				break;
			case 16:
				category2Value = 61;
				break;
			case 17:
				category2Value = 99;
				break;
			case 18:
				category2Value = 56;
				break;
			case 19:
				category2Value = 112;
				break;
			case 20:
				category2Value = 95;
				break;
			case 21:
				category2Value = 103;
				break;
			case 22:
				category2Value = 62;
				break;
			case 23:
				category2Value = 64;
				break;
			}
			break;
		case 15:
			switch (subSpinnerValue) {
			case 0:
				category2Value = 65;
				break;
			case 1:
				category2Value = 66;
				break;
			case 2:
				category2Value = 67;
				break;
			case 3:
				category2Value = 68;
				break;
			case 4:
				category2Value = 69;
				break;
			case 5:
				category2Value = 70;
				break;
			case 6:
				category2Value = 71;
				break;
			case 7:
				category2Value = 72;
				break;
			case 8:
				category2Value = 113;
				break;
			case 9:
				category2Value = 114;
				break;
			case 10:
				category2Value = 73;
				break;
			}
			break;
		}
		return category2Value;
	}
	
	static class ComplaintWriteHandler extends Handler {
		private final WeakReference<ComplaintWriteActivity> complaintWriteAcivity;
		
		public ComplaintWriteHandler(ComplaintWriteActivity activity) {
			// TODO Auto-generated constructor stub
			complaintWriteAcivity = new WeakReference<ComplaintWriteActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			ComplaintWriteActivity activity = complaintWriteAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */
