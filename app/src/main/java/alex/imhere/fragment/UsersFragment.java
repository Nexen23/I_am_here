package alex.imhere.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;

import java.util.ArrayList;

import alex.imhere.R;
import alex.imhere.activity.model.AbstractModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.adapter.UsersAdapter;
import alex.imhere.fragment.view.AbstractView;
import alex.imhere.layer.server.Session;

public class UsersFragment extends ListFragment implements AbstractView {
	private UsersAdapter usersAdapter = null;
	private ArrayList<Session> users = new ArrayList<>();

	public static UsersFragment newInstance() {
		UsersFragment fragment = new UsersFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, users);
		setListAdapter(usersAdapter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//return super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_users, container);
	}


	@Override
	public void onDataUpdate(AbstractModel abstractModel) {
		ImhereModel model = (ImhereModel) abstractModel;
		try {
			users.clear();
			users.addAll( model.getOnlineUsers() );
			usersAdapter.notifyDataSetChanged();
		} catch (ParseException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
