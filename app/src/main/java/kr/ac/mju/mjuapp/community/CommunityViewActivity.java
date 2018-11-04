package kr.ac.mju.mjuapp.community;

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
import kr.ac.mju.mjuapp.common.CustomTextView;
import kr.ac.mju.mjuapp.constants.MJUConstants;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CommunityViewActivity extends FragmentActivity {
	private final int LOADING_SUCCESS = 11;
	private final int LOADING_FAIL = 12;
	private final int FILE_DOWNLOAD_COMPLETE = 21;
	private final int FILE_DOWNLOAD_FAIL = 22;
	private final int FILE_DOWNLOADING = 23;
	private static final int REPLY_POST_SUCCESS = 24;
	private static final int REPLY_POST_FAIL = 25;
	private static final int LOADING_REPLY_SUCCESS = 26;
	private static final int LOADING_REPLY_FAIL = 27;
	
	private String communityViewUrl;
	private int typeOfArticle;
	private ProgressDialog downloadPDialog = null;
	private int progressState = 0;
	private String boardId;
	private String boardSeq;
	private HttpManager httpManager;
	
	private boolean isHaveReplyDiv;
	private boolean isDivStructureBroken;
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private CommunityViewHandler communityViewHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // TODO Auto-generated method stub
	    setContentView(R.layout.community_view_layout);
	    init();
	    
	    if (NetworkManager.checkNetwork(getApplicationContext())) {
			progressDialog.show(fragmentManager, "");
			showPosting();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) { 
		case LOADING_SUCCESS:
			// parsing
			@SuppressWarnings("unchecked")
			HashMap<String, List<Element>> elementMap = (HashMap<String, List<Element>>) msg.obj;
			List<Element> divElementList = elementMap.get(HTMLElementName.DIV);
			List<Element> tbodyElementList = elementMap.get(HTMLElementName.TBODY);
			List<Element> formElementList = elementMap.get(HTMLElementName.FORM);
			
			if (typeOfArticle == MJUConstants.IMG_ARTICLE) { 
				if (divElementList.size() > 0) {
					parseImageArticleData(divElementList);
				}
			} else if (typeOfArticle == MJUConstants.NORMAL_ARTICLE) {
				if (divElementList.size() > 0) {
					parseNormalArticleData(divElementList, tbodyElementList, formElementList);
				}
			}
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
		case REPLY_POST_SUCCESS:
			updateReplycontents();
			break;
		case REPLY_POST_FAIL:
			Toast.makeText(getBaseContext(), getString(R.string.write_fail), Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
			break;
		case LOADING_REPLY_SUCCESS:
			@SuppressWarnings("unchecked")
			HashMap<String, List<Element>> divElementMap = (HashMap<String, List<Element>>) msg.obj;
			List<Element> elementList = divElementMap.get(HTMLElementName.DIV);
			if (elementList.size() > 0) {
				parseUpdatedReply(elementList);
			}
			progressDialog.dismiss();
			break;
		case LOADING_REPLY_FAIL:
			Toast.makeText(getBaseContext(), getString(R.string.write_fail), Toast.LENGTH_SHORT).show();
			progressDialog.dismiss();
			break;
		default:
			break;
		}
	}
	
	private void init() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		communityViewUrl = intent.getStringExtra("url");
		String title = intent.getStringExtra("main_title");
		typeOfArticle = intent.getIntExtra("type", 0);

		((TextView) findViewById(R.id.community_view_main_title)).setText(title);
		
		httpManager = new HttpManager();
		
		isHaveReplyDiv = false;
		isDivStructureBroken = false;
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		communityViewHandler = new CommunityViewHandler(CommunityViewActivity.this);
	}

	private void showPosting() {
		// TODO Auto-generated method stub
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				httpManager.init();
				httpManager.setHttpPost(communityViewUrl);
				httpManager.setCookieHeader(LoginManager.getCookies(getApplicationContext())); 
				
				HttpResponse response = null;
				
				try {
					response = httpManager.executeHttpPost();
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						Vector<String> tagNames = new Vector<String>();
						tagNames.add(HTMLElementName.DIV);
						
						//--div태그 구조가 깨져서 div못 뽑아올때를 대비
						tagNames.add(HTMLElementName.TBODY);
						tagNames.add(HTMLElementName.FORM);
						//--
						
						HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
								entity, tagNames, HttpManager.UTF_8);
						
						if (elementMap != null) {
							Message msg = communityViewHandler.obtainMessage();
							msg.what = LOADING_SUCCESS;
							msg.obj = elementMap;
							communityViewHandler.sendMessage(msg);
						} 
					} else {
						communityViewHandler.sendEmptyMessage(LOADING_FAIL);
					}
				} catch (Exception e) {
					// TODO: handle exception
					communityViewHandler.sendEmptyMessage(LOADING_FAIL);
				} 
			}
		}).start();
	}
	
	private void parseImageArticleData(List<Element> divElementList) {
		// TODO Auto-generated method stub
		
		WebView webView = (WebView) findViewById(R.id.community_view_webview);
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		
		String content = new String();
		Element tbody = divElementList.get(0).getAllElements(HTMLElementName.TBODY).get(0);
		List<Element> trList = tbody.getAllElements(HTMLElementName.TR);
		
		for (int i = 0; i < trList.size(); i++) {
			if (i == 0) { 	//제목
				String title = trList.get(i).getAllElements(HTMLElementName.STRONG).get(0)
						.getContent().toString();
				if (title.contains("<div")) {
					title = title.substring(0, title.indexOf("<div"));
				}
				title = clearString(title);
				((TextView)findViewById(R.id.community_view_title)).setText(title);
			} else if (i == 1) {
				List<Element> tdList = trList.get(i).getAllElements(HTMLElementName.TD);
				String name = tdList.get(0).getContent().toString();
				String date= tdList.get(1).getContent().toString();
				
				name = clearString(name);
				if (name == null || name.equals("") || name.equals("(*ull)")) {
					name = getString(R.string.community_no_writer);
				} else if (name.startsWith("(")) {
					name = getString(R.string.community_no_writer) + name;
				} 
				date = clearString(date);
				
				((TextView)findViewById(R.id.community_view_writer)).setText(name);
				((TextView)findViewById(R.id.community_view_date)).setText(date);
			} else if (i == 2) {
				content += trList.get(i).getContent().toString();
				webView.loadDataWithBaseURL("", content, "text/html", "UTF-8", null);
				webView.setHorizontalScrollBarEnabled(false);
			}
		}
		Element replyDivElement = divElementList.get(21);
		List<Element> inputList = divElementList.get(0).getAllElements(HTMLElementName.INPUT);
		getBoardId(inputList);
		
		if (replyDivElement.getContent().toString().trim().equals("")) {
			//댓글 없음
		} else {
			//댓글 있음
			List<Element> dlList = replyDivElement.getAllElements(HTMLElementName.DL);
			addReplyLayout(dlList, true);
		}
		addReplyWriteLayout();
	}
	
	private void parseNormalArticleData(List<Element> divElementsList, List<Element> tbodyElementList, List<Element> formElementList) {
		// TODO Auto-generated method stub
		String attr;
		
		WebView webView = (WebView)findViewById(R.id.community_view_webview);
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.NARROW_COLUMNS);
		
		Element formElement = checkReplyDivExistence(formElementList);
		if (formElement != null) {
			isHaveReplyDiv = true; 
		} else {
			isHaveReplyDiv = false; 
		}
		
		for (Element divElement : divElementsList) {
			attr = divElement.getAttributeValue("id");
			
			if (attr != null) {
				if (attr.equals("rg_con")) {
					List<Element> tbodyElementsList = divElement.getAllElements(HTMLElementName.TBODY);
					
					if (tbodyElementsList.size() == 0 || tbodyElementsList == null) {
						//가끔 게시글에서 div태그 구조가 깨지는 경구가 있음
						
						isDivStructureBroken = true;
						tbodyElementsList = tbodyElementList;
					}
					
					String content = new String();
					List<Element> trList = tbodyElementsList.get(0).getAllElements(HTMLElementName.TR);
					
					for (int i = 0; i < trList.size(); i++) {
						if (i == 0) { 	//제목
							String title = trList.get(i).getAllElements(HTMLElementName.STRONG).get(0)
									.getContent().toString();
							if (title.contains("<div")) {
								title = title.substring(0, title.indexOf("<div"));
							}
							title = clearString(title);
							((TextView)findViewById(R.id.community_view_title)).setText(title);
						} else if (i == 1) {
							List<Element> tdList = trList.get(i).getAllElements(HTMLElementName.TD);
							String name = tdList.get(0).getContent().toString();
							String date= tdList.get(1).getContent().toString();
							
							name = clearString(name);
							date = clearString(date);
							
							((TextView)findViewById(R.id.community_view_writer)).setText(name);
							((TextView)findViewById(R.id.community_view_date)).setText(date);
						} else if (i == 2) {
							content += trList.get(i).getContent().toString();
							webView.loadDataWithBaseURL("", content, "text/html", "UTF-8", null);
							webView.setHorizontalScrollBarEnabled(false);
							
						} else if (i == trList.size() - 2) {
							List<Element> aList = trList.get(i).getAllElements(HTMLElementName.A);
							
							if (aList.size() > 0) {
								for (Element aElement : aList) {
									/*String downloadUrl = aElement.getAttributeValue("onclick").trim();
									downloadUrl = downloadUrl.substring(downloadUrl.indexOf("'") + 1);
									downloadUrl = downloadUrl.substring(0, downloadUrl.indexOf("'"));
									if (!downloadUrl.startsWith("http")) {
										downloadUrl = "http://www.mju.ac.kr" + downloadUrl;
									}*/

									String downloadUrl = aElement.getAttributeValue("href").trim();
									// 변경 끝

									if (!downloadUrl.startsWith("http"))
										downloadUrl = getString(R.string.mju_home_page_url) + downloadUrl;

									String fileName = aElement.getContent().toString();
									if (fileName != null) {
										if (fileName.trim().contains("<img")) {
											fileName = fileName.substring(fileName.indexOf(">") + 1).trim();
											addAttachedFileTextView(fileName.trim(), downloadUrl);
										}
									}
								}
							}
						}
					}
					
					if (isHaveReplyDiv) {
						Element replyDivElement;
						List<Element> inputList;
						
						if (!isDivStructureBroken) {
							//정상적인 경우
							replyDivElement = divElement.getAllElements(HTMLElementName.DIV).get(13);
							inputList = divElement.getAllElements(HTMLElementName.INPUT);
						} else {
							//div깨진경우 form태그에서 가져옴
							replyDivElement = formElement.getAllElements(HTMLElementName.DIV).get(3);
							inputList = formElement.getAllElements(HTMLElementName.INPUT);
						}
						getBoardId(inputList);
						
						if (replyDivElement.getContent().toString().trim().equals("")) {
							//댓글 없음
						} else {
							//댓글 있음
							List<Element> dlList = replyDivElement.getAllElements(HTMLElementName.DL);
							addReplyLayout(dlList, true);
						}
						addReplyWriteLayout();
					}
				}
			}
		}
	}

	private Element checkReplyDivExistence(List<Element> formList) {
		// TODO Auto-generated method stub
		String attr;
		
		for (Element formElement : formList) {
			attr = formElement.getAttributeValue("name");
			
			if (attr != null) {
				if (attr.equals("commentform")) {
					return formElement;
				}
			}
		}
		return null;
	}

	private void addReplyWriteLayout() {
		// TODO Auto-generated method stub
		LinearLayout root = (LinearLayout)findViewById(R.id.community_view_reply_write);
		final LinearLayout replyWriteLayout = (LinearLayout)LayoutInflater.from(this).inflate(
				R.layout.community_reply_write_layout, null);
		
		((ImageButton)replyWriteLayout.findViewById(R.id.community_write_reply_btn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						String replyStr = ((EditText)replyWriteLayout.findViewById(R.id
								.community_write_reply_edittext)).getText().toString().trim();
						if (replyStr.equals("")) {
							Toast.makeText(getBaseContext(), getString(R.string.community_input_data), Toast.LENGTH_SHORT)
								.show();
							return;
						} 
						postReply(replyStr);
						((EditText)replyWriteLayout.findViewById(R.id.community_write_reply_edittext))
							.setText("");
					}
				});
		
