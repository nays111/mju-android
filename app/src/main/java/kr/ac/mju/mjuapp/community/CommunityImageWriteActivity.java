package kr.ac.mju.mjuapp.community;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUAlertDialog;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.http.HttpManager;
import kr.ac.mju.mjuapp.login.LoginManager;
import kr.ac.mju.mjuapp.network.NetworkManager;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.CursorLoader;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 커뮤니티의 이미지 게시판(사각컷 이야기, 캠퍼스 이미지)에 게시글을 등록하는 액티비티 
 * @author Hs
 */

public class CommunityImageWriteActivity extends FragmentActivity implements OnClickListener {
	
	public static final int UPLOAD_SUCCESS = 4;
	public static final int UPLOAD_FAIL = 5;
	
	private final long MAX_FILE_SIZE = 10 * 1024 * 1024;

	private int whichArticle;
	private String boardId; 
	private String id;
	private String subjectOfArticle;
	
	private String fileName = null;
	private String uploadFileSize = null;
	private String uploadFileCode = null;
	private String imsiDir = null;
	private String insertedImagePath = null;
	
	private int orgImgWidth;
	private int orgImgHeight;
	private Uri captureUri;
	private Uri imageUri;
	private String fileBasePath;

	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MJUAlertDialog alertDialog;
	private CommunityImageWriteHandler communityImageWriteHandler;
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // TODO Auto-generated method stub
	    setContentView(R.layout.community_write_image_layout);
	    
	    init();
	    
	    findViewById(R.id.community_img_write_file_choose_btn).setOnClickListener(this);
		findViewById(R.id.community_img_write_submit_btn).setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ImageView selectedImageView = (ImageView) findViewById(R.id.community_img_write_selected_imgview);
		if (((BitmapDrawable) selectedImageView.getDrawable()) != null)
			((BitmapDrawable) selectedImageView.getDrawable()).getBitmap().recycle();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			ImageView iv = ((ImageView) findViewById(R.id.community_img_write_selected_imgview));
			Bitmap bitmap = null;
			AssetFileDescriptor afd = null;
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inDensity = DisplayMetrics.DENSITY_HIGH;
			opt.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
			opt.inSampleSize = 4;
			BitmapDrawable drawable;
			
