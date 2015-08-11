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
		ITEMS.add(new User("User 1", new Date()));
		ITEMS.add(new User("User 2", new Date()));
		ITEMS.add(new User("User 3", new Date()));
	}
}
