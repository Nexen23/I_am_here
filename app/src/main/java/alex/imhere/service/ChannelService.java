package alex.imhere.service;

import android.util.Log;

import com.google.gson.Gson;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import alex.imhere.layer.server.Session;

public class ChannelService {
	// TODO: 18.08.2015 split it to MainService and Service for ServerAPI only (also no hardcoded strings!)
	final static String CHANNEL_NAME = "events";

	private Pubnub pubnub = new Pubnub("", "sub-c-a3d06db8-410b-11e5-8bf2-0619f8945a4f");
	private ChannelEventsListener eventsListener;
	private JsonParser jsonParser = new JsonParser();

	public ChannelService(ChannelEventsListener eventsListener) {
		this.eventsListener = eventsListener;
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

							Session session = jsonParser.fromJson(message.toString(), Session.class);
							int timeComparizion = session.getLoginedAt().compareTo(session.getAliveTo());
							boolean sessionIsDead = false;
							if (timeComparizion >= 0) {
								sessionIsDead = true;
							}

							if (sessionIsDead) {
								eventsListener.onUserOffline(session);
							} else {
								eventsListener.onUserOnline(session);
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
		void onUserOnline(Session session);
		void onUserOffline(Session session);
	}
}
