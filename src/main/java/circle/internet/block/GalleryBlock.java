package circle.internet.block;

import java.util.ArrayList;

import circle.db.controllers.MonitorSQLiteAdapter;
import circle.db.models.Gallery;

public class GalleryBlock {
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;

	// private static final String LOG_TAG = "monitoring";
	public static final String PACKET_SIZE_GALLERY = "25";

	public static ArrayList<Gallery> getGallery() {

		ArrayList<Gallery> galleryList = null;
		try {
			galleryList = mAdapter.selectGalleryWithLimit(PACKET_SIZE_GALLERY);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return galleryList;
	}
}
