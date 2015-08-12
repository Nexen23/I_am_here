package alex.imhere;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;

public class ParseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// Enable Local Datastore.
		//Parse.enableLocalDatastore(this);

		Parse.initialize(this, "ckJRRjvnaJFT5dMkyCXaCOcDWSEHIhOvpviSLz0T", "F19u2qI3lYOBZo8Rr09gYydrZnQP9Nc0PXOnyYro");
		ParseInstallation.getCurrentInstallation().saveInBackground();

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();

		// If you would like all objects to be private by default, remove this
		// line.
		defaultACL.setPublicReadAccess(true);

		ParseACL.setDefaultACL(defaultACL, true);
	}
}
