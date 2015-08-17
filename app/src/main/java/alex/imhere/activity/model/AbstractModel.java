package alex.imhere.activity.model;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import alex.imhere.fragment.view.AbstractView;

abstract public class AbstractModel {
	ArrayList<AbstractView> listeners = new ArrayList<>();
	ArrayList<Fragment> listenersFragments = new ArrayList<>();

	public void addEventListener(@NonNull Fragment fragment, @NonNull AbstractView view) {
		listenersFragments.add(fragment);
		listeners.add(view);
	}

	public void notifyDataChanged() {
		final AbstractModel data = this;
		for (int i = 0; i < listeners.size(); i++) {
			final AbstractView view = listeners.get(i);
			listenersFragments.get(i).getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					view.onDataUpdate(data);
				}
			});
		}
	}
}
