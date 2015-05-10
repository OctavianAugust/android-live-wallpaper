package circle.db;

import com.android.os.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.util.Log;

public class InfoAboutThePhone {

	private static final String LOG_TAG = "monitoring";

	public static String getContactName(Context ctx, String num) {

		ContentResolver cr = ctx.getContentResolver();
		Uri u = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(num));
		String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME };

		Cursor c = cr.query(u, projection, null, null, null);

		Log.i(LOG_TAG, "getContactName");
		try {
			if (!c.moveToFirst())
				return null;

			int index = c
					.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			Log.i(LOG_TAG, "ContactName - " + c.getString(index));
			return c.getString(index);

		} finally {
			if (c != null)
				c.close();
		}
	}

	public static String getInfoAboutBatteryCharge(Context ctx) {
		final IntentFilter ifilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = ctx.registerReceiver(null, ifilter);
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		String batteryInfo = ctx.getResources().getString(R.string.battery);
		batteryInfo += " - " + String.valueOf(level) + "%";
		return batteryInfo;
	}

}
