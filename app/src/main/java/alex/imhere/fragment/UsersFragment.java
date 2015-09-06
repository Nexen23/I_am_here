package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import alex.imhere.ImhereApplication;
import alex.imhere.R;
import alex.imhere.container.TemporarySet;
import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.exception.BroadcastChannelException;
import alex.imhere.service.ComponentOwner;
import alex.imhere.service.UpdatingTimer;
import alex.imhere.service.api.UserApi;
import alex.imhere.service.channel.Channel;
import alex.imhere.service.parser.JsonParser;
import alex.imhere.view.adapter.UsersAdapter;

@EFragment(value = R.layout.fragment_users, forceLayoutInjection = true)
public class UsersFragment extends ListFragment implements UpdatingTimer.TimerListener {
	Logger l = LoggerFactory.getLogger(UsersFragment.class);
	Tracker tracker;

	@Inject	UserApi userApi;
	@Inject Channel channel;
	@Inject JsonParser jsonParser;

	DyingUser currentUser;
	TemporarySet<DyingUser> usersTempSet = new TemporarySet<>();

	TemporarySet.EventListener usersTempSetListener;
	Channel.EventListener channelListener;

	UsersAdapter usersAdapter;
	List<DyingUser> usersList = new ArrayList<>();

	UpdatingTimer updatingTimer;

	public static UsersFragment newInstance() {
		UsersFragment fragment = new UsersFragment_();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			((ComponentOwner) activity).getServicesComponent().inject(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ComponentOwner");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(usersAdapter);
	}

	@AfterViews
	public void onAfterViews() {
		tracker = ImhereApplication.newScreenTracker("LoginStatusFragment");

		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, usersList);
		updatingTimer = new UpdatingTimer(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isCurrentUserAlive()) {
			updatingTimer.start();
			startListeningEvents();
			updateOnlineUsers();
		}

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListeningEvents();
		updatingTimer.stop();

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@UiThread
	public void addUser(DyingUser dyingUser) {
		usersAdapter.add(dyingUser);
	}

	@UiThread
	public void removeUser(DyingUser dyingUser) {
		usersAdapter.remove(dyingUser);
	}

	@UiThread
	public void clearUsers() {
		usersAdapter.clear();
	}

	@UiThread
	public void notifyUsersDataChanged() {
		usersAdapter.notifyDataSetChanged();
	}

	@Override
	public void onTimerTick() {
		notifyUsersDataChanged();
	}

	public void updateOnlineUsers() {
		l.info("updateing online users");
		if (isCurrentUserAlive()) {
			List<DyingUser> onlineUsers = null;
			try {
				onlineUsers = userApi.getOnlineUsers(currentUser);
				for (DyingUser dyingUser : onlineUsers) {
					boolean wasAdded = usersTempSet.add(dyingUser, dyingUser.getAliveTo());
					l.info("User: {} was added ({})", dyingUser.getUdid(), Boolean.valueOf(wasAdded).toString());
				}
			} catch (ApiException e) {
				e.printStackTrace();
			}
		}
		//notifier.onUsersUpdate();
	}

	public boolean isCurrentUserAlive() {
		DyingUser currentUser = getCurrentUser();
		return currentUser != null && currentUser.isAlive();
	}

	public DyingUser getCurrentUser() {
		return currentUser;
	}

	public void clearCurrentUser() {
		stopListeningEvents();
		currentUser = null;
		usersTempSet.clear();
		clearUsers();

		updatingTimer.stop();
	}

	public void setCurrentUser(@Nullable DyingUser user) {
		currentUser = user;
		startListeningEvents();
		updateOnlineUsers();

		updatingTimer.start();
	}

	//region Listening helpers
	void startListeningEvents() {
		usersTempSetListener = new TemporarySet.EventListener() {
			@Override
			public void onClear() {
				UsersFragment.this.clearUsers();
			}

			@Override
			public void onAdd(Object item) {
				UsersFragment.this.addUser((DyingUser) item);
			}

			@Override
			public void onRemove(Object item) {
				UsersFragment.this.removeUser((DyingUser) item);
			}
		};
		usersTempSet.addListener(usersTempSetListener);
		usersTempSet.resume();

		channelListener = new Channel.EventListener() {
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
				// TODO: 03.09.2015 too cool for Controller. Make LoginLougoutChannel Service
				DyingUser dyingUser = jsonParser.fromJson(message, DyingUser.class);
				Boolean wasRemoved = false, wasAdded = false;
				if (dyingUser.isDead()) {
					wasRemoved = usersTempSet.remove(dyingUser);
				} else {
					wasAdded = usersTempSet.add(dyingUser, dyingUser.getAliveTo());
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
		try {
			channel.connect();
		} catch (BroadcastChannelException e) {
			e.printStackTrace();
		}
	}

	void stopListeningEvents() {
		channel.clearListener();
		channelListener = null;
		channel.disconnect();
		channel.pause();

		usersTempSet.removeListener(usersTempSetListener);
		usersTempSetListener = null;
		usersTempSet.pause();
	}
	//endregion
}
