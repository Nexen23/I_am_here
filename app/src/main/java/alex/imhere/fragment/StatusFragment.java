package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import alex.imhere.fragment.view.UiRunnable;
import alex.imhere.fragment.view.UpdatingViewTimer;
import alex.imhere.service.TimeFormatter;

public class StatusFragment extends Fragment implements AbstractView {
	private FragmentInteractionListener mListener;
	private ImhereModel model;
	private TimeFormatter timeFormatter = new TimeFormatter();

	private TextView tvStatus;
	private TextView tvTimer;
	private Button imhererButton;

	private Handler uiHandler;
	private UpdatingViewTimer updatingViewTimer;

	public static StatusFragment newInstance() {
		StatusFragment fragment = new StatusFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	public StatusFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_status, container, false);

		uiHandler = new Handler();
		updatingViewTimer = new UpdatingViewTimer(uiHandler, this);
		updatingViewTimer.start();

		tvStatus = (TextView) view.findViewById(R.id.tv_status);
		tvTimer = (TextView) view.findViewById(R.id.tv_timer);
		imhererButton = (Button) view.findViewById(R.id.b_imhere);

		imhererButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {

					UiRunnable onPreExecute = new UiRunnable(uiHandler, new Runnable() {
						@Override
						public void run() {
							imhererButton.setEnabled(false);
						}
					});
					UiRunnable onPostExecute = new UiRunnable(uiHandler, new Runnable() {
						@Override
						public void run() {
							imhererButton.setEnabled(true);
						}
					});

					onPreExecute.run();
					mListener.onImhereClick(onPostExecute);
				}
			}
		});

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (FragmentInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement FragmentInteractionListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onDataUpdate() {
		String status = "Offline";
		int timerVisibility = View.INVISIBLE;

		if (model.isCurrentSessionAlive()) {
			status = "Online";
			timerVisibility = View.VISIBLE;
			tvTimer.setText( timeFormatter.durationToMSString(model.getCurrentSessionLifetime()) );
		}

		tvStatus.setText(status);
		tvTimer.setVisibility(timerVisibility);
	}

	@Override
	public void setModel(AbstractModel abstractModel) {
		model = (ImhereModel) abstractModel;
		model.addEventListener(this, this);
	}

	public interface FragmentInteractionListener {
		void onImhereClick(@Nullable final UiRunnable onPostExecute);
	}

}
