package circle.box;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiverBoot extends BroadcastReceiver {

	private static final String LOG_TAG = "monitoring";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(LOG_TAG, "ReceiverListener onReceive - " + intent.getAction());
		if (!ServiceListener.isInstanceCreated()) {
			context.startService(new Intent(context, ServiceListener.class));
		}

	}

}
