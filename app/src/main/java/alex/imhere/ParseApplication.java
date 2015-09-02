package alex.imhere;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class ParseApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Parse.initialize(this, "ckJRRjvnaJFT5dMkyCXaCOcDWSEHIhOvpviSLz0T", "F19u2qI3lYOBZo8Rr09gYydrZnQP9Nc0PXOnyYro");

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
	}
}
