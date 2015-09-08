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
import alex.imhere.exception.ServerTunnelException;
import alex.imhere.service.component.ServicesComponent;
import alex.imhere.service.domain.channel.ServerChannel;
import alex.imhere.service.domain.ticker.TimeTicker;
import alex.imhere.service.domain.api.UserApi;
import alex.imhere.service.domain.parser.JsonParser;
import alex.imhere.view.adapter.UsersAdapter;

@EFragment(value = R.layout.fragment_users, forceLayoutInjection = true)
public class UsersFragment extends ListFragment implements TimeTicker.EventListener {
	//region Fields
	final Logger l = LoggerFactory.getLogger(UsersFragment.class);
	Tracker tracker;

	@Inject	UserApi userApi;
	@Inject
	ServerChannel serverChannel;
	@Inject JsonParser jsonParser;
	TimeTicker.Owner owner;

	DyingUser currentUser;
	TemporarySet<DyingUser> usersTempSet = new TemporarySet<>();

	TemporarySet.EventListener usersTempSetListener;
	ServerChannel.EventListener serverTunnelListener;

	UsersAdapter usersAdapter;
	List<DyingUser> usersList = new ArrayList<>();
	//endregion

	//region Lifecycle
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
			owner = (TimeTicker.Owner) activity;
			((ServicesComponent.Owner) activity).getServicesComponent().inject(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement Owner & ComponentOwner");
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(usersAdapter);
	}

	@AfterViews
	public void onAfterViews() {
		tracker = ImhereApplication.newScreenTracker("LoginFragment");

		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, usersList);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isCurrentUserAlive()) {
			startListeningEvents();
			updateOnlineUsers();
		}

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListeningEvents();

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
	//endregion

	//region Ui helpers
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
	//endregion

	public void updateOnlineUsers() {
		l.info("updateing online users");
		if (isCurrentUserAlive()) {
			List<DyingUser> onlineUsers;
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
	}

	public void setCurrentUser(@Nullable DyingUser user) {
		currentUser = user;
		startListeningEvents();
		updateOnlineUsers();
	}

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

		serverTunnelListener = new ServerChannel.EventListener() {
			@Override
			public void onDisconnect(String reason) {

			}

			@Override
			public void onUserLogin(DyingUser dyingUser) {
				boolean wasAdded = usersTempSet.add(dyingUser, dyingUser.getAliveTo());
			}

			@Override
			public void onUserLogout(DyingUser dyingUser) {
				boolean wasRemoved = usersTempSet.remove(dyingUser);
			}
		};
		serverChannel.setListener(serverTunnelListener);
		try {
			serverChannel.connect();
		} catch (ServerTunnelException e) {
			e.printStackTrace();
		}

		owner.getTimeTicker().addListener(this);
	}

	void stopListeningEvents() {
		owner.getTimeTicker().removeListener(this);

		serverChannel.clearListener();
		serverChannel.disconnect();
		serverTunnelListener = null;

		usersTempSet.removeListener(usersTempSetListener);
		usersTempSetListener = null;
		usersTempSet.pause();
	}

	//region Interfaces impls
	@Override
	public void onSecondTick() {
		notifyUsersDataChanged();
	}
	//endregion
}
