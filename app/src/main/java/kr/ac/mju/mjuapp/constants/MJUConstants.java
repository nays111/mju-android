package kr.ac.mju.mjuapp.constants;

/**
 * �댄뵆由ъ��댁뀡�먯꽌 怨듯넻�곸쑝濡��ъ슜�섎뒗 �곸닔瑜��뺤쓽���대옒�� *
 * 
 * @author Hs
 * 
 */
public class MJUConstants {
	public static final String MYIWEB_MAIN_URL = "https://myiweb.mju.ac.kr";
	public static final String CLASS_REGISTER_RECORD_URL = "http://myiweb.mju.ac.kr/servlet/su.sug.sug01.Sug01Svl03?attribute=sug240_int";

	public static final int SECOND_MAIN_LAYOUT = 1;
	public static final int FIRST_MAIN_LAYOUT = 2;

	public static final int MAIN_NOTICE_SUCCESS = 3;
	public static final int MAIN_NOTICE_FAIL = 4;

	public static final int TIME_TABLE_SUCCESS = 5;
	public static final int TIME_TABLE_FAIL = 6;
	public static final int TIME_TABLE_EMPTY = 7;

	public static final int PROGRESS_DIALOG = 0;
	public static final int LOGIN_FAIL_DIALOG = 13;

	public static final int WEATHER_SEOUL_COMPLETE = 8;
	public static final int WEATHER_YONGIN_COMPLETE = 9;
	public static final int WEATHER_FAIL = 10;

	public static final int LOAD_COMPLETE = 12;
	public static final int LOGIN_COMPLETE = 13;
	public static final int LOGIN_FAILED = 17;
	public static final int NETWORK_FAILED = 18;
	public static final int GET_STD_NAME_FAILED = 19;

	public static final String MYIWEB_FLAG = "myiweb_flag";
	public static final int MYIWEB_TIMETABLE = 14;
	public static final int MYIWEB_GRADE = 15;
	public static final int MYIWEB_GRADUATE = 16;

	// food url
	public static final String HUMANITYCAMPUS_STUDENT_CAFETERIA_URL = "http://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=3560&id=mjukr_051001030000";
	public static final String HUMANITYCAMPUS_STAFF_CAFETEREIA_URL = "http://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=11619&id=mjukr_051001020000";
	public static final String SCIENCECAMPUS_LIBRARY_CAFETEREIA_URL = "http://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36337&id=mjukr_051002050000";
	public static final String SCIENCECAMPUS_STUDENT_CAFETERIA_URL = "http://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=36548&id=mjukr_051002020000";
	public static final String SCIENCECAMPUS_STAFF_CAFETERIA_URL = "http://www.mju.ac.kr/mbs/mjukr/jsp/restaurant/restaurant.jsp?configIdx=58976&id=mjukr_051002040000";

	public static final int FOOD_SUCCESS = 20;
	public static final int FOOD_FAIL = 21;
	public static final int FOOD_EMPTY = 34;

	public static final int MAIN_BANNER = 22;
	public static final int MAIN_PICTURE = 23;

	// feedback email
	public static final String FEEDBACK_EMAIL_ADDR = "wedadmin@mju.ac.kr";

	// main notice urls
	public static final String[] MAIN_NOTICE_URLS = {
			"http://www.mju.ac.kr/mbs/mjukr/jsp/board/compile/mini_list1.jsp",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/board/compile/mini_list2.jsp",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/board/compile/mini_list3.jsp",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/board/compile/mini_list4.jsp",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/board/compile/mini_list5.jsp", };

	public static final String[] MAIN_BANNER_IMAGE_URLS = {
			"http://mju.ac.kr/mbs/mjukr/download/img_app/con01.jpg",
			"http://mju.ac.kr/mbs/mjukr/download/img_app/con02.jpg",
			"http://mju.ac.kr/mbs/mjukr/download/img_app/con03.jpg",
			"http://mju.ac.kr/mbs/mjukr/download/img_app/con04.jpg",
			"http://mju.ac.kr/mbs/mjukr/download/img_app/con05.jpg", };

	public static final int MAIN_BANNER_IMAGE_COMPLETE = 24;
	public static final int MAIN_BANNER_IMAGE_FAIL = 25;

	public static final String MJU_INDEX_URL = "http://www.mju.ac.kr/mbs/mjukr/index.jsp?SWIFT_SESSION_CHK=false";
	//kihin get data_MJUIMG 2016.01.12
	public static final String MJU_IMG_URL = "http://www.mju.ac.kr/mbs/mjumob/jsp/album/gallery_mobile_new.jsp";


	public static final int MAIN_BANNER_TEXTS_COMPLETE = 26;
	public static final int MAIN_BANNER_TEXTS_FAIL = 27;

