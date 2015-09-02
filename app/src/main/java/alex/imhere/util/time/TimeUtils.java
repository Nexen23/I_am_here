package alex.imhere.util.time;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.DateTime;

public abstract class TimeUtils {
	static public Duration GetNonNegativeDuration(DateTime a, DateTime b) {
		Duration duration = new Duration(a, b);
		return (duration.getMillis() < 0) ? new Duration(0) : duration;
	}
}
