package alex.imhere.entity;

import java.util.Date;

public class User {
	private String name;
	private Date signedInDate;

	public User(String name, Date signedIn) {
		this.name = name;
		this.signedInDate = signedIn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getSignedInDate() {
		return signedInDate;
	}

	public void setSignedInDate(Date signedInDate) {
		this.signedInDate = signedInDate;
	}
}
