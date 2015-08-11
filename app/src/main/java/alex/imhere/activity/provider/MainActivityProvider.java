package alex.imhere.activity.provider;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alex.imhere.entity.User;

public class MainActivityProvider {

	public static List<User> ITEMS = new ArrayList<>();

	static {
		// Add 3 sample items.
		Calendar calendar = Calendar.getInstance();
		for (int i = 1; i < 16; ++i) {
			calendar.add(Calendar.SECOND, 3);
			ITEMS.add(new User(String.format("User %d", i), calendar.getTime()));
		}
	}
}
