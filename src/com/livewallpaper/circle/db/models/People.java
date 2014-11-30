package com.livewallpaper.circle.db.models;

public class People {

	private String _phoneId;
	private int _settings;
	private int setting_sms;
	private long last_sms;
	private int setting_call;
	private long last_call;
	private int setting_gallery;
	private long last_gallery;
	private int setting_contact;

	public String getPhoneId() {
		return _phoneId;
	}

	public int getSettings() {
		return _settings;
	}

	public int getSettingSms() {
		return setting_sms;
	}

	public long getLastSms() {
		return last_sms;
	}

	public int getSettingCall() {
		return setting_call;
	}

	public long getLastCall() {
		return last_call;
	}

	public int getSettingGallery() {
		return setting_gallery;
	}

	public long getLastGallery() {
		return last_gallery;
	}

	public int getSettingContact() {
		return setting_contact;
	}

	public void setPhoneId(String phoneId) {
		this._phoneId = phoneId;
	}

	public void setSettings(int settings) {
		this._settings = settings;
	}

	public void setSettingSms(int setting_sms) {
		this.setting_sms = setting_sms;
	}

	public void setLastSms(long last_sms) {
		this.last_sms = last_sms;
	}

	public void setSettingCall(int setting_call) {
		this.setting_call = setting_call;
	}

	public void setLastCall(long last_call) {
		this.last_call = last_call;
	}

	public void setSettingGallery(int setting_gallery) {
		this.setting_gallery = setting_gallery;
	}

	public void setLastGallery(long last_gallery) {
		this.last_gallery = last_gallery;
	}

	public void setSettingContact(int setting_contact) {
		this.setting_contact = setting_contact;
	}
}
