package alex.imhere.fragment;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.animation.Animation;
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
import org.androidannotations.annotations.res.StringRes;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

import alex.imhere.R;
import alex.imhere.container.TemporarySet;
import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ApiException;
import alex.imhere.exception.BroadcastChannelException;
import alex.imhere.model.AbstractModel;
import alex.imhere.model.ImhereRoomModel;
import alex.imhere.service.ImhereServiceManager;
import alex.imhere.service.ServiceManager;
import alex.imhere.service.api.DateApi;
import alex.imhere.service.api.UserApi;
import alex.imhere.service.channel.Channel;
import alex.imhere.util.Resumable;
import alex.imhere.util.time.TimeFormatter;
import alex.imhere.util.time.TimeUtils;
import alex.imhere.service.UpdatingTimer;

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

	ServiceManager serviceManager = new ImhereServiceManager();
	UserApi usersApi;

	String udid;
	DyingUser currentUser;

	Timer timer = new Timer();
	TimerTask logoutTask;

	EventListener eventListener;

	UpdatingTimer updatingTimer;

	//region Injections
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

	public StatusFragment() {
		// Required empty public constructor
	}

	@AfterViews
	public void onAfterViews() {
		usersApi = serviceManager.getApiService().getUserApi();

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
			eventListener = (EventListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement EventListener");
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
		eventListener.onImhereClick(this);
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
			case LOGINNED_STATE :
				imhereButton.setText(imhereButtonLogined);
				drawable.startTransition(0);
				break;

			case LOGOUTED_STATE :
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
		// TODO: 27.08.2015 really bad hack
		Duration restTime = TimeUtils.GetNonNegativeDuration(new DateTime(), currentUserAliveTo);
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

	public void pause() {
		model.removeListener(eventsListener);
		eventsListener = null;
	}

	@Override
	public void onImhereClick(Fragment fragment) {
		if (isCurrentUserAlive()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					usersApi.logout(currentUser);
					setCurrentUser(null);
				}
			}).start();
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						setCurrentUser( usersApi.login(udid));
					} catch (ApiException e) {
						e.printStackTrace();
					}
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

	@Nullable
	public synchronized final DyingUser login() {
		currentUser = null;
		// TODO: 30.08.2015 log exceptions
		try {
			currentUser = api.login(udid);
			channel.connect();
		} catch (ApiException e) {
			e.printStackTrace();
		} catch (BroadcastChannelException e) {
			e.printStackTrace();
			api.logout(currentUser);
			currentUser = null;
		}
		scheduleLogoutAtCurrentUserDeath();

		updateOnlineUsers();

		notifier.onLogin(currentUser);
		return currentUser;
	}

	public synchronized void logout() {
		if (currentUser != null) {
			cancelLogoutAtCurrentUserDeath();

			channel.disconnect();
			api.logout(currentUser);
			currentUser = null;
			notifier.onLogout();

			onlineUsers.clear();
			notifier.onClearUsers();
		}
	}

	public void resume() {
		onlineUsersListener = new TemporarySet.EventListener() {
			@Override
			public void onClear() {
				notifier.onClearUsers();
			}

			@Override
			public void onAdd(Object item) {
				notifier.onUserLogin((DyingUser) item);
			}

			@Override
			public void onRemove(Object item) {
				notifier.onUserLogout((DyingUser) item);
			}
		};
		onlineUsers.addListener(onlineUsersListener);
		onlineUsers.resume();

		channelListener = new Channel.EventListener() {
			@Override
			public void onConnect(String channel, String greeting) {
			}

			@Override
			public void onDisconnect(String channel, String reason) {
			}

			@Override
			public void onReconnect(String channel, String reason) {
			}

			@Override
			public void onMessageRecieve(String channel, String message, String timetoken) {
				DyingUser dyingUser = userParser.fromJson(message, DyingUser.class);
				Boolean wasRemoved = false, wasAdded = false;
				if (dyingUser.isDead()) {
					wasRemoved = onlineUsers.remove(dyingUser);
				} else {
					wasAdded = onlineUsers.add(dyingUser, dyingUser.getAliveTo());
				}
				l.info("[{} : dead({})] wasRemoved == {}, wasAdded == {}",
						dyingUser.getUdid(), Boolean.valueOf(dyingUser.isDead()), wasAdded.toString(), wasRemoved.toString());
			}

			@Override
			public void onErrorOccur(String channel, String error) {
			}
		};
		channel.setListener(channelListener);
		channel.resume();

		onlineUsers.clear();

		if (isCurrentUserAlive())
		{
			notifier.onLogin(currentUser);
			updateOnlineUsers();
		}
		scheduleLogoutAtCurrentUserDeath();
	}

	public void pause() {
		cancelLogoutAtCurrentUserDeath();

		channel.clearListener();
		channelListener = null;

		onlineUsers.removeListener(onlineUsersListener);
		onlineUsersListener = null;
	}

	public interface EventListener {
		void onPreLogin();
		void onLogin(DyingUser currentUser);
		void onPreLogout();
		void onLogout();
	}
}
