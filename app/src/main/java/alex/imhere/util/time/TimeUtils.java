package alex.imhere.util.time;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

public abstract class TimeUtils {
	static public Duration GetNonNegativeDuration(DateTime a, DateTime b) {
		Duration duration = new Duration(a.toDateTime(), b.toDateTime());
		return (duration.getMillis() < 0) ? new Duration(0) : duration;
	}
}
