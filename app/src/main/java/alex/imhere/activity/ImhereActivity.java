package alex.imhere.activity;

import alex.imhere.service.component.DaggerServicesComponent;
import alex.imhere.service.component.ServicesComponent;
import android.animation.ValueAnimator;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import alex.imhere.R;
import alex.imhere.entity.DyingUser;
import alex.imhere.fragment.LoginFragment;
import alex.imhere.fragment.UsersFragment;
import alex.imhere.service.domain.ticker.TimeTicker;

@EActivity(R.layout.activity_main)
public class ImhereActivity extends AppCompatActivity
		implements ServicesComponent.Owner, TimeTicker.Owner,
		LoginFragment.EventListener {
	//region Fields
	Logger l = LoggerFactory.getLogger(ImhereActivity.class);

	ServicesComponent servicesComponent;
	@Inject	TimeTicker timeTicker;

	@InstanceState boolean usersFragmentIsShown = false;

	@FragmentById(R.id.fragment_users) UsersFragment usersFragment;
	//endregion

	//region Lifecycle
	public ImhereActivity() {
		super();
		servicesComponent = DaggerServicesComponent.builder().build();
		servicesComponent.inject(this);
	}

	@Override
	protected void onResumeFragments() {
		timeTicker.start();
		showUsersFragment(usersFragmentIsShown);
		super.onResumeFragments();
	}

	@Override
	protected void onPause() {
		super.onPause();
		timeTicker.stop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	//endregion

	//region Ui helpers
	@UiThread
	public void showUsersFragment(final boolean needShow) {
		final FrameLayout usersView = (FrameLayout) findViewById(R.id.fl_fragment_users);
		final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) usersView.getLayoutParams();
		final int marginInPx = (int) getResources().getDimension(R.dimen.fragment_users_margin);
		if (usersFragmentIsShown != needShow) {
			ValueAnimator animator = ValueAnimator.ofInt(marginInPx, 0);
			if (!needShow) {
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
		} else {
			if (usersFragmentIsShown) {
				params.rightMargin = 0;
			} else {
				params.rightMargin = marginInPx;
			}
		}

		usersFragmentIsShown = needShow;
	}
	//endregion

	//region Interfaces impls
	@Override
	public ServicesComponent getServicesComponent() {
		return servicesComponent;
	}

	@Override
	public TimeTicker getTimeTicker() {
		return timeTicker;
	}

	@Override
	public void onPreLogin() {
	}

	@Override
	public void onLoginned(DyingUser currentUser) {
		usersFragment.setCurrentUserAndStart(currentUser);
		showUsersFragment(true);
	}

	@Override
	public void onPreLogout() {
		showUsersFragment(false);
	}

	@Override
	public void onLogouted() {
		usersFragment.clearCurrentUserAndStop();
	}
	//endregion
}
