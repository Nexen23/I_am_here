package alex.imhere.service.domain.api;

import android.support.annotation.NonNull;

import com.parse.FunctionCallback;
import com.parse.Parse;
import com.parse.ParseCloud;
import com.parse.ParseException;

import javax.inject.Inject;

import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.service.domain.parser.JsonParser;
import bolts.Continuation;
import bolts.Task;

public class AuthApi extends Api {
	static final long TIMEOUT_DEFAULT = 5000;
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
			final String[] jsonObject = new String[1];
			final ParseException[] exception = new ParseException[1];
			synchronized (jsonObject) {
				ParseCloud.callFunctionInBackground(API_Login, constructRequestForUser(udid), new FunctionCallback<String>() {
					@Override
					public void done(String s, ParseException e) {
						synchronized (jsonObject) {
							jsonObject[0] = s;
							exception[0] = e;
							jsonObject.notifyAll();
						}
					}
				});

				try {
					jsonObject.wait(TIMEOUT_DEFAULT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (exception[0] != null) {
				throw exception[0];
			}

			if (jsonObject[0] == null) {
				throw new ApiException(ApiException.TIMEOUT_ERROR);
			}
			dyingUser = parser.fromJson(jsonObject[0], DyingUser.class);
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