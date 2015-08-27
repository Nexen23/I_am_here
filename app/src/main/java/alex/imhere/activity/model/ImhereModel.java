package alex.imhere.activity.model;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.parse.ParseException;

import org.joda.time.Duration;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import alex.imhere.layer.server.ServerAPI;
import alex.imhere.layer.server.Session;
import alex.imhere.service.ChannelService;
import alex.imhere.util.ListObservable;
import alex.imhere.util.TemporarySet;

public class ImhereModel extends BaseModel<ImhereModel.EventsListener> {
	EventsListener notifier = new EventsListener() {
		@Override
		public void onAddUser(final Session session) {
			for (EventsListener listener : listeners) {
				listener.onAddUser(session);
			}
		}

		@Override
		public void onRemoveUser(final Session session) {
			for (EventsListener listener : listeners) {
				listener.onRemoveUser(session);
			}
		}

		@Override
		public void onClearUsers() {
			for (EventsListener listener : listeners) {
				listener.onClearUsers();
			}
		}

		@Override
		public void onLogin(final Session session) {
			for (EventsListener listener : listeners) {
				listener.onLogin(session);
			}
		}

		@Override
		public void onLogout() {
			for (EventsListener listener : listeners) {
				listener.onLogout();
			}
		}
	};

	//TODO: exerpt methods to Service! This is too complex for Model in MVC
	private ServerAPI api = new ServerAPI();
	private ChannelService channel;

	private String udid;
	private Session currentSession = null;
	private TemporarySet<Session> onlineUsersSet = new TemporarySet<>();
	private Observer onlineUsersObserver = new Observer() {
		@Override
		public void update(Observable observable, Object data) {
			try {
				ListObservable.NotificationData notificationData = (ListObservable.NotificationData) data;

				if (notificationData.notification == ListObservable.Notification.ADD) {
					notifier.onAddUser((Session) notificationData.data);
				}
				if (notificationData.notification == ListObservable.Notification.REMOVE) {
					notifier.onRemoveUser((Session) notificationData.data);
				}
				if (notificationData.notification == ListObservable.Notification.CLEAR) {
					notifier.onClearUsers();
				}
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: 25.08.2015 log
				throw e;
			}
		}
	};

	private Timer timer = new Timer();
	private TimerTask timerTask;

	public ImhereModel(@NonNull String udid) {
		this.udid = udid;

		ChannelService.ChannelEventsListener channelListener = new ChannelService.ChannelEventsListener() {
			@Override
			public void onUserOnline(Session session) {
				if ( isCurrentSessionAlive() && onlineUsersSet.add(session, session.getAliveTo()) ) {
					//notifyDataChanged(ADD_USER_NOTIFICATION, session);
				}
			}

			@Override
			public void onUserOffline(Session session) {
				if ( isCurrentSessionAlive() && onlineUsersSet.remove(session) ) {
					//notifyDataChanged(REMOVE_USER_NOTIFICATION, session);
				}
			}
		};

		onlineUsersSet.addObserver(onlineUsersObserver);

		channel = new ChannelService(channelListener);
	}

	public boolean isCurrentSessionAlive() {
		return currentSession != null && currentSession.getRestLifetime().getMillis() != 0;
	}

	public Duration getCurrentSessionLifetime() {
		return currentSession.getRestLifetime();
	}

	public final List<Session> getOnlineUsersSet() {
		return onlineUsersSet.asReadonlyList();
	}

	public Session openNewSession(final Runnable onSessionClosed) throws ParseException {
		//TODO: log exception
		onlineUsersSet.clear();
		Session currentSession = api.login(udid);
		channel.connect();
		// TODO: 18.08.2015 log exception

		List<Session> onlineUsers = api.getOnlineUsers(currentSession);
		for (Session session : onlineUsers) {
			onlineUsersSet.add(session, session.getAliveTo());
		}

		timerTask = new TimerTask() {
			@Override
			public void run() {
				cancelCurrentSession();
				onSessionClosed.run();
			}
		};
		timer.schedule(timerTask, currentSession.getAliveTo().toDate());

		this.currentSession = currentSession;
		notifier.onLogin(currentSession);

		return currentSession;
	}

	public void cancelCurrentSession() {
		if (currentSession != null) {
			channel.disconnect();
			api.logout(currentSession);

			timerTask.cancel();
			timer.purge();
			timerTask = null;

			currentSession = null;
			//onlineUsersSet.clear();

			notifier.onClearUsers();
			notifier.onLogout();
		}
	}

	public interface EventsListener extends BaseModel.EventsListener {
		void onAddUser(final Session session);
		void onRemoveUser(final Session session);
		void onClearUsers();

		void onLogin(final Session session);
		void onLogout();
	}

	public interface EventsListenerOwner {
		public EventsListener getEventsListener();
	}
}
