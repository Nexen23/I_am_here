package alex.imhere.service.channel;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

public class PubnubBroadcastChannel extends BroadcastChannel {
	static private final String CHANNEL_NAME = "events"; // TODO: 29.08.2015 get name from Server & sub-key
	static private final String SUBSCRIBE_KEY = "sub-c-a3d06db8-410b-11e5-8bf2-0619f8945a4f";

	Pubnub pubnub = new Pubnub("", SUBSCRIBE_KEY);
	Callback pubnubCallback = new Callback() {
		@Override
		public void connectCallback(String channel, Object message) {
			if (listener != null && isResumed()) {
				listener.onConnect(channel, "Connected: " + message.toString());
			}
		}

		@Override
		public void disconnectCallback(String channel, Object message) {
			if (listener != null && isResumed()) {
				listener.onDisconnect(channel, "Disconnected: " + message.toString());
			}
		}

		@Override
		public void reconnectCallback(String channel, Object message) {
			if (listener != null && isResumed()) {
				listener.onReconnect(channel, "Reconnected: " + message.toString());
			}
		}

		@Override
		public void successCallback(String channel, Object message, String timetoken) {
			if (listener != null && isResumed()) {
				listener.onMessageRecieve(channel, message.toString(), timetoken);
			}
		}

		@Override
		public void errorCallback(String channel, PubnubError error) {
			if (listener != null && isResumed()) {
				listener.onErrorOccur(channel, "Error occured: " + error.toString());
			}
		}
	};

	public PubnubBroadcastChannel() {
		setName(CHANNEL_NAME);
	}

	@Override
	public void connect() throws BroadcastChannel.Exception {
		try {
			pubnub.subscribe(CHANNEL_NAME, pubnubCallback);
		} catch (PubnubException e) {
			e.printStackTrace();
			throw new BroadcastChannel.Exception("cannot connect", e);
		}
	}

	@Override
	public void disconnect() {
		pubnub.unsubscribeAll();
	}
}
