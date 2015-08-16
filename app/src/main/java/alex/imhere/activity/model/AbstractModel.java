package alex.imhere.activity.model;

import java.util.ArrayList;

import alex.imhere.fragment.view.AbstractView;

abstract public class AbstractModel {
	ArrayList<AbstractView> listeners = new ArrayList<>();

	public void addEventListener(AbstractView view) {
		listeners.add(view);
	}

	public void notifyDataChanged() {
		for (AbstractView view : listeners) {
			view.onDataUpdate(this);
		}
	}
}