	public static final int MAIN_PICTURE_IAMGE_COMPLETE = 28;
	public static final int MAIN_PICTURE_IMAGE_FAIL = 29;
	public static final int MAIN_PICTURE_TEXTS_COMPLETE = 30;
	public static final int MAIN_PICTURE_TEXTS_FAIL = 31;
	public static final String[] MAIN_PICTURE_IMAGE_URLS = {
			"http://www.mju.ac.kr/mbs/mjukr/download/1.jpg",
			"http://www.mju.ac.kr/mbs/mjukr/download/2.jpg",
			"http://www.mju.ac.kr/mbs/mjukr/download/3.jpg",
			"http://www.mju.ac.kr/mbs/mjukr/download/4.jpg",
			"http://www.mju.ac.kr/mbs/mjukr/download/5.jpg",
			"http://www.mju.ac.kr/mbs/mjukr/download/6.jpg",
			"http://www.mju.ac.kr/mbs/mjukr/download/7.jpg" };

	public static final String[] MAIN_PICTURE_TEXT_URLS = {
			"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?spage=1&boardType=02&boardId=16397111&listType=02&boardSeq=19500814&mcategoryId=&id=mjukr_060203000000",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?spage=1&boardType=02&boardId=16397111&listType=02&boardSeq=19500802&mcategoryId=&id=mjukr_060203000000",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?spage=1&boardType=02&boardId=16397111&listType=02&boardSeq=16491929&mcategoryId=&id=mjukr_060203000000",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?spage=1&boardType=02&boardId=16397111&listType=02&boardSeq=16489846&mcategoryId=&id=mjukr_060203000000",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?spage=1&boardType=02&boardId=16397111&listType=02&boardSeq=16489817&mcategoryId=&id=mjukr_060203000000",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?spage=1&boardType=02&boardId=16397111&listType=02&boardSeq=16489645&mcategoryId=&id=mjukr_060203000000",
			"http://www.mju.ac.kr/mbs/mjukr/jsp/album/gallery.jsp?spage=1&boardType=02&boardId=16397111&listType=02&boardSeq=16489143&mcategoryId=&id=mjukr_060203000000", };

	public static final int SPLASH_DISMISS = 32;
	public static final int RQ_COMMUNITY_AFTER_LOGIN = 33;

	// layout slide manager
	public static final int FIRST_LAYOUT_THRESHOLD = 40;
	public static final int SECOND_LAYOUT_THRESHOLD = 80;
	public static final int LEFT_DIRECTION = 34;
	public static final int RIGHT_DIRECTION = 35;
	public static final int SLIDING = 36;
	public static final int LAYOUT_CLOSED = 37;
	public static final int LAYOUT_OPENED = 38;
	public static final int EXECUTE_ACTION = 53;
	public static final int FIRST_LAYOUT_OPENED = 41;
	public static final int FIRST_LAYOUT_CLOSED = 42;
	public static final int SECOND_LAYOUT_OPENED = 43;
	public static final int SECOND_LAYOUT_CLOSED = 44;

	// notice
	public static final int NOTICE_IMAGE_COMPLETE = 45;
	public static final int NOTICE_IMAGE_FAIL = 46;
	public static final int NORMAL_ARTICLE = 47;
	public static final int VIDEO_ARTICLE = 48;
	public static final int IMG_ARTICLE = 49;
	// picture
	public static final int RQ_WRITE_IMG_BRD_AFTER_LOGIN = 50;
	public static final int RQ_TO_ALL_PICTURE = 51;

	public static final long APP_FINISH_TIME = 2500;
	public static final int MAKE_BACK_BTN_FALSE = 52;

	public static final int NORMAL_ALERT_DIALOG = 53;
	public static final int PHOTO_PICTURE_SELECT_DIALOG = 54;
	public static final int GREEN_PICTURE_SELECT_DIALOG = 55;

	public static final int SELECT_CAMPUS = 56;
	public static final int COMMUNITY_IMG_PICTURE_SELECT_DIALOG = 57;
	public static final int TAKE_GALLERY = 58;
	public static final int TAKE_CAMERA = 59;
	public static final int RQ_TIMETABLE_AFTER_LOGIN = 60;
	public static final int APP_NOTICE_DIALOG = 61;
	public static final int PHOTO_NOTICE_DIALOG = 62;
	public static final int IMAGE_BOARD_START_TIMER = 63;
	public static final int PHOTO_NEXT_PAGE = 64;
	public static final int BANNER_NEXT_PAGE = 65;
	public static final int OUT_OF_MEMORY = 66;
	public static final int PICTURE_IMAGE_FAIL = 67;
	public static final int PICTURE_IAMGE_COMPLETE = 68;
	public static final int ALL_PHOTO_DOWN_LOADED = 69;
}

/**
 * To-Do
 */
