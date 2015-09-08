package alex.imhere.service.module;

import alex.imhere.service.domain.channel.Channel;
import alex.imhere.service.domain.channel.PubnubChannel;
import alex.imhere.service.domain.channel.ServerChannel;
import alex.imhere.service.domain.parser.JsonParser;
import dagger.Module;
import dagger.Provides;

@Module
public class ChannelModule {
	@Provides
	public Channel provideChannel() {
		return new PubnubChannel();
	}

	@Provides
	public ServerChannel provideServerChannel(Channel serverChannel, JsonParser jsonParser) {
		return new ServerChannel(serverChannel, jsonParser);
	}
}
