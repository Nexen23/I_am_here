package alex.imhere.activity;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alex.imhere.R;
import alex.imhere.fragment.UsersFragment;
import alex.imhere.model.AbstractModel;
import alex.imhere.model.ImhereRoomModel;
import alex.imhere.fragment.StatusFragment;
import alex.imhere.service.ImhereService;
import alex.imhere.service.Service;

@EActivity
public class ImhereActivity extends AppCompatActivity
		implements StatusFragment.InteractionListener, UsersFragment.InteractionListener {
	Logger l = LoggerFactory.getLogger(ImhereActivity.class);

	ImhereRoomModel model;
	Service service = new ImhereService();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String udid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		model = new ImhereRoomModel(service, udid);

		setContentView(R.layout.activity_main);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		AbstractModel.ModelListener modelListener = (AbstractModel.ModelListener) fragment;
		modelListener.setModel(model);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		model.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.pause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@UiThread
	public void showUsersFragment(final boolean doShow) {
		//TODO : it MUST be in the Fragment
		final FrameLayout usersView = (FrameLayout) findViewById(R.id.fl_fragment_users);
		final LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) usersView.getLayoutParams();
		final int marginInPx = (int) getResources().getDimension(R.dimen.fragment_users_margin);
		ValueAnimator animator = ValueAnimator.ofInt(marginInPx, 0);
		if (doShow == false) {
			animator = ValueAnimator.ofInt(0, marginInPx);
		}
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator)
			{
				params.rightMargin = (Integer) valueAnimator.getAnimatedValue();
				usersView.requestLayout();
			}
		});
		animator.setDuration( getResources().getInteger(R.integer.duration_users_fragment_sliding) );
		animator.start();
	}

	@Override
	public void onImhereClick(Fragment fragment) {
		if (model.isCurrentSessionAlive()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.logout();

				}
			}).start();
		} else {
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.login();
				}
			}).start();
		}
	}

	@Override
	public void onShow(Fragment fragment) {
		//TODO : pass real Fragment type (StatusFragment/UsersFragment) for retrieving data from it
		showUsersFragment(true);
	}

	@Override
	public void onHide(Fragment fragment) {
		showUsersFragment(false);
	}
}
