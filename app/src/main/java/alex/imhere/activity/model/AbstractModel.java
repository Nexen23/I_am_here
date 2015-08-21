package alex.imhere.activity.model;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import alex.imhere.fragment.view.AbstractView;

abstract public class AbstractModel {
	private Handler uiHandler;
	ArrayList<AbstractView> listeners = new ArrayList<>();

	public AbstractModel(Handler uiHandler) {
		this.uiHandler = uiHandler;
	}

	public void addEventListener(@NonNull AbstractView view) {
		listeners.add(view);
	}

	public void notifyDataChanged() {
		for (int i = 0; i < listeners.size(); i++) {
			final AbstractView view = listeners.get(i);
			uiHandler.post(new Runnable() {
				@Override
				public void run() {
					view.onDataUpdate();
				}
			});
		}
	}

	public final Handler getUiHandler() {
		return uiHandler;
	}
}
