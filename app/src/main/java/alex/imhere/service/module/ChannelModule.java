package alex.imhere.service.module;

import alex.imhere.service.channel.Channel;
import alex.imhere.service.channel.PubnubBroadcastChannel;
import alex.imhere.service.channel.PubnubServerTunnel;
import alex.imhere.service.channel.ServerTunnel;
import dagger.Module;
import dagger.Provides;

@Module
public class ChannelModule {
	@Provides
	public Channel provideChannel() {
		return new PubnubBroadcastChannel();
	}

	@Provides
	public ServerTunnel provideServerTunnel() {
		return new PubnubServerTunnel();
	}
}
