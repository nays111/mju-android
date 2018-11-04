package kr.ac.mju.mjuapp.community;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.common.LayoutSlideManager;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.dialog.MJUAlertDialog;
import kr.ac.mju.mjuapp.dialog.MJUProgressDialog;
import kr.ac.mju.mjuapp.http.HttpManager;
import kr.ac.mju.mjuapp.login.LoginActivity;
import kr.ac.mju.mjuapp.login.LoginManager;
import kr.ac.mju.mjuapp.network.NetworkManager;
import kr.ac.mju.mjuapp.util.PixelConverter;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author davidkim
 *
 */
public class CommunityListActivity extends FragmentActivity implements OnClickListener, OnTouchListener,
																OnItemClickListener {
	private final int LOADING_SUCCESS = 14;
	private final int LOADING_FAIL = 13;
	
	private final int VIDEO_IMG_LOAD_SUCCESS = 19;
	private final int VIDEO_IMG_LOAD_FAIL = 21;
	private final int IMG_IMG_LOAD_SUCCESS = 20;
	private final int IMG_IMG_LOAD_FAIL = 22;
	private final int LOADING_NEXTPAGE_URL_FAIL = 23;
	private final int YOUTUBE_URL_SUCCESS = 25;
	private final int YOUTUBE_URL_FAIL = 26;
	
	private  int whichAritcle;
	
	private static final int RQ_WRITE_BOARD = 25;
	private static final int RQ_WRITE_AFTER_LOGIN = 26;
	
	private int typeOfArticle;
	
	private ListView listView;
	private int prevPageLastPosition = 0;
	private String listUrl;
	private String title;
	private ArrayList<NormalArticle> normalArticleList;
	private ArrayList<String> pagingUrlList;
	private NormalArticleListAdapter normalArticleListAdapter;
	private ArrayList<VideoArticle> viedoArticleList;
	private VideoArticleListAdapter videoArticleListAdapter;
	private ArrayList<ImageArticle> imgArticleList;
	private ImageArticleListAdapter imgArticleListAdapter;
	
	private HttpManager httpManager;
	
	private String requestShowArticleUrl;
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private MJUAlertDialog alertDialog;
	private LayoutSlideManager layoutSlideManager;
	private CommunityListHandler communityListHandler;
	private boolean mReturningWithRusult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.community_list_layout);
		Log.i("com", "oncreate()");
		init();
		initLayout();
		initListView();

		if (listUrl == null) {	// 硫붿씤 硫붾돱�먯꽌 諛붾줈 而ㅻ��덊떚 �ㅼ뼱�щ븣 , listUrl is null
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
				openLayout();
			}
		} else {					
			((TextView) findViewById(R.id.community_title)).setText(getString(R.string.community_sidemenu_cut));
			typeOfArticle = MJUConstants.IMG_ARTICLE; 
			whichAritcle = 8;
			
			showListFromPost(); 
		}
		
		setClickListener();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.i("com", "onNewInent()");
		setIntent(intent);
		
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String keyword = intent.getStringExtra(SearchManager.QUERY);
			searchKeyword(keyword.trim());
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i("com", "onActivity()");
		if (resultCode == RESULT_OK) {
			if (requestCode == RQ_WRITE_AFTER_LOGIN) {
				((ImageButton)findViewById(R.id.community_list_write_btn)).performClick();
			} else if (requestCode == RQ_WRITE_BOARD) {
				mReturningWithRusult = true;
			}
		}
	}
	
	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
		
		if (mReturningWithRusult) {
			initList();
			showListFromPost();
		}
		mReturningWithRusult = false;
	}
	
	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case LOADING_SUCCESS:
			@SuppressWarnings("unchecked")
			HashMap<String, List<Element>> elementMap = (HashMap<String, List<Element>>) msg.obj;
			List<Element> tbodyElementsList = elementMap.get(HTMLElementName.TBODY);
			List<Element> divElementsList = elementMap.get(HTMLElementName.DIV);
			List<Element> formElementList = elementMap.get(HTMLElementName.FORM);
			
			//�쇰컲, �대�吏� �숈쁺��寃뚯떆��怨듯넻
			if (divElementsList != null && pagingUrlList.size() == 0) {
				parsePagingList(divElementsList, formElementList);
			}
			
			if (typeOfArticle == MJUConstants.NORMAL_ARTICLE) {
				if (pagingUrlList.size() > 0) {
					if (tbodyElementsList != null) {
						parseNormalArticleList(tbodyElementsList);
					} 
				} else if (pagingUrlList.size() == 0) {
					if (tbodyElementsList.size() > 0) {
						parseNormalArticleList(tbodyElementsList);
					}
				}
				progressDialog.dismiss();
			} else if (typeOfArticle == MJUConstants.IMG_ARTICLE) {
				parseImageArticleList(formElementList);
			} else {
				parseVideoArticleList(divElementsList);
			}
			break;
		case VIDEO_IMG_LOAD_SUCCESS:
			videoArticleListAdapter.notifyDataSetChanged();
			((ListView) findViewById(R.id.community_listview)).setSelection(prevPageLastPosition);
			prevPageLastPosition = viedoArticleList.size();
			progressDialog.dismiss();
			break;
		case IMG_IMG_LOAD_SUCCESS:
			imgArticleListAdapter.notifyDataSetChanged();
			((ListView) findViewById(R.id.community_listview)).setSelection(prevPageLastPosition);
			prevPageLastPosition = imgArticleList.size();
			progressDialog.dismiss();
			break;
		case LOADING_FAIL:
		case IMG_IMG_LOAD_FAIL:	
		case VIDEO_IMG_LOAD_FAIL:
		case YOUTUBE_URL_FAIL:
			progressDialog.dismiss();
			Toast.makeText(getBaseContext(), getString(R.string.getting_info_fail), 
					Toast.LENGTH_SHORT).show();
			break;
		case LOADING_NEXTPAGE_URL_FAIL:
			Toast.makeText(getBaseContext(), getString(R.string.getting_page_info_fail), 
					Toast.LENGTH_SHORT).show();
			break;
		case YOUTUBE_URL_SUCCESS:
			String url = (String)msg.obj;
			progressDialog.dismiss();
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); 
			startActivity(i);
			break;
			
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.community_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.community_left_slidingbar).setClickable(true);
			break;	
		case MJUConstants.EXECUTE_ACTION:
			if (!listUrl.equals("") && !title.equals("")) {
				initList();
				showListFromPost();
				((TextView) findViewById(R.id.community_title)).setText(title);
			}
			break;
		}
	}
	
	/**
	 * 
	 */
	private void init() {
		normalArticleList = new ArrayList<NormalArticle>();
		pagingUrlList = new ArrayList<String>();
		listView = (ListView)findViewById(R.id.community_listview);
		
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		PixelConverter converter = new PixelConverter(this);
		
		typeOfArticle = MJUConstants.NORMAL_ARTICLE;
		httpManager = new HttpManager();
		
		whichAritcle = 0;
		
		listUrl = getIntent().getStringExtra("directUrl");
		if (listUrl == null) {
			Log.i("url", "null");
		} else {
			Log.i("url", listUrl);
		}
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		communityListHandler = new CommunityListHandler(CommunityListActivity.this);
		
		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(findViewById(R.id.community_content), 
					communityListHandler);
			layoutSlideManager.init((int) ((float)displaymetrics.widthPixels - converter.getWidth(135)));
		}
	}
	
	/**
	 * 
	 */
	private void initLayout(){
		PixelConverter converter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;
		rParams = (LayoutParams) findViewById(R.id.community_sub_layout).getLayoutParams();
		rParams.rightMargin = converter.getWidth(135);
		findViewById(R.id.community_sub_layout).setLayoutParams(rParams);
		
		rParams = (LayoutParams)findViewById(R.id.community_sliding_btn).getLayoutParams();
		rParams.width = converter.getWidth(50);
		rParams.height = converter.getHeight(50);
		
		rParams = (LayoutParams)findViewById(R.id.community_header_icon).getLayoutParams();
		rParams.width = converter.getWidth(30);
		rParams.height = converter.getHeight(30);
		rParams.setMargins(0, 0, converter.getWidth(15), 0);
		
		rParams = (LayoutParams)findViewById(R.id.community_list_search_btn).getLayoutParams();
		rParams.width = converter.getWidth(40);
		rParams.height = converter.getHeight(40);
		rParams.setMargins(0, 0, converter.getWidth(10), 0);
		
		rParams = (LayoutParams)findViewById(R.id.community_list_write_btn).getLayoutParams();
		rParams.width = converter.getWidth(40);
		rParams.height = converter.getHeight(40);
		rParams.setMargins(0, 0, converter.getWidth(10), 0);
		
		View view = findViewById(R.id.community_sidemenu_title);
		LinearLayout.LayoutParams linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));
		
		view = findViewById(R.id.community_sidemenu_square_title);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0, converter.getHeight(5));
		
		view = findViewById(R.id.community_sidemenu_our_history);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		
		view = findViewById(R.id.community_sidemenu_intellectual_title);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0, converter.getHeight(5));
		
		view = findViewById(R.id.community_sidemenu_intellectual);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		
		view = findViewById(R.id.community_sidemenu_elbum_title);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0, converter.getHeight(5));
		
		view = findViewById(R.id.community_sidemenu_ucc);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		
		view = findViewById(R.id.community_sidemenu_mjulife_title);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0, converter.getHeight(5));
		
		view = findViewById(R.id.community_sidemenu_job);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
		
		view = findViewById(R.id.community_sidemenu_media_title);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0, converter.getHeight(5));
		
		view = findViewById(R.id.community_sidemenu_news);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
	}

	private void initListView() {
		// TODO Auto-generated method stub
		normalArticleListAdapter = new NormalArticleListAdapter(getApplicationContext(), normalArticleList);
		listView.setAdapter(normalArticleListAdapter);
		
		((TextView)findViewById(R.id.community_list_more)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (NetworkManager.checkNetwork(CommunityListActivity.this)) {
					if (pagingUrlList.size() > 0) {
						listUrl = pagingUrlList.get(0);
						pagingUrlList.remove(0);
						showListFromPost();
					} else {
						Toast.makeText(getBaseContext(), getResources().getString(
								R.string.msg_load_more_last), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
		listView.setOnItemClickListener(this);
	}
	
	private void initList() {
		// clear list
		if (normalArticleList != null) {
			if (normalArticleList.size() > 0) {
				normalArticleList.clear();
				normalArticleListAdapter.notifyDataSetChanged();
				pagingUrlList.clear();
				prevPageLastPosition = 0;
			}
		}
		
		if (viedoArticleList != null) {
			if (viedoArticleList.size() > 0) {
				viedoArticleList.clear();
				videoArticleListAdapter.notifyDataSetChanged();
				pagingUrlList.clear();
				prevPageLastPosition = 0;
			}
		}
		
		if (imgArticleList != null) {
			if (imgArticleList.size() > 0) {
				imgArticleList.clear();
				imgArticleListAdapter.notifyDataSetChanged();
				pagingUrlList.clear();
				prevPageLastPosition = 0;
			}
		}
	}
	
	private void openLayout() {
		// TODO Auto-generated method stub
		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				layoutSlideManager.slideLayoutToRightAutomatically();
			}
		};
		timer.schedule(task, 500);
	}

	private void showListFromPost() {
		// TODO Auto-generated method stub
		Log.i("com", "shwListFromPost()");
		
		progressDialog.show(fragmentManager, "");
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				httpManager.init();
				httpManager.setHttpPost(listUrl);  
				HttpResponse response = null;
				 
				try {
					response = httpManager.executeHttpPost();
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						Vector<String> tagNames = new Vector<String>();
						tagNames.add(HTMLElementName.TBODY);
						tagNames.add(HTMLElementName.DIV); 
						tagNames.add(HTMLElementName.FORM);
						
						HashMap<String, List<Element>> elementMap = httpManager
								.getHttpElementsMap(entity, tagNames, HttpManager.UTF_8);
						if (elementMap != null) {
							Message msg = communityListHandler.obtainMessage();
							msg.what = LOADING_SUCCESS;
							msg.obj = elementMap;
							communityListHandler.sendMessage(msg);
						} else {
							communityListHandler.sendEmptyMessage(LOADING_FAIL);
						}
					} else {
						communityListHandler.sendEmptyMessage(LOADING_FAIL);
					}
				} catch (Exception e) {
					// TODO: handle exception
					communityListHandler.sendEmptyMessage(LOADING_FAIL);
				} 
			}
		}).start();
	}

	private void setClickListener() {
		// TODO Auto-generated method stub
		findViewById(R.id.community_sidemenu_our_history).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_praise).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_study).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_stepstone).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_club).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_advertising).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_gsa).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_intellectual).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_ucc).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_cut).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_job).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_trip).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_exhibition).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_market).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_loss).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_house).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_news).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_special).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_promotional_video).setOnClickListener(this);
		findViewById(R.id.community_sidemenu_campus_image).setOnClickListener(this);
		
		findViewById(R.id.community_sliding_btn).setOnTouchListener(this);
		findViewById(R.id.community_left_slidingbar).setOnTouchListener(this);
		findViewById(R.id.community_list_search_btn).setOnClickListener(this);
		findViewById(R.id.community_list_write_btn).setOnClickListener(this);
	}
	
	private void parsePagingList(List<Element> elementsList, List<Element> formElementList) {
		// TODO Auto-generated method stub
		List<Element> divList = elementsList.get(0).getAllElements(HTMLElementName.DIV);
		
		Element pageDivElement = null;
		String attr;
		if (typeOfArticle == MJUConstants.IMG_ARTICLE) {
			for (Element form : formElementList) {
				attr = form.getAttributeValue("name");
				if (attr != null) {
					if (attr.equals("form_list")) {
						pageDivElement = form.getAllElements(HTMLElementName.DIV).get(1);
					}
				}
			}
		} else {
			for (Element divElement : divList) {
				attr = divElement.getAttributeValue("class");
				if (attr != null) {
					if (attr.trim().equals("paging")) {
						pageDivElement = divElement;
					}
				}
			}
		}
		
		if (pageDivElement != null) {
			Element strongElement = pageDivElement.getFirstElement(HTMLElementName.STRONG);
			String currentPage = clearString(strongElement.getContent().toString());
			
			if (currentPage != null) {
				List<Element> aList = pageDivElement.getAllElements(HTMLElementName.A);
				
				for (Element aElement : aList) {
					String content = clearString(aElement.getContent().toString());
					
					if (content.startsWith("<img")) {
						String title = aElement.getAttributeValue("title").trim();
						if (title.startsWith("�ㅼ쓬")) {
							String url;
							if (typeOfArticle == MJUConstants.IMG_ARTICLE) {
								url = getString(R.string.community_album_url);
							} else {
								url = getString(R.string.community_board_url);
							}
							url += aElement.getAttributeValue("href").trim();
							pagingUrlList.add(url);
							break;
						}
					} else if (Integer.valueOf(content) > Integer.valueOf(currentPage)) {
						String url;
						if (typeOfArticle == MJUConstants.IMG_ARTICLE) {
							url = getString(R.string.community_album_url);
						} else {
							url = getString(R.string.community_board_url);
						}
						url += aElement.getAttributeValue("href").trim();
						pagingUrlList.add(url);
					}
				}
			}
		} else {
		}
	}
	
	private void parseNormalArticleList(List<Element> tbodyElementsList) {
		// TODO Auto-generated method stub
		listView.setAdapter(normalArticleListAdapter);
		
		List<Element> trList = tbodyElementsList.get(0).getAllElements(HTMLElementName.TR);
		
		for (Element trElement : trList) {
			List<Element> tdList = trElement.getAllElements(HTMLElementName.TD);
			NormalArticle community = new NormalArticle();
			boolean isHasNumber = true;
			
			for (int index = 0; index < tdList.size(); index++) {
				if (index == 0) {
					String number = clearString(tdList.get(index).getContent().toString());
					
					if (number.contains("<img")) {
						isHasNumber = false;
						break;
					}
					
					if (number.contains(getResources().getString(R.string.msg_no_list))) {
						Toast.makeText(getBaseContext(), getResources()
								.getString(R.string.msg_no_list), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				if (isHasNumber) {
					if (index == 1) {
						Element aElement = tdList.get(index).getFirstElement(HTMLElementName.A);
						String rawTitle = aElement.getContent().toString().trim();
						String pref = "";
						String title;
						String countStr;
						
						if (rawTitle.contains("<font")) {
							title = rawTitle.substring(0, rawTitle.indexOf("<font"));
							countStr = aElement.getAllElements(HTMLElementName.FONT).get(0)
									.getContent().toString().trim();
							countStr = clearString(countStr);
						} else {
							title = rawTitle;
							countStr = null;
						}
						
						//�듬��몄� �꾨땶移�泥댄겕�댁꽌 �뗮똿
						community.setReplyimg(checkReply(tdList.get(index).getAllElements(
								HTMLElementName.IMG)));
						
						if (whichAritcle == 6 || whichAritcle == 9 || whichAritcle == 12 
								|| whichAritcle == 13 || whichAritcle == 14) {	//留먮㉧由��뚯떛���꾪빐��
							pref = tdList.get(index).getFirstElement(HTMLElementName.P)
									.getContent().toString();
							if (community.isReplyimg()) { //�듬���寃쎌슦
								pref = pref.substring(0, pref.indexOf("<img"));
							} else { 					//�듬����꾨땶寃쎌슦
								pref = pref.substring(0, pref.indexOf("<a "));
							}
							title = clearString(pref) + " " + title;
						}
						
						if (title.equals("")) {
							title = getString(R.string.community_no_title);
						}
						
						String url = aElement.getAttributeValue("href").trim();
						if (url == null) {
							url = "";
						} else {
							url = getString(R.string.community_board_url) + url;
						}
						community.setTitle(title);
						community.setUrl(url);
						community.setReplyCount(countStr);
					} else if (index == 2) {
						String name = tdList.get(index).getContent().toString().trim();
						if (name == null) {
							name = getString(R.string.community_no_writer);
						}
						community.setName(name);
					} else if (index == 3) {
						String date = tdList.get(index).getContent().toString().trim();
						if (date == null) {
							date = getString(R.string.community_no_date);
						}
						community.setDate(date);
					} else if (index == 5) {
						String file = tdList.get(index).getContent().toString().trim();
						if (file != null) {
							if (file.contains("<img")) {
								community.setFile(true);
							} else {
								community.setFile(false);
							}
						}
					}
				} 
			}
			if (isHasNumber) {
				normalArticleList.add(community);
			}
		}
		normalArticleListAdapter.notifyDataSetChanged();
		
		((ListView) findViewById(R.id.community_listview)).setSelection(prevPageLastPosition);
		prevPageLastPosition = normalArticleList.size();
	}
	
	private boolean checkReply(List<Element> list) {
		// TODO Auto-generated method stub
		if (list == null || list.size() == 0) {
			return false;
		}
		
		String attr;
		for (Element imgElement : list) {
			attr = imgElement.getAttributeValue("src");
			
			if (attr != null) {
				if (attr.contains("icon_re.gif")) {
					return true;
				}
			}
		}
		return false;
	}

	private void parseVideoArticleList(final List<Element> divElementsList) {
		// TODO Auto-generated method stub
		
		if (viedoArticleList == null) {
			viedoArticleList = new ArrayList<VideoArticle>();
		}
		if (videoArticleListAdapter == null) {
			videoArticleListAdapter = new VideoArticleListAdapter(getApplicationContext(), 
					viedoArticleList);
		}
		listView.setAdapter(videoArticleListAdapter);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String attribute = "";
					for (Element divElement : divElementsList) {
						attribute = divElement.getAttributeValue("class");
						
						if (attribute != null) {
							if (attribute.trim().equals("board_media2")) {
								List<Element> liList = divElement.getAllElements(HTMLElementName.LI);
								
								if (liList != null) {
									VideoArticle videoArticle;
									String imgUrlPart;
									String title;
									String date;
									for (int i = 0; i < liList.size(); i++) {
										String articleUrl = getString(R.string.community_movie_url);
										String imgUrl = "http://mju.ac.kr";
										videoArticle = new VideoArticle();
										//html 臾몄꽌 援ъ“ �댁긽??
										//��닔 踰덉㎏ li �쒓렇留��좏슚
										if (i % 2 == 0) { 
											continue;
										}
										
										imgUrlPart = liList.get(i).getAllElements(HTMLElementName.IMG).get(0)
												.getAttributeValue("src").toString();
										if (imgUrlPart.contains("http://")) {
											imgUrl = imgUrlPart;
										} else {
											imgUrl += imgUrlPart;
										}
										Bitmap bitmap = getImage(imgUrl);
										if (bitmap != null) {
											videoArticle.setBitmap(bitmap);
										} else {
											videoArticle.setBitmap(null);
										}
										
										
										title = liList.get(i).getAllElements(HTMLElementName.A).get(1)
												.getContent().toString();
										if (title != null) {
											videoArticle.setTitle(clearString(title));
										} else {
											videoArticle.setTitle(getString(R.string.community_no_title));
										}
										
										
										date = liList.get(i).getAllElements(HTMLElementName.P)
												.get(1).getContent().toString();
										if (date == null) {
											videoArticle.setDate(getString(R.string.community_no_date));
										} else {
											date = date.substring(date.lastIndexOf(">") + 1, date.length());
											videoArticle.setDate(clearString(date));
										}
										
										articleUrl += liList.get(i).getFirstElement(HTMLElementName.A)
												.getAttributeValue("href").toString().trim();
										videoArticle.setUrl(clearString(articleUrl));
										
										viedoArticleList.add(videoArticle);
									}
								}
								communityListHandler.sendEmptyMessage(VIDEO_IMG_LOAD_SUCCESS);
								//�붿씠��猷⑦봽 ���꾩슂 �놁쓬.
								break;
							}
						}
					}
				} catch (Exception e) {
					// TODO: handle exception
					communityListHandler.sendEmptyMessage(VIDEO_IMG_LOAD_FAIL);
				}
			}
		}).start();
	}
	
	private void parseImageArticleList(List<Element> formElementList) {
		// TODO Auto-generated method stub
		if (imgArticleList == null) {
			imgArticleList = new ArrayList<ImageArticle>();
		} 
		if (imgArticleListAdapter == null) {
			imgArticleListAdapter = new ImageArticleListAdapter(getApplicationContext(),
					imgArticleList);
		}
		listView.setAdapter(imgArticleListAdapter);
		
		String attr;
		Element ul = null;
		for (Element formElement : formElementList) {
			attr = formElement.getAttributeValue("name");
			
			if (attr != null) {
				if (attr.equals("frm")) {
					ul = formElement.getAllElements(HTMLElementName.UL).get(0);
				}
			}
		}
		
		List<Element> liList = ul.getAllElements(HTMLElementName.LI);
		if (liList != null && liList.size() >= 0) {
			ImageArticle imgArticle;
			String title;  
			String name;
			String imgUrl;
			String articleUrl;
			String rawString;
			String countStr;
			
			CommunityImageThread thread;
			for (int i = 0; i < liList.size(); i++) {
				imgArticle = new ImageArticle();
				
				imgUrl = liList.get(i).getAllElements(HTMLElementName.IMG)
						.get(0).getAttributeValue("src").toString();
				try {
					thread = new CommunityImageThread("http://www.mju.ac.kr" + imgUrl);
					thread.start();
					thread.join();
					Bitmap bitmap = thread.getImageBitmap();
					
					if (bitmap != null) {
						imgArticle.setBitmap(bitmap);
					} else {
						imgArticle.setBitmap(null);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.d("CommunityImageThread", "throw Exception");
					e.printStackTrace();
				}
				
				rawString = liList.get(i).getFirstElement(HTMLElementName.A).getContent().toString();

				title = liList.get(i).getFirstElement(HTMLElementName.DIV).getContent().toString();
				if (title.contains("<span")) {
					title = title.substring(0, title.indexOf("<span"));
				}
				if (title.contains("<font")) {
					countStr = title.substring(title.indexOf("&nbsp"), title.indexOf("</font"));
					countStr = clearString(countStr);
					title = title.substring(0, title.indexOf("<font")); 
				} else {
					countStr = null;
				}
				imgArticle.setTitle(title);
				imgArticle.setCount(countStr);
				
				name = rawString.substring(rawString.indexOf(">", rawString.length() - 30) + 1, 
						rawString.lastIndexOf("</div"));
				
				if (name == null || name.equals("") || name.equals("(*ull)")) {
					imgArticle.setName(getString(R.string.community_no_writer));
				} else if (name.startsWith("(")) {
					imgArticle.setName(getString(R.string.community_no_writer) + name);
				} else {
					imgArticle.setName(name);
				}
				
				articleUrl = getString(R.string.community_album_url) + 
						liList.get(i).getAllElements(HTMLElementName.A)
						.get(0).getAttributeValue("href").toString();
				imgArticle.setUrl(articleUrl);
				
				imgArticleList.add(imgArticle);
			}
			communityListHandler.sendEmptyMessage(IMG_IMG_LOAD_SUCCESS);
		} else {
			communityListHandler.sendEmptyMessage(IMG_IMG_LOAD_FAIL);
		}
	}
	
	private Bitmap getImage(String imgUrl) {
		// TODO Auto-generated method stub
		Bitmap bitmap = null;
		try {
			URL imageUrl = new URL(imgUrl);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setDefaultUseCaches(true);
			conn.connect();
			int size = conn.getContentLength();
			if (size > 0) {
				BufferedInputStream bis = new BufferedInputStream(conn.getInputStream(), size);
				bitmap = BitmapFactory.decodeStream(bis);
			}
			conn.disconnect();
			return bitmap;
		} catch (OutOfMemoryError e) {
			// TODO: handle exception
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	private String clearString(String str) {
		if (str.contains("&nbsp;")) {
			str = str.replace("&nbsp;", "");
		}
		
		if (str.contains("\n")) {
			str = str.replace("\n", "");
		}
		return str.trim();
	}
	
	private void getYoutubeVideoUrl(final String url) {
		// TODO Auto-generated method stub
		progressDialog.show(fragmentManager, "");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				httpManager.init();
				httpManager.setHttpPost(url);
				HttpResponse response = null;
				try {
					response = httpManager.executeHttpPost();
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						Vector<String> tagNames = new Vector<String>();
						tagNames.add(HTMLElementName.EMBED);
						HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
								entity, tagNames, HttpManager.UTF_8);
						
						List<Element> embedList = elementMap.get(HTMLElementName.EMBED);
						String url = embedList.get(0).getAttributeValue("src").toString();
						
						Message msg = communityListHandler.obtainMessage();
						msg.what = YOUTUBE_URL_SUCCESS;
						msg.obj = url;
						communityListHandler.sendMessage(msg);
					} else {
						communityListHandler.sendEmptyMessage(YOUTUBE_URL_FAIL);
					}
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					communityListHandler.sendEmptyMessage(YOUTUBE_URL_FAIL);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					communityListHandler.sendEmptyMessage(YOUTUBE_URL_FAIL);
				} 
			}
		});
		thread.start();
	}
	
	public void showHumanityCampusSTDAssociation() {
		// TODO Auto-generated method stub
		String url = "https://www.facebook.com/mju2013?fref=ts";
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	}
	
	public void showScienceCampusSTDAssociation() {
		// TODO Auto-generated method stub
		String url = "https://www.facebook.com/mju42?ref=aymt_homepage_panel";
		startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
	}
	
	
	private void searchKeyword(String keyword) {
		listUrl = getSearchUrl(keyword);
		initList();
		showListFromGet();
		((TextView) findViewById(R.id.community_title)).setText(getResources().getString(R.string.title_search));
	}

	private String getSearchUrl(String keyword) {
		// TODO Auto-generated method stub
		String url = "http://www.mju.ac.kr/mbs/mjukr/jsp/";
		switch (whichAritcle) {
		case 0:
			url += "board/list.jsp?qt=&boardId=10487&listType=01&id=mjukr_060101000000&column=TITLE&search=" + keyword;
			break;
		case 1:
			url += "board/list.jsp?qt=&boardId=10495&listType=01&id=mjukr_060103000000&column=TITLE&search=" + keyword;
			break;
		case 2:
			url += "board/list.jsp?qt=&boardId=10512&listType=01&id=mjukr_060105000000&column=TITLE&search=" + keyword;
			break;
		case 3:
			url += "board/list.jsp?qt=&boardId=103023&listType=01&id=mjukr_060107000000&column=TITLE&search=" + keyword;
			break;
		case 4:
			url += "board/list.jsp?qt=&boardId=1976&listType=01&id=mjukr_060108000000&column=TITLE&search=" + keyword;
			break;
		case 5:
			url += "board/list.jsp?qt=&boardId=10592&listType=01&id=mjukr_060109000000&column=TITLE&search=" + keyword;
			break;
		case 6:
			url += "board/list.jsp?qt=&boardId=1954&listType=01&id=mjukr_060102000000&mcategoryId=&column=TITLE&search=" + keyword;
			break;
		case 7:
			url = "http://www.mju.ac.kr/mbs/mjukr/jsp/movie/list.jsp?qt=&boardId=2025&spage=1&listType=04&id=mjukr_060201000000&column=TITLE&search=" + keyword;
			break;
		case 8:
			url += "album/gallery.jsp?qt=&boardId=2012&spage=1&listType=02&id=mjukr_060202000000&column=TITLE&search=" + keyword;
			break;
		case 9:
			url += "board/list.jsp?qt=&boardId=10615&listType=01&id=mjukr_060302000000&mcategoryId=&column=TITLE&search=" + keyword;
			break;
		case 10:
			url += "board/list.jsp?qt=&boardId=10647&listType=01&id=mjukr_060303000000&column=TITLE&search=" + keyword;
			break;
		case 11:
			url += "board/list.jsp?qt=&boardId=10676&listType=01&id=mjukr_060304000000&column=TITLE&search=" + keyword;
			break;
		case 12:
			url += "board/list.jsp?qt=&boardId=10687&listType=01&id=mjukr_060305000000&mcategoryId=&column=TITLE&search=" + keyword;
			break;
		case 13:
			url += "board/list.jsp?qt=&boardId=10710&listType=01&id=mjukr_060306000000&mcategoryId=&column=TITLE&search=" + keyword;
			break;
		case 14:
			url += "board/list.jsp?qt=&boardId=10846&listType=01&id=mjukr_060307000000&mcategoryId=&column=TITLE&search=" + keyword;
			break;
		case 15:
			url += "album/gallery.jsp?qt=&boardId=15697&spage=1&listType=02&id=mjukr_060401000000&column=TITLE&search=" + keyword;
			break;
		case 16:
			url += "album/gallery.jsp?qt=&boardId=10882&spage=1&listType=02&id=mjukr_060402000000&column=TITLE&search=" + keyword;
			break;
		case 17:
			url = "http://www.mju.ac.kr/mbs/mjukr/jsp/movie/list.jsp?qt=&boardId=2025&spage=1&listType=04&id=mjukr_060405000000&column=TITLE&search=" + keyword;
			break;
		case 18:
			url += "album/gallery.jsp?qt=&boardId=10417&spage=1&listType=02&id=mjukr_060406000000&column=TITLE&search=" + keyword;
			break;
		}
		return url;
	}
	
	private void showListFromGet() {
		progressDialog.show(fragmentManager, "");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				httpManager.init();
				httpManager.setHttpGet(listUrl);
				HttpResponse response = null;
				try {
					response = httpManager.executeHttpGet();
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == HttpStatus.SC_OK) {
						HttpEntity entity = response.getEntity();
						Vector<String> tagNames = new Vector<String>();
						tagNames.add(HTMLElementName.TBODY);
						tagNames.add(HTMLElementName.DIV);
						tagNames.add(HTMLElementName.FORM);
						HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
								entity, tagNames, HttpManager.UTF_8);
						
						Message msg = communityListHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						communityListHandler.sendMessage(msg);
					} else
						communityListHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					communityListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					communityListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} 
			}  
		});
		thread.start();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (NetworkManager.checkNetwork(getApplicationContext())) {
			switch (v.getId()) {
			case R.id.community_list_search_btn:
				onSearchRequested();
				return;
			case R.id.community_list_write_btn:
				if (typeOfArticle == MJUConstants.VIDEO_ARTICLE) {
					Toast.makeText(getBaseContext(), getString(R.string.no_offer_write_function), 
							Toast.LENGTH_SHORT).show();
					return;
				} else if (typeOfArticle == MJUConstants.IMG_ARTICLE) {
					if (whichAritcle == 15 || whichAritcle == 16) {
						Toast.makeText(getBaseContext(), getString(R.string.no_offer_write_function), 
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
				
				if (NetworkManager.checkNetwork(getApplicationContext())) {
					if (LoginManager.checkLogin(getApplicationContext())) {
						Intent intent = null;
						if (whichAritcle == 8 || whichAritcle == 18) {
							intent = new Intent(CommunityListActivity.this, CommunityImageWriteActivity.class);
						} else {
							intent = new Intent(CommunityListActivity.this, CommunityWriteActivity.class);
						}
						intent.putExtra("whichArticle", whichAritcle);
						startActivityForResult(intent, RQ_WRITE_BOARD);
					} else {
						Intent intent = new Intent(CommunityListActivity.this, LoginActivity.class);
						startActivityForResult(intent, RQ_WRITE_AFTER_LOGIN);
					}
				}
				return;
			case R.id.community_sidemenu_our_history:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10487&id=mjukr_060101000000";
				title = getString(R.string.community_sidemenu_our_history);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 0;
				break;
			case R.id.community_sidemenu_praise:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10495&id=mjukr_060103000000";
				title = getString(R.string.community_sidemenu_praise);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 1;
				break;
			case R.id.community_sidemenu_study:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10512&id=mjukr_060105000000";
				title = getString(R.string.community_sidemenu_study);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 2;
				break;
			case R.id.community_sidemenu_stepstone:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=103023&id=mjukr_060107000000";
				title = getString(R.string.community_sidemenu_stepstone);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 3;
				break;	
			case R.id.community_sidemenu_club:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=1976&id=mjukr_060108000000";
				title = getString(R.string.community_sidemenu_club);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 4;
				break;	
			case R.id.community_sidemenu_advertising:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10592&id=mjukr_060109000000";
				title = getString(R.string.community_sidemenu_advertising);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 5;
				break;
			case R.id.community_sidemenu_gsa:
				alertDialog = MJUAlertDialog.newInstance(MJUConstants.SELECT_CAMPUS,  
						R.string.select_campus, 0, R.array.seoul_yongin_campus_array);
				alertDialog.show(fragmentManager, "");
				return;
			case R.id.community_sidemenu_intellectual:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=1954&id=mjukr_060102000000";
				title = getString(R.string.community_sidemenu_intellectual);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 6;
				break;		
			case R.id.community_sidemenu_ucc:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/movie/list.jsp?boardId=2025&id=mjukr_060201000000";
				title = getString(R.string.community_sidemenu_ucc);
				typeOfArticle = MJUConstants.VIDEO_ARTICLE;
				whichAritcle = 7;
				break;	
			case R.id.community_sidemenu_cut:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?boardType=02&boardId=2012&listType=02&mcategoryId=&row=4&id=mjukr_060202000000";
				title = getString(R.string.community_sidemenu_cut);
				typeOfArticle = MJUConstants.IMG_ARTICLE; 
				whichAritcle = 8;
				break;
			case R.id.community_sidemenu_job:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10615&id=mjukr_060302000000";
				title = getString(R.string.community_sidemenu_job);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 9;
				break;	
			case R.id.community_sidemenu_trip:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10647&id=mjukr_060303000000";
				title = getString(R.string.community_sidemenu_trip);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 10;
				break;
			case R.id.community_sidemenu_exhibition:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10676&id=mjukr_060304000000";
				title = getString(R.string.community_sidemenu_exhibition);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 11;
				break;	
			case R.id.community_sidemenu_market:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10687&id=mjukr_060305000000";
				title = getString(R.string.community_sidemenu_market);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 12;
				break;	
			case R.id.community_sidemenu_loss:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10710&id=mjukr_060306000000";
				title = getString(R.string.community_sidemenu_loss);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 13;
				break;
			case R.id.community_sidemenu_house:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=10846&id=mjukr_060307000000";
				title = getString(R.string.community_sidemenu_house);
				typeOfArticle = MJUConstants.NORMAL_ARTICLE;
				whichAritcle = 14;
				break;
			case R.id.community_sidemenu_news:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?boardType=02&boardId=15697&listType=02&mcategoryId=&row=4&id=mjukr_060401000000";
				title = getString(R.string.community_sidemenu_news);
				typeOfArticle = MJUConstants.IMG_ARTICLE;
				whichAritcle = 15;
				break;	
			case R.id.community_sidemenu_special:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?boardType=02&boardId=10882&listType=02&mcategoryId=&row=4&id=mjukr_060402000000";
				title = getString(R.string.community_sidemenu_special);
				typeOfArticle = MJUConstants.IMG_ARTICLE;
				whichAritcle = 16;
				break;	
			case R.id.community_sidemenu_promotional_video:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/movie/list.jsp?boardId=2025&id=mjukr_060405000000";
				title = getString(R.string.community_sidemenu_promotional_video);
				typeOfArticle = MJUConstants.VIDEO_ARTICLE;
				whichAritcle = 17;
				break;	
			case R.id.community_sidemenu_campus_image:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?boardType=02&boardId=10417&listType=02&mcategoryId=&row=4&id=mjukr_060406000000";
				title = getString(R.string.community_sidemenu_campus_image);
				typeOfArticle = MJUConstants.IMG_ARTICLE;
				whichAritcle = 18;
				break;
			}
			
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(true);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			layoutSlideManager.initXPostion(event.getRawX());
			break;
		case MotionEvent.ACTION_MOVE:
			layoutSlideManager.slideLayout(event.getRawX());
			break;
		case MotionEvent.ACTION_UP:
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
				layoutSlideManager.slideLayoutToRightAutomatically();
			} else if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(false);
			} else {
				layoutSlideManager.keepSlidingLayout();
			}
			break;
		}
		return false;
	}

	@Override
	public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
		// TODO Auto-generated method stub
		
		requestShowArticleUrl = ((TextView)view.findViewById(R.id.community_list_row_hidden_url)).getText()
				.toString();
		
		if (typeOfArticle == MJUConstants.VIDEO_ARTICLE) {
			getYoutubeVideoUrl(requestShowArticleUrl);
		} else {
			Intent intent = new Intent(CommunityListActivity.this, CommunityViewActivity.class);
			intent.putExtra("url", requestShowArticleUrl);
			intent.putExtra("main_title", ((TextView)findViewById(R.id.community_title)).getText()
					.toString());
			intent.putExtra("type", typeOfArticle);
			startActivity(intent);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (layoutSlideManager.getLayoutState() == MJUConstants.SLIDING) {
				return false;
			} else if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
				layoutSlideManager.slideLayoutToRightAutomatically();
			} else {
				layoutSlideManager.slideLayoutToLeftAutomatically(false);
			} 
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(false);
			} else {
				super.onBackPressed();
			}
		}
		return true;
	}
	
	static class CommunityListHandler extends Handler {
		private final WeakReference<CommunityListActivity> communityListAcivity;
		
		public CommunityListHandler(CommunityListActivity activity) {
			// TODO Auto-generated constructor stub
			communityListAcivity = new WeakReference<CommunityListActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			CommunityListActivity activity = communityListAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */
