package alex.imhere.activity.model;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import alex.imhere.fragment.view.AbstractView;

abstract public class AbstractModel {
	static public final int UNIVERSAL_NOTIFICATION = 0;
	static protected final int LAST_NOTIFICATION_FLAG = 0;
	// TODO: 25.08.2015 refactor Observer/Observable
	private Handler uiHandler;
	ArrayList<AbstractView> listeners = new ArrayList<>();

	public AbstractModel(Handler uiHandler) {
		this.uiHandler = uiHandler;
	}

	public void addEventListener(@NonNull AbstractView view) {
		listeners.add(view);
	}

	public void notifyDataChanged(int notification) {
		notifyDataChanged(notification, null);
	}

	public void notifyDataChanged(final int notification, final Object data) {
		for (int i = 0; i < listeners.size(); i++) {
			final AbstractView view = listeners.get(i);
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					view.onDataUpdate(notification, data);
				}
			});
		}
	}

	public final Handler getUiHandler() {
		return uiHandler;
	}
}
