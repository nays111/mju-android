package kr.ac.mju.mjuapp.notice;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
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
 * @author davidkim, hs
 * 
 */
public class NoticeListActivity extends FragmentActivity implements
		OnClickListener, OnItemClickListener, OnTouchListener {
	private final int LOADING_SUCCESS = 11;
	private final int LOADING_FAIL = 12;

	private String listUrl;
	private String title;
	private NoticeAdapter noticeAdapter;
	private ArrayList<Notice> noticeList;
	private ArrayList<String> pagingUrlList;

	private int prevLastPositionOfNoticeList = 0;
	// subTitle(general= 0, ... , bid = 6)
	private int currentSubTitle = 0;

	private LayoutSlideManager layoutSlideManager;

	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private NoticeListHandler noticeListHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice_list_layout);

		// init
		init();
		// init layout
		initLayout();
		// init listView
		initListView();

		if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
			openLayout();
		}

		// set subtitle click listener
		setSubtitleClickListener();
		// set search btn listener
		((ImageButton) findViewById(R.id.notice_list_search_btn))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (NetworkManager
								.checkNetwork(NoticeListActivity.this))
							onSearchRequested();
					}
				});
		findViewById(R.id.notice_list_sliding_btn).setOnTouchListener(this);
		findViewById(R.id.notice_list_left_slidingbar).setOnTouchListener(this);
	}

	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case LOADING_SUCCESS:
			// get tag element list
			@SuppressWarnings("unchecked")
			HashMap<String, List<Element>> elementMap = (HashMap<String, List<Element>>) msg.obj;
			List<Element> tbodyElementsList = elementMap
					.get(HTMLElementName.TBODY);
			List<Element> pageElementsList = elementMap
					.get(HTMLElementName.DIV);
			// parsing paging list
			if (pageElementsList != null && pagingUrlList.size() == 0)
				parsingPagingList(pageElementsList);
			// parsing article list
			if (pagingUrlList.size() > 0) {
				if (tbodyElementsList != null)
					parsingNoticeList(tbodyElementsList);
			} else if (pagingUrlList.size() == 0) {
				if (tbodyElementsList.size() > 0)
					parsingNoticeList(tbodyElementsList);
			}
			// dismissdialog
			progressDialog.dismiss();
			// init ListView top of the noticeList
			((ListView) findViewById(R.id.notice_list_listview))
					.setSelection(prevLastPositionOfNoticeList);
			// set preview last position of Article list
			// because of offering recent list by clicking 'load more'
			prevLastPositionOfNoticeList = noticeList.size();
			break;
		case LOADING_FAIL:
			// dismissdialog
			progressDialog.dismiss();
			// error weak signal msg
			Toast.makeText(
					getBaseContext(),
					getResources().getString(
							R.string.msg_network_error_weak_signal),
					Toast.LENGTH_SHORT).show();
			break;
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.notice_list_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.notice_list_left_slidingbar).setClickable(true);
			break;
		case MJUConstants.EXECUTE_ACTION:
			if (!listUrl.equals("") && !title.equals("")) {
				// clear list
				initList();
				// show list
				showListFromPost();
				// change main title
				((TextView) findViewById(R.id.notice_maintitle)).setText(title);
			}
			break;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
			String keyword = intent.getStringExtra(SearchManager.QUERY);
			searchKeyword(keyword.trim());
		}
	}

	/**
	 * 
	 */
	private void init() {
		title = "";
		listUrl = "";
		// set pageUrlList
		pagingUrlList = new ArrayList<String>();
		// set list posting
		noticeList = new ArrayList<Notice>();

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		PixelConverter converter = new PixelConverter(this);

		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		noticeListHandler = new NoticeListHandler(NoticeListActivity.this);

		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(
					findViewById(R.id.notice_list_content), noticeListHandler);
			layoutSlideManager
					.init((int) ((float) displaymetrics.widthPixels - converter
							.getWidth(135)));
		}

	}

	/**
	 * 
	 */
	private void initLayout() {
		PixelConverter converter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;
		rParams = (LayoutParams) findViewById(R.id.notice_sub_layout)
				.getLayoutParams();
		rParams.rightMargin = converter.getWidth(135);
		findViewById(R.id.notice_sub_layout).setLayoutParams(rParams);

		rParams = (LayoutParams) findViewById(R.id.notice_list_sliding_btn)
				.getLayoutParams();
		rParams.width = converter.getWidth(50);
		rParams.height = converter.getHeight(50);

		rParams = (LayoutParams) findViewById(R.id.notice_list_header_icon)
				.getLayoutParams();
		rParams.width = converter.getWidth(30);
		rParams.height = converter.getHeight(30);
		rParams.setMargins(0, 0, converter.getWidth(15), 0);

		rParams = (LayoutParams) findViewById(R.id.notice_list_search_btn)
				.getLayoutParams();
		rParams.width = converter.getWidth(40);
		rParams.height = converter.getHeight(40);
		rParams.setMargins(0, 0, converter.getWidth(10), 0);

		View view = findViewById(R.id.notice_sidemenu_title);
		LinearLayout.LayoutParams linearlayoutParams = (LinearLayout.LayoutParams) view
				.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));

		view = findViewById(R.id.notice_subtitle_general);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
	}

	/**
	 * 寃뚯떆��湲�紐⑸줉 ListView 珥덇린��
	 */
	private void initListView() {
		// set adapter
		noticeAdapter = new NoticeAdapter(getApplicationContext(), noticeList);
		ListView generalNoticeListView = (ListView) findViewById(R.id.notice_list_listview);
		generalNoticeListView.setAdapter(noticeAdapter);
		generalNoticeListView.setOnItemClickListener(this);
		noticeAdapter.notifyDataSetChanged();

		// set load next page listener
		((TextView) findViewById(R.id.notice_list_more))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (NetworkManager
								.checkNetwork(NoticeListActivity.this)) {
							if (pagingUrlList.size() > 0) {
								listUrl = pagingUrlList.get(0);
								pagingUrlList.remove(0);
								showListFromPost();
							} else
								Toast.makeText(
										getBaseContext(),
										getResources().getString(
												R.string.msg_load_more_last),
										Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	/**
	 * 湲�紐⑸줉 �곗씠��珥덇린��
	 */
	private void initList() {
		// clear list
		noticeList.clear();
		pagingUrlList.clear();
		noticeAdapter.notifyDataSetChanged();
		prevLastPositionOfNoticeList = 0;
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

	/**
	 * 寃뚯떆��湲�紐⑸줉 POST �붿껌
	 */
	private void showListFromPost() {
		progressDialog.show(fragmentManager, "");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
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
						HashMap<String, List<Element>> elementMap = httpManager
								.getHttpElementsMap(entity, tagNames,
										HttpManager.UTF_8);
						// send result to handler
						Message msg = noticeListHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						noticeListHandler.sendMessage(msg);
					} else
						noticeListHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					noticeListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					noticeListHandler.sendEmptyMessage(LOADING_FAIL);
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
	private void parsingNoticeList(List<Element> elementList) {
		// get <tr> tags
		List<Element> trList = elementList.get(0).getAllElements(
				HTMLElementName.TR);

		for (Element trElement : trList) {
			// <tr> 1媛쒖쓽 6媛쒖쓽 <td>
			List<Element> tdList = trElement.getAllElements(HTMLElementName.TD);
			Notice notice = new Notice();
			boolean itHasNumber = true;
			// �몃� 湲�紐⑸줉 �뺣낫 �뚯떛
			for (int index = 0; index < tdList.size(); index++) {
				// 湲�踰덊샇
				if (index == 0) {
					String num = tdList.get(index).getContent().toString()
							.trim();
					/*
					 * 怨듭�濡�理쒖긽��由ъ뒪�몄뿉 �щ씪��엳��寃쎌슦, �ㅼ젣 寃뚯떆紐⑸줉�먮룄 以묐났 �깅줉�섏뼱
					 * �덈떎. �댁쟾 援ы쁽 踰꾩쟾�먯꽌�� �닿쾬��由ъ뒪�몄뿉 �ы븿�쒖섟�붾뜲, '怨듭�'
					 * 紐⑸줉��留롮쓣 寃쎌슦, load more���대룄 怨꾩냽 以묐났�섏꽌 由ъ뒪�몄뿉 �ы븿�섎뒗
					 * 寃쎌슦媛�諛쒖깮�쒕떎. '怨듭�'��由ъ뒪�몄뿉���쒖쇅
					 */
					if (num.contains("<img")) {
						itHasNumber = false;
						break;
					}
					// �깅줉��寃뚯떆臾쇱씠 �놁쓣 寃쎌슦, return 泥섎━
					else if (num.contains(getResources().getString(
							R.string.msg_no_list))) {
						Toast.makeText(getBaseContext(),
								getResources().getString(R.string.msg_no_list),
								Toast.LENGTH_SHORT).show();
						return;
					}
					notice.setA_num(num);
				}
				// 湲�踰덊샇媛��덉쓣 寃쎌슦留��뚯떛
				if (itHasNumber) {
					// �쒕ぉ
					if (index == 1) {
						Element aElement = tdList.get(index).getFirstElement(
								HTMLElementName.A);
						String subject = aElement.getContent().toString()
								.trim();
						String url = aElement.getAttributeValue("href").trim();
						if (subject.contains("</a>"))
							subject = subject.substring(0,
									subject.indexOf("</a>"));
						else if (subject.equals(""))
							subject = getString(R.string.community_no_title);
						if (url == null)
							url = "";
						notice.setA_subject(subject);
						notice.setA_url(url);
					}
					// �좎쭨
					else if (index == 2) {
						String date = tdList.get(index).getContent().toString()
								.trim();
						if (date == null)
							date = getString(R.string.community_no_date);
						notice.setA_date(date);
					}
					// 泥⑤��뚯씪 �щ�
					else if (index == 4) {
						String attachFile = tdList.get(index).getContent()
								.toString();
						if (attachFile != null) {
							if (attachFile.contains("<img"))
								notice.setA_file(true);
							else
								notice.setA_file(false);
						}
					}
				}
			}
			// 湲�쾲�멸� �덉쓣寃쎌슦留�異붽�
			if (itHasNumber)
				noticeList.add(notice);
		}
		noticeAdapter.notifyDataSetChanged();
	}

	/**
	 * 寃뚯떆���섎떒 �섏씠吏��섎쾭 �뚯떛
	 * 
	 * @param elementList
	 */
	private void parsingPagingList(List<Element> elementList) {
		// get <div> element
		List<Element> divList = elementList.get(0).getAllElements(
				HTMLElementName.DIV);

		for (Element divElement : divList) {
			// get <div class="paging"> element
			String attr = divElement.getAttributeValue("class");
			if (attr != null) {
				if (attr.trim().equals("paging")) {
					// get <strong> element to find current page
					Element strongElement = divElement
							.getFirstElement(HTMLElementName.STRONG);
					String currentPage = strongElement.getContent().toString()
							.trim();
					if (currentPage != null) {
						// get <a> element list
						List<Element> aList = divElement
								.getAllElements(HTMLElementName.A);
						// get nextPages
						for (Element aElement : aList) {
							String content = aElement.getContent().toString()
									.trim();
							// 泥섏쓬 紐⑸줉 �대룞, �댁쟾 紐⑸줉�쇰줈 �대룞���쒖쇅�쒗궓�� // �ㅼ쓬
							// 紐⑸줉�쇰줈 �대룞怨� 留덉�留�紐⑸줉�쇰줈 �대룞留��ы븿�쒕떎
							// �ㅼ쓬 紐⑸줉�쇰줈 �대룞���덉쓣 寃쎌슦 , 留덉�留�紐⑸줉�쇰줈
							// �대룞���ы븿�쒗궎吏��딅뒗��
							if (content.startsWith("<img")) {
								String title = aElement.getAttributeValue(
										"title").trim();
								if (title.startsWith("�ㅼ쓬")) {
									String url = getString(R.string.community_board_url)
											+ aElement
													.getAttributeValue("href")
													.trim();
									pagingUrlList.add(url);
									break;
								}
							} else if (Integer.valueOf(content) > Integer
									.valueOf(currentPage)) {
								String url = getString(R.string.community_board_url)
										+ aElement.getAttributeValue("href")
												.trim();
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
	 * 寃뚯떆��subtitle clickListener
	 */
	private void setSubtitleClickListener() {
		((TextView) findViewById(R.id.notice_subtitle_general))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.notice_subtitle_event))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.notice_subtitle_grade))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.notice_subtitle_bachelor))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.notice_subtitle_scholarship))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.notice_subtitle_job))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.notice_subtitle_bid))
				.setOnClickListener(this);
	}

	/**
	 * 寃뚯떆��湲�寃�깋
	 * 
	 * @param keyword
	 */
	private void searchKeyword(String keyword) {
		// set post url
		listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?";

		switch (currentSubTitle) {
		// general
		case 0:
			listUrl += "qt=&boardId=11294&listType=01&id=mjukr_050101000000&column=TITLE&search="
					+ keyword;
			break;
		// event
		case 1:
			listUrl += "qt=&listType=01&column=TITLE&boardId=11302&id=mjukr_050102000000&search="
					+ keyword;
			break;
		// grade
		case 2:
			listUrl += "qt=&listType=01&column=TITLE&boardId=11310&id=mjukr_050103000000&search="
					+ keyword;
			break;
		// bachelor
		case 3:
			listUrl += "qt=&listType=01&column=TITLE&boardId=11318&id=mjukr_050104000000&search="
					+ keyword;
			break;
		// scholarship
		case 4:
			listUrl += "qt=&listType=01&column=TITLE&boardId=11327&id=mjukr_050106000000&search="
					+ keyword;
			break;
		// job
		case 5:
			listUrl += "qt=&listType=01&column=TITLE&boardId=11366&id=mjukr_050107000000&search="
					+ keyword;
			break;
		// bid
		case 6:
			listUrl += "qt=&listType=01&column=TITLE&boardId=15103&id=mjukr_050105000000&search="
					+ keyword;
			break;
		}
		// clear list
		initList();
		// showList
		showListFromGet();
		// setMainTitle
		((TextView) findViewById(R.id.notice_maintitle)).setText("");
		((TextView) findViewById(R.id.notice_maintitle)).setText(getResources()
				.getString(R.string.title_search));
	}

	/**
	 * 寃뚯떆��湲�紐⑸줉 GET �붿껌
	 */
	private void showListFromGet() {
		// showDialog
		progressDialog.show(fragmentManager, "");
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
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
						HashMap<String, List<Element>> elementMap = httpManager
								.getHttpElementsMap(entity, tagNames,
										HttpManager.UTF_8);
						// send result to handler
						Message msg = noticeListHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						noticeListHandler.sendMessage(msg);
					} else
						noticeListHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					noticeListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					noticeListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		});
		thread.start();
	}

	/**
	 * 寃뚯떆��subTitle �좏깮 �� �대깽��泥섎━
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		// checkNetwork
		if (NetworkManager.checkNetwork(this)) {
			switch (id) {
			case R.id.notice_subtitle_general:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=11294&id=mjukr_050101000000";
				title = getResources().getString(
						R.string.notice_sidemenu_general);
				currentSubTitle = 0;
				break;
			case R.id.notice_subtitle_event:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=11302&id=mjukr_050102000000";
				title = getResources()
						.getString(R.string.notice_sidemenu_event);
				currentSubTitle = 1;
				break;
			case R.id.notice_subtitle_grade:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=11310&id=mjukr_050103000000";
				title = getResources()
						.getString(R.string.notice_sidemenu_grade);
				currentSubTitle = 2;
				break;
			case R.id.notice_subtitle_bachelor:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=11318&id=mjukr_050104000000";
				title = getResources().getString(
						R.string.notice_sidemenu_bachelor);
				currentSubTitle = 3;
				break;
			case R.id.notice_subtitle_scholarship:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=11327&id=mjukr_050106000000";
				title = getResources().getString(
						R.string.notice_sidemenu_scholarship);
				currentSubTitle = 4;
				break;
			case R.id.notice_subtitle_job:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=11366&id=mjukr_050107000000";
				title = getResources().getString(R.string.notice_sidemenu_job);
				currentSubTitle = 5;
				break;
			case R.id.notice_subtitle_bid:
				listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/board/list.jsp?boardId=15103&id=mjukr_050105000000";
				title = getResources().getString(R.string.notice_sidemenu_bid);
				currentSubTitle = 6;
				break;
			}

			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(true);
			}
		}
	}

	/**
	 * 寃뚯떆��湲��좏깮 �� 湲��몃��댁슜 蹂닿린 �꾪븳 �대깽��泥섎━
	 * 
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
	 *      android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(NoticeListActivity.this,
				NoticeViewActivity.class);
		intent.putExtra("url", noticeList.get(position).getA_url());
		intent.putExtra("subtitle",
				((TextView) findViewById(R.id.notice_maintitle)).getText()
						.toString());
		startActivity(intent);
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

	static class NoticeListHandler extends Handler {
		private final WeakReference<NoticeListActivity> noticeListAcivity;

		public NoticeListHandler(NoticeListActivity activity) {
			// TODO Auto-generated constructor stub
			noticeListAcivity = new WeakReference<NoticeListActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			NoticeListActivity activity = noticeListAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */