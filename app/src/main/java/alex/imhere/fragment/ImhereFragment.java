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

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import alex.imhere.R;

public class ImhereFragment extends Fragment {
	private static final String ARG_DUMMY_PARAM = "param";
	private String dummy_param;

	private Button button;
	private OnFragmentInteractionListener mListener;

	public static ImhereFragment newInstance(String param) {
		ImhereFragment fragment = new ImhereFragment();
		Bundle args = new Bundle();
		args.putString(ARG_DUMMY_PARAM, param);
		fragment.setArguments(args);
		return fragment;
	}

	public ImhereFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			dummy_param = getArguments().getString(ARG_DUMMY_PARAM);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_imhere, container, false);

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
			final String username = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

			ParseUser.getCurrentUser().logOut();
			ParseUser parseUser = new ParseUser();
			parseUser.setUsername(username);
			parseUser.setPassword(username);

			parseUser.signUpInBackground(new SignUpCallback() {
				@Override
				public void done(ParseException e) {
					if (e == null) {
						Toast.makeText(getActivity(), String.format("%s - signed up", username), Toast.LENGTH_SHORT).show();
						mListener.onUserLogin(username);
					} else {
						Toast.makeText(getActivity(), String.format("Signing up failed. Exception: %s", e.getMessage()), Toast.LENGTH_SHORT).show();
					}
				}
			});
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
		void onUserLogin(final String username);
	}

}
