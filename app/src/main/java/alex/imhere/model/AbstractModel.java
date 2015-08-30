package alex.imhere.model;

import alex.imhere.service.Service;
import alex.imhere.util.listening.Listenable;
import alex.imhere.util.listening.ListeningLifecycle;

public abstract class AbstractModel<TEventsListener extends AbstractModel.EventListener>
		extends Listenable<TEventsListener> implements ListeningLifecycle {

	Service service;

	public AbstractModel(Service service) {
		this.service = service;
	}

	public interface EventListener extends Listenable.EventListener {
		void onModelDataChanged(AbstractModel abstractModel);
	}

	public interface ModelListener extends ListeningLifecycle {
		void setModel(AbstractModel abstractModel);
	}
}
