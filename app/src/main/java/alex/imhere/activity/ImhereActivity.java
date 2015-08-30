package alex.imhere.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.parse.ParseException;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alex.imhere.R;
import alex.imhere.activity.model.BaseModel;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.activity.fragment.StatusFragment;

@EActivity
public class ImhereActivity extends AppCompatActivity
		implements StatusFragment.FragmentInteractionsListener {
	Logger l = LoggerFactory.getLogger(ImhereActivity.class);

	ImhereModel model;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String udid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
		model = new ImhereModel(udid);

		setContentView(R.layout.activity_main);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);
		BaseModel.ModelListener modelListener = (BaseModel.ModelListener) fragment;
		modelListener.setModel(model);
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		model.startListening();
		model.updateOnlineUsers();
	}

	@Override
	protected void onPause() {
		super.onPause();
		model.stopListening();
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
					model.cancelCurrentSession();
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
