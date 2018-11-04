package kr.ac.mju.mjuapp.community;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.http.HttpManager;
import kr.ac.mju.mjuapp.login.LoginManager;
import kr.ac.mju.mjuapp.network.NetworkManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.content.Intent;
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

public class CommunityWriteActivity extends FragmentActivity implements OnClickListener {
	private static final int SPINNER_ENABLE = 1;
	private static final int SPINNER_DISABLE = 2;
	
	public static final int UPLOAD_SUCCESS = 4;
	public static final int UPLOAD_FAIL = 5;
	
	private int spinnerMode;
	private String boardId;
	private String id;
	private String boardName;
	private String boardType;
	private String pdsCnt;
	private String pdsSize;
	
	private int whichArticle;
	private String subjectOfArticle;
	private String contentOfArticle;
	private String optionValue1;
	
	private String boardWriteUrl = "http://www.mju.ac.kr/board/boardWriteExecute.mbs";
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private CommunityWriteHandler communityWriteHandler;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // TODO Auto-generated method stub
	    setContentView(R.layout.community_write_layout);
	    init();
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
		Intent intent = getIntent();
		whichArticle = intent.getIntExtra("whichArticle", -1);
		if (whichArticle != -1) {
			setGeneralParamValues();
			setParamValues();
		} else {
			Toast.makeText(getBaseContext(), getString(R.string.getting_page_info_fail), 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		setStdName();
		setSpinnerSettings();
		((Button)findViewById(R.id.community_write_submit_btn)).setOnClickListener(this);
		
		optionValue1 = "";
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		communityWriteHandler = new CommunityWriteHandler(CommunityWriteActivity.this);
	}
	
	private void setStdName() {
		// TODO Auto-generated method stub
		String name = getStdName();
		EditText nameEditText = (EditText)findViewById(R.id.community_write_writer_edittext);
		nameEditText.setText(name);
		nameEditText.setFocusable(false);
		nameEditText.setClickable(false);
	}
	
	private void setSpinnerSettings() {
		// TODO Auto-generated method stub
		Spinner communitySpinner = (Spinner) findViewById(R.id.community_write_community_spinner);
		ArrayAdapter<CharSequence> communitySpinnerAdapter;
		
		if (spinnerMode == SPINNER_ENABLE) {
			setSpinnerAdapter();
			setSpinnerListener();
		} else if (spinnerMode == SPINNER_DISABLE) {
			communitySpinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_no_choice, R.layout.mjuspinner_item_layout);
			communitySpinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			communitySpinner.setAdapter(communitySpinnerAdapter);
			communitySpinner.setSelection(0);
			communitySpinner.setEnabled(false);
			
			setSubSpinnerDisable();
		}
	}

