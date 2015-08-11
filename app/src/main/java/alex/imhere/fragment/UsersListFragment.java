package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import alex.imhere.R;
import alex.imhere.activity.provider.MainActivityProvider;
import alex.imhere.adapter.UsersListAdapter;
import alex.imhere.entity.User;

public class UsersListFragment extends ListFragment {
	private static final String ARG_DUMMY_PARAM = "param";
	private String dummy_param;

	private OnFragmentInteractionListener mListener;

	public static UsersListFragment newInstance(String param) {
		UsersListFragment fragment = new UsersListFragment();
		Bundle args = new Bundle();
		args.putString(ARG_DUMMY_PARAM, param);
		fragment.setArguments(args);
		return fragment;
	}

	public UsersListFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			dummy_param = getArguments().getString(ARG_DUMMY_PARAM);
		}

//		setListAdapter(new ArrayAdapter<>(getActivity(),
//				android.R.layout.simple_list_item_1, android.R.id.text1, MainActivityProvider.ITEMS));
		setListAdapter(new UsersListAdapter (getActivity(), R.layout.item_user, MainActivityProvider.ITEMS));
	}


	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnFragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		if (null != mListener) {
			mListener.onUserClick(MainActivityProvider.ITEMS.get(position));
		}
	}


	public interface OnFragmentInteractionListener {
		void onUserClick(User user);
	}

}
