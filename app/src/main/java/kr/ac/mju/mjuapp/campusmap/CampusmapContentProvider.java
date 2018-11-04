package kr.ac.mju.mjuapp.campusmap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * @author davidkim
 * 
 */
public class CampusmapContentProvider extends ContentProvider {
	private static final String URI_LIBERALARTS = "content://kr.ac.mju.mdc.mjuapp.campusmap"
			+ "/liberalarts";
	private static final String URI_NATURAL = "content://kr.ac.mju.mdc.mjuapp.campusmap"
			+ "/natural";
	private static final String URI_OFFICE = "content://kr.ac.mju.mdc.mjuapp.campusmap"
			+ "/office";

	public static final Uri CONTENT_URI_LIBERALARTS = Uri
			.parse(URI_LIBERALARTS);
	public static final Uri CONTENT_URI_NATURAL = Uri.parse(URI_NATURAL);
	public static final Uri CONTENT_URI_OFFICE = Uri.parse(URI_OFFICE);

	private static final String DATABASE_NAME = "campusmap.db";

	private static final int DATABASE_VERSION = 1;

	private static final int DATA_LIBERALARTS = 1;
	private static final int DATA_NATURAL = 2;
	private static final int DATA_OFFICE = 3;

	private static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("kr.ac.mju.mdc.mjuapp.campusmap", "liberalarts",
				DATA_LIBERALARTS);
		uriMatcher.addURI("kr.ac.mju.mdc.mjuapp.campusmap", "natural",
				DATA_NATURAL);
		uriMatcher.addURI("kr.ac.mju.mdc.mjuapp.campusmap", "office",
				DATA_OFFICE);
	}

	private static final String TABLE_NAME_LIBERALARTS = "liberalarts";
	private static final String TABLE_NAME_NATURAL = "natural";
	private static final String TABLE_NAME_OFFICE = "office";

	public static final String COL_ID = "_id";
	public static final String COL_BLDG_NAME = "bldg_name";
	public static final String COL_BLDG_CODE = "bldg_code";
	public static final String COL_BLDG_LATITUDE = "latitude";
	public static final String COL_BLDG_LONGITUDE = "longitude";

	public static final String COL_OFFICE_NAME = "office_name";
	public static final String COL_OFFICE_NAME2 = "office_name2";
	public static final String COL_OFFICE_NAME3 = "office_name3";
	public static final String COL_OFFICE_PHONE = "office_phone";

	private static final String SQL_CREATE_TABLE_LIBERALARTS = "CREATE TABLE "
			+ TABLE_NAME_LIBERALARTS + "(" + COL_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_BLDG_NAME
			+ " TEXT, " + COL_BLDG_LATITUDE + " TEXT, " + COL_BLDG_LONGITUDE
			+ " TEXT);";
	private static final String SQL_CREATE_TABLE_NATURAL = "CREATE TABLE "
			+ TABLE_NAME_NATURAL + "(" + COL_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_BLDG_NAME
			+ " TEXT, " + COL_BLDG_LATITUDE + " TEXT, " + COL_BLDG_LONGITUDE
			+ " TEXT);";
	private static final String SQL_CREATE_TABLE_OFFICE = "CREATE TABLE "
			+ TABLE_NAME_OFFICE + "(" + COL_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_BLDG_NAME
			+ " TEXT, " + COL_OFFICE_NAME + " TEXT, " + COL_OFFICE_NAME2
			+ " TEXT, " + COL_OFFICE_NAME3 + " TEXT, " + COL_OFFICE_PHONE
			+ " TEXT);";

	private SQLiteDatabase db = null;

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		SQLiteOpenHelper dbHelper = new SQLiteOpenHelper(getContext(),
				DATABASE_NAME, null, DATABASE_VERSION) {

			@Override
			public void onUpgrade(SQLiteDatabase db, int oldVersion,
					int newVersion) {
				// TODO Auto-generated method stub
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_LIBERALARTS);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_NATURAL);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME_OFFICE);
				onCreate(db);
			}

			@Override
			public void onCreate(SQLiteDatabase db) {
				// TODO Auto-generated method stub
				db.execSQL(SQL_CREATE_TABLE_LIBERALARTS);
				db.execSQL(SQL_CREATE_TABLE_NATURAL);
				db.execSQL(SQL_CREATE_TABLE_OFFICE);
				insertCampusmapData(db, getContext());
			}
		};
		db = dbHelper.getWritableDatabase();
		return (db == null) ? false : true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (uriMatcher.match(uri)) {
		case DATA_LIBERALARTS:
			qb.setTables(TABLE_NAME_LIBERALARTS);
			break;
		case DATA_NATURAL:
			qb.setTables(TABLE_NAME_NATURAL);
			break;
		case DATA_OFFICE:
			qb.setTables(TABLE_NAME_OFFICE);
			break;
		}

		// String orderBy;
		// if (TextUtils.isEmpty(sortOrder))
		// orderBy = COL_ID;
		// else
		// orderBy = sortOrder;

		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, null);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * @param db
	 */
	private static void insertCampusmapData(SQLiteDatabase db, Context context) {
		try {
			// debug
			long start = System.currentTimeMillis();
			// insert data
			// liberal arts
			String SQL_liberalarts = "insert into " + TABLE_NAME_LIBERALARTS
					+ "(" + COL_BLDG_NAME + ", " + COL_BLDG_LATITUDE + ", "
					+ COL_BLDG_LONGITUDE + ") values (";
			String SQL_natural = "insert into " + TABLE_NAME_NATURAL + "("
					+ COL_BLDG_NAME + ", " + COL_BLDG_LATITUDE + ", "
					+ COL_BLDG_LONGITUDE + ") values (";
			String SQL_office = "insert into " + TABLE_NAME_OFFICE + "("
					+ COL_BLDG_NAME + ", " + COL_OFFICE_NAME + ", "
					+ COL_OFFICE_NAME2 + ", " + COL_OFFICE_NAME3 + ", "
					+ COL_OFFICE_PHONE + ") values (";
			InputStream is = context.getAssets()
					.open("database/liberalarts.db");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String read = null;
			while ((read = br.readLine()) != null)
				db.execSQL(SQL_liberalarts + read + ");");
			// natural
			is = context.getAssets().open("database/natural.db");
			br = new BufferedReader(new InputStreamReader(is));
			read = null;
			while ((read = br.readLine()) != null)
				db.execSQL(SQL_natural + read + ");");
			// office
			is = context.getAssets().open("database/office.db");
			br = new BufferedReader(new InputStreamReader(is));
			read = null;
			while ((read = br.readLine()) != null)
				db.execSQL(SQL_office + read + ");");
			if (is != null)
				is.close();
			if (br != null)
				br.close();
			// debug
			long end = System.currentTimeMillis();
			Log.d("MDC", "insertCampusData : " + (double) (end - start) / 1000.);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
