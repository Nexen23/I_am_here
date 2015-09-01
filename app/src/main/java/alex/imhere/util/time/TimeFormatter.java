package alex.imhere.util.time;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public abstract class TimeFormatter {
	static public String DurationToMSString(Duration duration) {
		PeriodFormatter minutesAndSeconds = new PeriodFormatterBuilder()
				.printZeroAlways()
				.appendMinutes()
				.appendSeparator(":")
				.appendSeconds()
				.toFormatter();
		/*PeriodFormatter formatter = new PeriodFormatterBuilder()
				.appendDays()
				.appendSuffix("d")
				.appendHours()
				.appendSuffix("h")
				.appendMinutes()
				.appendSuffix("m")
				.appendSeconds()
				.appendSuffix("s")
				.toFormatter();*/
		String result = minutesAndSeconds.print(duration.toPeriod());

		return result;
	}
}
