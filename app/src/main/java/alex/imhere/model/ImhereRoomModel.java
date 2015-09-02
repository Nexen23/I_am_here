package alex.imhere.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import alex.imhere.entity.DyingUser;
import alex.imhere.service.Service;
import alex.imhere.service.api.UserApi;
import alex.imhere.service.channel.BroadcastChannel;
import alex.imhere.service.parser.UserParser;
import alex.imhere.util.Resumable;
import alex.imhere.container.TemporarySet;

public class ImhereRoomModel extends AbstractModel<ImhereRoomModel.EventListener> implements Resumable {
	Logger l = LoggerFactory.getLogger(ImhereRoomModel.class);

	int state;
	EventListener notifier = new EventListener() {
		@Override
		public void onModelDataChanged(AbstractModel abstractModel) {
			for (EventListener listener : getListenersSet()) {
				listener.onModelDataChanged(ImhereRoomModel.this);
			}
		}

		@Override
		public void onUserLogin(final DyingUser dyingUser) {
			for (EventListener listener : getListenersSet()) {
				listener.onUserLogin(dyingUser);
			}
			onModelDataChanged(ImhereRoomModel.this);
		}

		@Override
		public void onUserLogout(final DyingUser dyingUser) {
			for (EventListener listener : getListenersSet()) {
				listener.onUserLogout(dyingUser);
			}
			onModelDataChanged(ImhereRoomModel.this);
		}

		@Override
		public void onUsersUpdate() {
			for (EventListener listener : getListenersSet()) {
				listener.onUsersUpdate();
			}
			onModelDataChanged(ImhereRoomModel.this);
		}

		@Override
		public void onClearUsers() {
			for (EventListener listener : getListenersSet()) {
				listener.onClearUsers();
			}
			onModelDataChanged(ImhereRoomModel.this);
		}

		@Override
		public void onLogin(final DyingUser currentUser) {
			for (EventListener listener : getListenersSet()) {
				listener.onLogin(currentUser);
			}
			onModelDataChanged(ImhereRoomModel.this);
		}

		@Override
		public void onCurrentUserTimeout() {
			for (EventListener listener : getListenersSet()) {
				listener.onCurrentUserTimeout();
			}
			onModelDataChanged(ImhereRoomModel.this);
		}


		@Override
		public void onLogout() {
			for (EventListener listener : getListenersSet()) {
				listener.onLogout();
			}
			onModelDataChanged(ImhereRoomModel.this);
		}
	};

	private final String udid;
	DyingUser currentUser;
	TemporarySet<DyingUser> onlineUsers = new TemporarySet<>();
	TemporarySet.EventListener onlineUsersListener;

	Timer timer = new Timer();
	TimerTask logoutTask;

	UserApi api;
	UserParser userParser;
	BroadcastChannel channel;
	BroadcastChannel.EventListener channelListener;

	public ImhereRoomModel(@NonNull Service service, @NonNull String udid) {
		super(service);
		this.udid = udid;

		api = service.getApiService().getUserApi();
		userParser = service.getParserService().getUserParser();

		channel = service.getChannelService().getBroadcastChannel();
	}

	private void scheduleLogoutAtCurrentUserDeath() {
		if (currentUser != null) {
			logoutTask = new TimerTask() {
				@Override
				public void run() {
					notifier.onCurrentUserTimeout();
					logout();
				}
			};
			timer.schedule(logoutTask, currentUser.getAliveTo().toDate());
		}
	}

	private void cancelLogoutAtCurrentUserDeath() {
		if (logoutTask != null) {
			logoutTask.cancel();
			logoutTask = null;
		}
		timer.purge();
	}

	public boolean isCurrentSessionAlive() {
		return currentUser != null && currentUser.isAlive();
	}

	public Duration getCurrentSessionLifetime() {
		return currentUser.getRestLifetime();
	}

