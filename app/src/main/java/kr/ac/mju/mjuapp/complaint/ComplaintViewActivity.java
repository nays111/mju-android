package kr.ac.mju.mjuapp.complaint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import kr.ac.mju.mjuapp.R;
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

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author davidkim
 *
 */
public class ComplaintViewActivity extends FragmentActivity {
	private final int LOADING_SUCCESS = 11;
	private final int LOADING_FAIL = 12;

	private final int FILE_DOWNLOAD_COMPLETE = 21;
	private final int FILE_DOWNLOAD_FAIL = 22;
	private final int FILE_DOWNLOADING = 23;
	
	private String complaintUrl;

	private ProgressDialog downloadPDialog = null;
	private int progressState = 0;
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private ComplaintViewHandler complaintViewHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.complaint_view_layout);
		
		init();

		// check network
		if (NetworkManager.checkNetwork(this)) {
			progressDialog.show(fragmentManager, "");
			showPosting();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		setIntent(intent);
	}
	
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case LOADING_SUCCESS:
			// parsing
			@SuppressWarnings("unchecked")
			HashMap<String, List<Element>> elementMap = (HashMap<String, List<Element>>) msg.obj;
			List<Element> elementsList = elementMap.get(HTMLElementName.DIV);
			if (elementsList != null)
				parsingData(elementsList);
			// dismissdialog
			progressDialog.dismiss();
			break;
		case LOADING_FAIL:
			progressDialog.dismiss();
			// error weak signal msg
			Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_network_error_weak_signal),
					Toast.LENGTH_SHORT).show();
			break;
		case FILE_DOWNLOAD_COMPLETE:
			// dismiss dialog
			downloadPDialog.dismiss();
			// implement application to get rid of the file
			String fileName = (String) msg.obj;
			showDownloadFile(fileName);
			// set progress state
			progressState = 0;
			break;
		case FILE_DOWNLOAD_FAIL:
			Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_download_fail), Toast.LENGTH_SHORT).show();
			// set progress state
			progressState = 0;
			break;
		case FILE_DOWNLOADING:
			progressState += msg.arg1;
			downloadPDialog.setProgress(progressState);
			break;
		}
	}
	
	private void init() {
		// TODO Auto-generated method stub
		// get url
		Intent intent = getIntent();
		complaintUrl = intent.getExtras().getString("url");
		if (!complaintUrl.startsWith("http://"))
			complaintUrl = getString(R.string.complaint_url) + complaintUrl;
		String title = intent.getExtras().getString("subtitle");
		((TextView) findViewById(R.id.complaint_view_title)).setText("");
		((TextView) findViewById(R.id.complaint_view_title)).setText(title);
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		complaintViewHandler = new ComplaintViewHandler(ComplaintViewActivity.this);
	}

	/**
	 * 
	 */
	private void showPosting() {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// create httpManager
				HttpManager httpManager = new HttpManager();
				// init
				httpManager.init();
				// set httpPost
				httpManager.setHttpPost(complaintUrl);
				// httpResponse
				HttpResponse response = null;
				// execute
				try {
					response = httpManager.executeHttpPost();
					// get Status
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == HttpStatus.SC_OK) {
						// html response result
						HttpEntity entity = response.getEntity();
						// parsing element
						Vector<String> tagNames = new Vector<String>();
						tagNames.add(HTMLElementName.DIV);
						// parse result
						HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
						// send result to handler
						Message msg = complaintViewHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						complaintViewHandler.sendMessage(msg);
					} else
						complaintViewHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					complaintViewHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					complaintViewHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		});
		thread.start();
	}

	/**
	 * @param elementsList
	 */
	private void parsingData(List<Element> elementsList) {
		// get user content WebView
		WebView userWebView = (WebView) findViewById(R.id.complaint_view_user_webview);
		userWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);//SINGLE_COLUMN);
		// get admin content WebView
		WebView adminWebView = (WebView) findViewById(R.id.complaint_view_admin_webview);
		adminWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);//SINGLE_COLUMN);
		/********************
		 * get <div class="boardView"> element
		 ********************/
		Element boardViewDiv = null;
		for (Element e : elementsList) {
			String attr = e.getAttributeValue("class");
			if (attr != null) {
				if (attr.equals("boardView")) {
					boardViewDiv = e;
					break;
				}
			}
		}
		/********************
		 * set content
		 ********************/
		if (boardViewDiv != null) {
			// get tbodyList
			List<Element> tbodyList = boardViewDiv.getAllElements(HTMLElementName.TBODY);
			// set divier visible
			if (tbodyList.size() > 1) {
				((TextView) findViewById(R.id.complaint_view_divider)).setVisibility(View.VISIBLE);
				((TextView) findViewById(R.id.complaint_view_admin_divider)).setVisibility(View.VISIBLE);
				adminWebView.setVisibility(View.VISIBLE);
			}
			for (int tbodyIndex = 0; tbodyIndex < tbodyList.size(); tbodyIndex++) {
				List<Element> trList = tbodyList.get(tbodyIndex).getAllElements(HTMLElementName.TR);
				String content = new String();
				for (int trIndex = 0; trIndex < trList.size(); trIndex++) {
					// 사용자 글 제목
					if (trIndex == 0 && tbodyIndex == 0) {
						// strong tag
						List<Element> strongList = trList.get(trIndex).getAllElements(HTMLElementName.STRONG);
						if (strongList.size() > 0) {
							// get subject
							Element divElement = strongList.get(0);
							String subject = divElement.getContent().toString();
							subject = subject.replaceAll("<{1}.[^<>]*>{1}", "").trim();
							// set text
							((TextView) findViewById(R.id.complaint_view_user_subject)).setText(subject.trim());
						}
					}
					// 사용자 글 작성자, 일자
					else if (trIndex == 1 && tbodyIndex == 0) {
						// td tag
						List<Element> tdList = trList.get(trIndex).getAllElements(HTMLElementName.TD);
						if (tdList.size() > 0) {
							// get writer
							Element tdElement = tdList.get(0);
							String writer = tdElement.getContent().toString();
							tdElement = tdList.get(1);
							String date = tdElement.getContent().toString();
							((TextView) findViewById(R.id.complaint_view_user_writer)).setText(writer.trim());
							((TextView) findViewById(R.id.complaint_view_user_date)).setText(date.trim());
						}
					}
					// 사용자, 관리자 글 내용
					else if (trIndex == 2) {
						content += trList.get(trIndex).getContent().toString();
					}
					// 사용자 글, 관리자 글 첨부 파일
					else if (trIndex == trList.size() - 2) {
						Element tdElement = trList.get(trIndex).getFirstElement(HTMLElementName.TD);
						// get <a> tag list
						List<Element> aList = tdElement.getAllElements(HTMLElementName.A);
						if (aList.size() > 0) {
							for (Element aElement : aList) {
								// get downloadUrl
								String downloadUrl = aElement.getAttributeValue("onclick").trim();
								downloadUrl = downloadUrl.substring(downloadUrl.indexOf("'") + 1);
								downloadUrl = downloadUrl.substring(0, downloadUrl.indexOf("'"));
								if (!downloadUrl.startsWith("http"))
									downloadUrl = "http://www.mju.ac.kr" + downloadUrl;
								// get filename
								String fileName = aElement.getContent().toString();
								if (fileName != null) {
									if (fileName.trim().contains("<img")) {
										fileName = fileName.substring(fileName.indexOf(">") + 1).trim();
										// add attachedFile view
										addAttachedFileTextView(fileName.trim(), downloadUrl, tbodyIndex);
									}
								}
							}
						}
					}
					// 관리자 답변 제목
					else if (trIndex == 0 && tbodyIndex == 1) {
						// strong tag
						List<Element> strongList = trList.get(trIndex).getAllElements(HTMLElementName.STRONG);
						if (strongList.size() > 0) {
							// get subject
							Element divElement = strongList.get(0);
							String subject = divElement.getContent().toString();
							subject = subject.replaceAll("<{1}.[^<>]*>{1}", "").trim();
							// set text
							((TextView) findViewById(R.id.complaint_view_admin_subject)).setVisibility(View.VISIBLE);
							((TextView) findViewById(R.id.complaint_view_admin_subject)).setText(subject);
						}
					}
					// 관리자 답변 작성자, 일자
					else if (trIndex == 1 && tbodyIndex == 1) {
						// td tag
						List<Element> tdList = trList.get(trIndex).getAllElements(HTMLElementName.TD);
						if (tdList.size() > 0) {
							// get writer
							Element tdElement = tdList.get(0);
							String writer = tdElement.getContent().toString().trim();
							tdElement = tdList.get(1);
							String date = tdElement.getContent().toString();
							((TextView) findViewById(R.id.complaint_view_admin_writer)).setVisibility(View.VISIBLE);
							((TextView) findViewById(R.id.complaint_view_admin_writer)).setText(writer);
							((TextView) findViewById(R.id.complaint_view_admin_date)).setVisibility(View.VISIBLE);
							((TextView) findViewById(R.id.complaint_view_admin_date)).setText(date);
						}
					}
					if (tbodyIndex == 0 && trIndex > 1) {
						// add content to webView
						userWebView.loadDataWithBaseURL("", content, "text/html", "UTF-8", null);
						userWebView.setHorizontalScrollBarEnabled(false);
					} else if (tbodyIndex == 1 && trIndex > 1) {
						// add content to webView
						adminWebView.loadDataWithBaseURL("", content, "text/html", "UTF-8", null);
						adminWebView.setHorizontalScrollBarEnabled(false);
					}
				}
			}
		}
	}

	/**
	 * @param fileName
	 * @param downloadUrl
	 */
	private void addAttachedFileTextView(String fileName, final String downloadUrl, int tbodyIndex) {
		if (fileName != null && !fileName.equals("")) {
			// get complaintContentView layout
			LinearLayout contentView = (LinearLayout) findViewById(R.id.complaint_view_content);
			// get contentLayout
			LinearLayout ll = null;
			if (tbodyIndex == 0)
				ll = (LinearLayout) contentView.findViewById(R.id.complaint_view_user_attached_file);
			else
				ll = (LinearLayout) contentView.findViewById(R.id.complaint_view_admin_attached_file);
			/***
			 * set attached file textView
			 ***/
			LinearLayout attachll = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.attach_file_layout, null);
			TextView attachFileTextView = (TextView) attachll.findViewById(R.id.attach_file_textView);
			attachFileTextView.setText(fileName);
			attachFileTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					// show download dialog
					downloadPDialog = new ProgressDialog(ComplaintViewActivity.this);
					downloadPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					downloadPDialog.setTitle("");
					downloadPDialog.setCancelable(false);
					downloadPDialog.setMessage(getResources().getString(R.string.msg_download_progress));
					downloadPDialog.show();
					// file download
					downloadAttachedFile(((TextView) v).getText().toString(), downloadUrl);
				}
			});
			ll.addView(attachll);
		}
	}

	/**
	 * @param downloadUrl
	 */
	private void downloadAttachedFile(final String fileName, final String downloadUrl) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				// check external storage to enable to save file
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
					// make filename
					String saveFileName = getSaveFileName(fileName);
					File saveFile = new File(saveFileName);
					if (!saveFile.exists()) {
						// download from web
						URL fileUrl = null;
						InputStream is = null;
						FileOutputStream fos = null;
						try {
							// set http
							fileUrl = new URL(downloadUrl);
							URLConnection conn = fileUrl.openConnection();
							HttpURLConnection httpConn = (HttpURLConnection) conn;
							// get filelength
							final int fileLength = httpConn.getContentLength();
							// set temp byte array
							byte[] filebytes = new byte[1024];
							int nRead = -1;
							// set downloadprogress dialog max
							complaintViewHandler.post(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									downloadPDialog.setMax(fileLength);
								}
							});
							// get stream
							is = httpConn.getInputStream();
							fos = new FileOutputStream(saveFile);
							// download
							while ((nRead = is.read(filebytes)) > 0) {
								// save file
								fos.write(filebytes, 0, nRead);
								// send msg handler to progress bar update
								Message msg = complaintViewHandler.obtainMessage();
								msg.what = FILE_DOWNLOADING;
								msg.arg1 = nRead;
								complaintViewHandler.sendMessage(msg);
							}
							// disconnect
							httpConn.disconnect();
							// download success return to handler to get rid of
							// send intent
							Message msg = complaintViewHandler.obtainMessage();
							msg.what = FILE_DOWNLOAD_COMPLETE;
							msg.obj = saveFileName;
							complaintViewHandler.sendMessage(msg);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally {
							try {
								if (is != null)
									is.close();
								if (fos != null)
									fos.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					// create fail because same file name exists.
					// send message to handler about download fail
					else
						complaintViewHandler.sendEmptyMessage(FILE_DOWNLOAD_FAIL);
				}
				// does not mounted extra storage
				// so, download fail
				// send message to handler about download fail
				else
					complaintViewHandler.sendEmptyMessage(FILE_DOWNLOAD_FAIL);

			}
		}).start();
	}

	/**
	 * 
	 */
	private void showDownloadFile(String fileName) {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		File file = new File(fileName);

		// 파일 확장자 별로 mime type 지정해 준다.
		if (file.getName().endsWith("mp3")) {
			intent.setDataAndType(Uri.fromFile(file), "audio/*");
		} else if (file.getName().endsWith("mp4")) {
			intent.setDataAndType(Uri.fromFile(file), "vidio/*");
		} else if (file.getName().endsWith("jpg") || file.getName().endsWith("jpeg") || file.getName().endsWith("JPG")
				|| file.getName().endsWith("gif") || file.getName().endsWith("png") || file.getName().endsWith("bmp")) {
			intent.setDataAndType(Uri.fromFile(file), "image/*");
		} else if (file.getName().endsWith("txt")) {
			intent.setDataAndType(Uri.fromFile(file), "text/*");
		} else if (file.getName().endsWith("doc") || file.getName().endsWith("docx")) {
			intent.setDataAndType(Uri.fromFile(file), "application/msword");
		} else if (file.getName().endsWith("xls") || file.getName().endsWith("xlsx")) {
			intent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-excel");
		} else if (file.getName().endsWith("ppt") || file.getName().endsWith("pptx")) {
			intent.setDataAndType(Uri.fromFile(file), "application/vnd.ms-powerpoint");
		} else if (file.getName().endsWith("pdf")) {
			intent.setDataAndType(Uri.fromFile(file), "application/pdf");
		} else if (file.getName().endsWith("pdf")) {
			intent.setDataAndType(Uri.fromFile(file), "application/pdf");
		} else if (file.getName().endsWith(".hwp")) {
			intent.setDataAndType(Uri.fromFile(file), "application/hwp");
		}
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_implement_fail_download_file), Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * @param fileName
	 * @return
	 */
	private String getSaveFileName(String fileName) {
		// make directory
		String savePath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ getResources().getString(R.string.uri_file_save_directory);
		File saveDir = new File(savePath);
		if (!saveDir.exists())
			saveDir.mkdir();
		// get current date
		SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
		String currDate = yyyyMMddHHmmss.format(new Date());
		// make filename
		return savePath + currDate.hashCode() + fileName; 
	}
	
	static class ComplaintViewHandler extends Handler {
		private final WeakReference<ComplaintViewActivity> complaintViewAcivity;
		
		public ComplaintViewHandler(ComplaintViewActivity activity) {
			// TODO Auto-generated constructor stub
			complaintViewAcivity = new WeakReference<ComplaintViewActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			ComplaintViewActivity activity = complaintViewAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */
