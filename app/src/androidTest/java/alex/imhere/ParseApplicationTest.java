package alex.imhere;

import android.provider.Settings;
import android.test.ApplicationTestCase;

import java.util.ArrayList;

import alex.imhere.service.api.ServerAPI;
import alex.imhere.entity.DyingUser;

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
		DyingUser dyingUser = serverAPI.login(udid);
		ArrayList<DyingUser> users = serverAPI.getOnlineUsers(dyingUser);
		serverAPI.logout(dyingUser);

		assertEquals("Must be only one user", 1, users.size());
	}
}