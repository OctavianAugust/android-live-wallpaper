package circle.db.models;

public class Contacts {

	private String _contact_id;
	private String _people_id;

	public String getContactId() {
		return _contact_id;
	}

	public String getPeopleId() {
		return _people_id;
	}

	public void setContactId(String _contact_id) {
		this._contact_id = _contact_id;
	}

	public void setPeopleId(String _people_id) {
		this._people_id = _people_id;
	}

}