	private void setSubSpinnerDisable() {
		// TODO Auto-generated method stub
		Spinner communitySubSpinner = (Spinner) findViewById(R.id.community_write_community_spinner_sub);
		ArrayAdapter<CharSequence> communitySubSpinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
				R.array.community_write_spinner_no_choice, R.layout.mjuspinner_item_layout);
		communitySubSpinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
		communitySubSpinner.setAdapter(communitySubSpinnerAdapter);
		communitySubSpinner.setEnabled(false);
	}
	
	private void setSpinnerListener() {
		// TODO Auto-generated method stub
		Spinner communitySpinner = (Spinner) findViewById(R.id.community_write_community_spinner);
	
		communitySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View view, int position, long id) {
				// TODO Auto-generated method stub
				
				Spinner communitySubSpinner = (Spinner) findViewById(R.id.community_write_community_spinner_sub);
				ArrayAdapter<CharSequence> communitySubSpinnerAdapter = null;
				
				if (whichArticle == 4) { 
					//�숈븘由�
					if (position == 0) {	//�좏깮 �덊븳 寃쎌슦
						optionValue1 = "";
						setSubSpinnerDisable();
					} else if (position == 1) { //醫낃탳
						optionValue1 = "0002";
						setSubSpinnerDisable();
					} else if (position == 2) { //�ъ쭊
						optionValue1 = "0003";
						communitySubSpinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
								R.array.community_write_sub_spinner_array_club_picture, 
								R.layout.mjuspinner_item_layout);
					} else if (position == 3) { //�뚯븙
						optionValue1 = "0004";
						communitySubSpinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
								R.array.community_write_sub_spinner_array_club_music, 
								R.layout.mjuspinner_item_layout);
					} else if (position == 4) { //�대룞
						optionValue1 = "0005";
						communitySubSpinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
								R.array.community_write_sub_spinner_array_club_workout, 
								R.layout.mjuspinner_item_layout);
					} else if (position == 5) { //臾명븰
						optionValue1 = "0006";
						communitySubSpinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
								R.array.community_write_sub_spinner_array_club_literature, 
								R.layout.mjuspinner_item_layout);
					} else if (position == 6) { //�ы뻾
						optionValue1 = "0007";
						setSubSpinnerDisable();
					} else if (position == 7) { //�숈뾽
						optionValue1 = "0008";
						communitySubSpinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
								R.array.community_write_sub_spinner_array_club_study, 
								R.layout.mjuspinner_item_layout);
					} else if (position == 8) { //痍⑥뾽
						optionValue1 = "0009";
						communitySubSpinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
								R.array.community_write_sub_spinner_array_club_job, 
								R.layout.mjuspinner_item_layout);
					} else { 					// 遊됱궗
						optionValue1 = "0010";
						communitySubSpinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
								R.array.community_write_sub_spinner_array_club_service, 
								R.layout.mjuspinner_item_layout);
					}
				} else if (whichArticle == 6) {
					//紐낆��앹씤
					if (position == 0) {
						optionValue1 = "";
					} else if (position == 1) {
						optionValue1 = "0002";
					} else if (position == 2) {
						optionValue1 = "0003";
					} else if (position == 3) {
						optionValue1 = "0004";
					} else if (position == 4) {
						optionValue1 = "0005";
					} else if (position == 5) {
						optionValue1 = "0006";
					} else if (position == 6) {
						optionValue1 = "0007";
					} else {
						optionValue1 = "0008";
					}
				} else if (whichArticle == 9) {
					//�꾨Ⅴ諛붿씠��/痍⑥뾽
					optionValue1 = "";
				} else if (whichArticle == 12) {
					//�ㅽ뵂 留덉폆
					optionValue1 = "";
				} else if (whichArticle == 13) {
					//遺꾩떎臾��쇳꽣
					optionValue1 = "";
				} else if (whichArticle == 14) {
					//二쇨굅�뺣낫
					optionValue1 = "";
				}
				
				if (communitySubSpinnerAdapter != null) {
					communitySubSpinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
					communitySubSpinner.setAdapter(communitySubSpinnerAdapter);
					communitySubSpinner.setSelection(0);
					communitySubSpinner.setEnabled(true);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {	}
		});
		
	}

	@SuppressLint("CutPasteId")
	private void setSpinnerAdapter() {
		// TODO Auto-generated method stub
		Spinner superSpinner = (Spinner) findViewById(R.id.community_write_community_spinner);
		Spinner subSpinner;
		ArrayAdapter<CharSequence> spinnerAdapter = null;
		
		switch (whichArticle) {
		case 4:
			//�숈븘由�
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_array_club, R.layout.mjuspinner_item_layout);
			spinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			superSpinner.setAdapter(spinnerAdapter);
			superSpinner.setSelection(0);
			break;
		case 6: //紐낆��앹씤
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_array_knowledge, R.layout.mjuspinner_item_layout);
			spinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			superSpinner.setAdapter(spinnerAdapter);
			superSpinner.setSelection(0);
			
			subSpinner = (Spinner) findViewById(R.id.community_write_community_spinner_sub);
			subSpinner.setPrompt(getResources().getString(R.string.community_write_subject));
			spinnerAdapter =  ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_sub_spinner_array_knowledge, 
					R.layout.mjuspinner_item_layout);
			spinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			subSpinner.setAdapter(spinnerAdapter);
			subSpinner.setSelection(0);
			break;
		case 9: //�꾨Ⅴ諛붿씠��痍⑥뾽
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_no_choice, R.layout.mjuspinner_item_layout);
			superSpinner.setAdapter(spinnerAdapter);
			superSpinner.setEnabled(false);
			
			subSpinner = (Spinner) findViewById(R.id.community_write_community_spinner_sub);
			subSpinner.setPrompt(getResources().getString(R.string.community_write_subject));
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_array_job, R.layout.mjuspinner_item_layout);
			spinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			subSpinner.setAdapter(spinnerAdapter);
			subSpinner.setSelection(0);
			break;
		case 12: //�ㅽ뵂留덉폆
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_no_choice, R.layout.mjuspinner_item_layout);
			superSpinner.setAdapter(spinnerAdapter);
			superSpinner.setEnabled(false);
			
			subSpinner = (Spinner) findViewById(R.id.community_write_community_spinner_sub);
			subSpinner.setPrompt(getResources().getString(R.string.community_write_subject));
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_array_market, R.layout.mjuspinner_item_layout);
			spinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			subSpinner.setAdapter(spinnerAdapter);
			subSpinner.setSelection(0);
			break;
		case 13: // 遺꾩떎臾��쇳꽣
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_no_choice, R.layout.mjuspinner_item_layout);
			superSpinner.setAdapter(spinnerAdapter);
			superSpinner.setEnabled(false);
			
			subSpinner = (Spinner) findViewById(R.id.community_write_community_spinner_sub);
			subSpinner.setPrompt(getResources().getString(R.string.community_write_subject));
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_array_loss, R.layout.mjuspinner_item_layout);
			spinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			subSpinner.setAdapter(spinnerAdapter);
			subSpinner.setSelection(0);
			break;
		case 14: //二쇨굅�뺣낫
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_no_choice, R.layout.mjuspinner_item_layout);
			superSpinner.setAdapter(spinnerAdapter);
			superSpinner.setEnabled(false);
			
			subSpinner = (Spinner) findViewById(R.id.community_write_community_spinner_sub);
			subSpinner.setPrompt(getResources().getString(R.string.community_write_subject));
			spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
					R.array.community_write_spinner_array_house, R.layout.mjuspinner_item_layout);
			spinnerAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
			subSpinner.setAdapter(spinnerAdapter);
			subSpinner.setSelection(0);
			break;
		}
	}

	private String getStdName() {
		// TODO Auto-generated method stub
		SharedPreferences pref = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		String name = pref.getString(getString(R.string.pref_std_name), "");
		
		return name;
	}

	private void setGeneralParamValues() {
		// TODO Auto-generated method stub
		boardType  = "01";
		pdsCnt = "5";
		pdsSize = "5242880";
	} 

	private void setParamValues() {
		// TODO Auto-generated method stub
		switch (whichArticle) {
		case 0:
			boardId = "10487";
			id = "mjukr_060101000000";
			boardName = getString(R.string.community_sidemenu_our_history);
			spinnerMode = SPINNER_DISABLE;
			break;
		case 1:
			boardId = "10495";
			id = "mjukr_060103000000";
			boardName = getString(R.string.community_sidemenu_praise);
			spinnerMode = SPINNER_DISABLE;
			break;
		case 2: 
			boardId = "10512";
			id = "mjukr_060105000000";
			boardName = getString(R.string.community_sidemenu_study);
			spinnerMode = SPINNER_DISABLE;
			break;
		case 3:
			boardId = "103023";
			id = "mjukr_060107000000";
			boardName = "명커뮤니티 명지광장 디딤돌";
			spinnerMode = SPINNER_DISABLE;
			break;
		case 4:
			boardId = "1976";
			id = "mjukr_060108000000";
			boardName = getString(R.string.community_sidemenu_club);
			spinnerMode = SPINNER_ENABLE;
			break;
		case 5:
			boardId = "10592";
			id = "mjukr_060109000000";
			boardName = getString(R.string.community_sidemenu_advertising);
			spinnerMode = SPINNER_DISABLE;
			break;
		case 6:
			boardId = "1954";
			id = "mjukr_060102000000";
			boardName = getString(R.string.community_sidemenu_intellectual);
			spinnerMode = SPINNER_ENABLE;
			break;
		case 8:
			boardId = "2012";
			id = "mjukr_060202000000";
			boardName = "";
			boardType = "02";
			pdsCnt = "10";
			pdsSize = "10485760"; 
			break;
		case 9:
			boardId = "10615";
			id = "mjukr_060302000000";
			boardName = getString(R.string.community_sidemenu_job);
			spinnerMode = SPINNER_ENABLE;
			break;
		case 10:
			boardId = "10647";
			id = "mjukr_060303000000";
			boardName = getString(R.string.community_sidemenu_trip);
			pdsCnt = "200";
			pdsSize = "209715200";
			spinnerMode = SPINNER_DISABLE;
			break;
		case 11:
			boardId = "10676";
			id = "mjukr_060304000000";
			boardName = getString(R.string.community_sidemenu_exhibition);
			spinnerMode = SPINNER_DISABLE;
			break;
		case 12:
			boardId = "10687";
			id = "mjukr_060305000000";
			boardName = getString(R.string.community_sidemenu_market);
			spinnerMode = SPINNER_ENABLE;
			break;
		case 13:
			boardId = "10710";
			id = "mjukr_060306000000";
			boardName = getString(R.string.community_sidemenu_loss);
			spinnerMode = SPINNER_ENABLE;
			break;
		case 14:
			boardId = "10846";
			id = "mjukr_060307000000";
			boardName = getString(R.string.community_sidemenu_house);
			spinnerMode = SPINNER_ENABLE;
			break;
		case 18:
			boardId = "10417";
			id = "mjukr_060406000000";
			boardName = "";
			boardType = "02";
			pdsCnt = "10";
			pdsSize = "10485760";
			break;
		default:
			boardId = "";
			id = "";
			boardName = "";
			break;
		}
	}
	
	private boolean checkParam() {
		// check editText parameters
		subjectOfArticle = ((EditText) findViewById(R.id.community_write_subject_edittext)).getText().toString();
		if (subjectOfArticle.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.write_article_title_hint), Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.community_write_subject_edittext)).requestFocus();
			return false;
		}
		
		contentOfArticle = ((EditText) findViewById(R.id.community_write_content_edittext)).getText().toString();
		if (contentOfArticle.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.community_input_data), Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.community_write_content_edittext)).requestFocus();
			return false;
		}
		return true;
	}
	
	private void writeBoard() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpManager httpManager = new HttpManager();
				httpManager.init();
				httpManager.initSSL();
				
				try {
					httpManager.setHttpPost(getBoardWriteParamMap(), boardWriteUrl, HttpManager.UTF_8);
					httpManager.setCookieHeader(LoginManager.getCookies(getApplicationContext()));
					HttpResponse boardResponse = null;
					boardResponse = httpManager.executeHttpPost();
					HttpEntity entity = boardResponse.getEntity();
					
					if (entity != null) {
						communityWriteHandler.sendEmptyMessage(UPLOAD_SUCCESS);
					} else {
						communityWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
					}
				} catch (Exception e) { 
					// TODO: handle exception
					communityWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
				} 
			}
		}).start();
	} 
	
	private HashMap<String, String> getBoardWriteParamMap() {
		// set parameters
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		// get contents
		SharedPreferences pref = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		String name = pref.getString(getString(R.string.pref_std_name), "");
		String stdId = pref.getString(getString(R.string.pref_key_user_id), "");
		
		paramsMap.put("command", "write");
		paramsMap.put("boardSeq", "");
		paramsMap.put("boardRecord.boardConfig.boardId", boardId);
		paramsMap.put("boardId", boardId);
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
		paramsMap.put("boardRecord.boardType", boardType);
		paramsMap.put("filesize", "0");
		paramsMap.put("pdsCnt", pdsCnt);
		paramsMap.put("pdsSize", pdsSize);
		paramsMap.put("attechFile", pdsSize);
		
		paramsMap.put("spage", "1");
		paramsMap.put("boardType", boardType);
		paramsMap.put("boardRecord.remoteIp", ""); 
		paramsMap.put("listType", boardType);
		paramsMap.put("boardRecord.userId", stdId); // �숇쾲
		paramsMap.put("aliasYn", "N");
		paramsMap.put("boardRecord.editorYn", "Y");
		paramsMap.put("delFile", "");
		paramsMap.put("upLoadFileValue", "");
		paramsMap.put("upLoadFileText", "");
		
		paramsMap.put("chkBoxSeq", "");
		paramsMap.put("imsiDir", "");
		paramsMap.put("boardRecord.boardConfig.boardName", boardName);
		paramsMap.put("id", id);
		paramsMap.put("boardRecord.frontYn", "Y");
		paramsMap.put("boardRecord.title", subjectOfArticle); 
		paramsMap.put("boardRecord.tag", ""); 
		
		paramsMap.put("boardRecord.userName", name);
		paramsMap.put("boardRecord.contents", contentOfArticle); // �댁슜
		paramsMap.put("size_list", "");
		paramsMap.put("upFile", "");
		
		int subSpinnerValue = ((Spinner) findViewById(R.id.community_write_community_spinner_sub))
				.getSelectedItemPosition();
		
		String optionValue2 = CommunityOptionValueManager.getOption2Value(optionValue1, subSpinnerValue, whichArticle); 
		paramsMap.put("boardRecord.categoryId", optionValue2);	//id="category_id"

		String mcategoryId = CommunityOptionValueManager.getMcategory(whichArticle, subSpinnerValue);
		paramsMap.put("boardRecord.mcategoryId", mcategoryId);	//id="mcategory_id"
			
		return paramsMap;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.community_write_submit_btn:
			if (checkParam()) {
				if (NetworkManager.checkNetwork(getApplicationContext())) {
					progressDialog.show(fragmentManager, "");
					writeBoard();
				}
			}
			break;
		default:
			break;
		}
	}
	
	static class CommunityWriteHandler extends Handler {
		private final WeakReference<CommunityWriteActivity> communitytWriteAcivity;
		
		public CommunityWriteHandler(CommunityWriteActivity activity) {
			// TODO Auto-generated constructor stub
			communitytWriteAcivity = new WeakReference<CommunityWriteActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			CommunityWriteActivity activity = communitytWriteAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
