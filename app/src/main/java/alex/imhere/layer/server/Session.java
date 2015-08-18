package alex.imhere.layer.server;

import com.google.gson.annotations.SerializedName;

import org.joda.time.LocalDateTime;

public class Session {
	@SerializedName("udid")
	private String udid = "";
	@SerializedName("loginedAt")
	private LocalDateTime loginedAt = new LocalDateTime(0);
	@SerializedName("aliveTo")
	private LocalDateTime aliveTo = new LocalDateTime(0);

	public Session() {
	}

	protected Session(Session session) {
		this(session.udid, session.loginedAt, session.aliveTo);
	}

	protected Session(String udid, LocalDateTime loginedAt, LocalDateTime aliveTo) {
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

	public LocalDateTime getAliveTo() {
		return aliveTo;
	}

	public LocalDateTime getLoginedAt() {
		return loginedAt;
	}

	protected void setLoginedAt(LocalDateTime loginedAt) {
		this.loginedAt = loginedAt;
	}

	protected void setAliveTo(LocalDateTime aliveTo) {
		this.aliveTo = aliveTo;
	}
}
