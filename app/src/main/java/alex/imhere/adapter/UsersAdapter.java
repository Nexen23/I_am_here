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
import alex.imhere.fragment.view.UserLayout;
import alex.imhere.layer.server.Session;
import alex.imhere.service.TimeFormatter;

public class UsersAdapter extends ArrayAdapter<Session> {
	private final int resourceId;
	private Context context;
	/*@BindColor(R.color.user_born) */int userBornColor;
	/*@BindColor(R.color.user_alive) */int userAliveColor;
	/*@BindColor(R.color.user_dead) */int userDeadColor;

	public UsersAdapter(Activity activity, int item_user, List<Session> items) {
		super(activity, item_user, items);

		this.context = activity;
		this.resourceId = item_user;

		/*ButterKnife.bind(activity);*/
		userBornColor = activity.getResources().getColor(R.color.user_born);
		userAliveColor = activity.getResources().getColor(R.color.user_alive);
		userDeadColor = activity.getResources().getColor(R.color.user_dead);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		UserLayout userView = (UserLayout) convertView;
		Session session = getItem(position);

		/*String sessionLoggingString = "null";
		String userViewLoggingString = "null";
		String animationLoggingString = "null";

		if (session != null) {
			sessionLoggingString = session.getUdid();
		}

		if (userView != null) {
			userViewLoggingString = String.format("%d", userView.hashCode());

			Animation userViewAnimation = userView.getAnimation();
			if (userViewAnimation != null) {
				animationLoggingString = String.format("%d", userViewAnimation.hashCode());
			}
		}

		String loggingString = String.format("[UsersAdapter] [%d - %s] {userView == %s} {animation == %s}",
				position, sessionLoggingString, userViewLoggingString, animationLoggingString);
		Log.d("TAG", loggingString);*/

		if (userView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			userView = (UserLayout) inflater.inflate(resourceId, parent, false);


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
