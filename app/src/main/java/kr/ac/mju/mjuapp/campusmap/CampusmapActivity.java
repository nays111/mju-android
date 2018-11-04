package kr.ac.mju.mjuapp.campusmap;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import kr.ac.mju.mjuapp.R;
import kr.ac.mju.mjuapp.common.LayoutSlideManager;
import kr.ac.mju.mjuapp.constants.MJUConstants;
import kr.ac.mju.mjuapp.network.NetworkManager;
import kr.ac.mju.mjuapp.util.PixelConverter;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;
import net.daum.mf.map.api.MapView.CurrentLocationTrackingMode;
import net.daum.mf.map.api.MapView.MapType;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

/**
 * @author davidkim
 * 
 */
public class CampusmapActivity extends Activity implements OnClickListener,
		OnTouchListener, MapView.OpenAPIKeyAuthenticationResultListener,
		MapView.MapViewEventListener, MapView.CurrentLocationEventListener,
		MapView.POIItemEventListener {

	private CampusMapHandler campusMapHandler;
	private LayoutSlideManager layoutSlideManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.campusmap_layout);
		// init
		init();
		// init layout
		initLayout();

		if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_CLOSED) {
			openLayout();
		}
	}

	public void handleMessage(Message msg) {
		// TODO Auto-generated method stub
		int what = msg.what;
		switch (what) {
		case MJUConstants.LAYOUT_CLOSED:
			findViewById(R.id.campusmap_left_slidingbar).setClickable(false);
			break;
		case MJUConstants.LAYOUT_OPENED:
			findViewById(R.id.campusmap_left_slidingbar).setClickable(true);
			break;
		case MJUConstants.EXECUTE_ACTION:
			break;
		}
	}

	/**
	 * 
	 */
	private void init() {
		// init variables
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		PixelConverter converter = new PixelConverter(this);

		campusMapHandler = new CampusMapHandler(CampusmapActivity.this);

		if (layoutSlideManager == null) {
			layoutSlideManager = new LayoutSlideManager(
					findViewById(R.id.campusmap_content), campusMapHandler);
			layoutSlideManager
					.init((int) ((float) displaymetrics.widthPixels - converter
							.getWidth(135)));
		}

		// init mapview
		MapView mapView = (MapView) findViewById(R.id.campusmap_mapview);
		mapView.setDaumMapApiKey("18d7990adbaddb3f5c8def55be9cca28933c6dbf");
		mapView.setMapType(MapType.Hybrid);
		mapView.setZoomLevel(1, false);
		mapView.setCurrentLocationEventListener(this);
		mapView.setPOIItemEventListener(this);
		mapView.setOpenAPIKeyAuthenticationResultListener(this);
		mapView.setMapViewEventListener(this);

		// /////////////////////////////////////////////////////////////////////////////////////
		// //////////////////////////2014.01.21
		// 주석처리///////////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////////
		// 자연, 인문 빌딩 리스트 보여주고 빌딩위치 보여주기????

		// // init listview liberal
		// ContentResolver cr = getContentResolver();
		// Cursor liberalCursor = cr
		// .query(CampusmapContentProvider.CONTENT_URI_LIBERALARTS, null, null,
		// null, CampusmapContentProvider.COL_ID);
		// CampusmapBldgAdapter liberalAdapter = new CampusmapBldgAdapter(this,
		// liberalCursor);
		// liberalAdapter.notifyDataSetChanged();
		// ((ListView)
		// findViewById(R.id.campusmap_liberalarts_building_listview)).setAdapter(liberalAdapter);
		// ((ListView)
		// findViewById(R.id.campusmap_liberalarts_building_listview)).setOnItemClickListener(new
		// OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int
		// position, long id) {
		// // TODO Auto-generated method stub
		// ((MapView) findViewById(R.id.campusmap_mapview)).removeAllPOIItems();
		// MapPOIItem poiItem = new MapPOIItem();
		// poiItem.setItemName(((TextView)
		// view.findViewById(R.id.campusmap_row_bldg_name)).getText().toString());
		// poiItem.setMapPoint(MapPoint.mapPointWithGeoCoord(
		// Double.parseDouble(((TextView)
		// view.findViewById(R.id.campusmap_row_bldg_latitude)).getText().toString()),
		// Double.parseDouble(((TextView)
		// view.findViewById(R.id.campusmap_row_bldg_longitude)).getText().toString())));
		// poiItem.setMarkerType(MapPOIItem.MarkerType.RedPin);
		// poiItem.setShowAnimationType(MapPOIItem.ShowAnimationType.NoAnimation);
		// // poiItem.setShowCalloutBalloonOnTouch(false);
		// poiItem.setTag(153);
		// ((MapView) findViewById(R.id.campusmap_mapview)).addPOIItem(poiItem);
		// ((MapView)
		// findViewById(R.id.campusmap_mapview)).fitMapViewAreaToShowAllPOIItems();
		// handler.sendEmptyMessage(SLIDING_MOVE_LEFT);
		// }
		// });
		// // init listview natural
		// Cursor naturalCursor =
		// cr.query(CampusmapContentProvider.CONTENT_URI_NATURAL, null, null,
		// null, CampusmapContentProvider.COL_ID);
		// CampusmapBldgAdapter naturalAdapter = new CampusmapBldgAdapter(this,
		// naturalCursor);
		// naturalAdapter.notifyDataSetChanged();
		// ((ListView)
		// findViewById(R.id.campusmap_natural_building_listview)).setAdapter(naturalAdapter);
		// ((ListView)
		// findViewById(R.id.campusmap_natural_building_listview)).setSelection(0);
		// ((ListView)
		// findViewById(R.id.campusmap_natural_building_listview)).setOnItemClickListener(new
		// OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int
		// position, long id) {
		// // TODO Auto-generated method stub
		// ((MapView) findViewById(R.id.campusmap_mapview)).removeAllPOIItems();
		// MapPOIItem poiItem = new MapPOIItem();
		// poiItem.setItemName(((TextView)
		// view.findViewById(R.id.campusmap_row_bldg_name)).getText().toString());
		// poiItem.setMapPoint(MapPoint.mapPointWithGeoCoord(
		// Double.parseDouble(((TextView)
		// view.findViewById(R.id.campusmap_row_bldg_latitude)).getText().toString()),
		// Double.parseDouble(((TextView)
		// view.findViewById(R.id.campusmap_row_bldg_longitude)).getText().toString())));
		// poiItem.setMarkerType(MapPOIItem.MarkerType.RedPin);
		// poiItem.setShowAnimationType(MapPOIItem.ShowAnimationType.NoAnimation);
		// // poiItem.setShowCalloutBalloonOnTouch(false);
		// poiItem.setTag(153);
		// ((MapView) findViewById(R.id.campusmap_mapview)).addPOIItem(poiItem);
		// ((MapView)
		// findViewById(R.id.campusmap_mapview)).fitMapViewAreaToShowAllPOIItems();
		// handler.sendEmptyMessage(SLIDING_MOVE_LEFT);
		// }
		// });

		// /////////////////////////////////////////////////////////////////////////////////////
		// //////////////////////////2014.01.21 주석처리
		// 끝/////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////////

		// set listener
		findViewById(R.id.campusmap_left_slidingbar).setOnTouchListener(this);
		findViewById(R.id.campusmap_sliding_btn).setOnTouchListener(this);
		findViewById(R.id.campusmap_btn_current_location).setOnClickListener(
				this);
		findViewById(R.id.campusmap_btn_maptype).setOnClickListener(this);
		findViewById(R.id.campusmap_humainty_campus).setOnClickListener(this);
		findViewById(R.id.campusmap_science_campus).setOnClickListener(this);
	}

	/**
	 * 
	 */
	private void initLayout() {
		PixelConverter converter = new PixelConverter(this);
		RelativeLayout.LayoutParams rParams = null;
		rParams = (LayoutParams) findViewById(R.id.campusmap_sub_layout)
				.getLayoutParams();
		rParams.rightMargin = converter.getWidth(135);
		findViewById(R.id.campusmap_sub_layout).setLayoutParams(rParams);

		rParams = (LayoutParams) findViewById(R.id.campusmap_sliding_btn)
				.getLayoutParams();
		rParams.width = converter.getWidth(50);
		rParams.height = converter.getHeight(50);

		rParams = (LayoutParams) findViewById(R.id.campusmap_header_icon)
				.getLayoutParams();
		rParams.width = converter.getWidth(30);
		rParams.height = converter.getHeight(30);
		rParams.setMargins(0, 0, converter.getWidth(15), 0);

		View view = findViewById(R.id.campusmap_submenu_title);
		LinearLayout.LayoutParams linearlayoutParams = (LinearLayout.LayoutParams) view
				.getLayoutParams();
		linearlayoutParams.setMargins(0, 0, 0, converter.getHeight(5));

		view = findViewById(R.id.campusmap_seoul_campus_title);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0,
				converter.getHeight(5));

		view = findViewById(R.id.campusmap_humainty_campus);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);

		view = findViewById(R.id.campusmap_yongin_campus_title);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(25), 0,
				converter.getHeight(5));

		view = findViewById(R.id.campusmap_science_campus);
		linearlayoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
		linearlayoutParams.setMargins(0, converter.getHeight(15), 0, 0);
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (NetworkManager.checkNetwork(getApplicationContext())) {
			MapView mapView = (MapView) findViewById(R.id.campusmap_mapview);
			switch (v.getId()) {
			case R.id.campusmap_btn_current_location:
				CurrentLocationTrackingMode trackingMode = mapView
						.getCurrentLocationTrackingMode();
				if (trackingMode == CurrentLocationTrackingMode.TrackingModeOff)
					mapView.setCurrentLocationTrackingMode(CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
				else if (trackingMode == CurrentLocationTrackingMode.TrackingModeOnWithoutHeading)
					mapView.setCurrentLocationTrackingMode(CurrentLocationTrackingMode.TrackingModeOnWithHeading);
				else {
					mapView.setCurrentLocationTrackingMode(CurrentLocationTrackingMode.TrackingModeOff);
					mapView.setShowCurrentLocationMarker(false);
				}
				break;
			case R.id.campusmap_btn_maptype:
				MapType mapType = mapView.getMapType();
				if (mapType == MapType.Standard)
					mapView.setMapType(MapType.Satellite);
				else if (mapType == MapType.Satellite)
					mapView.setMapType(MapType.Hybrid);
				else
					mapView.setMapType(MapType.Standard);
				break;
			case R.id.campusmap_humainty_campus:
				mapView.setMapCenterPointAndZoomLevel(
						MapPoint.mapPointWithGeoCoord(37.580002, 126.922607),
						1, true);
				break;
			case R.id.campusmap_science_campus:
				mapView.setMapCenterPointAndZoomLevel(
						MapPoint.mapPointWithGeoCoord(37.222363, 127.187813),
						1, true);
				break;

			}
			if (layoutSlideManager.getLayoutState() == MJUConstants.LAYOUT_OPENED) {
				layoutSlideManager.slideLayoutToLeftAutomatically(true);
			}
		}
	}

	/**
	 * net.daum.mf.map.api.MapView.POIItemEventListener
	 */
	@Override
	public void onCalloutBalloonOfPOIItemTouched(MapView mapView,
			MapPOIItem poiItem) {
		// TODO Auto-generated method stub
		String title = poiItem.getItemName();
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(CampusmapContentProvider.CONTENT_URI_OFFICE,
				null, CampusmapContentProvider.COL_BLDG_NAME + " = '" + title
						+ "'", null, CampusmapContentProvider.COL_ID);
		int size = cursor.getCount();
		if (size > 0) {
			CampusmapDialog dialog = new CampusmapDialog(this, cursor);
			dialog.setTitle(poiItem.getItemName());
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
		}
	}

	@Override
	public void onDraggablePOIItemMoved(MapView arg0, MapPOIItem arg1,
			MapPoint arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPOIItemSelected(MapView arg0, MapPOIItem arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * net.daum.mf.map.api.MapView.CurrentLocationEventListener
	 */
	@Override
	public void onCurrentLocationDeviceHeadingUpdate(MapView arg0, float arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCurrentLocationUpdate(MapView arg0, MapPoint arg1, float arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCurrentLocationUpdateCancelled(MapView arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCurrentLocationUpdateFailed(MapView arg0) {
		// TODO Auto-generated method stub

	}

	/**
	 * net.daum.mf.map.api.MapView.MapViewEventListener
	 */
	@Override
	public void onMapViewCenterPointMoved(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapViewDoubleTapped(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapViewInitialized(MapView mapView) {
		// TODO Auto-generated method stub
		mapView.setCurrentLocationTrackingMode(CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
	}

	@Override
	public void onMapViewLongPressed(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapViewSingleTapped(MapView arg0, MapPoint arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onMapViewZoomLevelChanged(MapView arg0, int arg1) {
		// TODO Auto-generated method stub

	}

	/**
	 * net.daum.mf.map.api.MapView.OpenAPIKeyAuthenticationResultListener
	 */
	@Override
	public void onDaumMapOpenAPIKeyAuthenticationResult(MapView arg0, int arg1,
			String arg2) {
		// TODO Auto-generated method stub

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

	/**
	 * Desc static 헨들러 클래스
	 * 
	 * @author hs
	 * @date 2014. 1. 27. 오후 3:38:22
	 * @version
	 */
	static class CampusMapHandler extends Handler {
		private final WeakReference<CampusmapActivity> campusMapActivity;

		public CampusMapHandler(CampusmapActivity activity) {
			// TODO Auto-generated constructor stub
			campusMapActivity = new WeakReference<CampusmapActivity>(activity);
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			CampusmapActivity activity = campusMapActivity.get();
			if (activity != null) {
				activity.handleMessage(msg);
			}
		}
	}
}
/* end of file */
