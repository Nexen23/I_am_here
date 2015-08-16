package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import com.parse.ParseException;

import java.util.ArrayList;

import alex.imhere.R;
import alex.imhere.activity.model.AbstractModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.adapter.UsersAdapter;
import alex.imhere.fragment.view.AbstractView;
import alex.imhere.layer.server.Session;

public class UsersFragment extends ListFragment implements AbstractView {
	private ImhereModel model;
	private UsersAdapter usersAdapter = null;
	private ArrayList<Session> users = new ArrayList<Session>();

	public static UsersFragment newInstance(String param) {
		UsersFragment fragment = new UsersFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public UsersFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		usersAdapter = new UsersAdapter(getActivity(), R.layout.item_user, users);
		setListAdapter(usersAdapter);
		//setRetainInstance(true);
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void setModel(AbstractModel model) {
		this.model = (ImhereModel) model;
	}

	@Override
	public void onDataUpdate() {
		try {
			users = model.getOnlineUsers();
			usersAdapter.notifyDataSetChanged();
		} catch (ParseException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
}
