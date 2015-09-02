package alex.imhere.activity;

import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import org.androidannotations.annotations.EActivity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alex.imhere.R;
import alex.imhere.fragment.StatusFragment;
import alex.imhere.fragment.UsersFragment;
import alex.imhere.model.AbstractModel;
import alex.imhere.model.ImhereRoomModel;
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
		return false;
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
}
