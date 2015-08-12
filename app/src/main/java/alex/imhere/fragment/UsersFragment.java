package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import alex.imhere.R;
import alex.imhere.activity.dataprovider.MainActivityDataProvider;
import alex.imhere.adapter.UsersAdapter;
import alex.imhere.entity.User;

public class UsersFragment extends ListFragment {
	private static final String ARG_DUMMY_PARAM = "param";
	private String dummy_param;

	private OnFragmentInteractionListener mListener;

	public static UsersFragment newInstance(String param) {
		UsersFragment fragment = new UsersFragment();
		Bundle args = new Bundle();
		args.putString(ARG_DUMMY_PARAM, param);
		fragment.setArguments(args);
		return fragment;
	}

	public UsersFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			dummy_param = getArguments().getString(ARG_DUMMY_PARAM);
		}

//		setListAdapter(new ArrayAdapter<>(getActivity(),
//				android.R.layout.simple_list_item_1, android.R.id.text1, MainActivityDP.ITEMS));
		setListAdapter(new UsersAdapter(getActivity(), R.layout.item_user, MainActivityDataProvider.ITEMS));
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
			mListener.onUserClick(MainActivityDataProvider.ITEMS.get(position));
		}
	}


	public interface OnFragmentInteractionListener {
		void onUserClick(User user);
	}

}
