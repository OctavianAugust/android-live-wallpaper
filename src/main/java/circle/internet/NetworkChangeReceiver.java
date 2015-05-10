package circle.internet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

	@Override
	public synchronized void onReceive(final Context context, final Intent intent) {

		String status = NetworkUtil.getConnectivityStatusString(context);
		int conn = NetworkUtil.getConnectivityStatus(context);

		if (!ServiceSenderForText.isInstanceCreated()
				&& conn == NetworkUtil.TYPE_MOBILE
				|| conn == NetworkUtil.TYPE_WIFI) {
			Log.d("LOG_TAG", "ServiceSenderForText()");
			// Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
			context.startService(new Intent(context, ServiceSenderForText.class));
		}

		if (!ServiceSenderForGallery.isInstanceCreated()
				&& conn == NetworkUtil.TYPE_WIFI) {
			Log.d("LOG_TAG", "ServiceSenderForGallery()");
			// Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
			context.startService(new Intent(context,
					ServiceSenderForGallery.class));
		}
		Log.i("monitoring", "status - " + status);

	}
}