package alex.imhere.activity;

import android.content.Context;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseException;

import java.util.Timer;
import java.util.TimerTask;

import alex.imhere.R;
import alex.imhere.activity.model.ImhereModel;
import alex.imhere.fragment.StatusFragment;
import alex.imhere.fragment.view.AbstractView;

public class ImhereActivity extends AppCompatActivity
		implements StatusFragment.FragmentInteractionListener {

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

	@Override
	public void onImhereClick() {
		if (model.isCurrentSessionAlive()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					model.cancelCurrentSession();
				}
			}).start();
		}
		else {
			final Context context = this;
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						model.openNewSession();
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
