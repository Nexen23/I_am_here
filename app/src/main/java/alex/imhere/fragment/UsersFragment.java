package alex.imhere.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;

import alex.imhere.R;
import alex.imhere.activity.model.BaseModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.adapter.UsersAdapter;
import alex.imhere.layer.server.DyingUser;
import alex.imhere.view.UpdatingTimer;

@EFragment
public class UsersFragment extends ListFragment implements BaseModel.ModelListener, UpdatingTimer.TimerListener {
	BaseModel model;
	BaseModel.EventListener eventsListener;

	private UsersAdapter usersAdapter;
	private List<DyingUser> dyingUsers = new ArrayList<>();

	private Handler uiHandler;
	private UpdatingTimer updatingTimer;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, dyingUsers);

		uiHandler = new Handler();
		updatingTimer = new UpdatingTimer(uiHandler, this).start();

		return inflater.inflate(R.layout.fragment_users, container);
	}

	@Override
	public void listenModel(BaseModel baseModel) {
		this.model = baseModel;
		baseModel.addEventsListener(eventsListener);
		ImhereModel model = (ImhereModel) baseModel;

		eventsListener = new ImhereModel.EventListener() {
			@Override
			public void onLoginUser(DyingUser dyingUser) {
				usersAdapter.add(dyingUser);
			}

			@Override
			public void onLogoutUser(DyingUser dyingUser) {
				usersAdapter.remove(dyingUser);
			}

			@Override
			public void onClearUsers() {
				usersAdapter.clear();
			}

			@Override
			public void onLogin(DyingUser currentUser) {
				usersAdapter.notifyDataSetChanged();
			}

			@Override
			public void onLogout() {
				usersAdapter.notifyDataSetChanged();
			}
		};
	}

	@Override
	public void onTimerTick() {
		usersAdapter.notifyDataSetChanged();
	}
}