//		PixelConverter converter = new PixelConverter(getApplicationContext());
//		LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams)replyWriteLayout
//				.findViewById(R.id.community_write_reply_btn).getLayoutParams();
//		lParams.height = converter.getHeight(45);
//		replyWriteLayout.findViewById(R.id.community_write_reply_btn).setLayoutParams(lParams);
//		
//		lParams = (LinearLayout.LayoutParams)replyWriteLayout.findViewById(R.id
//				.community_write_reply_edittext).getLayoutParams();
//		lParams.height = converter.getHeight(60);   
//		replyWriteLayout.findViewById(R.id.community_write_reply_edittext).setLayoutParams(lParams);
		
		root.addView(replyWriteLayout);
	}
 
	private void getBoardId(List<Element> inputList) {
		// TODO Auto-generated method stub
		boardId = "";
		boardSeq = "";
		
		String attr;
		
		for (Element inputElement : inputList) {
			attr = inputElement.getAttributeValue("name");
			
			if (attr != null) {
				if (attr.equals("boardId")) {
					boardId = inputElement.getAttributeValue("value");
				} else if (attr.equals("boardSeq")) {
					boardSeq = inputElement.getAttributeValue("value");
				}
				
				if (!boardId.equals("") && !boardSeq.equals("")) {
					break;
				}
			}
		}
	}

	private void addReplyLayout(List<Element> dlList, boolean isAddWriteReplyLayout) {
		// TODO Auto-generated method stub
		LinearLayout replyRootLayout = (LinearLayout)findViewById(R.id.community_view_reply);
		replyRootLayout.removeAllViews();
		
		String content;
		String date;
		String name;
		for (int i = dlList.size() - 1 ; i >= 0; i--) {
			content = dlList.get(i).getFirstElement(HTMLElementName.DD).getContent().toString();
			date = dlList.get(i).getFirstElement(HTMLElementName.LABEL).getContent().toString();
			name = dlList.get(i).getAllElements(HTMLElementName.LABEL).get(1).getContent().toString();
			
			content = clearString(content);
			date = clearString(date);
			name = clearString(name); 
			
			LinearLayout replyLayout = (LinearLayout)LayoutInflater.from(this).inflate(
					R.layout.community_reply_layout, null);
			((CustomTextView)replyLayout.findViewById(R.id.community_reply_content)).setText(content);
			((TextView)replyLayout.findViewById(R.id.community_reply_name)).setText(name);
			((TextView)replyLayout.findViewById(R.id.community_reply_date)).setText(date);
			replyRootLayout.addView(replyLayout);
		}
		
		((TextView)findViewById(R.id.divider)).setVisibility(View.VISIBLE);
	}
	
	private void postReply(final String replyStr) {
		// TODO Auto-generated method stub
		progressDialog.show(fragmentManager, "");
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				httpManager.init();
				httpManager.initSSL();
				
				try {
					HashMap<String, String> paramsMap = getReplyParamsMap(replyStr);
					httpManager.setHttpPost(paramsMap, "http://www.mju.ac.kr/board/commentCreate.mbs", 
							HttpManager.UTF_8);
					httpManager.setCookieHeader(LoginManager.getCookies(getApplicationContext()));
					
					HttpResponse boardResponse = null;
					boardResponse = httpManager.executeHttpPost();
					HttpEntity entity = boardResponse.getEntity();

					if (entity != null) {
						communityViewHandler.sendEmptyMessage(REPLY_POST_SUCCESS);
					} else {
						communityViewHandler.sendEmptyMessage(REPLY_POST_FAIL);
					}
				} catch (Exception e) {
					// TODO: handle exception
					communityViewHandler.sendEmptyMessage(REPLY_POST_FAIL);
				} 
			}
		}).start();
	}
	
	private void updateReplycontents() {
		// TODO Auto-generated method stub
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				httpManager.init();
				httpManager.setHttpPost(communityViewUrl);
				httpManager.setCookieHeader(LoginManager.getCookies(getApplicationContext())); 
				
				HttpResponse response = null;
				try {
					response = httpManager.executeHttpPost();
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						Vector<String> tagNames = new Vector<String>();
						tagNames.add(HTMLElementName.DIV);
						
						HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
								entity, tagNames, HttpManager.UTF_8);
						
						if (elementMap != null) {
							Message msg = communityViewHandler.obtainMessage();
							msg.what = LOADING_REPLY_SUCCESS;
							msg.obj = elementMap;
							communityViewHandler.sendMessage(msg);
						} 
					} else {
						communityViewHandler.sendEmptyMessage(LOADING_REPLY_FAIL);
					}
				} catch (Exception e) {
					// TODO: handle exception
					communityViewHandler.sendEmptyMessage(LOADING_REPLY_FAIL);
				} 
			}
		}).start();
	}
	
	private void parseUpdatedReply(List<Element> elementList) {
		// TODO Auto-generated method stub
		String attr;
		
		for (Element divElement : elementList) {
			attr = divElement.getAttributeValue("class");
			
			if (attr != null) {
				if (attr.equals("replyList")) {
					List<Element> dlList = divElement.getAllElements(HTMLElementName.DL);
					addReplyLayout(dlList, false);
				}
			}
		}
	}
	
	private HashMap<String, String> getReplyParamsMap(String msg) {
		
		HashMap<String, String> paramsMap = new HashMap<String, String>();
		paramsMap.put("boardId", boardId);
		paramsMap.put("boardSeq", boardSeq);
		paramsMap.put("refSeq", boardSeq);
		paramsMap.put("famSeq", "0");
		paramsMap.put("pos", "0");
		paramsMap.put("depth", "0");
		paramsMap.put("spage", "1");
		paramsMap.put("boardType", "01");
		paramsMap.put("listType", "01");
		paramsMap.put("command", "view");
		paramsMap.put("mode", "create");
		paramsMap.put("boardComment.boardRecord.boardSeq", boardSeq);
		paramsMap.put("boardComment.comments", msg);//
		paramsMap.put("boardComment.commentSeq", "");
		paramsMap.put("id", "mjukr_060101000000");
		paramsMap.put("mcategoryId", "");
		paramsMap.put("boardComment.icon", "icon1");

		return paramsMap;
	}
	private void addAttachedFileTextView(String fileName, final String downloadUrl) {
		// TODO Auto-generated method stub
		if (fileName != null && !fileName.equals("")) {
			LinearLayout ll = (LinearLayout)findViewById(R.id.community_view_attached_file);
			
			LinearLayout attachll = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.attach_file_layout, null);
			TextView attachFileTextView = (TextView) attachll.findViewById(R.id.attach_file_textView);
			attachFileTextView.setText(fileName);
			attachFileTextView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					downloadPDialog = new ProgressDialog(CommunityViewActivity.this);
					downloadPDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					downloadPDialog.setTitle("");
					downloadPDialog.setCancelable(false);
					downloadPDialog.setMessage(getResources().getString(R.string.msg_download_progress));
					downloadPDialog.show();
					
					downloadAttachedFile(((TextView)v).getText().toString(), downloadUrl);
				}
			});
			ll.addView(attachll);
			
			((TextView)findViewById(R.id.divider)).setVisibility(View.VISIBLE);
		}
	}
	
	private void downloadAttachedFile(final String fileName, final String downloadUrl) {
		// TODO Auto-generated method stub
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
							communityViewHandler.post(new Runnable() {
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
								Message msg = communityViewHandler.obtainMessage();
								msg.what = FILE_DOWNLOADING;
								msg.arg1 = nRead;
								communityViewHandler.sendMessage(msg);
							}
							// disconnect
							httpConn.disconnect();
							// download success return to handler to get rid of
							// send intent
							Message msg = communityViewHandler.obtainMessage();
							msg.what = FILE_DOWNLOAD_COMPLETE;
							msg.obj = saveFileName;
							communityViewHandler.sendMessage(msg);
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
						communityViewHandler.sendEmptyMessage(FILE_DOWNLOAD_FAIL);
				}
				// does not mounted extra storage
				// so, download fail
				// send message to handler about download fail
				else
					communityViewHandler.sendEmptyMessage(FILE_DOWNLOAD_FAIL);

			}
		}).start();
	}
	
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
	
	private String clearString(String str) {
		if (str.contains("&nbsp;")) {
			str = str.replace("&nbsp;", "");
		}
		
		if (str.contains("\n")) {
			str = str.replace("\n", "");
		}
		
		if (str.contains("  ")) {
			str = str.replaceAll("  ", "");
		}
		return str.trim();
	}
	
	static class CommunityViewHandler extends Handler {
		private final WeakReference<CommunityViewActivity> communityViewAcivity;
		
		public CommunityViewHandler(CommunityViewActivity activity) {
			// TODO Auto-generated constructor stub
			communityViewAcivity = new WeakReference<CommunityViewActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			CommunityViewActivity activity = communityViewAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
