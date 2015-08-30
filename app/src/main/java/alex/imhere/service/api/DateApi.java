package alex.imhere.service.api;

import android.support.annotation.NonNull;

import com.parse.ParseCloud;
import com.parse.ParseException;

import org.joda.time.DateTime;

import alex.imhere.entity.DyingUser;
import alex.imhere.service.Service;
import alex.imhere.service.domain.ParserService;
import alex.imhere.service.parser.UserParser;

public class DateApi extends Api {
	static final String
			API_GetNow = "GetNow";

	public DateApi(@NonNull ParserService parserService) {
		super(parserService);
	}

	public DateTime getNow(@NonNull final DyingUser dyingUser) throws DateApi.Exception {
		DateTime date = null;
		try {
			String jsonDate = ParseCloud.callFunction(API_GetNow, constructRequestForUser(dyingUser.getUdid()));
			date = parser.fromJson(jsonDate, DateTime.class);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new DateApi.Exception("cannot get now date", e);
		}
		return date;
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