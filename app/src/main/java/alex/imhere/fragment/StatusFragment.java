package alex.imhere.fragment;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
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

import com.skyfishjy.library.RippleBackground;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import alex.imhere.R;
import alex.imhere.activity.model.BaseModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.layer.server.DyingUser;
import alex.imhere.view.UiRunnable;
import alex.imhere.view.UpdatingViewTimer;
import alex.imhere.service.TimeFormatter;

@EFragment
public class StatusFragment extends Fragment implements BaseModel.ModelListener {
	BaseModel model;
	BaseModel.EventListener eventsListener;

	FragmentInteractionsListener interactionsListener;

	boolean currentSessionWasAlive = false;

	TimeFormatter timeFormatter = new TimeFormatter();

	@ViewById(R.id.ts_status) TextSwitcher tsStatus;
	@ViewById(R.id.tv_timer) TextView tvTimer;
	@ViewById(R.id.b_imhere) Button imhereButton;
	@ViewById(R.id.e_b_imhere) RippleBackground imhereButtonClickEffect;

	Handler uiHandler;
	UpdatingViewTimer updatingViewTimer;

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

		tsStatus.setAnimateFirstView(false);
		TextView tvSessionDead = (TextView) inflater.inflate(R.layout.textview_status, null);
		tvSessionDead.setText("Offline");
		tsStatus.addView(tvSessionDead);
		tsStatus.showNext();

		TextView tvSessionAlive = (TextView) inflater.inflate(R.layout.textview_status, null);
		tvSessionAlive.setText("Online");
		tsStatus.addView(tvSessionAlive);

		imhereButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (interactionsListener != null) {

					UiRunnable onPreExecute = new UiRunnable(uiHandler, new Runnable() {
						@Override
						public void run() {
							imhereButtonClickEffect.startRippleAnimation();
							imhereButton.setEnabled(false);
							tsStatus.setText("   Connecting...");
						}
					});
					UiRunnable onPostExecute = new UiRunnable(uiHandler, new Runnable() {
						@Override
						public void run() {
							imhereButton.setEnabled(true);
							imhereButtonClickEffect.stopRippleAnimation();
						}
					});

					onPreExecute.run();
					interactionsListener.onImhereClick(onPostExecute);
				}
			}
		});

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			interactionsListener = (FragmentInteractionsListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement FragmentInteractionsListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		interactionsListener = null;
	}

	@Override @UiThread
	public void onDataUpdate(final int notification, final Object data) {
		boolean currentSessionIsAlive = model.isCurrentSessionAlive(),
				statusChanged = currentSessionIsAlive != currentSessionWasAlive;
		updateStatus(statusChanged, currentSessionIsAlive);
		updateTimer(statusChanged, currentSessionIsAlive);
		if (statusChanged) {
			updateButton(currentSessionIsAlive);
		}
		currentSessionWasAlive = currentSessionIsAlive;
	}

	void updateStatus(boolean statusChanged, boolean currentSessionIsAlive) {
		if (statusChanged) {
			if (currentSessionIsAlive) {
				tsStatus.setText("Online");
			} else {
				tsStatus.setText("Offline");
			}
		}
	}

	void updateTimer(boolean statusChanged, boolean currentSessionIsAlive) {
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

	void updateButton(boolean currentSessionIsAlive) {
		TransitionDrawable drawable = (TransitionDrawable) imhereButton.getBackground();
		if (currentSessionIsAlive) {
			imhereButton.setText("I\'m out!");
			drawable.startTransition(0);
		} else {
			imhereButton.setText("I\'m here!");
			drawable.reverseTransition(0);
		}
	}

	@Override
	public void listenModel(BaseModel baseModel) {
		this.model = baseModel;
		baseModel.addEventsListener(eventsListener);
		ImhereModel model = (ImhereModel) baseModel;

		eventsListener = new ImhereModel.EventListener() {
			@Override
			public void onLoginUser(DyingUser dyingUser) {

			}

			@Override
			public void onLogoutUser(DyingUser dyingUser) {

			}

			@Override
			public void onClearUsers() {

			}

			@Override
			public void onLogin(DyingUser currentUser) {

			}

			@Override
			public void onLogout() {

			}
		};
	}

	public interface FragmentInteractionsListener {
		void onImhereClick(final @NonNull UiRunnable onPostExecute);
	}

}
