package alex.imhere;

import android.provider.Settings;
import android.test.ApplicationTestCase;

import alex.imhere.service.domain.api.UserApi;

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
		/*DyingUser dyingUser = userApi.loginInBackground(udid);
		ArrayList<DyingUser> users = userApi.getOnlineUsers(dyingUser);
		userApi.logoutInBackground(dyingUser);

		assertEquals("Must be only one user", 1, users.size());*/
	}
}