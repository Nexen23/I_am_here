package alex.imhere.activity.model;

import android.support.annotation.NonNull;

import com.parse.ParseException;

import org.joda.time.DateTime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import alex.imhere.layer.server.ServerAPI;
import alex.imhere.layer.server.Session;
import alex.imhere.service.ChannelService;

public class ImhereModel extends AbstractModel {
	//TODO: exerpt methods to Service! This is too complex for Model in MVC
	ServerAPI api = new ServerAPI();
	ChannelService channel;

	String udid;
	Session currentSession = null;
	List<Session> onlineUsers = new ArrayList<>();

	/*LocalDateTime now = new LocalDateTime();*/

	public ImhereModel(@NonNull String udid) {
		this.udid = udid;
		channel = new ChannelService(new ChannelService.ChannelEventsListener() {
			@Override
			public void onUserOnline(Session session) {
				int sessionIndex = onlineUsers.lastIndexOf(session);
				if (sessionIndex != -1) {
					onlineUsers.remove(sessionIndex);
				}

				onlineUsers.add(0, session);

				notifyDataChanged();
			}

			@Override
			public void onUserOffline(Session session) {
				boolean userWasRemoved = onlineUsers.remove(session);
				if (userWasRemoved) {
					notifyDataChanged();
				}
			}
		});
	}

	public boolean isCurrentSessionAlive() {
		// TODO: do tests about aliveTo field
		return currentSession != null;
	}

	public final List<Session> getOnlineUsersReadonly() {
		return Collections.unmodifiableList(onlineUsers);
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
		onlineUsers.clear();

		currentSession = api.login(udid);
		// TODO: 18.08.2015 log exception
		onlineUsers.addAll( api.getOnlineUsers(currentSession) );
		channel.connect();

		notifyDataChanged();

		return currentSession;
	}

	public void cancelCurrentSession() {
		if (currentSession != null) {
			channel.disconnect();
			api.logout(currentSession);

			currentSession = null;
			onlineUsers.clear();

			notifyDataChanged();
		}
	}
}
