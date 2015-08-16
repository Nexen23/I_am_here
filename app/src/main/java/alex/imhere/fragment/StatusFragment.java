package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import alex.imhere.R;

public class StatusFragment extends Fragment {
	private Button button;
	private OnFragmentInteractionListener mListener;

	public static StatusFragment newInstance(String param) {
		StatusFragment fragment = new StatusFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public StatusFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_status, container, false);

		button = (Button) view.findViewById(R.id.b_imhere);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onImherePress(v);
			}
		});

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public void onImherePress(View v) {
		if (mListener != null) {
			mListener.onImhereClick();
		}
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

	public interface OnFragmentInteractionListener {
		void onImhereClick();
	}

}
