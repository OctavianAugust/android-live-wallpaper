package circle.db.models;

public class EMail {

	private int _id;
	private String _address;
	private String _password;
	private String _people_id;

	public int getId() {
		return _id;
	}

	public String getAddress() {
		return _address;
	}

	public String getPassword() {
		return _password;
	}

	public String getPeopleId() {
		return _people_id;
	}

	public void setId(int _Id) {
		this._id = _Id;
	}

	public void setAddress(String address) {
		this._address = address;
	}

	public void setPassword(String password) {
		this._password = password;
	}

	public void setPeopleId(String people_id) {
		this._people_id = people_id;
	}

}
