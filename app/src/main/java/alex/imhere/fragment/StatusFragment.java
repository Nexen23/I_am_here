package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import alex.imhere.R;
import alex.imhere.activity.model.AbstractModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.fragment.view.AbstractView;

public class StatusFragment extends Fragment implements AbstractView {
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

	@Override
	public void onDataUpdate(AbstractModel abstractModel) {
		ImhereModel model = (ImhereModel) abstractModel;

		TextView tv_status = (TextView) getView().findViewById(R.id.tv_status);
		String status = "Offline";
		// TODO: hardcoded strings. Move it to res
		if (model.isCurrentSessionAlive()) {
			status = "Online";
		}
		tv_status.setText(status);
	}

	public interface OnFragmentInteractionListener {
		void onImhereClick();
	}

}
