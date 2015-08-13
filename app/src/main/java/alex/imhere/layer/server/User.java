package alex.imhere.layer.server;

import com.google.gson.annotations.SerializedName;

public class User {
	@SerializedName("udid")
	private String udid = "";
	@SerializedName("dieAfterMs")
	private long dieAfterMs = 0;

	public User() {
	}

	protected User(User user) {
		this(user.udid, user.dieAfterMs);
	}

	protected User(String udid, long dieAfterMs) {
		setUdid(udid);
		setDieAfterMs(dieAfterMs);
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public long getDieAfterMs() {
		return dieAfterMs;
	}

	protected void setDieAfterMs(long dieAfterMs) {
		this.dieAfterMs = dieAfterMs;
	}
}
