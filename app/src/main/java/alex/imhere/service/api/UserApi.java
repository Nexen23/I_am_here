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

public class UserApi extends Api {
	static final String
			API_Login = "Login",
			API_Logout = "Logout",
			API_GetOnlineUsers = "GetOnlineUsers";

	@Inject
	public UserApi(JsonParser parser) {
		super(parser);
	}

	// TODO: 03.09.2015 must be in another AuthApi
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

	public final ArrayList<DyingUser> getOnlineUsers(@NonNull final DyingUser dyingUser) throws ApiException {
		ArrayList<DyingUser> users = null;
		try {
			String jsonUsers = ParseCloud.callFunction(API_GetOnlineUsers, constructRequestForUser(dyingUser.getUdid()));
			users = parser.fromJson(jsonUsers, new TypeToken<List<DyingUser>>(){}.getType());
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ApiException("cannot get online users", e);
		}
		return users;
	}
}