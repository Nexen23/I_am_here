package alex.imhere.fragment;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
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
import alex.imhere.model.AbstractModel;
import alex.imhere.model.ImhereRoomModel;
import alex.imhere.entity.DyingUser;
import alex.imhere.util.Resumable;
import alex.imhere.util.time.TimeFormatter;
import alex.imhere.util.time.TimeUtils;
import alex.imhere.util.time.UpdatingTimer;

@EFragment(R.layout.fragment_status)
public class StatusFragment extends Fragment
		implements AbstractModel.ModelListener, UpdatingTimer.TimerListener, Resumable {
	Logger l = LoggerFactory.getLogger(StatusFragment.class);

	ImhereRoomModel model;
	ImhereRoomModel.EventListener eventsListener;

	InteractionListener interactionsListener;

	Handler uiHandler;
	UpdatingTimer updatingTimer;

	@ViewById(R.id.ts_status) TextSwitcher tsStatus;
	@ViewById(R.id.tv_timer) TextView tvTimer;
	@ViewById(R.id.b_imhere) Button imhereButton;
	@ViewById(R.id.e_b_imhere) RippleBackground imhereButtonClickEffect;

	boolean isCurrentUserLoginned = false;
	LocalDateTime currentUserAliveTo = new LocalDateTime(0);

	public StatusFragment() {
		// Required empty public constructor
	}

	@AfterViews
	public void onViewsInjected() {
		// TODO: 30.08.2015 make all string non hardcoded
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

		final Fragment thisFragment = this;
		imhereButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (interactionsListener != null) {

					imhereButtonClick();
					interactionsListener.onImhereClick(thisFragment);
				}
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			interactionsListener = (InteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement InteractionListener");
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
		resume();
	}

	@Override
	public void onPause() {
		super.onPause();
		pause();
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
		Duration restTime = TimeUtils.GetNonNegativeDuration(new LocalDateTime().toDateTime(), currentUserAliveTo.toDateTime());
		String durationMsString = TimeFormatter.DurationToMSString(restTime);
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

	@UiThread
	void imhereButtonClick() {
		imhereButton.setEnabled(false);
		// TODO: 30.08.2015 another hack
		String text = "   Loging out...";
		if (!isCurrentUserLoginned) {
			text = "   Logining...";
			imhereButtonClickEffect.startRippleAnimation();
		}
		tsStatus.setText(text);
	}

	@Override
	public void onTimerTick() {
		updateTimerTick();
	}

	@Override
	public void setModel(AbstractModel abstractModel) {
		this.model = (ImhereRoomModel) abstractModel;
	}

	@Override
	public void resume() {
		eventsListener = new ImhereRoomModel.EventListener() {
			@Override
			public void onModelDataChanged(AbstractModel abstractModel) {

			}

			@Override
			public void onUserLogin(DyingUser dyingUser) {
			}

			@Override
			public void onUserLogout(DyingUser dyingUser) {

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
			public void onPreLogout() {
				imhereButtonClick();
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
	public void pause() {
		model.removeListener(eventsListener);
		eventsListener = null;
	}

	public interface InteractionListener {
		void onImhereClick(Fragment fragment);
	}
}
