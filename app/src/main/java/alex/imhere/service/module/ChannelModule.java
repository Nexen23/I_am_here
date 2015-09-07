package alex.imhere.service.module;

import alex.imhere.service.domain.channel.Channel;
import alex.imhere.service.domain.channel.PubnubBroadcastChannel;
import alex.imhere.service.domain.channel.PubnubServerTunnel;
import alex.imhere.service.domain.channel.ServerTunnel;
import alex.imhere.service.domain.parser.JsonParser;
import dagger.Module;
import dagger.Provides;

@Module
public class ChannelModule {
	@Provides
	public Channel provideChannel() {
		return new PubnubBroadcastChannel();
	}

	@Provides
	public ServerTunnel provideServerTunnel(Channel serverChannel, JsonParser jsonParser) {
		return new PubnubServerTunnel(serverChannel, jsonParser);
	}
}
