package alex.imhere.service.domain;

import alex.imhere.service.channel.BroadcastChannel;

public class ChannelService {
	BroadcastChannel broadcastChannel;

	public BroadcastChannel getBroadcastChannel() {
		return broadcastChannel;
	}

	public void setBroadcastChannel(BroadcastChannel broadcastChannel) {
		this.broadcastChannel = broadcastChannel;
	}
}
