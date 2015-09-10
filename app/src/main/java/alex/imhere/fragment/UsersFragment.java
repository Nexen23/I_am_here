package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;
import org.androidannotations.annotations.res.StringRes;
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
import alex.imhere.exception.ChannelException;
import alex.imhere.service.component.ServicesComponent;
import alex.imhere.service.domain.channel.ServerChannel;
import alex.imhere.service.domain.ticker.TimeTicker;
import alex.imhere.service.domain.api.UserApi;
import alex.imhere.service.domain.parser.JsonParser;
import alex.imhere.util.wrapper.UiToast;
import alex.imhere.view.adapter.UsersAdapter;

@EFragment(value = R.layout.fragment_users, forceLayoutInjection = true)
public class UsersFragment extends ListFragment implements TimeTicker.EventListener {
	//region Fields
	final Logger l = LoggerFactory.getLogger(UsersFragment.class);
	Tracker tracker;

	//region Resources
	@StringRes(R.string.users_channel_connection_failed) String usersChannelConnectionFailed;
	@StringRes(R.string.users_channel_disconnection) String usersChannelDisconnection;
	@ViewsById({R.id.lv_loading_users, R.id.lv_no_users, R.id.lv_loading_error})
	List<View> emptyListViews;
	//endregion

	@Inject	UserApi userApi;
	@Inject	ServerChannel serverChannel;
	@Inject JsonParser jsonParser;
	TimeTicker.Owner timeTickerOwner;

	@InstanceState DyingUser currentUser;
	TemporarySet<DyingUser> usersTempSet = new TemporarySet<>();

	TemporarySet.EventListener usersTempSetListener;
	ServerChannel.EventListener serverTunnelListener;

	UsersAdapter usersAdapter;
	List<DyingUser> usersList = new ArrayList<>();
	//endregion

	//region Lifecycle
	@AfterViews
	public void onAfterViews() {
		tracker = ImhereApplication.newScreenTracker("LoginFragment");

		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, usersList);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			timeTickerOwner = (TimeTicker.Owner) activity;
			((ServicesComponent.Owner) activity).getServicesComponent().inject(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement TimeTickerOwner & ComponentOwner");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		timeTickerOwner = null;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListAdapter(usersAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isCurrentUserExist()) {
			startListeningEvents();
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

	@UiThread
	public void setEmptyListView(int resId) {
		for (View view : emptyListViews) {
			if (view.getId() == resId) {
				view.setVisibility(View.VISIBLE);
			} else {
				view.setVisibility(View.GONE);
			}
		}
	}
	//endregion

	public void onErrorOccur(Exception e) {
		setEmptyListView(R.id.lv_loading_error);
		UiToast.Show(getActivity(), usersChannelConnectionFailed, e.getMessage());
		stopListeningEvents();
	}

	public void updateOnlineUsers() throws ApiException {
		l.info("updateing online users");
		if (isCurrentUserAlive()) {
			List<DyingUser> onlineUsers;
			onlineUsers = userApi.getOnlineUsers(currentUser);
			for (DyingUser dyingUser : onlineUsers) {
				boolean wasAdded = usersTempSet.add(dyingUser, dyingUser.getAliveTo());
				l.info("User: {} was added ({})", dyingUser.getUdid(), Boolean.valueOf(wasAdded).toString());
			}
		}
	}

	public boolean isCurrentUserAlive() {
		DyingUser currentUser = getCurrentUser();
		return isCurrentUserExist() && currentUser.isAlive();
	}

	public boolean isCurrentUserExist() {
		return getCurrentUser() != null;
	}

	public DyingUser getCurrentUser() {
		return currentUser;
	}

	@Background
	public void clearCurrentUser() {
		stopListeningEvents();
		currentUser = null;
		usersTempSet.clear();
		clearUsers();
	}

	@Background
	public void setCurrentUser(@NonNull DyingUser user) {
		currentUser = user;
		startListeningEvents();
	}

	@Background
	void startListeningEvents() {
		timeTickerOwner.getTimeTicker().addListener(this);

		usersTempSetListener = new TemporarySet.EventListener() {
			@Override
			public void onCleared() {
				UsersFragment.this.clearUsers();
			}

			@Override
			public void onAdded(Object item) {
				UsersFragment.this.addUser((DyingUser) item);
			}

			@Override
			public void onRemoved(Object item) {
				UsersFragment.this.removeUser((DyingUser) item);
			}
		};
		usersTempSet.addListener(usersTempSetListener);
		usersTempSet.resume();

		serverTunnelListener = new ServerChannel.EventListener() {
			@Override
			public void onDisconnect(String reason) {
				UiToast.Show(getActivity(), usersChannelDisconnection);
				stopListeningEvents();
			}

			@Override
			public void onUserLogin(@NonNull DyingUser dyingUser) {
				boolean wasAdded = usersTempSet.add(dyingUser, dyingUser.getAliveTo());
			}

			@Override
			public void onUserLogout(@NonNull DyingUser dyingUser) {
				boolean wasRemoved = usersTempSet.remove(dyingUser);
			}
		};

		try {
			setEmptyListView(R.id.lv_loading_users);
			serverChannel.setListener(serverTunnelListener);
			serverChannel.subscribe(); // TODO: 08.09.2015 do it in Loader
			updateOnlineUsers();
		} catch (Exception e) {
			e.printStackTrace();
			onErrorOccur(e);
			return;
		}
	}

	void stopListeningEvents() {
		timeTickerOwner.getTimeTicker().removeListener(this);

		serverChannel.clearListener();
		serverChannel.unsubscribe();
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
