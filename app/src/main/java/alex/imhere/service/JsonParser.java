package alex.imhere.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.lang.reflect.Type;

public class JsonParser {
	private Gson gson;

	public JsonParser() {
		JsonDeserializer<LocalDateTime> deserializer = new JsonDeserializer<LocalDateTime>() {
			@Override
			public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				LocalDateTime localDateTime = new LocalDateTime(0);
				if (json != null) {
					JsonObject asJsonObject = json.getAsJsonObject();
					String dateTimeString = asJsonObject.get("iso").getAsString();

					//DateTimeZone localTZ = DateTimeZone.getDefault();
					//localDateTime = new LocalDateTime( localTZ.convertUTCToLocal(new DateTime(dateTimeString).getMillis()) );
					localDateTime = new DateTime(dateTimeString).toLocalDateTime();
				}
				return localDateTime;
			}
		};

		gson = new GsonBuilder()
				.registerTypeAdapter(LocalDateTime.class, deserializer).create();
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
