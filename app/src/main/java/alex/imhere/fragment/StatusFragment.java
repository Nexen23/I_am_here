package alex.imhere.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
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
	private boolean currentSessionWasAlive = false;

	private TextSwitcher tsStatus;
	private TextView tvTimer;
	private Button imhererButton;

	private Handler uiHandler;
	private UpdatingViewTimer updatingViewTimer;

	public StatusFragment() {
		// Required empty public constructor
	}

	public static StatusFragment newInstance() {
		StatusFragment fragment = new StatusFragment();
		Bundle args = new Bundle();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_status, container, false);

		uiHandler = new Handler();
		updatingViewTimer = new UpdatingViewTimer(uiHandler, this);
		updatingViewTimer.start();

		tsStatus = (TextSwitcher) view.findViewById(R.id.ts_status);
		tvTimer = (TextView) view.findViewById(R.id.tv_timer);
		imhererButton = (Button) view.findViewById(R.id.b_imhere);

		tsStatus.setAnimateFirstView(false);
		TextView tvSessionDead = (TextView) inflater.inflate(R.layout.textview_status, null);
		tvSessionDead.setText("Offline");
		tsStatus.addView(tvSessionDead);
		tsStatus.showNext();

		TextView tvSessionAlive = (TextView) inflater.inflate(R.layout.textview_status, null);
		tvSessionAlive.setText("Online");
		tsStatus.addView(tvSessionAlive);

		imhererButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {

					UiRunnable onPreExecute = new UiRunnable(uiHandler, new Runnable() {
						@Override
						public void run() {
							imhererButton.setEnabled(false);
							tsStatus.setText("   Connecting...");
						}
					});
					UiRunnable onPostExecute = new UiRunnable(uiHandler, new Runnable() {
						@Override
						public void run() {
							imhererButton.setEnabled(true);
							//imhererButton.animate().setDuration(700).rotationX(180f).start();
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
		boolean currentSessionIsAlive = model.isCurrentSessionAlive(),
				statusChanged = currentSessionIsAlive != currentSessionWasAlive;
		updateStatus(statusChanged, currentSessionIsAlive);
		updateTimer(statusChanged, currentSessionIsAlive);
		currentSessionWasAlive = currentSessionIsAlive;
	}

	@Override
	public void setModel(AbstractModel abstractModel) {
		model = (ImhereModel) abstractModel;
		model.addEventListener(this);
	}

	private void updateStatus(boolean statusChanged, boolean currentSessionIsAlive) {
		if (statusChanged) {
			if (currentSessionIsAlive) {
				tsStatus.setText("Online");
			} else {
				tsStatus.setText("Offline");
			}
		}
	}

	private void updateTimer(boolean statusChanged, boolean currentSessionIsAlive) {
		int timerVisibility = View.INVISIBLE;

		if (currentSessionIsAlive) {
			timerVisibility = View.VISIBLE;
			String durationMsString = timeFormatter.durationToMSString(model.getCurrentSessionLifetime());
			tvTimer.setText(durationMsString);
		}

		if (currentSessionWasAlive != currentSessionIsAlive) {
			int animationType = R.anim.fade_in;
			if (currentSessionIsAlive != true) {
				animationType = R.anim.fade_out;
			}
			Animation timerAnimation = AnimationUtils.loadAnimation(getActivity(), animationType);

			tvTimer.startAnimation(timerAnimation);
		}

		tvTimer.setVisibility(timerVisibility);
	}

	public interface FragmentInteractionListener {
		void onImhereClick(final @NonNull UiRunnable onPostExecute);
	}

}
