package alex.imhere.model;

import alex.imhere.service.ServiceManager;
import alex.imhere.util.Listenable;
import alex.imhere.util.Resumable;

public abstract class AbstractModel<TEventsListener extends AbstractModel.EventListener>
		extends Listenable<TEventsListener> implements Resumable {

	ServiceManager serviceManager;

	public AbstractModel(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
	}

	public interface EventListener extends Listenable.EventListener {
		void onModelDataChanged(AbstractModel abstractModel);
	}

	public interface ModelListener extends Resumable {
		void setModel(AbstractModel abstractModel);
	}
}
