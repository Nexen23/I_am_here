package alex.imhere.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.ParseException;

import alex.imhere.R;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.fragment.StatusFragment;
import alex.imhere.fragment.view.AbstractView;
import alex.imhere.fragment.view.UiRunnable;

public class ImhereActivity extends AppCompatActivity
		implements StatusFragment.FragmentInteractionListener {

	ImhereModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String udid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		model = new ImhereModel(new Handler(), udid);

		setContentView(R.layout.activity_main);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		AbstractView abstractView = (AbstractView) fragment;
		abstractView.setModel(model);
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

	public void showUsersFragment(final boolean doShow) {
		final Activity activity = this;
		model.getUiHandler().post(new Runnable() {
			@Override
			public void run() {
				final FrameLayout usersView = (FrameLayout) activity.findViewById(R.id.fl_fragment_users);
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
		});
	}

	@Override
	public void onImhereClick(@NonNull final UiRunnable onPostExecute) {
		if (model.isCurrentSessionAlive()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.cancelCurrentSession();
					onPostExecute.run();
					showUsersFragment(false);
				}
			}).start();
		} else {
			final Context context = this;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Runnable onSessionClosed = new Runnable() {
							@Override
							public void run() {
								showUsersFragment(false);
							}
						};

						model.openNewSession(onSessionClosed);
						onPostExecute.run();
						showUsersFragment(true);
					} catch (ParseException e) {
						e.printStackTrace();
						String toaskString = "Error logining to server: " + e.getMessage();
						Toast.makeText(context, toaskString, Toast.LENGTH_SHORT).show();
					}
				}
			}).start();
		}
	}
}
