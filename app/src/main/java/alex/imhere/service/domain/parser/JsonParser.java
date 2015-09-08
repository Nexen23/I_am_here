package alex.imhere.service.domain.parser;

import java.lang.reflect.Type;

public interface JsonParser {
	<T> T fromJson(String json, Type type);
	<T> T fromJson(String json, Class<T> clazz);
}
