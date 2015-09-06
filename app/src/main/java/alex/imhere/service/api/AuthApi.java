package alex.imhere.service.api;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.service.parser.JsonParser;

public class AuthApi extends Api {
	static final String
			API_Login = "Login",
			API_Logout = "Logout";

	@Inject
	public AuthApi(JsonParser parser) {
		super(parser);
	}

	public DyingUser login(@NonNull final String udid) throws ApiException {
		DyingUser dyingUser = null;
		try {
			String jsonObject = ParseCloud.callFunction(API_Login, constructRequestForUser(udid));
			dyingUser = parser.fromJson(jsonObject, DyingUser.class);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ApiException("cannot login", e);
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