	public void updateOnlineUsers() {
		l.info("updateing online users");
		if (isCurrentSessionAlive()) {
			List<DyingUser> onlineUsers = null;
			try {
				onlineUsers = api.getOnlineUsers(currentUser);
				for (DyingUser dyingUser : onlineUsers) {
					boolean wasAdded = this.onlineUsers.add(dyingUser, dyingUser.getAliveTo());
					l.info("User: {} was added ({})", dyingUser.getUdid(), Boolean.valueOf(wasAdded).toString());
				}
			} catch (UserApi.Exception e) {
				e.printStackTrace();
			}
		}
		//notifier.onUsersUpdate();
	}

	@Nullable
	public synchronized final DyingUser login() {
		currentUser = null;
		// TODO: 30.08.2015 log exceptions
		try {
			currentUser = api.login(udid);
			channel.connect();
		} catch (UserApi.Exception e) {
			e.printStackTrace();
		} catch (BroadcastChannel.Exception e) {
			e.printStackTrace();
			api.logout(currentUser);
			currentUser = null;
		}
		scheduleLogoutAtCurrentUserDeath();

		updateOnlineUsers();

		notifier.onLogin(currentUser);
		return currentUser;
	}

	public synchronized void logout() {
		if (currentUser != null) {
			cancelLogoutAtCurrentUserDeath();

			channel.disconnect();
			api.logout(currentUser);
			currentUser = null;
			notifier.onLogout();

			onlineUsers.clear();
			notifier.onClearUsers();
		}
	}

	@Override
	public void resume() {
		onlineUsersListener = new TemporarySet.EventListener() {
			@Override
			public void onClear() {
				notifier.onClearUsers();
			}

			@Override
			public void onAdd(Object item) {
				notifier.onUserLogin((DyingUser) item);
			}

			@Override
			public void onRemove(Object item) {
				notifier.onUserLogout((DyingUser) item);
			}
		};
		onlineUsers.addListener(onlineUsersListener);
		onlineUsers.resume();

		channelListener = new BroadcastChannel.EventListener() {
			@Override
			public void onConnect(String channel, String greeting) {
			}

			@Override
			public void onDisconnect(String channel, String reason) {
			}

			@Override
			public void onReconnect(String channel, String reason) {
			}

			@Override
			public void onMessageRecieve(String channel, String message, String timetoken) {
				DyingUser dyingUser = userParser.fromJson(message, DyingUser.class);
				Boolean wasRemoved = false, wasAdded = false;
				if (dyingUser.isDead()) {
					wasRemoved = onlineUsers.remove(dyingUser);
				} else {
					wasAdded = onlineUsers.add(dyingUser, dyingUser.getAliveTo());
				}
				l.info("[{} : dead({})] wasRemoved == {}, wasAdded == {}",
						dyingUser.getUdid(), Boolean.valueOf(dyingUser.isDead()), wasAdded.toString(), wasRemoved.toString());
			}

			@Override
			public void onErrorOccur(String channel, String error) {
			}
		};
		channel.setListener(channelListener);
		channel.resume();

		onlineUsers.clear();

		if (isCurrentSessionAlive())
		{
			notifier.onLogin(currentUser);
			updateOnlineUsers();
		}
		scheduleLogoutAtCurrentUserDeath();
	}

	@Override
	public void pause() {
		cancelLogoutAtCurrentUserDeath();

		channel.clearListener();
		channelListener = null;

		onlineUsers.removeListener(onlineUsersListener);
		onlineUsersListener = null;
	}

	// TODO: 30.08.2015 make default lazy class with null implementation! or do this for base class
	public interface EventListener extends AbstractModel.EventListener {
		void onUserLogin(final DyingUser dyingUser);
		void onUserLogout(final DyingUser dyingUser);

		void onUsersUpdate();
		void onClearUsers();

		void onLogin(final DyingUser currentUser);
		void onCurrentUserTimeout();
		void onLogout();
	}
}
