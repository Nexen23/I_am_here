package alex.imhere.layer.server;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.parceler.Parcel;

@Parcel
public class Session {
	@SerializedName("udid")
	String udid = "";
	@SerializedName("loginedAt")
	LocalDateTime loginedAt = new LocalDateTime(0);
	@SerializedName("aliveTo")
	LocalDateTime aliveTo = new LocalDateTime(0);

	public Session() {
	}

	//region Protected
	protected Session(Session session) {
		this(session.udid, session.loginedAt, session.aliveTo);
	}

	protected Session(String udid, LocalDateTime loginedAt, LocalDateTime aliveTo) {
		setUdid(udid);
		setLoginedAt(loginedAt);
		setAliveTo(aliveTo);
	}

	protected void setLoginedAt(LocalDateTime loginedAt) {
		this.loginedAt = loginedAt;
	}

	protected void setAliveTo(LocalDateTime aliveTo) {
		this.aliveTo = aliveTo;
	}
	//endregion

	//region Public
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

	@NonNull
	public Duration getRestLifetime() {
		Duration duration = new Duration(new LocalDateTime().toDateTime(), aliveTo.toDateTime());
		return (duration.getMillis() < 0) ? new Duration(0) : duration;
	}

	@NonNull
	public Duration getFullLifetime() {
		Duration duration = new Duration(loginedAt.toDateTime(), aliveTo.toDateTime());
		return (duration.getMillis() < 0) ? new Duration(0) : duration;
	}
	//endregion

	//region Override
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Session session = (Session) o;

		return getUdid().equals(session.getUdid());

	}

	@Override
	public int hashCode() {
		return getUdid().hashCode();
	}
	//endregion
}
