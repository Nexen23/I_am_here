package alex.imhere.service.api;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import alex.imhere.entity.DyingUser;
import alex.imhere.service.domain.ParserService;

public class UserApi extends Api {
	static final String
			API_Login = "Login",
			API_Logout = "Logout",
			API_GetOnlineUsers = "GetOnlineUsers";

	public UserApi(@NonNull ParserService parserService) {
		super(parserService);
	}

	public DyingUser login(@NonNull final String udid) throws UserApi.Exception {
		DyingUser dyingUser = null;
		try {
			String jsonObject = ParseCloud.callFunction(API_Login, constructRequestForUser(udid));
			dyingUser = parser.fromJson(jsonObject, DyingUser.class);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new UserApi.Exception("cannot login", e);
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

	public final ArrayList<DyingUser> getOnlineUsers(@NonNull final DyingUser dyingUser) throws UserApi.Exception {
		ArrayList<DyingUser> users = null;
		try {
			String jsonUsers = ParseCloud.callFunction(API_GetOnlineUsers, constructRequestForUser(dyingUser.getUdid()));
			users = parser.fromJson(jsonUsers, new TypeToken<List<DyingUser>>(){}.getType());
		} catch (ParseException e) {
			e.printStackTrace();
			throw new UserApi.Exception("cannot get online users", e);
		}
		return users;
	}

	static public class Exception extends Api.Exception {
		public Exception() {
			super();
		}

		public Exception(String detailMessage) {
			super(detailMessage);
		}

		public Exception(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public Exception(Throwable throwable) {
			super(throwable);
		}
	};
}