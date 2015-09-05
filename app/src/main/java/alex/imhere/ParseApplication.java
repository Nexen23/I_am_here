package alex.imhere;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

public class ParseApplication extends Application {
	static ParseApplication instance;
	static Tracker tracker;

	public ParseApplication() {
		super();
		instance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Parse.com
		Parse.initialize(this, "ckJRRjvnaJFT5dMkyCXaCOcDWSEHIhOvpviSLz0T", "F19u2qI3lYOBZo8Rr09gYydrZnQP9Nc0PXOnyYro");

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
	}

	public synchronized Tracker getDefaultTracker() {
		if (tracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			analytics.setLocalDispatchPeriod(1800);

			tracker = analytics.newTracker("UA-67232439-1");
			tracker.enableExceptionReporting(true);
			tracker.enableAdvertisingIdCollection(true);
			tracker.enableAutoActivityTracking(true);
		}
		return tracker;
	}

	static public ParseApplication Instance() {
		return instance;
	}
}
