package alex.imhere.model;

import alex.imhere.service.Service;
import alex.imhere.util.Listenable;
import alex.imhere.util.Lifecycle;

public abstract class AbstractModel<TEventsListener extends AbstractModel.EventListener>
		extends Listenable<TEventsListener> implements Lifecycle {

	Service service;

	public AbstractModel(Service service) {
		this.service = service;
	}

	public interface EventListener extends Listenable.EventListener {
		void onModelDataChanged(AbstractModel abstractModel);
	}

	public interface ModelListener extends Lifecycle {
		void setModel(AbstractModel abstractModel);
	}
}
