package alex.imhere.fragment;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.skyfishjy.library.RippleBackground;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.AnimationRes;
import org.androidannotations.annotations.res.StringRes;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import alex.imhere.ImhereApplication;
import alex.imhere.R;
import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.service.ComponentOwner;
import alex.imhere.service.UpdatingTimer;
import alex.imhere.service.api.AuthApi;
import alex.imhere.util.time.TimeFormatter;
import alex.imhere.util.time.TimeUtils;

@EFragment(R.layout.fragment_status)
public class LoginStatusFragment extends Fragment implements UpdatingTimer.TimerListener {
	//region Fields
	Logger l = LoggerFactory.getLogger(LoginStatusFragment.class);
	Tracker tracker;

	//region Resources
	@ViewById(R.id.ts_status) TextSwitcher tsStatus;
	@ViewById(R.id.tv_timer) TextView tvTimer;
	@ViewById(R.id.b_imhere) Button imhereButton;
	@ViewById(R.id.e_b_imhere) RippleBackground imhereButtonClickEffect;

	@AnimationRes(R.anim.fade_in) Animation fadeInAnim;
	@AnimationRes(R.anim.fade_out) Animation fadeOutAnim;

	@StringRes(R.string.non_initialized) String nonInitialized;

	@StringRes(R.string.b_imhere_logined) String imhereButtonLogined;
	@StringRes(R.string.b_imhere_logouted) String imhereButtonLogouted;

	@StringRes(R.string.ts_status_loginned) String statusLoginned;
	@StringRes(R.string.ts_status_logouted) String statusLogouted;
	@StringRes(R.string.ts_status_logining) String statusLogining;
	@StringRes(R.string.ts_status_logouting) String statusLogouting;
	//endregion

	// TODO: 02.09.2015 place it to Model?
	static final int LOGINNED_STATE = 1;
	static final int LOGOUTED_STATE = 2;
	static final int LOGINING_STATE = 3;
	static final int LOGOUTING_STATE = 4;
	int state = LOGOUTED_STATE, prevState = LOGOUTED_STATE;

	String udid;
	DyingUser currentUser;

	@Inject AuthApi authApi;
	UpdatingTimer updatingTimer;
	Timer timer = new Timer();
	TimerTask logoutTask;

	EventListener eventListener;
	//endregion

	//region Lifecycle
	public LoginStatusFragment() {
		// Required empty public constructor
	}

	@AfterViews
	public void onAfterViews() {
		tracker = ImhereApplication.newScreenTracker("LoginStatusFragment");

		udid = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

		updatingTimer = new UpdatingTimer(this);

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
			eventListener = (EventListener) activity;
			((ComponentOwner) activity).getServicesComponent().inject(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement EventListener & ComponentOwner");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		eventListener = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		updatingTimer.start();
		scheduleLogoutAtCurrentUserDeath();

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public void onPause() {
		super.onPause();
		cancelLogoutAtCurrentUserDeath();
		updatingTimer.stop();

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}
	//endregion

	//region Ui helpers
	@UiThread
	void updateStatus() {
		String status = nonInitialized;
		switch (state) {
			case LOGINING_STATE :
				status = statusLogining;
				break;

			case LOGINNED_STATE :
				status = statusLoginned;
				break;

			case LOGOUTED_STATE :
				status = statusLogouted;
				break;

			case LOGOUTING_STATE :
				status = statusLogouting;
				break;
		}
		tsStatus.setText(status);
	}

	@UiThread
	void updateImhereButton() {
		TransitionDrawable drawable = (TransitionDrawable) imhereButton.getBackground();
		switch (state) {
			case LOGINNED_STATE:
				imhereButton.setText(imhereButtonLogined);
				drawable.startTransition(0);
				break;

			case LOGOUTED_STATE:
				imhereButton.setText(imhereButtonLogouted);
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
		// TODO: 27.08.2015 really bad hack because of UpdatingTimer
		if (isCurrentUserAlive()) {
			Duration restTime = TimeUtils.GetNonNegativeDuration(new DateTime(), getCurrentUser().getAliveTo());
			String durationMsString = TimeFormatter.DurationToMSString(restTime);
			tvTimer.setText(durationMsString);
		}
	}
	//endregion

	@Click(R.id.b_imhere)
	void imhereButtonClick() {
		setImhereButtonEnabled(false);
		if (isCurrentUserAlive()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					logout();
					setCurrentUser(null);
				}
			}).start();
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					login();
				}
			}).start();
		}
	}

	void setState(int state) {
		prevState = this.state;
		this.state = state;

		updateStatus();
		updateImhereButton();
		updateTimer();
	}

	public void setCurrentUser(@Nullable DyingUser user) {
		currentUser = user;
	}

	public DyingUser getCurrentUser() {
		return currentUser;
	}

	public boolean isCurrentUserAlive() {
		DyingUser currentUser = getCurrentUser();
		return currentUser != null && currentUser.isAlive();
	}

	public void scheduleLogoutAtCurrentUserDeath() {
		if (isCurrentUserAlive()) {
			logoutTask = new TimerTask() {
				@Override
				public void run() {
					logout();
				}
			};
			timer.schedule(logoutTask, getCurrentUser().getAliveTo().toDate());
		}
	}

	public void cancelLogoutAtCurrentUserDeath() {
		if (logoutTask != null) {
			logoutTask.cancel();
			logoutTask = null;
		}
		timer.purge();
	}

	public synchronized final void login() {
		eventListener.onPreLogin();

		try {
			setCurrentUser(authApi.login(udid));
		} catch (ApiException e) {
			e.printStackTrace();
		}
		scheduleLogoutAtCurrentUserDeath();

		eventListener.onLogin(currentUser);
		setImhereButtonEnabled(true);
	}

	public synchronized void logout() {
		DyingUser currentUser = getCurrentUser();
		if (currentUser != null) {
			setImhereButtonEnabled(false);
			eventListener.onPreLogout();

			cancelLogoutAtCurrentUserDeath();

			authApi.logout(currentUser);
			setCurrentUser(null);

			eventListener.onLogout();
			setImhereButtonEnabled(true);
		}
	}

	//region Interfaces impls
	@Override
	public void onTimerTick() {
		updateTimerTick();
	}
	//endregion

	public interface EventListener {
		void onPreLogin();
		void onLogin(DyingUser currentUser);
		void onPreLogout();
		void onLogout();
	}
}
