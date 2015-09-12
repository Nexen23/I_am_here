package alex.imhere.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import alex.imhere.R;
import alex.imhere.entity.DyingUser;
import alex.imhere.util.time.TimeFormatter;
import alex.imhere.view.UserLayout;

public class UsersAdapter extends ArrayAdapter<DyingUser> {
	final int resourceId;
	final Context context;
	final List<DyingUser> items;

	public UsersAdapter(Activity activity, int item_user, List<DyingUser> items) {
		super(activity, item_user, items);
		this.items = items;

		this.context = activity;
		this.resourceId = item_user;
	}

	@Override
	public void add(DyingUser insertingDyingUser) {
		sortedInsert(insertingDyingUser);
	}

	private void sortedInsert(DyingUser insertingDyingUser) {
		if (getCount() == 0) {
			super.add(insertingDyingUser);
		} else {
			boolean notInserted = true;
			for( int i = 0; i < getCount() && notInserted; i++ ) {
				DyingUser dyingUser = getItem(i);
				if (dyingUser.getRestLifetime().getMillis() >= insertingDyingUser.getRestLifetime().getMillis()) {
					insert(insertingDyingUser, i);
					notInserted = false;
				}
			}
			if (notInserted) {
				super.add(insertingDyingUser);
			}
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		DyingUser dyingUser = getItem(position);

		UserLayout userView;
		if (convertView != null) {
			userView = (UserLayout) convertView;
		} else {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			userView = (UserLayout) inflater.inflate(resourceId, parent, false);
		}

		if (userView.getTag() != dyingUser)
		{
			/*userView.setTag(null);
			userView.setVisibility(View.GONE);*/

			Long restLifetimeMs = dyingUser.getRestLifetime().getMillis();
			Long fullLifetimeMs = dyingUser.getFullLifetime().getMillis();
			Long timeElapsedMs = fullLifetimeMs - restLifetimeMs;

			userView.setLifetime(fullLifetimeMs, timeElapsedMs);
			userView.startGradientAnimation();

			userView.setTag(dyingUser);
		}

		fillView(userView, dyingUser);

		return userView;
	}

	private void fillView(View userView, DyingUser dyingUser)
	{
		TextView tv_name = (TextView) userView.findViewById(R.id.tv_name);
		tv_name.setText(dyingUser.getUdid());

		TextView tv_singed_in_date = (TextView) userView.findViewById(R.id.tv_singed_in_date);

		String result = TimeFormatter.DurationToMSString(dyingUser.getRestLifetime());

		tv_singed_in_date.setText( result );
	}
}
