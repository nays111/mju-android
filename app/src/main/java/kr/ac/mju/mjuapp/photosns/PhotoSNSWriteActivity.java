package kr.ac.mju.mjuapp.photosns;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @author davidkim
 *
 */
public class PhotoSNSWriteActivity extends FragmentActivity implements OnClickListener {
	
	private final int UPLOAD_SUCCESS = 20;
	private final int UPLOAD_FAIL = 21;

	private String fileName = null;
	private String uploadFileSize = null;
	private String uploadFileCode = null;
	private String imsiDir = null;
	private String insertedImagePath = null;
	
	private Uri captureUri;
	private Uri imageUri;
	private String fileBasePath;
	private final long MAX_FILE_SIZE = 10 * 1024 * 1024;

	private int orgImgWidth;
	private int orgImgHeight;
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MJUAlertDialog alertDialog;
	private PhotoSNSWriteHandler photoSNSWriteHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photosns_write_layout);

		init();
		findViewById(R.id.photosns_write_file_choose_btn).setOnClickListener(this);
		findViewById(R.id.photosns_write_submit_btn).setOnClickListener(this);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			ImageView iv = ((ImageView) findViewById(R.id.photosns_write_preview));
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
			finish();
			break;
		case UPLOAD_FAIL:
			progressDialog.dismiss();
			Toast.makeText(getBaseContext(), getString(R.string.write_fail), Toast.LENGTH_SHORT).show();
			// error weak signal msg
			Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_network_error_weak_signal),
					Toast.LENGTH_SHORT).show();
			// finish();
			break;
		}
	}
	
	/**
	 * 
	 */
	private void init() {
		fileName = new String();
		uploadFileSize = new String();
		uploadFileCode = new String();
		imsiDir = new String();
		insertedImagePath = new String();
		orgImgWidth = 0;
		orgImgHeight = 0;
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		photoSNSWriteHandler = new PhotoSNSWriteHandler(PhotoSNSWriteActivity.this);
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
	/**
	 * @param uri
	 * @return   
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
	 * 
	 */
	private void writeBoard() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// create httpManager
				HttpManager httpManager = new HttpManager();
				// init
				httpManager.init();
				// initSSL
				httpManager.initSSL();
				try {
					// parse element
					Vector<String> tagNames = new Vector<String>();
					tagNames.add(HTMLElementName.HTML);
					/**************************
					 * upLoadImage attached
					 **************************/
					// set httpPost
					httpManager.setHttpPost("https://www.mju.ac.kr/common/upFileExecute1.mbs");
					// set cookies
					httpManager.setCookieHeader(getCookies());
					// init filePost
					httpManager.initFilePost();
					// set file
					File file = new File(fileName);
					ContentBody cbFile = new FileBody(file, "image/jpeg");
					// set MultipartEntity
					MultipartEntity multipartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null,
							Charset.forName("UTF-8"));
					multipartContent.addPart("file1", cbFile);
					multipartContent.addPart("boardType", new StringBody("02"));
					multipartContent.addPart("boardId", new StringBody("2012"));
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
							HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
									entity, tagNames, HttpManager.UTF_8);
							getUploadFileName(elementMap);
						}
					} else {
						photoSNSWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
						return;
					}
					/**************************
					 * upLoadImage inserted
					 **************************/
					// set httpPost
					httpManager.setHttpPost("https://www.mju.ac.kr/common/innoditorExecute.mbs?uploadpath=/upload/board/2012/editor/");
					// set cookies
					httpManager.setCookieHeader(getCookies());
					// init filePost
					httpManager.initFilePost();
					// set multipartEntity
					multipartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
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
							HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
									entity, tagNames, HttpManager.UTF_8);
							// get insertedImageFilePath
							getInsertImageFileName(elementMap);
						}
					} else {
						photoSNSWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
						return;
					}
					/**************************
					 * write board
					 **************************/
					// set parameters
					HashMap<String, String> paramsMap = getBoardWriteParamMap();
					// set httpPost
					httpManager.setHttpPost(paramsMap, "https://www.mju.ac.kr/board/albumWriteExecute.mbs", 
							HttpManager.UTF_8);
					// httpResponse
					HttpResponse boardResponse = null;
					// execute
					boardResponse = httpManager.executeHttpPost();
					// get response
					HttpEntity entity = boardResponse.getEntity();
					// upload success
					if (entity != null)
						photoSNSWriteHandler.sendEmptyMessage(UPLOAD_SUCCESS);
					// upload fail
					else
						photoSNSWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					photoSNSWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					photoSNSWriteHandler.sendEmptyMessage(UPLOAD_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		});
		thread.start();
	}

	/**
	 * @return
	 */
	private HashMap<String, String> getBoardWriteParamMap() {
		String title = ((EditText) findViewById(R.id.photosns_write_edittext)).getText().toString();
		// get contents
		SharedPreferences pref = getSharedPreferences(getString(R.string.pref_name), MODE_PRIVATE);
		String stdId = pref.getString(getString(R.string.pref_key_user_id), "");
		String name = pref.getString(getString(R.string.pref_std_name), "");
		// set parameters
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("command", "write");
		paramsMap.put("boardSeq", "");
		paramsMap.put("boardRecord.boardConfig.boardId", "2012");
		paramsMap.put("boardId", "2012");
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
		paramsMap.put("boardRecord.fileCnt", "1");
		paramsMap.put("boardRecord.boardType", "02");
		paramsMap.put("filesize", uploadFileSize);
		paramsMap.put("pdsCnt", "10");
		paramsMap.put("pdsSize", "10485760");
		paramsMap.put("attechFile", "10485760");
		paramsMap.put("spage", "1");
		paramsMap.put("boardType", "02");
		paramsMap.put("listType", "02");
		paramsMap.put("boardRecord.userId", stdId);
		paramsMap.put("aliasYn", "N");
		paramsMap.put("boardRecord.editorYn", "Y");
		paramsMap.put("delFile", "");
		paramsMap.put("upLoadFileValue", "/" + uploadFileCode);
		paramsMap.put("upLoadFileText", "/" + fileName.substring(fileName.lastIndexOf("/") + 1));
		paramsMap.put("chkBoxSeq", "");
		paramsMap.put("imsiDir", imsiDir);
		paramsMap.put("boardRecord.categoryId", "");
		paramsMap.put("boardRecord.mcategoryId", "");
		paramsMap.put("mcategoryId", "");
		paramsMap.put("id", "mjukr_060202000000");
		paramsMap.put("boardRecord.title", title);
		paramsMap.put("boardRecord.userName", name);
		paramsMap.put("boardRecord.contents", insertedImagePath);
		paramsMap.put("upFile", uploadFileCode);
		paramsMap.put("size_list", "/" + fileName.substring(fileName.lastIndexOf("/") + 1) + "***" + uploadFileSize);
		paramsMap.put("boardRecord.orgFile", fileName.substring(fileName.lastIndexOf("/") + 1));
		paramsMap.put("boardRecord.renameFile", "");
		paramsMap.put("thumbnailFileSeq", uploadFileCode);
		return paramsMap;
	}

	/**
	 * @return
	 */
	private boolean checkParam() {
		// get fileName
		if (fileName.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.select_pic), Toast.LENGTH_SHORT).show();
			return false;
		}
		String content = ((EditText) findViewById(R.id.photosns_write_edittext)).getText().toString();
		if (content.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.community_input_data), Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.photosns_write_edittext)).requestFocus();
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View v) { 
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.photosns_write_file_choose_btn:
			alertDialog = MJUAlertDialog.newInstance(MJUConstants.PHOTO_PICTURE_SELECT_DIALOG, 
					R.string.community_write_aritlce_select_pic, 0, 
					R.array.green_write_picture_select_list);
			alertDialog.show(fragmentManager, "");
			break;
		case R.id.photosns_write_submit_btn:
			if (checkParam() && NetworkManager.checkNetwork(PhotoSNSWriteActivity.this)) {
				progressDialog.show(fragmentManager, "");
				writeBoard();
			}
			break;
		}
	}

	/**
	 * @return
	 */
	private String getCookies() {
		// CookieSyncManager
		CookieSyncManager.createInstance(getApplicationContext());
		CookieManager cookieManager = CookieManager.getInstance();
		String cookieMobile01 = cookieManager.getCookie(getString(R.string.url_cookie_mobile_01));
		String cookieMobile02 = cookieManager.getCookie(getString(R.string.url_cookie_mobile_02));
		String cookieMain = cookieManager.getCookie(getString(R.string.url_cookie_main));
		return cookieMobile01 + "; " + cookieMobile02 + "; " + cookieMain;
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
	
	static class PhotoSNSWriteHandler extends Handler {
		private final WeakReference<PhotoSNSWriteActivity> photoSNSWriteAcivity;
		
		public PhotoSNSWriteHandler(PhotoSNSWriteActivity activity) {
			// TODO Auto-generated constructor stub
			photoSNSWriteAcivity = new WeakReference<PhotoSNSWriteActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			PhotoSNSWriteActivity activity = photoSNSWriteAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */
