package circle.db.models;

public class Call {

	private String _Id;
	private String phone;
	private String date;
	private String duraction;
	private int type;
	private String _idPeople;

	public String getCallId() {
		return _Id;
	}

	public String getPhone() {
		return phone;
	}

	public String getDate() {
		return date;
	}

	public String getDuraction() {
		return duraction;
	}

	public int getType() {
		return type;
	}

	public String getIdPeople() {
		return _idPeople;
	}

	public void setCallId(String _Id) {
		this._Id = _Id;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDuraction(String duraction) {
		this.duraction = duraction;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setIdPeople(String _idPeople) {
		this._idPeople = _idPeople;
	}

}