			try {
				if (requestCode == MJUConstants.TAKE_GALLERY) {
					imageUri = data.getData();
					if (imageUri == null) {
						imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; 
					}
					fileName = getFilenameFromUri(imageUri);
					afd = getContentResolver().openAssetFileDescriptor(imageUri, "r");
				} else if (requestCode == MJUConstants.TAKE_CAMERA) {
					// 카메라 호출하는 메소드에서 이미 fileName, uri를 대입해놨음.
					afd = getContentResolver().openAssetFileDescriptor(captureUri, "r");
				}
				if (checkFileSize(fileName)) {
					bitmap = BitmapFactory.decodeFileDescriptor(afd.getFileDescriptor(), null, opt);
					if (bitmap != null) {
						drawable = new BitmapDrawable(getResources(), bitmap);
						orgImgWidth = bitmap.getWidth();
						orgImgHeight = bitmap.getHeight();
						iv.setImageDrawable(drawable);
						iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
						iv.setTag(fileName);
						iv.setVisibility(View.VISIBLE);
						((TextView) findViewById(R.id.community_img_write_file_state_textview)).
							setVisibility(View.GONE);
						
						addFileNameTextView();
					} else {
						Toast.makeText(getBaseContext(), getString(R.string.
								community_write_aritlce_no_pic), Toast.LENGTH_SHORT).show();
						return; 
					}
				}
			} catch (OutOfMemoryError e) {
				// TODO: handle exception
			} catch (FileNotFoundException e) {
				// TODO: handle exception
				Toast.makeText(getBaseContext(), getString(R.string.
						community_write_aritlce_no_pic), Toast.LENGTH_SHORT).show();
				return;
			} 
		}
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
	
	public void pickUpPicture() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, MJUConstants.TAKE_GALLERY);
	}
	
	public void takePicture() {
		// 사진찍으면 임시 저장할 경로
		fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ getString(R.string.uri_file_save_directory) + getString(R.string.picture_folder_name);
		SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
		String currDate = yyyyMMddHHmmss.format(new Date());
		fileName = fileBasePath + currDate.hashCode() + ".jpg";
		captureUri = Uri.fromFile(new File(fileName));
		
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, captureUri);
		startActivityForResult(intent, MJUConstants.TAKE_CAMERA);
	}

	private void init() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		whichArticle = intent.getIntExtra("whichArticle", -1);
		if (whichArticle != -1) {
			setParamValues();
		} else {
			Toast.makeText(getBaseContext(), getString(R.string.getting_page_info_fail), 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		setStdName();
		fileName = new String();
		uploadFileSize = new String();
		uploadFileCode = new String();
		imsiDir = new String();
		insertedImagePath = new String();
		orgImgWidth = 0;
		orgImgHeight = 0;
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		communityImageWriteHandler = new CommunityImageWriteHandler(CommunityImageWriteActivity.this);
	}
	
	private void setParamValues() {
		// TODO Auto-generated method stub
		
		if (whichArticle == 8) {
			boardId = "2012";
			id = "mjukr_060202000000";
		} else if (whichArticle == 18) {
			boardId = "10417";
			id = "mjukr_060406000000";
		}
	}

	private void setStdName() {
		// TODO Auto-generated method stub
		String name = getStdName();
		EditText nameEditText = (EditText)findViewById(R.id.community_img_write_writer_edittext);
		nameEditText.setText(name);
		nameEditText.setFocusable(false);
		nameEditText.setClickable(false);
	}
	
	private String getStdName() {
		// TODO Auto-generated method stub
		SharedPreferences pref = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		String name = pref.getString(getString(R.string.pref_std_name), "");
		
		return name;
	}
	
	/**
	 * 
	 * @param uri
	 * @return String
	 * @author
	 */
	private String getFilenameFromUri(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		String sortOrder = MediaStore.Images.Media._ID + " DESC";
		CursorLoader loader = new CursorLoader(getApplicationContext(), uri, proj, null, null, sortOrder);
		Cursor cursor = loader.loadInBackground();
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();

		return cursor.getString(index);
	}
	
	/**
	 * @param fileName
	 * @return
	 */
	private boolean checkFileSize(String fileName) {
		File file = new File(fileName);
		if (file.length() > MAX_FILE_SIZE) {
			Toast.makeText(getBaseContext(), getString(R.string.file_size_10m), Toast.LENGTH_SHORT).show();
			return false;
		} else
			return true;
	}
		
	/**
	 * @param fileName
	 */
	private void addFileNameTextView() {
		// get parent layout
		LinearLayout fileListLayout = (LinearLayout) findViewById(R.id.community_img_write_filelist_layout);
		// set textview
		TextView tv = new TextView(this);
		String tempFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		tv.setText(tempFileName);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
		tv.setTextColor(getResources().getColor(android.R.color.black));
		tv.setPadding(5, 7, 0, 7);
		tv.setBackgroundResource(R.drawable.green_write_filename_selector);
		// set layout
		LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, 0.0f);
		tv.setLayoutParams(tvParams);
		// add view to parent
		fileListLayout.addView(tv, fileListLayout.getChildCount() - 1);
		// set onclick listener to delete
		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				final TextView tv = (TextView) v;
				new AlertDialog.Builder(CommunityImageWriteActivity.this)
						.setTitle(getString(R.string.del_picture))
						.setMessage(getResources().getString(R.string.community_write_file_delete_content))
						.setCancelable(true)
						.setPositiveButton(getString(R.string.check), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								// remove
								fileName = "";
								
								((ImageView) findViewById(R.id.community_img_write_selected_imgview))
								.setVisibility(ImageView.GONE);
								((TextView) findViewById(R.id.community_img_write_file_state_textview))
								.setVisibility(View.VISIBLE);
								
								// remove textview from parent layout
								((LinearLayout) findViewById(R.id.community_img_write_filelist_layout))
								.removeView(tv);
							}
						}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								// do nothing
							}
						}).show();
			}
		});
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
					Vector<String> tagNames = new Vector<String>();
					tagNames.add(HTMLElementName.HTML);
					/**************************
					 * upLoadImage attached
					 **************************/
					// set httpPost
					httpManager.setHttpPost("https://www.mju.ac.kr/common/upFileExecute1.mbs");
					// set cookies
					httpManager.setCookieHeader(LoginManager.getCookies(getApplicationContext()));
					httpManager.initFilePost();
					
					File file = new File(fileName);
					ContentBody cbFile = new FileBody(file, "image/jpeg");
					
					// set MultipartEntity
					MultipartEntity multipartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null,
							Charset.forName("UTF-8"));
					multipartContent.addPart("file1", cbFile);
					multipartContent.addPart("boardType", new StringBody("02"));
					multipartContent.addPart("boardId", new StringBody(boardId));
					multipartContent.addPart("attachFile", new StringBody("10"));
					multipartContent.addPart("imgid", new StringBody(""));
					multipartContent.addPart("professorId", new StringBody(""));
					multipartContent.addPart("command", new StringBody("imgUpload"));
					
					httpManager.setEntity(multipartContent);
					// httpResponse
					HttpResponse fileResponse = null;
					// excute
					fileResponse = httpManager.executeHttpPost();
					// get Status
					StatusLine fileStatus = fileResponse.getStatusLine();
					if (fileStatus.getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = fileResponse.getEntity();
						// upload success
						if (entity != null) {
							// parse result
							HashMap<String, List<Element>> elementMap = httpManager.
									getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
							getUploadFileName(elementMap);
						}
					} else {
						communityImageWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
						return;
					}
					
					/**************************
					 * upLoadImage inserted
					 **************************/
					// set httpPost
					httpManager.setHttpPost("https://www.mju.ac.kr/common/innoditorExecute.mbs?uploadpath=/upload/board/2012/editor/");
					// set cookies
					httpManager.setCookieHeader(LoginManager.getCookies(getApplicationContext()));
					// init filePost
					httpManager.initFilePost();
					
					// set multipartEntity
					multipartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, 
							null, Charset.forName("UTF-8"));
					multipartContent.addPart("fileUpload", cbFile);
					String tempFileName = fileName.substring(fileName.lastIndexOf("/") + 1);
					multipartContent.addPart("orgFile", new StringBody(tempFileName));
					multipartContent.addPart("hdnUploadType", new StringBody("1"));
					httpManager.setEntity(multipartContent);
					// excute
					fileResponse = httpManager.executeHttpPost();
					// get Status
					fileStatus = fileResponse.getStatusLine();
					if (fileStatus.getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = fileResponse.getEntity();
						// upload success
						if (entity != null) {
							// parse result
							HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
							// get insertedImageFilePath
							getInsertImageFileName(elementMap);
						}
					} else {
						communityImageWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
						return;
					}
					
					/**************************
					 * write board
					 **************************/
					// set parameters
					HashMap<String, String> paramsMap = getBoardWriteParamMap();
					// set httpPost
					httpManager.setHttpPost(paramsMap, "https://www.mju.ac.kr/board/albumWriteExecute.mbs", HttpManager.UTF_8);
					// httpResponse
					HttpResponse boardResponse = null;
					// execute
					boardResponse = httpManager.executeHttpPost();
					// get response
					HttpEntity entity = boardResponse.getEntity();
					// upload success
					if (entity != null)
						communityImageWriteHandler.sendEmptyMessage(UPLOAD_SUCCESS);
					// upload fail
					else
						communityImageWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					communityImageWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					communityImageWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		}).start();
	}
	
	/**
	 * @param entity
	 * @param httpManager
	 * @return
	 */
	private void getUploadFileName(HashMap<String, List<Element>> elementMap) {
		List<Element> elementsList = elementMap.get(HTMLElementName.HTML);
		Element htmlElement = elementsList.get(0);
		String temp = htmlElement.getContent().toString();
		temp = temp.substring(temp.indexOf("filename1"));
		
		/**
		 * 이 기능 처음 개발되었을 당시 관련 html 문서에 싱글 쿼테이션이 사용되었나 봄.
		 * 정확히 언제부터인지는 모르겠으나 2013년 하반기에 확인해보니 더블 쿼테이션이 사용되었음
		 * 그래서 싱글/ 더블 쿼테이션이 사용되었는지 검사한 한다음에  사용되어진 문자열을 이용해서 subString을 이용하는방법으로 버그수정.
		 * by Hs.  
		 */
		String deviderStr = "";
		if (temp.contains("';")) {	// html 태그에  싱글쿼테이션이 사용되었으면
			deviderStr = "'";	
		} else if (temp.contains("\"")) { // html 태그에  더블 쿼테이션이 사용되었으면
			deviderStr = "\"";
		}
		
		imsiDir = temp.substring(temp.indexOf(deviderStr) + 1, temp.indexOf("/"));
		temp = temp.substring(temp.indexOf("fileSize"));
		uploadFileSize = temp.substring(temp.indexOf(deviderStr) + 1, temp.indexOf(deviderStr +";")).trim();
		temp = temp.substring(temp.indexOf("fileCode"));
		uploadFileCode = temp.substring(temp.indexOf(deviderStr) + 1, temp.indexOf(deviderStr +";"));
	}
	
	/**
	 * @param elementMap
	 */
	private void getInsertImageFileName(HashMap<String, List<Element>> elementMap) {
		int uploadWidth, uploadHeight;
		uploadWidth = (orgImgWidth > 400) ? 400 : orgImgWidth;
		uploadHeight = (orgImgWidth > 400) ? (int) ((float) orgImgHeight * (float) ((float) 400 / (float) orgImgWidth)) : orgImgHeight;
		List<Element> elementsList = elementMap.get(HTMLElementName.HTML);
		Element htmlElement = elementsList.get(0);
		String temp = htmlElement.getContent().toString();
		temp = temp.substring(temp.indexOf("fnUploadResult"));
		temp = temp.substring(temp.indexOf("'") + 1, temp.indexOf("');"));
		insertedImagePath = "<br><img src=\"http://www.mju.ac.kr" + temp + "\" width=\"" + uploadWidth + "\" height=\"" + uploadHeight
				+ "\"/>";
	}
	
	private HashMap<String, String> getBoardWriteParamMap() {
		// set parameters
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		// get contents
		SharedPreferences pref = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		String name = pref.getString(getString(R.string.pref_std_name), "");
		String stdId = pref.getString(getString(R.string.pref_key_user_id), "");
		
		paramsMap.put("command", 							"write");
		paramsMap.put("boardSeq", 							"");
		paramsMap.put("boardRecord.boardConfig.boardId",	boardId);
		paramsMap.put("boardId", 							boardId);
		paramsMap.put("boardRecord.boardSeq", 				"");
		paramsMap.put("boardRecord.refSeq", 				"0");
		paramsMap.put("boardRecord.famSeq", 				"0");
		paramsMap.put("boardRecord.pos", 					"0");
		paramsMap.put("boardRecord.depth",					"0");
		paramsMap.put("boardRecord.readCnt",				"0"); 
		
		paramsMap.put("regDate", 							"");
		paramsMap.put("boardRecord.emailReceive", 			"");
		paramsMap.put("boardRecord.basketYn", 				"");
		paramsMap.put("boardRecord.commentCnt", 			"0");
		paramsMap.put("boardRecord.fileCnt", 				"1");
		paramsMap.put("boardRecord.boardType",				"02");
		paramsMap.put("filesize", 							uploadFileSize);
		paramsMap.put("pdsCnt", 							"10");
		paramsMap.put("pdsSize", 							"10485760");
		paramsMap.put("attechFile", 						"10485760");
		
		paramsMap.put("spage", 								"1");
		paramsMap.put("boardType", 							"02");
		paramsMap.put("boardRecord.remoteIp", 				""); 
		paramsMap.put("listType", 							"02");
		paramsMap.put("boardRecord.userId", 				stdId);
		paramsMap.put("aliasYn", 							"N");
		paramsMap.put("boardRecord.editorYn", 				"Y");
		paramsMap.put("delFile", 							"");
		paramsMap.put("upLoadFileValue",					"/" + uploadFileCode);
		paramsMap.put("upLoadFileText", 					"/" + fileName.substring(fileName.lastIndexOf("/") + 1));
		
		paramsMap.put("chkBoxSeq", 							"");
		paramsMap.put("imsiDir", 							imsiDir);
		paramsMap.put("boardRecord.categoryId", 			"");	
		paramsMap.put("boardRecord.mcategoryId", 			"");	
		paramsMap.put("mcategoryId", 						"");	
		paramsMap.put("id", 								id);
		paramsMap.put("boardRecord.frontYn", 				"Y");
		paramsMap.put("boardRecord.title", 					subjectOfArticle); 
		
		paramsMap.put("boardRecord.userName", 				name);
		paramsMap.put("boardRecord.contents", 				insertedImagePath + ""); 
		paramsMap.put("size_list", 							"/" + fileName.substring(fileName.lastIndexOf("/") + 1) + "***" + uploadFileSize);
		paramsMap.put("upFile", 							uploadFileCode);
		paramsMap.put("boardRecord.orgFile", 				fileName.substring(fileName.lastIndexOf("/") + 1));
		paramsMap.put("boardRecord.renameFile", 			"");
		paramsMap.put("thumbnailFileSeq", 					uploadFileCode);
		
		return paramsMap;
	}
	
	private boolean checkParam() {
		// check editText parameters
		subjectOfArticle = ((EditText) findViewById(R.id.community_img_write_subject_edittext)).getText().toString();
		if (subjectOfArticle.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.write_article_title_hint), Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.community_img_write_subject_edittext)).requestFocus();
			return false;
		}
		
		if (fileName.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.select_pic), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
	}

	@Override  
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.community_img_write_file_choose_btn:
			if (!fileName.equals("")) {
				Toast.makeText(getBaseContext(), getString(R.string.select_pic_only_one), Toast.LENGTH_SHORT)
					.show();
				return;
			}
			alertDialog  = MJUAlertDialog.newInstance(MJUConstants.COMMUNITY_IMG_PICTURE_SELECT_DIALOG, 
					R.string.community_write_aritlce_select_pic, 0, 
					R.array.green_write_picture_select_list);
			alertDialog.show(fragmentManager, "");
			break;
		case R.id.community_img_write_submit_btn:
			if (checkParam()) {
				if (NetworkManager.checkNetwork(getApplicationContext())) {
					progressDialog.show(fragmentManager, "");
					writeBoard();
				}
			} 
			break;
		}
	}
	
	static class CommunityImageWriteHandler extends Handler {
		private final WeakReference<CommunityImageWriteActivity> communityImgtWriteAcivity;
		
		public CommunityImageWriteHandler(CommunityImageWriteActivity activity) {
			// TODO Auto-generated constructor stub
			communityImgtWriteAcivity = new WeakReference<CommunityImageWriteActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			CommunityImageWriteActivity activity = communityImgtWriteAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
