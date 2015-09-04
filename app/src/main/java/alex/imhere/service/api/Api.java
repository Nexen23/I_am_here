package alex.imhere.service.api;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import alex.imhere.service.parser.JsonParser;

public abstract class Api {
	JsonParser parser;

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
