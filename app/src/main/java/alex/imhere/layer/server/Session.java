package alex.imhere.layer.server;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class Session {
	@SerializedName("udid")
	private String udid = "";
	@SerializedName("loginedAt")
	private DateTime loginedAt = new DateTime(0);
	@SerializedName("aliveTo")
	private DateTime aliveTo = new DateTime(0);

	public Session() {
	}

	protected Session(Session session) {
		this(session.udid, session.loginedAt, session.aliveTo);
	}

	protected Session(String udid, DateTime loginedAt, DateTime aliveTo) {
		setUdid(udid);
		setLoginedAt(loginedAt);
		setAliveTo(aliveTo);
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public DateTime getAliveTo() {
		return aliveTo;
	}

	public DateTime getLoginedAt() {
		return loginedAt;
	}

	protected void setLoginedAt(DateTime loginedAt) {
		this.loginedAt = loginedAt;
	}

	protected void setAliveTo(DateTime aliveTo) {
		this.aliveTo = aliveTo;
	}
}
