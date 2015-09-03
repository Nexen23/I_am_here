package alex.imhere.activity;

import android.animation.ValueAnimator;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alex.imhere.R;
import alex.imhere.entity.DyingUser;
import alex.imhere.fragment.ImhereLoginFragment;
import alex.imhere.fragment.UsersFragment;
import alex.imhere.service.ImhereServiceManager;
import alex.imhere.service.ServiceManager;
import alex.imhere.service.api.UserApi;

@EActivity(R.layout.activity_main)
public class ImhereActivity extends AppCompatActivity implements ImhereLoginFragment.EventListener {
	Logger l = LoggerFactory.getLogger(ImhereActivity.class);

	ServiceManager serviceManager = new ImhereServiceManager();
	UserApi usersApi;

	String udid;
	DyingUser currentUser;

	boolean usersFragmentIsShown = false;

	@FragmentById(R.id.fragment_users) UsersFragment usersFragment;

	@AfterViews
	public void onAfterViews() {
		udid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

		usersApi = serviceManager.getApiService().getUserApi();
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	//region UsersFragment controlling helpers
	@UiThread
	public void showUsersFragment(final boolean doNeedToShow) {
		if (isUsersFragmentShown() != doNeedToShow) {
			final FrameLayout usersView = (FrameLayout) findViewById(R.id.fl_fragment_users);
			final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) usersView.getLayoutParams();
			final int marginInPx = (int) getResources().getDimension(R.dimen.fragment_users_margin);
			ValueAnimator animator = ValueAnimator.ofInt(marginInPx, 0);
			if (!doNeedToShow) {
				animator = ValueAnimator.ofInt(0, marginInPx);
			}
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
					usersView.requestLayout();
				}
			});
			animator.setDuration(getResources().getInteger(R.integer.duration_users_fragment_sliding));
			animator.start();
		}
	}

	public void showUsersFragment() {
		showUsersFragment(true);
	}

	public void hideUsersFragment() {
		showUsersFragment(false);
	}

	public boolean isUsersFragmentShown() {
		return usersFragmentIsShown;
	}
	//endregion

	@Override
	public void onPreLogin() {}

	@Override
	public void onLogin(DyingUser currentUser) {
		usersFragment.setCurrentUser(currentUser);
		showUsersFragment();
	}

	@Override
	public void onPreLogout() {
		hideUsersFragment();
	}

	@Override
	public void onLogout() {
		usersFragment.clearCurrentUser();
	}
}
