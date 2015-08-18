package alex.imhere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joda.time.Duration;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.SimpleDateFormat;
import java.util.List;

import alex.imhere.R;

import alex.imhere.layer.server.Session;

public class UsersAdapter extends ArrayAdapter<Session> {
	private final int resourceId;
	private Context context;

	public UsersAdapter(Context context, int item_user, List<Session> items) {
		super(context, item_user, items);

		this.context = context;
		this.resourceId = item_user;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View userView = convertView;
		if (userView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			userView = inflater.inflate(resourceId, parent, false);
		}

		Session session = getItem(position);
		if (session != null)
		{
			fillViewWithUser(userView, session);
		}

		return userView;
	}

	private void fillViewWithUser(View userView, Session session)
	{
		TextView tv_name = (TextView) userView.findViewById(R.id.tv_name);
		tv_name.setText(session.getUdid());

		TextView tv_singed_in_date = (TextView) userView.findViewById(R.id.tv_singed_in_date);

		/*long deathTimeMillis = session.getAliveTo().toDateTime().getMillis(),
				nowMillis = (new LocalDateTime()).toDateTime().getMillis();
		long delay = Math.max(0, deathTimeMillis - nowMillis);

		Duration duration = new Duration(delay);*/

		LocalDateTime ldt = new LocalDateTime();
		Duration duration = new Duration(ldt.toDateTime(), session.getAliveTo().toDateTime());

		Period period = duration.toPeriod();
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
		String result = minutesAndSeconds.print(period);

		tv_singed_in_date.setText( result );
	}
}
