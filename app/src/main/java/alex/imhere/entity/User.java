package alex.imhere.entity;

import java.util.Date;

public class User {
	private String name;
	private Date signedIn;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getSignedIn() {
		return signedIn;
	}

	public void setSignedIn(Date signedIn) {
		this.signedIn = signedIn;
	}
}
