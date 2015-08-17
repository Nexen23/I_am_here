package alex.imhere.layer.server;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerAPI {
	static final String
			API_Login = "Login",
			API_Logout = "Logout",
			API_GetOnlineUsers = "GetOnlineUsers",
			API_GetNow = "GetNow";

	private Gson gson = new Gson();

	public ServerAPI() {
		gson = new Gson();

		JsonSerializer<Date> ser = new JsonSerializer<Date>() {
			@Override
			public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
				return src == null ? null : new JsonPrimitive(src.getTime());
			}
		};

		JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
			@Override
			public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				return json == null ? null : new Date(json.getAsLong());
			}
		};

		gson = new GsonBuilder()
				.registerTypeAdapter(Date.class, ser)
				.registerTypeAdapter(Date.class, deser).create();
	}

	public Session login(@NonNull final String udid) throws ParseException {
		String jsonObject = ParseCloud.callFunction(API_Login, GetRequestFor(udid));
		Session session = new Session( gson.fromJson(jsonObject, Session.class) );
		return session;
	}

	public void logout(@NonNull final Session session) {
		try {
			ParseCloud.callFunction(API_Logout, GetRequestFor(session.getUdid()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public final ArrayList<Session> getOnlineUsers(@NonNull final Session session) throws ParseException {
		String jsonUsers = ParseCloud.callFunction(API_GetOnlineUsers, GetRequestFor(session.getUdid()));
		ArrayList<Session> users = gson.fromJson(jsonUsers, new TypeToken<List<Session>>(){}.getType());
		return users;
	}

	public DateTime getNow(@NonNull final Session session) throws ParseException {
		String jsonDate = ParseCloud.callFunction(API_GetNow, GetRequestFor(session.getUdid()));
		return gson.fromJson(jsonDate, DateTime.class);
	}

	private Map<String, ?> GetRequestFor(@NonNull final String udid)
	{
		Map<String, String> result = new HashMap<>();
		result.put("udid", udid);
		return result;
	}
}