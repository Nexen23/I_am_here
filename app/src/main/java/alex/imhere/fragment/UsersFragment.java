package alex.imhere.fragment;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import alex.imhere.R;
import alex.imhere.entity.DyingUser;
import alex.imhere.model.AbstractModel;
import alex.imhere.model.ImhereRoomModel;
import alex.imhere.util.Resumable;
import alex.imhere.util.time.UpdatingTimer;
import alex.imhere.view.adapter.UsersAdapter;

@EFragment(value = R.layout.fragment_users, forceLayoutInjection = true)
public class UsersFragment extends ListFragment
		implements AbstractModel.ModelListener, UpdatingTimer.TimerListener, Resumable {
	Logger l = LoggerFactory.getLogger(UsersFragment.class);

	ImhereRoomModel model;
	ImhereRoomModel.EventListener eventsListener;

	InteractionListener interactionsListener;
	boolean isShown = false;

	UsersAdapter usersAdapter;
	List<DyingUser> dyingUsers = new ArrayList<>();

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
		resume();
	}

	@Override
	public void onPause() {
		super.onPause();
		pause();
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

	@UiThread
	public void showFragment(final boolean doShow) {
		if (isShown != doShow) {
			final FrameLayout usersView = (FrameLayout) getActivity().findViewById(R.id.fl_fragment_users);
			final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) usersView.getLayoutParams();
			final int marginInPx = (int) getResources().getDimension(R.dimen.fragment_users_margin);
			ValueAnimator animator = ValueAnimator.ofInt(marginInPx, 0);
			if (!doShow) {
				animator = ValueAnimator.ofInt(0, marginInPx);
			}
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
					usersView.requestLayout();
				}
			});
			animator.setDuration(getResources().getInteger(R.integer.duration_users_fragment_sliding));
			animator.start();

			isShown = doShow;
		}
	}

	@Override
	public void resume() {
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
				showFragment(true);
			}

			@Override
			public void onCurrentUserTimeout() {
				showFragment(false);
			}

			@Override
			public void onLogout() {
				showFragment(false);
				notifyUsersDataChanged();
			}
		};

		model.addListener(eventsListener);
	}

	@Override
	public void pause() {
		model.removeListener(eventsListener);
		eventsListener = null;
	}

	public interface InteractionListener {
	}
}
