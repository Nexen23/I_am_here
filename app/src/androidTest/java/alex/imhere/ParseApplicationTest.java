package alex.imhere;

import android.provider.Settings;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.parse.ParseCloud;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import alex.imhere.layer.server.Session;
import alex.imhere.layer.server.User;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ParseApplicationTest extends ApplicationTestCase<ParseApplication> {
	private Session session;
	private String udid;
	private Gson gson;

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

	public ParseApplicationTest() {
		super(ParseApplication.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		session = new Session();
		udid = Settings.Secure.getString(this.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
		gson = new GsonBuilder()
				.registerTypeAdapter(Date.class, ser)
				.registerTypeAdapter(Date.class, deser).create();
	}

	public void testLogin() throws Exception {
		//session.login( udid );

		User user = new User();
		user.setUdid(udid);

		Log.d("TAG", gson.toJson(user));

		String jsonUser = gson.toJson(user);
		String result = ParseCloud.callFunction("Login", wrapJsonWithMap(jsonUser));

		user = gson.fromJson( result, User.class );
		//User user = session.getCurrentUser();

		Log.d("TAG", gson.toJson(user));
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