package alex.imhere.service;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import alex.imhere.layer.server.DyingUser;

public class ChannelService {
	// TODO: 18.08.2015 split it to MainService and Service for ServerAPI only (also no hardcoded strings!)
	final static String CHANNEL_NAME = "events";

	private Pubnub pubnub = new Pubnub("", "sub-c-a3d06db8-410b-11e5-8bf2-0619f8945a4f");
	private ChannelEventsListener eventsListener;
	private JsonParser jsonParser = new JsonParser();

	public ChannelService() {
	}

	public ChannelService(ChannelEventsListener eventsListener) {
		setListener(eventsListener);
	}

	public void setListener(ChannelEventsListener eventsListener) {
		this.eventsListener = eventsListener;
	}

	public void clearListener() {
		this.eventsListener = null;
	}

	public void connect() {
		try {
			pubnub.subscribe(CHANNEL_NAME, new Callback() {
						@Override
						public void connectCallback(String channel, Object message) {
							Log.d("TAG", "SUBSCRIBE : CONNECT on channel:" + channel
									+ " : " + message.getClass() + " : "
									+ message.toString());
						}

						@Override
						public void disconnectCallback(String channel, Object message) {
							Log.d("TAG", "SUBSCRIBE : DISCONNECT on channel:" + channel
									+ " : " + message.getClass() + " : "
									+ message.toString());
						}

						@Override
						public void reconnectCallback(String channel, Object message) {
							Log.d("TAG", "SUBSCRIBE : RECONNECT on channel:" + channel
									+ " : " + message.getClass() + " : "
									+ message.toString());
						}

						@Override
						public void successCallback(String channel, Object message, String timetoken) {
							Log.d("TAG", "SUBSCRIBE : " + channel + " : "
									+ message.getClass() + " : " + message.toString() + " [" + timetoken + "]");

							DyingUser dyingUser = jsonParser.fromJson(message.toString(), DyingUser.class);
							int timeComparizion = dyingUser.getLoginedAt().compareTo(dyingUser.getAliveTo());
							boolean sessionIsDead = false;
							if (timeComparizion >= 0) {
								sessionIsDead = true;
							}

							if (sessionIsDead) {
								eventsListener.onUserOffline(dyingUser);
							} else {
								eventsListener.onUserOnline(dyingUser);
							}
						}

						@Override
						public void errorCallback(String channel, PubnubError error) {
							Log.d("TAG", "SUBSCRIBE : ERROR on channel " + channel
									+ " : " + error.toString());
						}
					}
			);
		} catch (PubnubException e) {
			Log.d("TAG", e.toString());
		}
	}

	public void disconnect() {
		pubnub.unsubscribeAll();
	}

	public interface ChannelEventsListener {
		void onUserOnline(DyingUser dyingUser);
		void onUserOffline(DyingUser dyingUser);
	}
}
