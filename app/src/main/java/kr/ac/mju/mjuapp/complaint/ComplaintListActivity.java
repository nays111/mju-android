package kr.ac.mju.mjuapp.complaint;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.common.LayoutSlideManager;
import kr.ac.mju.mjuapp.constants.MJUConstants;
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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
public class ComplaintListActivity extends FragmentActivity implements OnClickListener, 
																	OnTouchListener {
	private final int LOADING_SUCCESS = 11;
	private final int LOADING_FAIL = 12;

	private final int RQ_WRITE_BOARD = 30;
	private final int RQ_WRITE_AFTER_LOGIN = 31;
	
	private String title;
	private String listUrl;

	private ComplaintAdapter complaintAdapter;
	private ArrayList<Complaint> complaintList;
	private ArrayList<String> pagingUrlList;

	private int prevLastPositionOfComplaintList = 0;
	
	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private LayoutSlideManager layoutSlideManager;
	private ComplaintListHandler  complaintistHandler;
	private boolean mReturningWithRusult;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.complaint_list_layout);
		//init
		init();
		//initlayout
		initLayout();
		// init listView
		initListView();
		
		if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
			openLayout();
		}
		
		// set subtitle click listener
		setSubtitleClickListener();
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		setIntent(intent);
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String keyword = intent.getStringExtra(SearchManager.QUERY);
			searchKeyword(keyword.trim());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (resultCode == RESULT_OK) {
			if (requestCode == RQ_WRITE_BOARD) {
				mReturningWithRusult = true;
			} else if (requestCode == RQ_WRITE_AFTER_LOGIN) {
				((ImageButton) findViewById(R.id.complaint_list_write_btn)).performClick();
			}
		}
	}
	
	@Override
	protected void onPostResume() {
		// TODO Auto-generated method stub
		super.onPostResume();
		
		if (mReturningWithRusult) {
			// initList
			initList();
			// show list
			showListFromPost();
		}
		mReturningWithRusult = false;
	}

	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case LOADING_SUCCESS:
			// get tag element list
			@SuppressWarnings("unchecked")
			HashMap<String, List<Element>> elementMap = (HashMap<String, List<Element>>) msg.obj;
			List<Element> tbodyElementsList = elementMap.get(HTMLElementName.TBODY);
			List<Element> pageElementsList = elementMap.get(HTMLElementName.DIV);
			// parsing paging list
			if (pageElementsList != null && pagingUrlList.size() == 0)
				parsingPagingList(pageElementsList);
			// parsing article list
			if (pagingUrlList.size() > 0) {
				if (tbodyElementsList != null)
					parsingComplaintList(tbodyElementsList);
			} else if (pagingUrlList.size() == 0) {
				if (tbodyElementsList.size() > 0)
					parsingComplaintList(tbodyElementsList);
			}
			// dismissdialog
			progressDialog.dismiss();
			// init ListView top of the complaintlist
			((ListView) findViewById(R.id.complaint_list_listview)).setSelection(prevLastPositionOfComplaintList);
			// set preview last position of Complaint list
			// because of offering recent list by clicking 'load more'
			prevLastPositionOfComplaintList = complaintList.size();
			break;
		case LOADING_FAIL:
			// dismissdialog
			progressDialog.dismiss();
			// error weak signal msg
			Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_network_error_weak_signal),
					Toast.LENGTH_SHORT).show();
			break;
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.complaint_list_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.complaint_list_left_slidingbar).setClickable(true);
			break;	
		case MJUConstants.EXECUTE_ACTION:
			if (!listUrl.equals("") && !title.equals("")) {
				// clear list
				initList();
				// show list
				showListFromPost();
				// change main title
				((TextView) findViewById(R.id.complaint_maintitle)).setText(title);
			}
			break;
		}
	}

	/**
	 * 
	 */
	private void init() {
		// init url //general notice url
		listUrl = new String("http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=6&id=mjukr_050300000000");
		// set pageUrlList
		pagingUrlList = new ArrayList<String>();
		// set list posting
		complaintList = new ArrayList<Complaint>();
		//
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		PixelConverter converter = new PixelConverter(this);
		
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		complaintistHandler = new ComplaintListHandler(ComplaintListActivity.this);
		
		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(findViewById(R.id.complaint_list_content), 
					complaintistHandler);
			layoutSlideManager.init((int) ((float)displaymetrics.widthPixels - converter.getWidth(135)));
		}
	}
	
	/**
	 * 
	 */
	private void initLayout(){
		PixelConverter converter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;
		rParams = (LayoutParams) findViewById(R.id.complaint_sub_layout).getLayoutParams();
		rParams.rightMargin = converter.getWidth(135);
		
		rParams = (LayoutParams)findViewById(R.id.complaint_list_sliding_btn).getLayoutParams();
		rParams.width = converter.getWidth(50);
		rParams.height = converter.getHeight(50);
		
		rParams = (LayoutParams)findViewById(R.id.complaint_list_header_icon).getLayoutParams();
		rParams.width = converter.getWidth(30);
		rParams.height = converter.getHeight(30);
		rParams.setMargins(0, 0, converter.getWidth(15), 0);
		
		rParams = (LayoutParams)findViewById(R.id.complaint_list_search_btn).getLayoutParams();
		rParams.width = converter.getWidth(40);
		rParams.height = converter.getHeight(40);
		rParams.setMargins(0, 0, converter.getWidth(10), 0);
		
		rParams = (LayoutParams)findViewById(R.id.complaint_list_write_btn).getLayoutParams();
		rParams.width = converter.getWidth(40);
		rParams.height = converter.getHeight(40);
		rParams.setMargins(0, 0, converter.getWidth(10), 0);
		
		View view = findViewById(R.id.complaint_sidemenu_title);
		LinearLayout.LayoutParams linearlayoutParams =  (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));
		
		view = findViewById(R.id.complaint_subtitle_chapel);
		linearlayoutParams = (LinearLayout.LayoutParams)view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
	}

	/**
	 * 
	 */
	private void initListView() {
		// set adapter
		complaintAdapter = new ComplaintAdapter(getApplicationContext(), complaintList);
		ListView complaintListView = (ListView) findViewById(R.id.complaint_list_listview);
		complaintListView.setAdapter(complaintAdapter);
		complaintAdapter.notifyDataSetChanged();

		// set load next page listener
		((TextView) findViewById(R.id.complaint_list_more)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (NetworkManager.checkNetwork(ComplaintListActivity.this)) {
					if (pagingUrlList.size() > 0) {
						listUrl = pagingUrlList.get(0);
						pagingUrlList.remove(0);
						showListFromPost();
					} else
						Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_load_more_last), Toast.LENGTH_SHORT)
								.show();
				}
			}
		});
	}

	/**
	 * 
	 */
	private void showListFromPost() {
		progressDialog.show(fragmentManager, "");
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// create httpManager
				HttpManager httpManager = new HttpManager();
				// init
				httpManager.init();
				// set httpPost
				httpManager.setHttpPost(listUrl);
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
						tagNames.add(HTMLElementName.TBODY);
						tagNames.add(HTMLElementName.DIV);
						// parse result
						HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
								entity, tagNames, HttpManager.UTF_8);
						// send result to handler
						Message msg = complaintistHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						complaintistHandler.sendMessage(msg);
					} else
						complaintistHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					complaintistHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					complaintistHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		});
		thread.start();
	}

	/**
	 * 
	 */
	private void showListFromGet() {
		// showDialog
		progressDialog.show(fragmentManager, "");
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// create httpManager
				HttpManager httpManager = new HttpManager();
				// init
				httpManager.init();
				// set httpGet
				httpManager.setHttpGet(listUrl);
				// httpResponse
				HttpResponse response = null;
				// execute
				try {
					response = httpManager.executeHttpGet();
					// get Status
					StatusLine status = response.getStatusLine();
					if (status.getStatusCode() == HttpStatus.SC_OK) {
						// html response result
						HttpEntity entity = response.getEntity();
						// parsing element
						Vector<String> tagNames = new Vector<String>();
						tagNames.add(HTMLElementName.TBODY);
						tagNames.add(HTMLElementName.DIV);
						// parse result
						HashMap<String, List<Element>> elementMap = httpManager.getHttpElementsMap(
								entity, tagNames, HttpManager.UTF_8);
						// send result to handler
						Message msg = complaintistHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						complaintistHandler.sendMessage(msg);
					} else
						complaintistHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					complaintistHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					complaintistHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		});
		thread.start();
	}

	/**
	 * 寃뚯떆��湲�紐⑸줉 �뚯떛
	 * 
	 * @param elementList
	 */
	private void parsingComplaintList(List<Element> elementList) {
		// get <tr> tags
		List<Element> trList = elementList.get(0).getAllElements(HTMLElementName.TR);

		for (Element trElement : trList) {
			// <tr> 1媛쒖쓽 6媛쒖쓽 <td>
			List<Element> tdList = trElement.getAllElements(HTMLElementName.TD);
			Complaint complaint = new Complaint();
			boolean itHasNumber = true;
			// get subject, date, writer, status
			for (int index = 0; index < tdList.size(); index++) {
				// 湲�踰덊샇 / �ㅼ젣濡��듯빀誘쇱썝�쇳꽣 寃뚯떆��由ъ뒪�몄뿉�쒕뒗 湲�踰덊샇瑜�異쒕젰�섏� �딆쑝��				// 寃�깋 �쒖뿉 寃�깋寃곌낵���깅줉 寃뚯떆臾쇱씠 �놁쓣 寃쎌슦 泥섎━瑜��꾪빐 �ъ슜
				if (index == 0) {
					String num = tdList.get(index).getContent().toString().trim();
					/*
					 * 怨듭�濡�理쒖긽��由ъ뒪�몄뿉 �щ씪��엳��寃쎌슦, �ㅼ젣 寃뚯떆紐⑸줉�먮룄 以묐났 �깅줉�섏뼱 �덈떎. �댁쟾 援ы쁽 踰꾩쟾�먯꽌��
					 * �닿쾬��由ъ뒪�몄뿉 �ы븿�쒖섟�붾뜲, '怨듭�' 紐⑸줉��留롮쓣 寃쎌슦, load more���대룄 怨꾩냽 以묐났�섏꽌
					 * 由ъ뒪�몄뿉 �ы븿�섎뒗 寃쎌슦媛�諛쒖깮�쒕떎. '怨듭�'��由ъ뒪�몄뿉���쒖쇅
					 */
					if (num.contains("<img")) {
						itHasNumber = false;
						break;
					}
					// �깅줉��寃뚯떆臾쇱씠 �놁쓣 寃쎌슦, return 泥섎━
					if (num.contains(getResources().getString(R.string.msg_no_list))) {
						Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_no_list), Toast.LENGTH_SHORT).show();
						return;
					}
				}
				// 湲�踰덊샇媛��덉쓣 寃쎌슦留��뚯떛
				if (itHasNumber) {
					// �쒕ぉ
					if (index == 1) {
						Element aElement = tdList.get(index).getFirstElement(HTMLElementName.A);
						String subject = aElement.getContent().toString().trim();
						String url = aElement.getAttributeValue("href").trim();
						if (subject.contains("</a>"))
							subject = subject.substring(0, subject.indexOf("</a>"));
						else if (subject.equals(""))
							subject = "�쒕ぉ�놁쓬";
						if (url == null)
							url = "";
						complaint.setC_subject(subject);
						complaint.setC_hidden_url(url);
					}
					// �좎쭨
					else if (index == 2) {
						String date = tdList.get(index).getContent().toString().trim();
						if (date == null)
							date = getString(R.string.community_no_date);
						complaint.setC_date(date);
					}
					// 泥섎━�곹깭
					else if (index == 3) {
						String status = tdList.get(index).getContent().toString();
						if (status == null)
							status = getString(R.string.community_no_result);
						if (status.contains("<"))
							status = status.replaceAll("<{1}.[^<>]*>{1}", "");
						complaint.setC_status(status.trim());
					}
					// �좎껌��					else if (index == 4) {
						String writer = tdList.get(index).getContent().toString();
						if (writer == null)
							writer = getString(R.string.community_no_writer);
						complaint.setC_writer(writer.trim());
					}
				}
			// 湲�쾲�멸� �덉쓣寃쎌슦留�異붽�
			if (itHasNumber){
				complaintList.add(complaint);
			}
		}
		complaintAdapter.notifyDataSetChanged();
	}

	/**
	 * 寃뚯떆���섎떒 �섏씠吏��섎쾭 �뚯떛
	 * 
	 * @param elementList
	 */
	private void parsingPagingList(List<Element> elementList) {
		// get <div> element
		List<Element> divList = elementList.get(0).getAllElements(HTMLElementName.DIV);

		for (Element divElement : divList) {
			// get <div class="paging"> element
			String attr = divElement.getAttributeValue("class");
			if (attr != null) {
				if (attr.trim().equals("paging")) {
					// get <strong> element to find current page
					Element strongElement = divElement.getFirstElement(HTMLElementName.STRONG);
					String currentPage = strongElement.getContent().toString().trim();
					if (currentPage != null) {
						// get <a> element list
						List<Element> aList = divElement.getAllElements(HTMLElementName.A);
						// get nextPages
						for (Element aElement : aList) {
							String content = aElement.getContent().toString().trim();
							// 泥섏쓬 紐⑸줉 �대룞, �댁쟾 紐⑸줉�쇰줈 �대룞���쒖쇅�쒗궓��							// �ㅼ쓬 紐⑸줉�쇰줈 �대룞怨� 留덉�留�紐⑸줉�쇰줈 �대룞留��ы븿�쒕떎
							if (content.startsWith("<img")) {
								String title = aElement.getAttributeValue("title").trim();
								if (title.startsWith("�ㅼ쓬")) {
									String url = getString(R.string.complaint_url)
											+ aElement.getAttributeValue("href").trim();
									pagingUrlList.add(url);
									break;
								}
								/**********
								 * 2012.05.31 "留덉�留됱쑝濡��대룞" 泥섎━ 蹂�꼍 援녹씠 留덉�留됱쑝濡��대룞��泥섎━�댁쨪
								 * �꾩슂媛��놁쓬. �명꽣�섏씠���뺥깭媛��ㅼ쓬 �섏씠吏�줈 �대룞留��쒓났�섎�濡� �꾩옱 �섏씠吏��
								 * 留덉�留됱뿉 �ㅻ떎��떎怨��댁꽌 留덉�留됱쑝濡��대룞���쒓났�댁쨪 �꾩슂媛��놁쓬
								 **********/
								 else if (title.startsWith("마지막")) {
									 String url = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/"
									 + aElement.getAttributeValue("href").trim();
								// // 媛�옣 留덉�留��섏씠吏�줈 媛붿쓣 寃쎌슦,
								// // �꾩옱 �섏씠吏��ㅼ쓬��"留덉�留됱쑝濡��대룞"���쒕떎. �ш린���덉쇅泥섎━瑜��댁＜吏�								// // �딆쑝硫�								// // 留�留덉�留��섏씠吏�� 以묐났�쇰줈 怨꾩냽 由ъ뒪�몄뿉 �щ씪媛�쾶 �쒕떎.
								 	if (pagingUrlList.size() == 0){
								 		break;
								 	}else{
								 		pagingUrlList.add(url);
								 	}
								 }
							}else if (Integer.valueOf(content) > Integer.valueOf(currentPage)) {
									 String url = getString(R.string.complaint_url) + aElement.getAttributeValue("href").trim();
									 pagingUrlList.add(url);

							}
						}
					}
					/**** if (currentPage != null) ******/
			}
		}
	}
}
	

	/**
	 * 
	 */
	private void setSubtitleClickListener() {
		((TextView) findViewById(R.id.complaint_subtitle_chapel)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_loan)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_commute)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_amenities)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_handicapped)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_study_abroad)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_military)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_absence)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_bachelor)).setOnClickListener(this);
		((TextView) findViewById(R.id.complaint_subtitle_suggestion)).setOnClickListener(this);
		
		((ImageButton) findViewById(R.id.complaint_list_search_btn)).setOnClickListener(this);
		((ImageButton) findViewById(R.id.complaint_list_write_btn)).setOnClickListener(this);
		findViewById(R.id.complaint_list_sliding_btn).setOnTouchListener(this);
		findViewById(R.id.complaint_list_left_slidingbar).setOnTouchListener(this);
	}

	/**
	 * @param keyword
	 */
	private void searchKeyword(String keyword) {
		// set post url
		listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&listType=06&id=mjukr_050300000000&column=TITLE&search="
				+ keyword;
		// clear list
		initList();
		// showList
		showListFromGet();
		// setMainTitle
		((TextView) findViewById(R.id.complaint_maintitle)).setText("");
		((TextView) findViewById(R.id.complaint_maintitle)).setText(getResources().getString(R.string.title_search));
	}

	/**
	 * 
	 */
	private void initList() {
		// clear list
		complaintList.clear();
		pagingUrlList.clear();
		complaintAdapter.notifyDataSetChanged();
		prevLastPositionOfComplaintList = 0;
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
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		// checkNetwork
		if (NetworkManager.checkNetwork(this)) {
			switch (id) {
			case R.id.complaint_subtitle_chapel:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=6&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_chapel);
				break;
			case R.id.complaint_subtitle_loan:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=7&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_scholarship);
				break;
			case R.id.complaint_subtitle_commute:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=8&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_bus);
				break;
			case R.id.complaint_subtitle_amenities:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=9&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_facility);
				break;
			case R.id.complaint_subtitle_handicapped:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=10&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_handicap);
				break;
			case R.id.complaint_subtitle_military:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=11&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_military);
				break;
			case R.id.complaint_subtitle_study_abroad:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=12&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_interchange);
				break;
			case R.id.complaint_subtitle_absence:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=13&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_certificate);
				break;
			case R.id.complaint_subtitle_bachelor:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=14&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_bachelor);
				break;
			case R.id.complaint_subtitle_suggestion:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint/list.jsp?boardId=5203&mcategoryId=15&id=mjukr_050300000000";
				title = getString(R.string.complaint_sidemenu_suggestion);
				break;
			case R.id.complaint_list_search_btn:
				if (NetworkManager.checkNetwork(ComplaintListActivity.this))
					onSearchRequested();
				return;
			case R.id.complaint_list_write_btn:
				// check login
				if (LoginManager.checkLogin(ComplaintListActivity.this) && NetworkManager.checkNetwork(ComplaintListActivity.this)) {
					// start complaintWriteActivity
					Intent intent = new Intent(ComplaintListActivity.this, ComplaintWriteActivity.class);
					startActivityForResult(intent, RQ_WRITE_BOARD);
				} else if (NetworkManager.checkNetwork(ComplaintListActivity.this)) {
					// start login
					Intent intent = new Intent(ComplaintListActivity.this, LoginActivity.class);
					startActivityForResult(intent, RQ_WRITE_AFTER_LOGIN);
				}
				return;
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
	
	private class ComplaintAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<Complaint> mComplaintList;

		public ComplaintAdapter(Context _context, ArrayList<Complaint> _complaintList) {
			// TODO Auto-generated constructor stub
			this.mInflater = (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			this.mComplaintList = _complaintList;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mComplaintList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mComplaintList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ComplaintViewHolder vh;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.complaint_list_row_layout, parent, false);
				vh = new ComplaintViewHolder();

				// set viewholder
				vh.setSubjectView((TextView) convertView.findViewById(R.id.complaint_list_row_subject));
				vh.setWriterView((TextView) convertView.findViewById(R.id.complaint_list_row_writer));
				vh.setDateView((TextView) convertView.findViewById(R.id.complaint_list_row_date));
				vh.setStatusView((TextView) convertView.findViewById(R.id.complaint_list_row_status));
				vh.setHiddenUrlView((TextView) convertView.findViewById(R.id.complaint_list_row_hidden_url));
				// set viewholder to convertview
				convertView.setTag(vh);
			} else
				vh = (ComplaintViewHolder) convertView.getTag();

			// set data
			((TextView) vh.getSubjectView()).setText(mComplaintList.get(position).getC_subject());
			((TextView) vh.getWriterView()).setText(mComplaintList.get(position).getC_writer());
			((TextView) vh.getDateView()).setText(mComplaintList.get(position).getC_date());
			((TextView) vh.getHiddenUrlView()).setText(mComplaintList.get(position).getC_hidden_url());
			((TextView) vh.getStatusView()).setText(mComplaintList.get(position).getC_status());

			// set click listener
			final int pos = position;
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (mComplaintList.get(pos) != null) {
						Intent intent = new Intent(ComplaintListActivity.this, ComplaintViewActivity.class);
						intent.putExtra("url", mComplaintList.get(pos).getC_hidden_url());
						intent.putExtra("subtitle", ((TextView) findViewById(R.id.complaint_maintitle)).getText().toString());
						startActivity(intent);
					}
				}
			});
			return convertView;
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
	
	static class ComplaintListHandler extends Handler {
		private final WeakReference<ComplaintListActivity> complaintListAcivity;
		
		public ComplaintListHandler(ComplaintListActivity activity) {
			// TODO Auto-generated constructor stub
			complaintListAcivity = new WeakReference<ComplaintListActivity>(activity);
		}
		
		@Override
		public void handleMessage(Message msg) { 
			// TODO Auto-generated method stub
			ComplaintListActivity activity = complaintListAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */
