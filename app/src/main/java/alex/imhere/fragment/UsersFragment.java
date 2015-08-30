package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import alex.imhere.R;
import alex.imhere.model.AbstractModel;
import alex.imhere.model.ImhereRoomModel;
import alex.imhere.view.adapter.UsersAdapter;
import alex.imhere.entity.DyingUser;
import alex.imhere.util.listening.ListeningLifecycle;
import alex.imhere.util.datetime.UpdatingTimer;

@EFragment(value = R.layout.fragment_users, forceLayoutInjection = true)
public class UsersFragment extends ListFragment
		implements AbstractModel.ModelListener, UpdatingTimer.TimerListener, ListeningLifecycle {
	Logger l = LoggerFactory.getLogger(UsersFragment.class);

	ImhereRoomModel model;
	ImhereRoomModel.EventListener eventsListener;

	InteractionListener interactionsListener;

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
		updatingTimer = new UpdatingTimer(this);
		updatingTimer.start();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			interactionsListener = (InteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement InteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		interactionsListener = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		startListening();
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListening();
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

	@Override
	public void setModel(AbstractModel abstractModel) {
		this.model = (ImhereRoomModel) abstractModel;
	}

	@Override
	public void startListening() {
		final Fragment thisFragment = this;
		eventsListener = new ImhereRoomModel.EventListener() {
			@Override
			public void onModelDataChanged(AbstractModel abstractModel) {

			}

			@Override
			public void onUserLogin(DyingUser dyingUser) {
				addUser(dyingUser);
			}

			@Override
			public void onUserLogout(DyingUser dyingUser) {
				removeUser(dyingUser);
			}

			@Override
			public void onUsersUpdate() {
				notifyUsersDataChanged();
			}

			@Override
			public void onClearUsers() {
				clearUsers();
			}

			@Override
			public void onLogin(DyingUser currentUser) {
				notifyUsersDataChanged();
				interactionsListener.onShow(thisFragment);
			}

			@Override
			public void onPreLogout() {
				interactionsListener.onHide(thisFragment);
			}

			@Override
			public void onLogout() {
				notifyUsersDataChanged();
			}
		};

		model.addListener(eventsListener);
	}

	@Override
	public void stopListening() {
		model.removeListener(eventsListener);
		eventsListener = null;
	}

	public interface InteractionListener {
		void onShow(Fragment fragment);
		void onHide(Fragment fragment);
	}
}
