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
import com.parse.ParseUser;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import alex.imhere.layer.server.ServerAPI;
import alex.imhere.layer.server.Session;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ParseApplicationTest extends ApplicationTestCase<ParseApplication> {
	private ServerAPI serverAPI;
	private String udid;



	public ParseApplicationTest() {
		super(ParseApplication.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		serverAPI = new ServerAPI();
		udid = Settings.Secure.getString(this.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public void testGetOnlineUsers() throws Exception {
		Session session = serverAPI.login(udid);
		ArrayList<Session> users = serverAPI.getOnlineUsers(session);
		serverAPI.logout(session);

		assertEquals("Must be only one user", 1, users.size());
	}
}