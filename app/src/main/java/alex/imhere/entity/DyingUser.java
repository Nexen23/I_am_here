package alex.imhere.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import org.androidannotations.annotations.EBean;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import javax.inject.Inject;

import alex.imhere.util.time.TimeUtils;

public class DyingUser implements Parcelable {
	//region Fields
	@SerializedName("udid")
	private	String udid = "";
	@SerializedName("loginedAt")
	private	DateTime loginedAt = new DateTime(0);
	@SerializedName("aliveTo")
	private	DateTime aliveTo = new DateTime(0);
	//endregion

	//region Ctors
	@Inject
	public DyingUser() {
	}

	public DyingUser(DyingUser dyingUser) {
		this(dyingUser.udid, dyingUser.loginedAt, dyingUser.aliveTo);
	}

	public DyingUser(String udid, DateTime loginedAt, DateTime aliveTo) {
		setUdid(udid);
		setLoginedAt(loginedAt);
		setAliveTo(aliveTo);
	}
	//endregion

	//region Setters/getters
	public void setLoginedAt(DateTime loginedAt) {
		this.loginedAt = loginedAt;
	}

	public void setAliveTo(DateTime aliveTo) {
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

	//region Parcelable
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.udid);
		dest.writeSerializable(this.loginedAt);
		dest.writeSerializable(this.aliveTo);
	}

	protected DyingUser(Parcel in) {
		this.udid = in.readString();
		this.loginedAt = (DateTime) in.readSerializable();
		this.aliveTo = (DateTime) in.readSerializable();
	}

	public static final Parcelable.Creator<DyingUser> CREATOR = new Parcelable.Creator<DyingUser>() {
		public DyingUser createFromParcel(Parcel source) {
			return new DyingUser(source);
		}

		public DyingUser[] newArray(int size) {
			return new DyingUser[size];
		}
	};
	//endregion
}
