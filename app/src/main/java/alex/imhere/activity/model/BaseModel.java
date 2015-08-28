package alex.imhere.activity.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;

public abstract class BaseModel<TEventsListener extends BaseModel.EventListener> {
	// TODO: 28.08.2015 store weak ref (than make forEach with Runnable to check if Null)
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

	public interface EventListener {
	}

	public interface ModelListener {
		void setModel(BaseModel baseModel);
		void subscribeModel();
		void unsubscribeModel();
	}
}
