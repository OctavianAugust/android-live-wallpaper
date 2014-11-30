package com.livewallpaper.circle.db.models;

public class Sms {

	private String _smsId;
	private String address;
	private String body;
	private int type;
	private String date;
	private String _idPeople;

	public String getSmsId() {
		return _smsId;
	}

	public String getAddress() {
		return address;
	}

	public String getBody() {
		return body;
	}

	public int getType() {
		return type;
	}

	public String getDate() {
		return date;
	}

	public String getIdPeople() {
		return _idPeople;
	}

	public void setSmsId(String _smsId) {
		this._smsId = _smsId;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setIdPeople(String _idPeople) {
		this._idPeople = _idPeople;
	}

}
