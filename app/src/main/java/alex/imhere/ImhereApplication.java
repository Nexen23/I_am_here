package alex.imhere;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.res.StringRes;

@ReportsCrashes(
		formUri = "http://nexen23.iriscouch.com/acra-imhere/_design/acra-storage/_update/report",
		reportType = HttpSender.Type.JSON,
		httpMethod = HttpSender.Method.PUT,
		formUriBasicAuthLogin = "imhere",
		formUriBasicAuthPassword = "imhere_crashs",
		// Your usual ACRA configuration
		mode = ReportingInteractionMode.TOAST,
		resToastText = R.string.crash_message
)
@EApplication
public class ImhereApplication extends Application {
	static ImhereApplication instance;
	static Tracker tracker;

	@StringRes(R.string.ga_tracker_ua) static String GA_TRACKER_UA;

	public ImhereApplication() {
		super();
		instance = this;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// ACRA
		ACRA.init(this);

		// Parse.com
		Parse.initialize(this, "ckJRRjvnaJFT5dMkyCXaCOcDWSEHIhOvpviSLz0T", "F19u2qI3lYOBZo8Rr09gYydrZnQP9Nc0PXOnyYro");

		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
	}

	public synchronized Tracker getGlobalTracker() {
		if (tracker == null) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
			analytics.setLocalDispatchPeriod(1800);

			tracker = analytics.newTracker(GA_TRACKER_UA);
			tracker.enableExceptionReporting(true);
			tracker.enableAdvertisingIdCollection(true);
			tracker.enableAutoActivityTracking(true);
		}
		return tracker;
	}

	static public synchronized Tracker newScreenTracker(String name) {
		GoogleAnalytics analytics = GoogleAnalytics.getInstance(Instance());
		analytics.setLocalDispatchPeriod(1800);

		Tracker tracker = analytics.newTracker(GA_TRACKER_UA);
		tracker.enableExceptionReporting(true);
		tracker.enableAdvertisingIdCollection(true);
		tracker.enableAutoActivityTracking(true);
		tracker.setScreenName(name);
		return tracker;
	}

	static public synchronized ImhereApplication Instance() {
		return instance;
	}
}
