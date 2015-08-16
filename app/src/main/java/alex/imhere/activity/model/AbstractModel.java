package alex.imhere.activity.model;

import java.util.ArrayList;

import alex.imhere.fragment.view.AbstractView;

abstract public class AbstractModel {
	ArrayList<AbstractView> listeners = new ArrayList<>();

	void addEventListener(AbstractView view) {
		listeners.add(view);
	}

	void notifyDataChanged() {
		for (AbstractView view : listeners) {
			view.onDataUpdate();
		}
	}
}
