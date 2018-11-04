package kr.ac.mju.mjuapp.green;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import kr.ac.mju.mjuapp.util.PixelConverter;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author davidkim
 *
 */
public class GreencampusWriteActivity extends FragmentActivity implements Runnable, OnClickListener {

	private final int UPLOAD_SUCCESS = 20;
	private final int UPLOAD_FAIL = 21;
	private final long MAX_FILE_SIZE = 10 * 1024 * 1024;

	/* �명뀗�몃줈 �섍꺼諛쏆쓣 �곸닔媛�*/
	private final int WRITE_SELF = 32; // 蹂몄씤議곗튂 寃곌낵 �좉퀬��	
	private final int WRITE_SUGGEST = 33; // �쒖븞�⑸땲��	
	private ArrayList<String> absoluteFileList;

	private String addImagePath = new String();
	
	private Uri captureUri;
	private Uri imageUri;
	private String fileBasePath;
	private String fileName;
	private int orgImgWidth;
	private int orgImgHeight;
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MJUAlertDialog alertDialog;
	private GreenCampusWriteHandler greenCampusWHandler;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.greencampus_write_layout);
		
		init();
		initLayout();

		((Button) findViewById(R.id.green_write_file_choose_btn)).setOnClickListener(this);
		((Button) findViewById(R.id.green_write_submit_btn)).setOnClickListener(this);
	}
	
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case UPLOAD_SUCCESS:
			progressDialog.dismiss();
			Toast.makeText(getBaseContext(), getString(R.string.write_success), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(GreencampusWriteActivity.this, GreencampusListActivity.class);
			startActivity(intent);
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
		greenCampusWHandler = new GreenCampusWriteHandler(GreencampusWriteActivity.this);
		
		// create addFileList
		absoluteFileList = new ArrayList<String>();

		// complaint spinner
		Spinner complaint = (Spinner) findViewById(R.id.green_write_complaint_spinner);
		ArrayAdapter<CharSequence> complaintAdapter = ArrayAdapter.createFromResource(getApplicationContext(),
				R.array.green_write_complaint_arr, R.layout.mjuspinner_item_layout);
		complaintAdapter.setDropDownViewResource(R.layout.mjuspinner_dropdown_item);
		complaint.setAdapter(complaintAdapter);

		// �명뀗�몄뿉���곸닔媛믪쓣 諛쏆븘��꽌 �대뼡 �좉퀬�몄� �먮떒.
		Intent intent = getIntent();
		int which = intent.getIntExtra("write_which", 0);
		int select = which; // default
				
		if (which == WRITE_SELF) {
			select = 0; // 3rd
		} else if (which == WRITE_SUGGEST) {
			select = 1; // 4th
		}
		complaint.setSelection(select); // �좉퀬 �댁슜��留욊쾶 �명똿
	}
	
	private void initLayout() {
		// TODO Auto-generated method stub
		PixelConverter pixelConveter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;
		
		rParams = (RelativeLayout.LayoutParams)findViewById(R.id.greencampus_write_header_icon).
				getLayoutParams();
		rParams.width = pixelConveter.getWidth(30);
		rParams.height = pixelConveter.getHeight(30);
		rParams.setMargins(0, 0, pixelConveter.getWidth(15), 0);
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ImageView selectedImageView = (ImageView) findViewById(R.id.green_write_selected_img_view);
		if (((BitmapDrawable) selectedImageView.getDrawable()) != null)
			((BitmapDrawable) selectedImageView.getDrawable()).getBitmap().recycle();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Uri uri = null;
			if (requestCode == MJUConstants.TAKE_GALLERY) {
				imageUri = data.getData();
				if (imageUri == null) {
					imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI; 
				}
				fileName = getFilenameFromUri(imageUri);
				uri = imageUri;
			} else if (requestCode == MJUConstants.TAKE_CAMERA) {
				// 移대찓���몄텧�섎뒗 硫붿냼�쒖뿉���대� fileName, uri瑜���엯�대넧��
				uri = captureUri;
			}
			
			if (uri != null && checkFileSize(fileName)) {
				setSelectedImage(uri);
				addFileNameTextView();
				absoluteFileList.add(fileName.trim());
				if (absoluteFileList.size() == 2) {
					Toast.makeText(getBaseContext(), "媛�옣 理쒓렐���좏깮���ъ쭊留�蹂댁뿬吏묐땲��", 
							Toast.LENGTH_SHORT).show();
				}
			} else {
//				Toast.makeText(getBaseContext(), getString(R.string.
//						community_write_aritlce_no_pic), Toast.LENGTH_SHORT).show();
				Toast.makeText(getBaseContext(), "tlqkf", Toast.LENGTH_SHORT).show();
				return; 
			}
		}
	}

	private void setSelectedImage(Uri uri) {
		// TODO Auto-generated method stub
		AssetFileDescriptor afd;
		Bitmap bitmap = null;
		try {
			afd = getContentResolver().openAssetFileDescriptor(uri, "r");
			BitmapFactory.Options opt = new BitmapFactory.Options();
			opt.inDensity = DisplayMetrics.DENSITY_HIGH;
			opt.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
			opt.inSampleSize = 4;
			bitmap = BitmapFactory.decodeFileDescriptor(afd.getFileDescriptor(), null, opt);
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (bitmap != null) {
			ImageView iv = ((ImageView) findViewById(R.id.green_write_selected_img_view));
			BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
			orgImgWidth = bitmap.getWidth();
			orgImgHeight = bitmap.getHeight();
			iv.setImageDrawable(drawable);
			iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			iv.setTag(fileName);
			iv.setVisibility(View.VISIBLE);
			((TextView) findViewById(R.id.green_write_file_state_textview)).setVisibility(View.GONE);
		} else {
//			Toast.makeText(getBaseContext(), getString(R.string.community_write_aritlce_no_pic), 
//					Toast.LENGTH_SHORT).show();
			Toast.makeText(getBaseContext(), "�먮윭", 
					Toast.LENGTH_SHORT).show();
			return; 
		}
		
	}

	public void pickUpPicture() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
		intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, MJUConstants.TAKE_GALLERY);
	}
	
	public void takePicture() {
		// �ъ쭊李띿쑝硫��꾩떆 ��옣��寃쎈줈
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
		Thread thread = new Thread(this);
		thread.start();
	}

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
			/**************************
			 * upLoadImage
			 **************************/
			for (int index = 0; index < absoluteFileList.size(); index++) {
				// set httpPost
				httpManager.setHttpPost("https://www.mju.ac.kr/common/innoditorExecute.mbs?uploadpath=/upload/board/7088978/editor/");
				httpManager.initFilePost();
				// set file
				File file = new File(absoluteFileList.get(index));
				ContentBody cbFile = new FileBody(file, "image/jpeg");

				// set MultipartEntity
				MultipartEntity multipartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName("UTF-8"));
				multipartContent.addPart("fileUpload", cbFile);
				String fileName = absoluteFileList.get(index).substring(absoluteFileList.get(index).lastIndexOf('/') + 1);
				multipartContent.addPart("orgFile", new StringBody(fileName));
				multipartContent.addPart("hdnUploadType", new StringBody("1"));
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
						// upLoadFileParsing
						parsingUploadImageFileName(entity);
					}
				} else {
					greenCampusWHandler.sendEmptyMessage(UPLOAD_FAIL);
					break;
				}
			}
			/**************************
			 * write board
			 **************************/
			// set parameters
			HashMap<String, String> paramsMap = getBoardWriteParamMap();
			// set httpPost
			httpManager.setHttpPost(paramsMap, getString(R.string.board_write_url), 
					HttpManager.UTF_8);
			// httpResponse
			HttpResponse boardResponse = null;
			// execute
			boardResponse = httpManager.executeHttpPost();
			// get response
			HttpEntity entity = boardResponse.getEntity();
			// upload success
			if (entity != null)
				greenCampusWHandler.sendEmptyMessage(UPLOAD_SUCCESS);
			// upload fail
			else
				greenCampusWHandler.sendEmptyMessage(UPLOAD_FAIL);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			greenCampusWHandler.sendEmptyMessage(UPLOAD_FAIL);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			greenCampusWHandler.sendEmptyMessage(UPLOAD_FAIL);
			e.printStackTrace();
		} finally {
			httpManager.shutdown();
		}
	}

	/**
	 * @return
	 */
	private HashMap<String, String> getBoardWriteParamMap() {
		// get contents
		String subject = ((EditText) findViewById(R.id.green_write_subject_edittext)).getText().toString().trim();
		String id = ((EditText) findViewById(R.id.green_write_writer_id_edittext)).getText().toString().trim();
		String phone = ((EditText) findViewById(R.id.green_write_phone_edittext)).getText().toString().trim();
		if (phone == null)
			phone = "";
		String content = ((EditText) findViewById(R.id.green_write_content_edittext)).getText().toString();
		int complaintSelectedIndex = ((Spinner) findViewById(R.id.green_write_complaint_spinner)).getSelectedItemPosition() + 3;
		// set parameters
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("boardRecord.title", subject);
		paramsMap.put("boardRecord.userName", id);
		paramsMap.put("boardRecord.addContents", phone);
		paramsMap.put("boardRecord.contents", content + addImagePath);		
		paramsMap.put("boardRecord.mcategoryId", String.valueOf(complaintSelectedIndex));
		paramsMap.put("command", "write");
		paramsMap.put("boardSeq", "");
		paramsMap.put("boardRecord.boardConfig.boardId", "7088978");
		paramsMap.put("boardId", "7088978");
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
		
		paramsMap.put("boardRecord.boardType", "07");
		paramsMap.put("boardRecord.remoteIp", "");
		paramsMap.put("filesize", "");
		paramsMap.put("pdsCnt", "20");
		paramsMap.put("pdsSize", "20971520");
		paramsMap.put("attechFile", "20971520");
		paramsMap.put("spage", "1");
		paramsMap.put("boardType", "07");
		paramsMap.put("listType", "07");
		paramsMap.put("boardRecord.userId", "");
		
		paramsMap.put("viewType", "");
		paramsMap.put("aliasYn", "Y");
		paramsMap.put("boardRecord.editorYn", "Y");
		paramsMap.put("upFile", "");
		paramsMap.put("upLoadFileValue", "");
		paramsMap.put("upLoadFileText", "");
		paramsMap.put("delFile", "");
		paramsMap.put("chkBoxSeq", "");
		paramsMap.put("imsiDir", "");
		paramsMap.put("boardRecord.boardConfig.boardName", "에너지낭비신고센터");
		
		paramsMap.put("mcategoryId", String.valueOf(complaintSelectedIndex));
		paramsMap.put("id", "mjukr_110500000000");
		paramsMap.put("boardRecord.frontYn", "Y");
		paramsMap.put("mcategory1", "");
		paramsMap.put("mcategory2", "");
		paramsMap.put("size_list", "");

		return paramsMap;
	}

	/**
	 * 
	 */
	private boolean checkParam() {
		// get subject
		String subject = ((EditText) findViewById(R.id.green_write_subject_edittext)).getText().toString();
		if (subject.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.write_article_title_hint), Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.green_write_subject_edittext)).requestFocus();
			return false;
		}
		// get id
		String id = ((EditText) findViewById(R.id.green_write_writer_id_edittext)).getText().toString();
		if (id.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.green_write_input_stdNum), Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.green_write_writer_id_edittext)).requestFocus();
			return false;
		}
		// get content
		String content = ((EditText) findViewById(R.id.green_write_content_edittext)).getText().toString();
		if (content.equals("")) {
			Toast.makeText(getBaseContext(), getString(R.string.community_input_data), Toast.LENGTH_SHORT).show();
			((EditText) findViewById(R.id.green_write_content_edittext)).requestFocus();
			return false;
		}
		return true;
	}

	/**
	 * @param entity
	 */
	private void parsingUploadImageFileName(HttpEntity entity) {
		// set SCRIPT element
		Vector<String> tagNames = new Vector<String>();
		tagNames.add(HTMLElementName.SCRIPT);
		// get SCRIPT element
		HttpManager httpManager = new HttpManager();

		HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
		List<Element> elementsList = elementMap.get(HTMLElementName.SCRIPT);

		// element check
		if (elementsList != null) {
			for (Element element : elementsList) {
				// var check
				String temp = element.getContent().toString().trim();
				temp = temp.replaceAll("\r\n", "").replace("\r", "").replace("\n", "");

				// get upLoad image Path
				if (temp.contains("fnUploadResult")) {
					// calculate upload width, height
					int uploadWidth, uploadHeight;
					uploadWidth = (orgImgWidth > 400) ? 400 : orgImgWidth;
					uploadHeight = (orgImgWidth > 400) ? (int)((float)orgImgHeight *(float)((float)400 / (float)orgImgHeight)) : orgImgHeight;
					// set upload image path
					temp = temp.substring(temp.indexOf("'") + 1);
					addImagePath += "<br><img src=\"http://www.mju.ac.kr" + temp.substring(0, temp.indexOf("'")) + "\" alt=\"\" width=\""
							+ uploadWidth + "\" height=\"" + uploadHeight + "\"/><br/>";
				}
			}
		}
	}
 
	/**
	 * @param fileName
	 */
	private void addFileNameTextView() {
		// get parent layout
		LinearLayout fileListLayout = (LinearLayout) findViewById(R.id.green_write_filelist_layout);
		// set textview
		TextView tv = new TextView(this);
		String tempFileName = fileName.substring(fileName.lastIndexOf('/') + 1);
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
			public void onClick(final View v) {
				// TODO Auto-generated method stub
				new AlertDialog.Builder(GreencampusWriteActivity.this)
						.setTitle(getString(R.string.del_picture))
						.setMessage(getResources().getString(R.string.msg_green_write_file_delete_content))
						.setCancelable(true)
						.setPositiveButton(getString(R.string.check), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								String fileNameFromTextView = ((TextView)v).getText().toString();
								// remove
								for (int index = 0; index < absoluteFileList.size(); index++) {
									String tempFileName = absoluteFileList.get(index);
									tempFileName = tempFileName.substring(tempFileName.lastIndexOf('/') + 1);
									if (tempFileName.equals(fileNameFromTextView)) {
										absoluteFileList.remove(index);
										break;
									}
								}
								// if filelist is not empty
								// then fill new last file image
								if (absoluteFileList.size() > 0) {
									String lastFileName = absoluteFileList.get(absoluteFileList.size() - 1);
									setSelectedImage(Uri.fromFile(new File(lastFileName)));
								}
								// if filelist empty imageview visibility GONE
								else {
									((ImageView) findViewById(R.id.green_write_selected_img_view)).setVisibility(ImageView.GONE);
									((TextView) findViewById(R.id.green_write_file_state_textview)).setVisibility(View.VISIBLE);
								}
								// remove textview from parent layout
								((LinearLayout) findViewById(R.id.green_write_filelist_layout)).removeView(v);
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

	static class GreenCampusWriteHandler extends Handler {
		private final WeakReference<GreencampusWriteActivity> greenCampusWriteAcivity;
		
		public GreenCampusWriteHandler(GreencampusWriteActivity activity) {
			// TODO Auto-generated constructor stub
			greenCampusWriteAcivity = new WeakReference<GreencampusWriteActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			GreencampusWriteActivity activity = greenCampusWriteAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.green_write_file_choose_btn:
			if (absoluteFileList.size() > 2) {
				Toast.makeText(getBaseContext(), getResources().getString(R.string.
						msg_green_write_upload_limit), Toast.LENGTH_SHORT).show();
			} else {
				alertDialog = MJUAlertDialog.newInstance(MJUConstants.GREEN_PICTURE_SELECT_DIALOG, 
						R.string.community_write_aritlce_select_pic, 0, 
						R.array.green_write_picture_select_list);
				alertDialog.show(fragmentManager, "");
			}
			break;
		case R.id.green_write_submit_btn:
			if (checkParam() && NetworkManager.checkNetwork(GreencampusWriteActivity.this)) {
				progressDialog.show(fragmentManager, "");
				writeBoard();
			}
			break;
		}
	}
}
/* end of file */