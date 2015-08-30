package alex.imhere;

import android.provider.Settings;
import android.test.ApplicationTestCase;

import java.util.ArrayList;

import alex.imhere.service.api.UserApi;
import alex.imhere.entity.DyingUser;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ParseApplicationTest extends ApplicationTestCase<ParseApplication> {
	private UserApi userApi;
	private String udid;



	public ParseApplicationTest() {
		super(ParseApplication.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		userApi = new UserApi();
		udid = Settings.Secure.getString(this.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public void testGetOnlineUsers() throws Exception {
		DyingUser dyingUser = userApi.login(udid);
		ArrayList<DyingUser> users = userApi.getOnlineUsers(dyingUser);
		userApi.logout(dyingUser);

		assertEquals("Must be only one user", 1, users.size());
	}
}