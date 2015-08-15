package alex.imhere.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import alex.imhere.R;
import alex.imhere.layer.server.Session;
import alex.imhere.fragment.StatusFragment;
import alex.imhere.fragment.UsersFragment;

public class ImhereActivity extends AppCompatActivity
		implements StatusFragment.OnFragmentInteractionListener, UsersFragment.OnFragmentInteractionListener{

	protected Session session = new Session();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onUserLogin(final String username) {
		session.setUdid(username);
	}

	@Override
	public void onUserClick(Session session) {
		//Toast.makeText(ImhereActivity.this, String.format("onUserClick (%s | %s) from Activity", session.getUdid(), session.getDieAfterMs()), Toast.LENGTH_SHORT).show();
	}
}
