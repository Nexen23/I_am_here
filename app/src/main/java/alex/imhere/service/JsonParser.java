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

import java.lang.reflect.Type;

public class JsonParser {
	private Gson gson;

	public JsonParser() {
		JsonDeserializer<DateTime> deserializer = new JsonDeserializer<DateTime>() {
			@Override
			public DateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
				DateTime dataTime = null;
				if (json != null) {
					JsonObject asJsonObject = json.getAsJsonObject();
					String dateTimeString = asJsonObject.get("iso").getAsString();
					dataTime = new DateTime(dateTimeString);
				}
				return dataTime;
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
