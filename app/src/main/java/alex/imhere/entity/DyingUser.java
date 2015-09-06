package alex.imhere.entity;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import alex.imhere.util.time.TimeUtils;

public class DyingUser {
	//region Fields
	@SerializedName("udid")
	String udid = "";
	@SerializedName("loginedAt")
	DateTime loginedAt = new DateTime(0);
	@SerializedName("aliveTo")
	DateTime aliveTo = new DateTime(0);
	//endregion

	//region Ctors
	public DyingUser() {
	}


	protected DyingUser(DyingUser dyingUser) {
		this(dyingUser.udid, dyingUser.loginedAt, dyingUser.aliveTo);
	}

	protected DyingUser(String udid, DateTime loginedAt, DateTime aliveTo) {
		setUdid(udid);
		setLoginedAt(loginedAt);
		setAliveTo(aliveTo);
	}
	//endregion

	//region Setters/getters
	protected void setLoginedAt(DateTime loginedAt) {
		this.loginedAt = loginedAt;
	}

	protected void setAliveTo(DateTime aliveTo) {
		this.aliveTo = aliveTo;
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
	//endregion

	//region Public helpers
	@NonNull
	public Duration getRestLifetime() {
		return TimeUtils.GetNonNegativeDuration(new DateTime(), aliveTo);
	}

	@NonNull
	public Duration getFullLifetime() {
		return TimeUtils.GetNonNegativeDuration(loginedAt, aliveTo);
	}

	public boolean isDead() {
		return getFullLifetime().getMillis() <= 0 || getRestLifetime().getMillis() == 0;
	}

	public boolean isAlive() {
		return !isDead();
	}
	//endregion

	//region Override
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DyingUser dyingUser = (DyingUser) o;

		return getUdid().equals(dyingUser.getUdid());

	}

	@Override
	public int hashCode() {
		return getUdid().hashCode();
	}
	//endregion
}
