package alex.imhere.activity.model;

import android.support.annotation.NonNull;

import com.parse.ParseException;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import alex.imhere.layer.server.ServerAPI;
import alex.imhere.layer.server.Session;
import alex.imhere.service.ChannelService;
import alex.imhere.util.TemporarySet;

public class ImhereModel extends AbstractModel {
	//TODO: exerpt methods to Service! This is too complex for Model in MVC
	ServerAPI api = new ServerAPI();
	ChannelService channel;

	String udid;
	Session currentSession = null;
	TemporarySet<Session> onlineUsersReadonly = new TemporarySet<>();

	/*LocalDateTime now = new LocalDateTime();*/

	public ImhereModel(@NonNull String udid) {
		this.udid = udid;

		ChannelService.ChannelEventsListener channelListener = new ChannelService.ChannelEventsListener() {
			@Override
			public void onUserOnline(Session session) {
				if ( onlineUsersReadonly.add(session, session.getAliveTo()) ) {
					notifyDataChanged();
				}
			}

			@Override
			public void onUserOffline(Session session) {
				if ( onlineUsersReadonly.remove(session) ) {
					notifyDataChanged();
				}
			}
		};

		onlineUsersReadonly.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				notifyDataChanged();
			}
		});

		channel = new ChannelService(channelListener);
	}

	public boolean isCurrentSessionAlive() {
		// TODO: do tests about aliveTo field
		return currentSession != null;
	}

	public final List<Session> getOnlineUsersReadonly() {
		return onlineUsersReadonly.asReadonlyList();
	}

	/*public DateTime getNow() throws ParseException {
		//TODO: log exception
		// TODO: 17.08.2015 session can be null
		now = api.getNow(currentSession);
		return now;
	}*/

	public Session openNewSession() throws ParseException {
		//TODO: log exception
		currentSession = null;
		onlineUsersReadonly.clear();

		currentSession = api.login(udid);
		channel.connect();
		// TODO: 18.08.2015 log exception

		List<Session> onlineUsers = api.getOnlineUsers(currentSession);
		for (Session session : onlineUsers) {
			onlineUsersReadonly.add(session, session.getAliveTo());
		}

		notifyDataChanged();

		return currentSession;
	}

	public void cancelCurrentSession() {
		if (currentSession != null) {
			channel.disconnect();
			api.logout(currentSession);

			currentSession = null;
			onlineUsersReadonly.clear();

			notifyDataChanged();
		}
	}
}
