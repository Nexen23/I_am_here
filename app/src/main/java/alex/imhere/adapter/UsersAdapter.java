package alex.imhere.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import alex.imhere.R;
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
		View userView = convertView;
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
			userView = inflater.inflate(resourceId, parent, false);

			//userView = new UserView(context, session.getRestLifetime().getMillis()); // TODO: 24.08.2015 no comparision with null

			if (session != null) {
				Long restLifetimeMs = session.getRestLifetime().getMillis();
				Long fullLifetimeMs = session.getFullLifetime().getMillis();
				float lifetimeCoef = restLifetimeMs.floatValue() / fullLifetimeMs.floatValue();

				int startingColor = Color.argb(255, 0, (int) (255 * lifetimeCoef), 0);

				/*ColorDrawable[] color = {
						new ColorDrawable(startingColor),
						new ColorDrawable(Color.RED)}; //only 2 allowed
				TransitionDrawable trans = new TransitionDrawable(color);
				userView.setBackground(trans);
				trans.startTransition((int) session.getRestLifetime().getMillis());*/


				/*final GradientDrawable background = (GradientDrawable) userView.getBackground();
				//background.mutate();

				final int[] colors = new int[]{userBornColor, userAliveColor, userAliveColor, userDeadColor};
				background.setGradientType(GradientDrawable.LINEAR_GRADIENT);
				background.setColors(colors);
				//colors[0] = userDeadColor; colors[1] = userDeadColor; colors[2] = userDeadColor;
				TimeAnimator timeAnimator = new TimeAnimator();
				final long duration = restLifetimeMs;
				final View finalUserView = userView;
				final int[] x = {0};
				timeAnimator.setTimeListener(new TimeAnimator.TimeListener() {
					@Override
					public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
						if (totalTime > duration) {
							animation.cancel();
						}

						if (totalTime / 250 > x[0]) {
							x[0] = x[0] + 1;

							float coef = (float) (totalTime) / duration;
							//colors[0] = (int) (coef * 0xFF0000) + 0xFF000000;
							//Log.d("TAG", String.format("%f[coef] - %d[colors[0]] :: %d[pointer]", coef, colors[0], colors.hashCode()));
							colors[0] = colors[0] + 10000;
							colors[1] = colors[1] + 20000;
							colors[2] = colors[2] + 70000;
							colors[3] = colors[3] + 150000;
							//background.invalidateSelf();
							//finalUserView.postInvalidate();
							finalUserView.setBackground(background);
						}
					}
				});
				timeAnimator.start();*/
				//ObjectAnimator dyingColorAnimation = ObjectAnimator.ofInt(userView, "backgroundColor", userBornColor, userAliveColor, userDeadColor);

				/*ObjectAnimator dyingColorAnimation = ObjectAnimator.ofInt(userView, "backgroundColor", userBornColor, userAliveColor, userDeadColor);
				dyingColorAnimation.setDuration(restLifetimeMs);
				dyingColorAnimation.setEvaluator(new ArgbEvaluator());
				dyingColorAnimation.setInterpolator(new LinearInterpolator());
				dyingColorAnimation.start(); // how to use startingColor??*/
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
