package alex.imhere.service.domain.channel;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import alex.imhere.exception.ChannelException;

public class PubnubChannel extends Channel {
	static private final String CHANNEL_NAME = "events"; // TODO: 29.08.2015 get name from Server & sub-key
	static private final String SUBSCRIBE_KEY = "sub-c-a3d06db8-410b-11e5-8bf2-0619f8945a4f";

	Pubnub pubnub = new Pubnub("", SUBSCRIBE_KEY);
	Callback pubnubCallback = new Callback() {
		@Override
		public void connectCallback(String channel, Object message) {
			if (listener != null) {
				listener.onConnect(channel, "Connected: " + message.toString());
			}
		}

		@Override
		public void disconnectCallback(String channel, Object message) {
			if (listener != null) {
				listener.onDisconnect(channel, "Disconnected: " + message.toString());
			}
		}

		@Override
		public void reconnectCallback(String channel, Object message) {
			if (listener != null) {
				listener.onReconnect(channel, "Reconnected: " + message.toString());
			}
		}

		@Override
		public void successCallback(String channel, Object message, String timetoken) {
			if (listener != null) {
				listener.onMessageRecieve(channel, message.toString(), timetoken);
			}
		}

		@Override
		public void errorCallback(String channel, PubnubError error) {
			if (listener != null) {
				listener.onErrorOccur(channel, "Error occured: " + error.toString());
			}
		}
	};

	public PubnubChannel() {
		setName(CHANNEL_NAME);
	}

	@Override
	public void subscribe() throws ChannelException {
		try {
			pubnub.subscribe(CHANNEL_NAME, pubnubCallback);
		} catch (PubnubException e) {
			e.printStackTrace();
			throw new ChannelException(ChannelException.CONNECT_ERROR, e);
		}
	}

	@Override
	public void unsubscribe() {
		pubnub.unsubscribeAll();
	}
}
