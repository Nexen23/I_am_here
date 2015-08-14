package alex.imhere.layer.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Session {
	static final String
			API_Login = "Login",
			API_Logout = "Logout",
			API_GetUsers = "GetUsers",
			API_IsUserAlive = "IsUserAlive";

	private Gson gson = new Gson();

	private User user = new User();

	public User getCurrentUser() {
		return user;
	}

	public void login(String udid) throws Exception {
		user.setUdid(udid);

		try {
			String jsonUser = gson.toJson(user);
			Map<String, String> resultWrapped = ParseCloud.callFunction(API_Login, wrapJsonWithMap(jsonUser));

			String resultJson = unwrapJsonWithMap(resultWrapped);
			user = new User( gson.fromJson(resultJson, User.class) );
		} catch (ParseException e) {
			throw (Exception)e;
		}
	}

	public void logout() {

	}

	public final ArrayList<User> getUsers() {
		ArrayList<User> ITEMS = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		for (int i = 1; i < 16; ++i) {
			calendar.add(Calendar.SECOND, 3);
			ITEMS.add(new User(String.format("User %d", i), calendar.getTime().getTime()));
		}
		return ITEMS;
	}

	private Map<String, String> wrapJsonWithMap(String json)
	{
		Map<String, String> jsonMapWrapper = new HashMap<>();
		jsonMapWrapper.put("data", json);
		return jsonMapWrapper;
	}

	private String unwrapJsonWithMap(Map<String, String> jsonMapWrapper)
	{
		return (String) jsonMapWrapper.get("data");
	}
}