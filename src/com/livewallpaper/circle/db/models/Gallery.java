package com.livewallpaper.circle.db.models;

public class Gallery {

	private String _id;
	private String data;
	private String title;
	private String _people_id;

	public String getId() {
		return _id;
	}

	public String getData() {
		return data;
	}

	public String getTitle() {
		return title;
	}

	public String getPeopleId() {
		return _people_id;
	}

	public void setId(String _people_id) {
		this._people_id = _people_id;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setPeopleId(String _people_id) {
		this._people_id = _people_id;
	}

}
