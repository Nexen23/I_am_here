package alex.imhere.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import alex.imhere.R;
import alex.imhere.activity.model.AbstractModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.adapter.UsersAdapter;
import alex.imhere.fragment.view.AbstractView;
import alex.imhere.layer.server.Session;

public class UsersFragment extends ListFragment implements AbstractView {
	private UsersAdapter usersAdapter = null;
	private List<Session> readOnlyUsers = new ArrayList<>();
	private ImhereModel model = null;

	private Handler uiHandler;
	private final long REFRESH_PERIOD_MS = 777;
	private final Runnable refreshOnlineUsers = new Runnable() {
		@Override
		public void run() {
			usersAdapter.notifyDataSetChanged();
			uiHandler.postDelayed(this, REFRESH_PERIOD_MS);
		}
	};

	public static UsersFragment newInstance() {
		UsersFragment fragment = new UsersFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		setListAdapter(usersAdapter);

		uiHandler = new Handler();
		uiHandler.post(refreshOnlineUsers);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_users, container);
	}


	@Override
	public void onDataUpdate() {
		usersAdapter.notifyDataSetChanged();
	}

	@Override
	public void setModel(AbstractModel abstractModel) {
		model = (ImhereModel) abstractModel;
		model.addEventListener(this, this);

		readOnlyUsers = model.getOnlineUsersSet();
		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, readOnlyUsers);
	}
}
