package alex.imhere;

import android.provider.Settings;
import android.test.ApplicationTestCase;

import alex.imhere.service.domain.api.UserApi;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ImhereApplicationTest extends ApplicationTestCase<ImhereApplication> {
	private UserApi userApi;
	private String udid;



	public ImhereApplicationTest() {
		super(ImhereApplication.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		/*userApi = new UserApi();*/
		udid = Settings.Secure.getString(this.getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
	}

	public void testGetOnlineUsers() throws Exception {
		/*DyingUser dyingUser = userApi.login(udid);
		ArrayList<DyingUser> users = userApi.getOnlineUsers(dyingUser);
		userApi.logout(dyingUser);

		assertEquals("Must be only one user", 1, users.size());*/
	}
}