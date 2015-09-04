package alex.imhere.service.module;

import alex.imhere.service.parser.GsonJsonParser;
import alex.imhere.service.parser.JsonParser;
import dagger.Module;
import dagger.Provides;

@Module
public class ParserModule {
	@Provides
	public JsonParser provideUserParser() {
		return new GsonJsonParser();
	}
}
