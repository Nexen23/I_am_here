package alex.imhere.fragment;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.skyfishjy.library.RippleBackground;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;
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
	// TODO: 02.09.2015 place it to Model?
	static final int LOGINNED_STATE = 1;
	static final int LOGOUTED_STATE = 2;
	static final int LOGINING_STATE = 3;
	static final int LOGOUTING_STATE = 4;
	int state = LOGOUTED_STATE, prevState = LOGOUTED_STATE;

	Logger l = LoggerFactory.getLogger(StatusFragment.class);

	ImhereRoomModel model;
	ImhereRoomModel.EventListener eventsListener;

	InteractionListener interactionsListener;

	UpdatingTimer updatingTimer;

	@ViewById(R.id.ts_status) TextSwitcher tsStatus;
	@ViewById(R.id.tv_timer) TextView tvTimer;
	@ViewById(R.id.b_imhere) Button imhereButton;
	@ViewById(R.id.e_b_imhere) RippleBackground imhereButtonClickEffect;

	@AnimationRes(R.anim.fade_in) Animation fadeInAnim;
	@AnimationRes(R.anim.fade_out) Animation fadeOutAnim;

	LocalDateTime currentUserAliveTo = new LocalDateTime(0);

	public StatusFragment() {
		// Required empty public constructor
	}

	@AfterViews
	public void onViewsInjected() {
		// TODO: 30.08.2015 make all string non hardcoded
		updatingTimer = new UpdatingTimer(this);
		updatingTimer.start();

		tsStatus.setAnimateFirstView(false);
		TextView tv1 = (TextView) View.inflate(getActivity(), R.layout.textview_status, null);
		tsStatus.addView(tv1);
		TextView tv2 = (TextView) View.inflate(getActivity(), R.layout.textview_status, null);
		tsStatus.addView(tv2);
		tsStatus.showNext();

		setState(LOGOUTED_STATE);
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

	@Click(R.id.b_imhere)
	void imhereButtonClick() {
		setImhereButtonEnabled(false);
		interactionsListener.onImhereClick(this);
	}

	void setState(int state) {
		prevState = this.state;
		this.state = state;

		updateStatus();
		updateImhereButton();
		updateTimer();
	}

	@UiThread
	void updateStatus() {
		String status = "_ERROR_";
		switch (state) {
			case LOGINING_STATE :
				status = "   Logining...";
				break;

			case LOGINNED_STATE :
				status = "Online";
				break;

			case LOGOUTED_STATE :
				status = "Offline";
				break;

			case LOGOUTING_STATE :
				status = "   Loging out...";
				break;
		}
		tsStatus.setText(status);
	}

	@UiThread
	void updateImhereButton() {
		TransitionDrawable drawable = (TransitionDrawable) imhereButton.getBackground();
		switch (state) {
			case LOGINNED_STATE :
				imhereButton.setText("I\'m out!");
				drawable.startTransition(0);
				break;

			case LOGOUTED_STATE :
				imhereButton.setText("I\'m here!");
				drawable.resetTransition();
				break;
		}
	}

	@UiThread
	void updateTimer() {
		switch (state) {
			case LOGINNED_STATE :
				tvTimer.startAnimation(fadeInAnim);
				tvTimer.setVisibility(View.VISIBLE);
				updateTimerTick();
				break;

			case LOGOUTED_STATE :
				tvTimer.startAnimation(fadeOutAnim);
				tvTimer.setVisibility(View.INVISIBLE);
				break;
		}
	}

	@UiThread
	void setImhereButtonEnabled(boolean isEnabled) {
		imhereButton.setEnabled(isEnabled);
		if (isEnabled) {
			imhereButtonClickEffect.stopRippleAnimation();
			switch (state) {
				case LOGOUTING_STATE :
					setState(LOGOUTED_STATE);
					break;

				case LOGINING_STATE :
					setState(LOGINNED_STATE);
					break;
			}
		} else {
			imhereButtonClickEffect.startRippleAnimation();
			switch (state) {
				case LOGINNED_STATE :
					setState(LOGOUTING_STATE);
					break;

				case LOGOUTED_STATE :
					setState(LOGINING_STATE);
					break;
			}
		}
	}

	@UiThread
	void updateTimerTick() {
		// TODO: 27.08.2015 really bad hack
		Duration restTime = TimeUtils.GetNonNegativeDuration(new LocalDateTime().toDateTime(), currentUserAliveTo.toDateTime());
		String durationMsString = TimeFormatter.DurationToMSString(restTime);
		tvTimer.setText(durationMsString);
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
				setImhereButtonEnabled(true);
			}

			@Override
			public void onCurrentUserTimeout() {
				setImhereButtonEnabled(false);
			}

			@Override
			public void onLogout() {
				setImhereButtonEnabled(true);
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
