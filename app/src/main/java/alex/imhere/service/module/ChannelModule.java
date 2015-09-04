package alex.imhere.service.module;

import alex.imhere.service.channel.Channel;
import alex.imhere.service.channel.PubnubBroadcastChannel;
import dagger.Module;
import dagger.Provides;

@Module
public class ChannelModule {
	@Provides
	public Channel provideChannel() {
		return new PubnubBroadcastChannel();
	}
}
