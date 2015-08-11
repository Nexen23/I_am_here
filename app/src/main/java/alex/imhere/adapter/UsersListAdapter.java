package alex.imhere.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import alex.imhere.R;

import alex.imhere.fragment.provider.DummyContent.DummyItem;

public class UsersListAdapter extends ArrayAdapter<DummyItem> {
	private final int resourceId;
	private Context context;

	public UsersListAdapter(Context context, int item_user, List<DummyItem> items) {
		super(context, item_user, items);

		this.context = context;
		this.resourceId = item_user;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView = convertView;
		if (itemView == null)
		{
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			itemView = inflater.inflate(resourceId, parent, false);
		}

		DummyItem item = getItem(position);
		if (item != null)
		{
			fillViewWithItem(itemView, item);
		}

		return itemView;
	}

	private void fillViewWithItem(View itemView, DummyItem item)
	{
		TextView tv_name = (TextView) itemView.findViewById(R.id.tv_name);
		tv_name.setText(item.id + " - " + item.content);
	}
}
