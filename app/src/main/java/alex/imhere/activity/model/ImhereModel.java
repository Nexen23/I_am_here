package alex.imhere.activity.model;

import android.support.annotation.NonNull;

import com.parse.ParseException;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import alex.imhere.layer.server.DyingUser;
import alex.imhere.layer.server.ServerAPI;
import alex.imhere.service.ChannelService;
import alex.imhere.util.ListObservable;
import alex.imhere.util.ListeningController;
import alex.imhere.util.TemporarySet;
import hugo.weaving.DebugLog;

public class ImhereModel extends BaseModel<ImhereModel.EventListener> implements ListeningController {
	Logger l = LoggerFactory.getLogger(ImhereModel.class);

	EventListener notifier = new EventListener() {
		@Override
		public void onLoginUser(final DyingUser dyingUser) {
			for (EventListener listener : getListenersSet()) {
				listener.onLoginUser(dyingUser);
			}
		}

		@Override
		public void onLogoutUser(final DyingUser dyingUser) {
			for (EventListener listener : getListenersSet()) {
				listener.onLogoutUser(dyingUser);
			}
		}

		@Override
		public void onUsersUpdate() {
			for (EventListener listener : getListenersSet()) {
				listener.onUsersUpdate();
			}
		}

		@Override
		public void onClearUsers() {
			for (EventListener listener : getListenersSet()) {
				listener.onClearUsers();
			}
		}

		@Override
		public void onLogin(final DyingUser currentUser) {
			for (EventListener listener : getListenersSet()) {
				listener.onLogin(currentUser);
			}
		}

		@Override
		public void onLogout() {
			for (EventListener listener : getListenersSet()) {
				listener.onLogout();
			}
		}
	};

	//TODO: exerpt methods to Service! This is too complex for Model in MVC
	private ServerAPI api = new ServerAPI();
	private ChannelService channel = new ChannelService();
	ChannelService.ChannelEventsListener channelListener;


	private String udid;
	private DyingUser currentUser = null;
	private TemporarySet<DyingUser> onlineUsersSet = new TemporarySet<>();
	private Observer onlineUsersObserver;

	private Timer timer = new Timer();
	private TimerTask timerTask;

	public ImhereModel(@NonNull String udid) {
		this.udid = udid;
	}

	public boolean isCurrentSessionAlive() {
		return currentUser != null && currentUser.getRestLifetime().getMillis() != 0;
	}

	public Duration getCurrentSessionLifetime() {
		return currentUser.getRestLifetime();
	}

	/*public final List<DyingUser> getOnlineUsersSet() {
		return onlineUsersSet.asReadonlyList();
	}*/

	public DyingUser openNewSession(final Runnable onSessionClosed) throws ParseException {
		//TODO: log exception
		onlineUsersSet.clear();
		currentUser = api.login(udid);
		channel.connect();
		// TODO: 18.08.2015 log exception

		updateOnlineUsers();

		timerTask = new TimerTask() {
			@Override
			public void run() {
				cancelCurrentSession();
				onSessionClosed.run();
			}
		};
		timer.schedule(timerTask, currentUser.getAliveTo().toDate());

		notifier.onLogin(currentUser);

		return currentUser;
	}

	public void updateOnlineUsers() {
		l.info("updateing online users");
		if (isCurrentSessionAlive()) {
			List<DyingUser> onlineUsers = null;
			try {
				onlineUsers = api.getOnlineUsers(currentUser);
				for (DyingUser dyingUser : onlineUsers) {
					boolean wasAdded = onlineUsersSet.add(dyingUser, dyingUser.getAliveTo());
					l.info("User: {} was added ({})", dyingUser.getUdid(), Boolean.valueOf(wasAdded).toString());
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		//notifier.onUsersUpdate();
	}

	public void cancelCurrentSession() {
		if (currentUser != null) {
			channel.disconnect();
			api.logout(currentUser);

			timerTask.cancel();
			timer.purge();
			timerTask = null;

			currentUser = null;
			//onlineUsersSet.clear();

			notifier.onClearUsers();
			notifier.onLogout();
		}
	}

	@Override
	public void startListening() {
		onlineUsersObserver = new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				try {
					ListObservable.NotificationData notificationData = (ListObservable.NotificationData) data;

					if (notificationData.notification == ListObservable.Notification.ADD) {
						notifier.onLoginUser((DyingUser) notificationData.data);
					}
					if (notificationData.notification == ListObservable.Notification.REMOVE) {
						notifier.onLogoutUser((DyingUser) notificationData.data);
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
		onlineUsersSet.addObserver(onlineUsersObserver);

		channelListener = new ChannelService.ChannelEventsListener() {
			@Override
			public void onUserOnline(DyingUser session) {
				if ( isCurrentSessionAlive() && onlineUsersSet.add(session, session.getAliveTo()) ) {
					//notifyDataChanged(ADD_USER_NOTIFICATION, session);
				}
			}

			@Override
			public void onUserOffline(DyingUser session) {
				if ( isCurrentSessionAlive() && onlineUsersSet.remove(session) ) {
					//notifyDataChanged(REMOVE_USER_NOTIFICATION, session);
				}
			}
		};
		channel.setListener(channelListener);
	}

	@Override
	public void stopListening() {
		onlineUsersSet.deleteObserver(onlineUsersObserver);
		onlineUsersObserver = null;

		channel.clearListener();
		channelListener = null;
	}

	public interface EventListener extends BaseModel.EventListener {
		void onLoginUser(final DyingUser dyingUser);
		void onLogoutUser(final DyingUser dyingUser);

		void onUsersUpdate();
		void onClearUsers();

		void onLogin(final DyingUser currentUser);
		void onLogout();
	}
}
