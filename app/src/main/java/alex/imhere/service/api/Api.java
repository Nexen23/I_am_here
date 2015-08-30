package alex.imhere.service.api;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import alex.imhere.service.Service;
import alex.imhere.service.domain.ParserService;
import alex.imhere.service.parser.UserParser;

public abstract class Api {
	UserParser parser;

	public Api(@NonNull ParserService parserService) {
		this.parser = parserService.getUserParser();
	}

	public UserParser getParser() {
		return parser;
	}

	public void setParser(UserParser parser) {
		this.parser = parser;
	}

	protected Map<String, ?> constructRequestForUser(@NonNull final String udid)
	{
		Map<String, String> result = new HashMap<>();
		result.put("udid", udid);
		return result;
	}

	static public class Exception extends java.lang.Exception {
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
	}
}
