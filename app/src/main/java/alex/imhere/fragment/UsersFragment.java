package alex.imhere.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.Tracker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
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
	@StringRes(R.string.users_querying_failed) String usersQueryingFailed;

	Animator itemAddingAnim;
	Animator itemRemovingAnim;
	final LayoutTransition itemsTransitionAnim = new LayoutTransition();

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
		tracker = ImhereApplication.newScreenTracker(this.getClass().getSimpleName());

		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, usersList);

		itemAddingAnim = AnimatorInflater.loadAnimator(getActivity(), R.animator.user_appearing);
		itemsTransitionAnim.setAnimator(LayoutTransition.APPEARING, itemAddingAnim);
		itemsTransitionAnim.setStartDelay(LayoutTransition.APPEARING, 0);

		// TODO: 12.09.2015  not work for some reason
		/*itemRemovingAnim = AnimatorInflater.loadAnimator(getActivity(), R.animator.user_disappearing);
		itemsTransitionAnim.setAnimator(LayoutTransition.DISAPPEARING, itemRemovingAnim);
		itemsTransitionAnim.setStartDelay(LayoutTransition.DISAPPEARING, 0);*/

		getListView().setLayoutTransition(itemsTransitionAnim);
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
			startListeningServer();
			queryAndAddOnlineUsersInBackground();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListeningServer();
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

	public void onErrorOccur(String message, @Nullable Exception e) {
		setEmptyListView(R.id.lv_loading_error);
		String exceptionMessage = ((e == null) ? "" : e.getMessage());
		UiToast.Show(getActivity(), message, exceptionMessage);
		l.error("message={}; exception={}", message, exceptionMessage);

		stopListeningServer();
	}

	@Background
	public void queryAndAddOnlineUsersInBackground() {
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
				onErrorOccur(usersQueryingFailed, e);
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

	public void clearCurrentUserAndStop() {
		stopListeningServer();
		currentUser = null;
	}

	public void setCurrentUserAndStart(@NonNull DyingUser user) {
		currentUser = user;
		startListeningServer();
		queryAndAddOnlineUsersInBackground();
	}

	void startListeningServer() {
		timeTickerOwner.getTimeTicker().addWeakListener(this);

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
		usersTempSet.addWeakListener(usersTempSetListener);
		usersTempSet.resume();

		serverTunnelListener = new ServerChannel.EventListener() {
			@Override
			public void onDisconnect(String reason) {
				onErrorOccur(usersChannelDisconnection, null);
			}

			@Override
			public void onUserLogin(@NonNull DyingUser dyingUser) {
				boolean wasAdded = usersTempSet.add(dyingUser, dyingUser.getAliveTo());
				l.info("User: {} was added ({})", dyingUser.getUdid(), Boolean.valueOf(wasAdded).toString());
			}

			@Override
			public void onUserLogout(@NonNull DyingUser dyingUser) {
				boolean wasRemoved = usersTempSet.remove(dyingUser);
				l.info("User: {} was removed ({})", dyingUser.getUdid(), Boolean.valueOf(wasRemoved).toString());
			}
		};

		try {
			setEmptyListView(R.id.lv_loading_users);
			serverChannel.setListener(serverTunnelListener);
			serverChannel.subscribe(); // TODO: 08.09.2015 do it in Loader
		} catch (Exception e) {
			e.printStackTrace();
			onErrorOccur(usersChannelConnectionFailed, e);
			return;
		}
	}

	void stopListeningServer() {
		timeTickerOwner.getTimeTicker().removeWeakListener(this);

		serverChannel.clearListener();
		serverChannel.unsubscribe();
		serverTunnelListener = null;

		usersTempSet.removeWeakListener(usersTempSetListener);
		usersTempSetListener = null;
		usersTempSet.pause();

		usersTempSet.clear();
		clearUsers();
	}

	//region Interfaces impls
	@Override
	public void onSecondTick() {
		notifyUsersDataChanged();
	}
	//endregion
}
