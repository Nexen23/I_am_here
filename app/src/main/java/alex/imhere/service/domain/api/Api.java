package alex.imhere.service.domain.api;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import alex.imhere.service.domain.parser.JsonParser;

abstract class Api {
	final JsonParser parser;

	Api(JsonParser parser) {
		this.parser = parser;
	}

	protected Map<String, ?> constructRequestForUser(@NonNull final String udid)
	{
		Map<String, String> result = new HashMap<>();
		result.put("udid", udid);
		return result;
	}
}
