package alex.imhere.model;

import alex.imhere.util.listening.Listenable;
import alex.imhere.util.listening.ListeningLifecycle;

public abstract class BaseModel<TEventsListener extends BaseModel.EventListener> extends Listenable<TEventsListener> {

	public interface EventListener extends Listenable.EventListener {
	}

	public interface ModelListener extends ListeningLifecycle {
		void setModel(BaseModel baseModel);
	}
}
