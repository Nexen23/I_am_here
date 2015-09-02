package alex.imhere.service.api;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

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
}
