package alex.imhere.service.channel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import alex.imhere.entity.DyingUser;
import alex.imhere.service.Service;
import alex.imhere.service.parser.UserParser;

public class PubnubBroadcastChannel extends BroadcastChannel {
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
