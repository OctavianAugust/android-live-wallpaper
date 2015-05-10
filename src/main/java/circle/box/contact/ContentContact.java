package circle.box.contact;

import circle.db.controllers.MonitorSQLiteAdapter;
import circle.db.models.People;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

public class ContentContact {
	private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;

	public static void getAllContact(People people, Context context) {

		if (people.getSettingContact() == 1) {
			Cursor cursorContact = null;
			Cursor cursorPhones = null;
			try {

				ContentResolver cr = context.getContentResolver();
				cursorContact = cr.query(ContactsContract.Contacts.CONTENT_URI,
						null, null, null, null);
				if (cursorContact != null) {
					while (cursorContact.moveToNext()) {
						int name = cursorContact
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
						String contactId = cursorContact
								.getString(cursorContact
										.getColumnIndex(ContactsContract.Contacts._ID));
						String contactName = cursorContact.getString(name);

						//
						// Get all phone numbers.
						//
						mAdapter.insertContacts(contactName,
								people.getPhoneId());
						Log.i(LOG_TAG, "insertContacts : contactName - "
								+ contactName + " people.getPhoneId() -- "
								+ people.getPhoneId());
						cursorPhones = cr.query(Phone.CONTENT_URI, null,
								Phone.CONTACT_ID + " = " + contactId, null,
								null);
						if (cursorPhones != null) {
							while (cursorPhones.moveToNext()) {
								String number = cursorPhones
										.getString(cursorPhones
												.getColumnIndex(Phone.NUMBER));
								int type = cursorPhones.getInt(cursorPhones
										.getColumnIndex(Phone.TYPE));
								mAdapter.insertNumber(number, contactName);
								Log.i(LOG_TAG, "insertNumber : number - "
										+ number + " contactName -- "
										+ contactName + " Type - " + type);
							}
							cursorPhones.close();
						}

					}
					cursorContact.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
