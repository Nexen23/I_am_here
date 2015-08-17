package alex.imhere.activity.model;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import alex.imhere.fragment.view.AbstractView;

abstract public class AbstractModel {
	ArrayList<AbstractView> listeners = new ArrayList<>();

	public void addEventListener(@NonNull AbstractView view) {
		listeners.add(view);
	}

	public void notifyDataChanged() {
		for (AbstractView view : listeners) {
			view.onDataUpdate(this);
		}
	}
}
