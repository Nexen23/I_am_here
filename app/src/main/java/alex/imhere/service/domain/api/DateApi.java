package alex.imhere.service.domain.api;

import android.support.annotation.NonNull;

import com.parse.ParseCloud;
import com.parse.ParseException;

import org.joda.time.DateTime;

import javax.inject.Inject;

import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.service.domain.parser.JsonParser;

public class DateApi extends Api {
	static final String
			API_GetNow = "GetNow";

	@Inject
	public DateApi(JsonParser parser) {
		super(parser);
	}

	public DateTime getNow(@NonNull final DyingUser dyingUser) throws ApiException {
		DateTime date;
		try {
			String jsonDate = ParseCloud.callFunction(API_GetNow, constructRequestForUser(dyingUser.getUdid()));
			date = parser.fromJson(jsonDate, DateTime.class);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new ApiException("cannot get now date", e);
		}
		return date;
	}
}