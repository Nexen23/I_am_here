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
import org.androidannotations.annotations.InstanceState;
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
import alex.imhere.service.component.ServicesComponent;
import alex.imhere.service.domain.ticker.TimeTicker;
import alex.imhere.service.domain.api.AuthApi;
import alex.imhere.util.time.TimeFormatter;
import alex.imhere.util.time.TimeUtils;
import alex.imhere.util.wrapper.UiToast;

@EFragment(R.layout.fragment_status)
public class LoginFragment extends Fragment implements TimeTicker.EventListener {
	//region Fields
	Logger l = LoggerFactory.getLogger(LoginFragment.class);
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

	@StringRes(R.string.loginning_failed) String loginFailed;
	//endregion

	static final int LOGINNED_STATE = 1;
	static final int LOGOUTED_STATE = 2;
	static final int LOGINING_STATE = 3;
	static final int LOGOUTING_STATE = 4;
	@InstanceState int state = LOGOUTED_STATE, prevState = LOGOUTED_STATE;

	@InstanceState String udid;
	/*@InstanceState */DyingUser currentUser;

	@Inject AuthApi authApi;
	TimeTicker.Owner timeTickerOwner;

	Timer timer = new Timer();
	TimerTask logoutTask;

	EventListener eventListener;
	//endregion

	//region Lifecycle
	private void constructStatusTextSwitcher() {
		tsStatus.setAnimateFirstView(false);
		TextView tv1 = (TextView) View.inflate(getActivity(), R.layout.textview_status, null);
		tsStatus.addView(tv1);
		TextView tv2 = (TextView) View.inflate(getActivity(), R.layout.textview_status, null);
		tsStatus.addView(tv2);
		tsStatus.showNext();
	}

	@AfterViews
	public void onAfterViews() {
		tracker = ImhereApplication.newScreenTracker("LoginFragment");
		udid = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
		constructStatusTextSwitcher();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			eventListener = (EventListener) activity;
			timeTickerOwner = (TimeTicker.Owner) activity;
			((ServicesComponent.Owner) activity).getServicesComponent().inject(this);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement EventListener & TimeTickerOwner & ComponentOwner");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		eventListener = null;
		timeTickerOwner = null;
	}

	@Override
	public void onResume() {
		super.onResume();
		setState(state);
		timeTickerOwner.getTimeTicker().addListener(this);
		scheduleLogoutAtCurrentUserDeath();

		tracker.send(new HitBuilders.ScreenViewBuilder().build());
	}

	@Override
	public void onPause() {
		super.onPause();
		cancelLogoutAtCurrentUserDeath();
		timeTickerOwner.getTimeTicker().removeListener(this);

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
		if (isStateNotChanged()) {
			tsStatus.setCurrentText(status);
		} else {
			tsStatus.setText(status);
		}
	}

	@UiThread
	void updateImhereButton() {
		TransitionDrawable drawable = (TransitionDrawable) imhereButton.getBackground();
		switch (state) {
			case LOGINNED_STATE:
				imhereButtonClickEffect.stopRippleAnimation();
				imhereButton.setEnabled(true);
				imhereButton.setText(imhereButtonLogined);
				drawable.startTransition(0);
				break;

			case LOGOUTED_STATE:
				imhereButtonClickEffect.stopRippleAnimation();
				imhereButton.setEnabled(true);
				imhereButton.setText(imhereButtonLogouted);
				drawable.resetTransition();
				break;

			case LOGINING_STATE:
			case LOGOUTING_STATE:
				imhereButton.setEnabled(false);
				imhereButtonClickEffect.startRippleAnimation();
				break;
		}
	}

	@UiThread
	void updateLifetime() {
		switch (state) {
			case LOGINNED_STATE :
				if (!isStateNotChanged()) {
					tvTimer.startAnimation(fadeInAnim);
				}
				tvTimer.setVisibility(View.VISIBLE);
				updateTimerTick();
				break;

			case LOGOUTED_STATE :
				if (!isStateNotChanged()) {
					tvTimer.startAnimation(fadeOutAnim);
				}
				tvTimer.setVisibility(View.INVISIBLE);
				break;
		}
	}

	@UiThread
	void updateTimerTick() {
		if (isCurrentUserAlive()) {
			Duration restTime = TimeUtils.GetNonNegativeDuration(new DateTime(), getCurrentUser().getAliveTo());
			String durationMsString = TimeFormatter.DurationToMSString(restTime);
			tvTimer.setText(durationMsString);
		}
	}

	void setState(int state) {
		prevState = this.state;
		this.state = state;

		updateStatus();
		updateImhereButton();
		updateLifetime();
	}

	boolean isStateNotChanged() {
		return prevState == state;
	}
	//endregion

	@Click(R.id.b_imhere)
	void imhereButtonClick() {
		if (isCurrentUserAlive()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					logout();
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
		setState(LOGINING_STATE);
		eventListener.onPreLogin();

		try {
			setCurrentUser(authApi.login(udid));
		} catch (final ApiException e) {
			e.printStackTrace();

			setState(LOGOUTED_STATE);
			eventListener.onLogouted();
			UiToast.Show(getActivity(), loginFailed, e.getMessage());
			return;
		}

		scheduleLogoutAtCurrentUserDeath();
		setState(LOGINNED_STATE);
		eventListener.onLoginned(currentUser);
	}

	public synchronized void logout() {
		DyingUser currentUser = getCurrentUser();
		if (currentUser != null) {
			setState(LOGOUTING_STATE);
			eventListener.onPreLogout();

			cancelLogoutAtCurrentUserDeath();

			authApi.logout(currentUser);
			setCurrentUser(null);

			eventListener.onLogouted();
			setState(LOGOUTED_STATE);
		}
	}

	//region Interfaces impls
	@Override
	public void onSecondTick() {
		updateTimerTick();
	}
	//endregion

	public interface EventListener {
		void onPreLogin();
		void onLoginned(DyingUser currentUser);
		void onPreLogout();
		void onLogouted();
	}
}
