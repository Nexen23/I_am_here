package alex.imhere.service.api;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import alex.imhere.entity.DyingUser;
import alex.imhere.service.parser.JsonParser;

public class ServerAPI {
	static final String
			API_Login = "Login",
			API_Logout = "Logout",
			API_GetOnlineUsers = "GetOnlineUsers",
			API_GetNow = "GetNow";

	private JsonParser parser = new JsonParser();

	public DyingUser login(@NonNull final String udid) throws ParseException {
		String jsonObject = ParseCloud.callFunction(API_Login, GetRequestFor(udid));
		DyingUser dyingUser = parser.fromJson(jsonObject, DyingUser.class);
		return dyingUser;
	}

	public void logout(@NonNull final DyingUser dyingUser) {
		try {
			ParseCloud.callFunction(API_Logout, GetRequestFor(dyingUser.getUdid()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public final ArrayList<DyingUser> getOnlineUsers(@NonNull final DyingUser dyingUser) throws ParseException {
		String jsonUsers = ParseCloud.callFunction(API_GetOnlineUsers, GetRequestFor(dyingUser.getUdid()));
		ArrayList<DyingUser> users = parser.fromJson(jsonUsers, new TypeToken<List<DyingUser>>(){}.getType());
		return users;
	}

	public DateTime getNow(@NonNull final DyingUser dyingUser) throws ParseException {
		String jsonDate = ParseCloud.callFunction(API_GetNow, GetRequestFor(dyingUser.getUdid()));
		return parser.fromJson(jsonDate, DateTime.class);
	}

	private Map<String, ?> GetRequestFor(@NonNull final String udid)
	{
		Map<String, String> result = new HashMap<>();
		result.put("udid", udid);
		return result;
	}
}