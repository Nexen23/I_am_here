package alex.imhere.activity.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;

public abstract class BaseModel<TEventsListener extends BaseModel.EventsListener> {
	ArrayList<TEventsListener> listeners = new ArrayList<>();

	public void addEventsListener(@Nullable TEventsListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void removeEventsListener(@Nullable TEventsListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	interface EventsListener {
	};
}
