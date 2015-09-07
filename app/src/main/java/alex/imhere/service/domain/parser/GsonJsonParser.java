package alex.imhere.service.domain.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Type;

public class GsonJsonParser implements JsonParser {
	private Gson gson;

	public GsonJsonParser() {
		JsonDeserializer<DateTime> deserializer = new JsonDeserializer<DateTime>() {
			@Override
			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				DateTime dateTime = new DateTime(0);
				if (json != null) {
					JsonObject asJsonObject = json.getAsJsonObject();
					String dateTimeString = asJsonObject.get("iso").getAsString();

					//DateTimeZone localTZ = DateTimeZone.getDefault();
					//dateTime = new DateTime( localTZ.convertUTCToLocal(new DateTime(dateTimeString).getMillis()) );
					dateTime = new DateTime(dateTimeString);
				}
				return dateTime;
			}
		};

		gson = new GsonBuilder()
				.registerTypeAdapter(DateTime.class, deserializer).create();
	}

	public <T> T fromJson(String json, Type type) {
		T object = gson.fromJson(json, type);
		return object;
	}

	public <T> T fromJson(String json, Class<T> clazz) {
		T object = gson.fromJson(json, clazz);
		return object;
	}
}
