package alex.imhere.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.parceler.transfuse.annotations.OnActivityCreated;

import java.util.ArrayList;
import java.util.List;

import alex.imhere.R;
import alex.imhere.activity.model.BaseModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.adapter.UsersAdapter;
import alex.imhere.layer.server.DyingUser;
import alex.imhere.view.UpdatingTimer;

@EFragment(value = R.layout.fragment_users, forceLayoutInjection = true)
public class UsersFragment extends ListFragment implements BaseModel.ModelListener, UpdatingTimer.TimerListener {
	BaseModel model;
	BaseModel.EventListener eventsListener;

	UsersAdapter usersAdapter;
	List<DyingUser> dyingUsers = new ArrayList<>();

	Handler uiHandler;
	UpdatingTimer updatingTimer;

	public static UsersFragment newInstance() {
		UsersFragment fragment = new UsersFragment_();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setListAdapter(usersAdapter);
	}

	@AfterViews
	public void onViewsInjected() {
		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, dyingUsers);

		uiHandler = new Handler();
		updatingTimer = new UpdatingTimer(uiHandler, this).start();
	}

	@Override
	public void onResume() {
		super.onResume();
		subscribeModel();
	}

	@Override
	public void onPause() {
		super.onPause();
		unsubscribeModel();
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
	public void setModel(BaseModel baseModel) {
		this.model = baseModel;
	}

	@Override
	public void subscribeModel() {
		eventsListener = new ImhereModel.EventListener() {
			@Override
			public void onLoginUser(DyingUser dyingUser) {
				addUser(dyingUser);
			}

			@Override
			public void onLogoutUser(DyingUser dyingUser) {
				removeUser(dyingUser);
			}

			@Override
			public void onClearUsers() {
				clearUsers();
			}

			@Override
			public void onLogin(DyingUser currentUser) {
				notifyUsersDataChanged();
			}

			@Override
			public void onLogout() {
				notifyUsersDataChanged();
			}
		};

		model.addEventsListener(eventsListener);
	}

	@Override
	public void unsubscribeModel() {
		model.removeEventsListener(eventsListener);
		eventsListener = null;
	}

	@Override
	public void onTimerTick() {
		notifyUsersDataChanged();
	}
}
