package alex.imhere.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import alex.imhere.R;
import alex.imhere.layer.server.Session;
import alex.imhere.service.TimeFormatter;

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
		Session session = getItem(position);

		String sessionLoggingString = "null";
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
		Log.d("TAG", loggingString);

		if (userView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			userView = inflater.inflate(resourceId, parent, false);

			if (session != null) {
				Long restLifetimeMs = session.getRestLifetime().getMillis();
				Long fullLifetimeMs = session.getFullLifetime().getMillis();
				float lifetimeCoef = restLifetimeMs.floatValue() / fullLifetimeMs.floatValue();

				int startingColor = Color.argb(255, 0, (int) (255 * lifetimeCoef), 0);

				/*ColorDrawable[] color = {
						new ColorDrawable(startingColor),
						new ColorDrawable(Color.RED)};
				TransitionDrawable trans = new TransitionDrawable(color);
				userView.setBackground(trans);
				trans.startTransition((int) session.getRestLifetime().getMillis());*/

				/*ObjectAnimator dyingColorAnimation = ObjectAnimator.ofInt(userView, "backgroundColor", startingColor, Color.RED);
				dyingColorAnimation.setDuration(restLifetimeMs);
				dyingColorAnimation.setEvaluator(new ArgbEvaluator());
				dyingColorAnimation.start();*/
			}
		}

		if (session != null)
		{
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
