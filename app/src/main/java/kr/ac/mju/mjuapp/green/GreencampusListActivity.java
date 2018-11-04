package kr.ac.mju.mjuapp.green;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import kr.ac.mju.mjuapp.R;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author davidkim
 * 
 */
public class GreencampusListActivity extends FragmentActivity implements
		OnItemClickListener, OnClickListener {
	private final int LOADING_SUCCESS = 11;
	private final int LOADING_FAIL = 12;

	private String listUrl;

	private GreencampusAdapter greencampusAdapter;
	private ArrayList<Greencampus> greencampusList;
	private ArrayList<String> pageUrlList;

	private FragmentManager fragmentManager;
	private MJUProgressDialog progressDialog;
	private GreenCampusListHandler greenCampusListHandler;

	private int prevLastPositionOfList = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.greencampus_list_layout);

		init();
		initLayout();
		// show list
		if (NetworkManager.checkNetwork(this)) {
			showListFromPost();
		}

		((ImageButton) findViewById(R.id.green_list_write_btn))
				.setOnClickListener(this);
		((ImageButton) findViewById(R.id.green_list_search_btn))
				.setOnClickListener(this);
		((TextView) findViewById(R.id.green_list_more))
				.setOnClickListener(this);
	}

	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case LOADING_SUCCESS:
			// parsing
			@SuppressWarnings("unchecked")
			HashMap<String, List<Element>> elementMap = (HashMap<String, List<Element>>) msg.obj;
			List<Element> tbodyElementsList = elementMap
					.get(HTMLElementName.TBODY);
			List<Element> pageElementsList = elementMap
					.get(HTMLElementName.DIV);
			// parsing page data
			if (pageElementsList != null && pageUrlList.size() == 0)
				parsingPageList(pageElementsList);
			if (pageUrlList.size() > 0) {
				// parsing list data
				if (tbodyElementsList != null)
					parsingList(tbodyElementsList);
			} else if (pageUrlList.size() == 0) {
				if (tbodyElementsList.size() > 0)
					parsingList(tbodyElementsList);
			}
			// dismissdialog
			progressDialog.dismiss();
			// move listView on new top
			((ListView) findViewById(R.id.green_list_listview))
					.setSelection(prevLastPositionOfList);
			prevLastPositionOfList = greencampusList.size();
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
		}
	}

	private void init() {
		// TODO Auto-generated method stub
		fragmentManager = getSupportFragmentManager();
		progressDialog = new MJUProgressDialog();
		greenCampusListHandler = new GreenCampusListHandler(
				GreencampusListActivity.this);

		pageUrlList = new ArrayList<String>();
		// set url // greencampus board home url
		listUrl = new String(
				"http://www.mju.ac.kr/mbs/mjukr/jsp/complaint2/list.jsp?boardId=7088978&id=mjukr_110500000000");
		// set pageUrlList

		// set listview
		greencampusList = new ArrayList<Greencampus>();
		greencampusAdapter = new GreencampusAdapter(getApplicationContext(),
				greencampusList);
		ListView greenListView = (ListView) findViewById(R.id.green_list_listview);
		greenListView.setAdapter(greencampusAdapter);
		greenListView.setOnItemClickListener(this);
		greencampusAdapter.notifyDataSetChanged();
	}

	private void initLayout() {
		// TODO Auto-generated method stub
		PixelConverter pixelConveter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;

		rParams = (RelativeLayout.LayoutParams) findViewById(
				R.id.green_list_header_icon).getLayoutParams();
		rParams.width = pixelConveter.getWidth(30);
		rParams.height = pixelConveter.getHeight(30);
		rParams.setMargins(0, 0, pixelConveter.getWidth(15), 0);

		rParams = (LayoutParams) findViewById(R.id.green_list_search_btn)
				.getLayoutParams();
		rParams.width = pixelConveter.getWidth(40);
		rParams.height = pixelConveter.getHeight(40);
		rParams.setMargins(0, 0, pixelConveter.getWidth(10), 0);

		rParams = (LayoutParams) findViewById(R.id.green_list_write_btn)
				.getLayoutParams();
		rParams.width = pixelConveter.getWidth(40);
		rParams.height = pixelConveter.getHeight(40);
		rParams.setMargins(0, 0, pixelConveter.getWidth(10), 0);
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
						HashMap<String, List<Element>> elementMap = httpManager
								.getHttpElementsMap(entity, tagNames,
										HttpManager.UTF_8);
						// send result to handler
						Message msg = greenCampusListHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						greenCampusListHandler.sendMessage(msg);
					} else
						greenCampusListHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					greenCampusListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					greenCampusListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		});
		thread.start();
	}

	/**
	 * @param elementList
	 */
	private void parsingList(List<Element> elementList) {
		// get <tr> tags
		List<Element> trList = elementList.get(0).getAllElements(
				HTMLElementName.TR);

		for (Element trElement : trList) {
			// <tr> 1媛쒖쓽 6媛쒖쓽 <td>
			List<Element> tdList = trElement.getAllElements(HTMLElementName.TD);
			Greencampus greencampus = new Greencampus();
			boolean itHasNumber = true;
			// �몃� 湲�紐⑸줉 �뺣낫 �뚯떛.
			for (int index = 0; index < tdList.size(); index++) {
				// 湲�踰덊샇 / �ㅼ젣濡�洹몃┛ 罹좏띁��寃뚯떆��由ъ뒪�몄뿉�쒕뒗 湲�踰덊샇瑜�異쒕젰�섏� �딆쑝�� //
				// 寃�깋 �쒖뿉 寃�깋寃곌낵���깅줉 寃뚯떆臾쇱씠 �놁쓣 寃쎌슦 泥섎━瑜��꾪빐 �ъ슜
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
						greencampus.setP_subject(subject);
						greencampus.setP_url(url);
					}
					// �좎쭨
					else if (index == 2) {
						String date = tdList.get(index).getContent().toString();
						if (date == null)
							date = getString(R.string.community_no_date);
						greencampus.setP_date(date.trim());
					}
					// 泥섎━�곹깭
					else if (index == 3) {
						String status = tdList.get(index).getContent()
								.toString();
						if (status == null)
							status = getString(R.string.community_no_result);
						if (status.contains("<"))
							status = status.replaceAll("<{1}.[^<>]*>{1}", "");
						greencampus.setP_status(status.trim());
					} else if (index == 4) {
						String writer = tdList.get(index).getContent()
								.toString();
						if (writer == null)
							writer = getString(R.string.community_not_show_writer);
						greencampus.setP_writer(writer.trim());
					}
				}
			}
			// 湲�踰덊샇媛��덉쓣 寃쎌슦留�異붽�
			if (itHasNumber)
				greencampusList.add(greencampus);
		}
		greencampusAdapter.notifyDataSetChanged();
	}

	/**
	 * @param elementList
	 */
	private void parsingPageList(List<Element> elementList) {
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
							if (content.startsWith("<img")) {
								String title = aElement.getAttributeValue(
										"title").trim();
								if (title.startsWith("�ㅼ쓬")) {
									String url = getString(R.string.complaint_url2)
											+ aElement
													.getAttributeValue("href")
													.trim();
									pageUrlList.add(url);
									break;
								}
							} else if (Integer.valueOf(content) > Integer
									.valueOf(currentPage)) {
								String url = getString(R.string.complaint_url2)
										+ aElement.getAttributeValue("href")
												.trim();
								pageUrlList.add(url);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 */
	private void initList() {
		// clear List
		greencampusList.clear();
		pageUrlList.clear();
		greencampusAdapter.notifyDataSetChanged();
		prevLastPositionOfList = 0;
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
				// set httpPost
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
						Message msg = greenCampusListHandler.obtainMessage();
						msg.what = LOADING_SUCCESS;
						msg.obj = elementMap;
						greenCampusListHandler.sendMessage(msg);
					} else
						greenCampusListHandler.sendEmptyMessage(LOADING_FAIL);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					greenCampusListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					greenCampusListHandler.sendEmptyMessage(LOADING_FAIL);
					e.printStackTrace();
				} finally {
					httpManager.shutdown();
				}
			}
		});
		thread.start();
	}

	/**
	 * @param keyword
	 */
	private void searchKeyword(String keyword) {
		// set searchUrl
		listUrl = "http://www.mju.ac.kr/mbs/mjukr/jsp/complaint2/list.jsp?boardId=7088978&listType=07&id=mjukr_110500000000&column=TITLE&search="
				+ keyword;
		// clear list
		initList();
		// showList
		showListFromGet();
		// setMainTitle
		((TextView) findViewById(R.id.green_main_title)).setText("");
		((TextView) findViewById(R.id.green_main_title)).setText(getResources()
				.getString(R.string.title_search));
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(GreencampusListActivity.this,
				GreencampusViewActivity.class);
		intent.putExtra("url", greencampusList.get(position).getP_url());
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.green_list_write_btn:
			// 湲�벐湲�踰꾪듉 �꾨Ⅴ硫�泥��≫떚鍮꾪떚濡��대룞.
			Intent intent = new Intent(GreencampusListActivity.this,
					GreencampusMainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.green_list_search_btn:
			if (NetworkManager.checkNetwork(GreencampusListActivity.this))
				onSearchRequested();
			break;
		case R.id.green_list_more:
			if (NetworkManager.checkNetwork(GreencampusListActivity.this)) {
				if (pageUrlList.size() > 0) {
					listUrl = pageUrlList.get(0);
					pageUrlList.remove(0);
					showListFromPost();
				} else
					Toast.makeText(
							getBaseContext(),
							getResources().getString(
									R.string.msg_load_more_last),
							Toast.LENGTH_SHORT).show();
			}
			break;
		}
	}

	/**
	 * Desc static �⑤뱾���대옒��
	 * 
	 * @author hs
	 * @date 2014. 1. 27. �ㅽ썑 3:38:22
	 * @version
	 */
	static class GreenCampusListHandler extends Handler {
		private final WeakReference<GreencampusListActivity> greenCampusListAcivity;

		public GreenCampusListHandler(GreencampusListActivity activity) {
			// TODO Auto-generated constructor stub
			greenCampusListAcivity = new WeakReference<GreencampusListActivity>(
					activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			GreencampusListActivity activity = greenCampusListAcivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}

/* end of file */
