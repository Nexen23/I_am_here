package alex.imhere.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import alex.imhere.R;
import alex.imhere.view.UserLayout;
import alex.imhere.layer.server.Session;
import alex.imhere.service.TimeFormatter;

public class UsersAdapter extends ArrayAdapter<Session> {
	private final int resourceId;
	private Context context;
	List<Session> items;

	public UsersAdapter(Activity activity, int item_user, List<Session> items) {
		super(activity, item_user, items);
		this.items = items;

		this.context = activity;
		this.resourceId = item_user;
	}

	@Override
	public void add(Session insertingSession) {
		if (getCount() == 0) {
			super.add(insertingSession);
		} else {
			boolean notInserted = true;
			for( int i = 0; i < getCount() && notInserted; i++ ) {
				Session session = getItem(i);
				if (session.getRestLifetime().getMillis() >= insertingSession.getRestLifetime().getMillis()) {
					insert(insertingSession, i);
					notInserted = false;
				}
			}
			if (notInserted) {
				super.add(insertingSession);
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		UserLayout userView = (UserLayout) convertView;
		Session session = getItem(position);

		/*String sessionLoggingString = "null";
		String userViewLoggingString = "null";

		if (session != null) {
			sessionLoggingString = session.getUdid();
		}

		if (userView != null) {
			userViewLoggingString = String.format("%d", userView.hashCode());
		}

		String loggingString = String.format("[UsersAdapter] [%d - %s] {userView == %s}",
				position, sessionLoggingString, userViewLoggingString);
		Log.d("TAG", loggingString);*/

		if (userView == null || userView.getTag() == null || userView.getTag() != session)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			userView = (UserLayout) inflater.inflate(resourceId, parent, false);
			userView.setTag(session);

			if (session != null) {
				Long restLifetimeMs = session.getRestLifetime().getMillis();
				Long fullLifetimeMs = session.getFullLifetime().getMillis();
				Long timeElapsedMs = fullLifetimeMs - restLifetimeMs;

				userView.setLifetime(fullLifetimeMs, timeElapsedMs);
				userView.startGradientAnimation();
			}
		}

		if (session != null) {
			fillView(userView, session);
		}

		return userView;
	}

	private void fillView(View userView, Session session)
	{
		TextView tv_name = (TextView) userView.findViewById(R.id.tv_name);
		tv_name.setText(session.getUdid());

		TextView tv_singed_in_date = (TextView) userView.findViewById(R.id.tv_singed_in_date);

		String result = new TimeFormatter().durationToMSString( session.getRestLifetime() );

		tv_singed_in_date.setText( result );
	}
}
