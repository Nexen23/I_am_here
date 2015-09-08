package alex.imhere.service.domain.api;

import android.support.annotation.NonNull;

import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;

import javax.inject.Inject;

import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.service.domain.parser.JsonParser;

public class AuthApi extends Api {
	static final String
			API_Login = "Login",
			API_Logout = "Logout";

	@Inject
	public AuthApi(JsonParser parser) {
		super(parser);
	}

	public DyingUser login(@NonNull final String udid) throws ApiException {
		DyingUser dyingUser;
		try {
			String jsonObject = ParseCloud.callFunction(API_Login, constructRequestForUser(udid));
			dyingUser = parser.fromJson(jsonObject, DyingUser.class);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ApiException(ApiException.LOGIN_ERROR, e);
		}
		return dyingUser;
	}

	public void logout(@NonNull final DyingUser dyingUser) {
		try {
			ParseCloud.callFunction(API_Logout, constructRequestForUser(dyingUser.getUdid()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}