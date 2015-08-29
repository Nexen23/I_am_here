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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alex.imhere.R;
import alex.imhere.activity.model.BaseModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.layer.server.DyingUser;
import alex.imhere.util.ListeningController;
import alex.imhere.view.UiRunnable;
import alex.imhere.service.TimeFormatter;
import alex.imhere.view.UpdatingTimer;

@EFragment(R.layout.fragment_status)
public class StatusFragment extends Fragment
		implements BaseModel.ModelListener, UpdatingTimer.TimerListener, ListeningController {
	Logger l = LoggerFactory.getLogger(StatusFragment.class);

	ImhereModel model;
	ImhereModel.EventListener eventsListener;

	FragmentInteractionsListener interactionsListener;

	Handler uiHandler;
	UpdatingTimer updatingTimer;

	@ViewById(R.id.ts_status) TextSwitcher tsStatus;
	@ViewById(R.id.tv_timer) TextView tvTimer;
	@ViewById(R.id.b_imhere) Button imhereButton;
	@ViewById(R.id.e_b_imhere) RippleBackground imhereButtonClickEffect;
	TimeFormatter timeFormatter = new TimeFormatter();

	boolean isCurrentUserLoginned = false;
	LocalDateTime currentUserAliveTo = new LocalDateTime(0);

	public StatusFragment() {
		// Required empty public constructor
	}

	@AfterViews
	public void onViewsInjected() {
		uiHandler = new Handler();
		updatingTimer = new UpdatingTimer(this);
		updatingTimer.start();

		updateTimer();

		tsStatus.setAnimateFirstView(false);
		TextView tvSessionDead = (TextView) View.inflate(getActivity(), R.layout.textview_status, null);
		tvSessionDead.setText("Offline");
		tsStatus.addView(tvSessionDead);
		tsStatus.showNext();

		TextView tvSessionAlive = (TextView) View.inflate(getActivity(), R.layout.textview_status, null);
		tvSessionAlive.setText("Online");
		tsStatus.addView(tvSessionAlive);

		imhereButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (interactionsListener != null) {

					UiRunnable onPreExecute = new UiRunnable(new Runnable() {
						@Override
						public void run() {
							imhereButtonClickEffect.startRippleAnimation();
							imhereButton.setEnabled(false);
							tsStatus.setText("   Connecting...");
						}
					});

					onPreExecute.run();
					interactionsListener.onImhereClick();
				}
			}
		});
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

	@Override
	public void onResume() {
		super.onResume();

		startListening();
		//l.info("subscribed to Model");
	}

	@Override
	public void onPause() {
		super.onPause();
		stopListening();
	}

	@UiThread
	void updateStatus() {
		if (isCurrentUserLoginned) {
			tsStatus.setText("Online");
		} else {
			tsStatus.setText("Offline");
		}
	}

	@UiThread
	void updateTimer() {
		int timerVisibility = View.INVISIBLE;
		int animationType = R.anim.fade_out;
		Animation timerAnimation;

		if (isCurrentUserLoginned) {
			timerVisibility = View.VISIBLE;
			animationType = R.anim.fade_in;
			updateTimerTick();
		}

		timerAnimation = AnimationUtils.loadAnimation(getActivity(), animationType);
		tvTimer.startAnimation(timerAnimation);

		tvTimer.setVisibility(timerVisibility);
	}

	@UiThread
	void updateTimerTick() {
		// TODO: 27.08.2015 really bad hack
		String durationMsString = timeFormatter.durationToMSString( new Duration(new LocalDateTime().toDateTime(), currentUserAliveTo.toDateTime()) );
		tvTimer.setText(durationMsString);
	}

	@UiThread
	void updateImhereButton() {
		TransitionDrawable drawable = (TransitionDrawable) imhereButton.getBackground();
		if (isCurrentUserLoginned) {
			imhereButton.setText("I\'m out!");
			drawable.startTransition(0);
		} else {
			imhereButton.setText("I\'m here!");
			drawable.reverseTransition(0);
		}
	}

	void setStatusLoginned(boolean currentUserIsLoggined) {
		isCurrentUserLoginned = currentUserIsLoggined;
		updateStatus();
		updateImhereButton();
		updateTimer();
	}

	@UiThread
	void setImhereButtonEnabled() {
		imhereButton.setEnabled(true);
		imhereButtonClickEffect.stopRippleAnimation();
	}

	@Override
	public void onTimerTick() {
		updateTimerTick();
	}

	@Override
	public void setModel(BaseModel baseModel) {
		this.model = (ImhereModel) baseModel;
	}

	@Override
	public void startListening() {
		eventsListener = new ImhereModel.EventListener() {
			@Override
			public void onLoginUser(DyingUser dyingUser) {
			}

			@Override
			public void onLogoutUser(DyingUser dyingUser) {

			}

			@Override
			public void onUsersUpdate() {

			}

			@Override
			public void onClearUsers() {

			}

			@Override
			public void onLogin(DyingUser currentUser) {
				currentUserAliveTo = currentUser.getAliveTo();
				setStatusLoginned(true);
				setImhereButtonEnabled();
			}

			@Override
			public void onLogout() {
				setStatusLoginned(false);
				setImhereButtonEnabled();
			}
		};

		model.addListener(eventsListener);
	}

	@Override
	public void stopListening() {
		model.removeListener(eventsListener);
		eventsListener = null;
	}

	public interface FragmentInteractionsListener {
		void onImhereClick();
	}

}
