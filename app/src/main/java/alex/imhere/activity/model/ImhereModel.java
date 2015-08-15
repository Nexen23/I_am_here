package alex.imhere.activity.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import alex.imhere.layer.server.Session;

public class ImhereModel {

	public static List<Session> ITEMS = new ArrayList<>();

	static {
		// Add 3 sample items.
		Calendar calendar = Calendar.getInstance();
		for (int i = 1; i < 16; ++i) {
			calendar.add(Calendar.SECOND, 3);
			Session session = new Session();
			session.setUdid(String.format("Session %d", i));
			ITEMS.add(session);
		}
	}
}
