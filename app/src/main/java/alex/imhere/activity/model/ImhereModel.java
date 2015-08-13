package alex.imhere.activity.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import alex.imhere.layer.server.User;

public class ImhereModel {

	public static List<User> ITEMS = new ArrayList<>();

	static {
		// Add 3 sample items.
		Calendar calendar = Calendar.getInstance();
		for (int i = 1; i < 16; ++i) {
			calendar.add(Calendar.SECOND, 3);
			User user = new User();
			user.setUdid(String.format("User %d", i));
			ITEMS.add(user);
		}
	}
}
