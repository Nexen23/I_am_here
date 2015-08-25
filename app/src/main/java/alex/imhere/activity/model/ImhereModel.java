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

public class ImhereModel extends AbstractModel {
	// TODO: 25.08.2015 make smart flags without chances of collision
	static public final int ADD_USER_NOTIFICATION = 1 + LAST_NOTIFICATION_FLAG;
	static public final int REMOVE_USER_NOTIFICATION = 2 + LAST_NOTIFICATION_FLAG;
	static public final int CLEAR_USER_NOTIFICATION = 3 + LAST_NOTIFICATION_FLAG;
	static public final int LOGIN_NOTIFICATION = 4 + LAST_NOTIFICATION_FLAG;
	//static protected final int LAST_NOTIFICATION_FLAG = 5;

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
					notifyDataChanged(ADD_USER_NOTIFICATION, notificationData.data);
				}
				if (notificationData.notification == ListObservable.Notification.REMOVE) {
					notifyDataChanged(REMOVE_USER_NOTIFICATION, notificationData.data);
				}
				if (notificationData.notification == ListObservable.Notification.CLEAR) {
					notifyDataChanged(UNIVERSAL_NOTIFICATION, null);
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

	/*LocalDateTime now = new LocalDateTime();*/

	public ImhereModel(@NonNull Handler uiHandler, @NonNull String udid) {
		super(uiHandler);
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

	/*public DateTime getNow() throws ParseException {
		//TODO: log exception
		// TODO: 17.08.2015 session can be null
		now = api.getNow(currentSession);
		return now;
	}*/

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
		notifyDataChanged(LOGIN_NOTIFICATION);

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

			notifyDataChanged(CLEAR_USER_NOTIFICATION);
		}
	}
}
