package alex.imhere.service.module;

import alex.imhere.service.domain.ticker.TimeTicker;
import dagger.Module;
import dagger.Provides;

@Module
public class TickerModule {
	@Provides
	public TimeTicker provideTimeTicker() {
		return new TimeTicker();
	}
}