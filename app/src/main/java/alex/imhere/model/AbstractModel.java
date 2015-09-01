package alex.imhere.model;

import alex.imhere.service.Service;
import alex.imhere.util.Listenable;
import alex.imhere.util.Resumable;

public abstract class AbstractModel<TEventsListener extends AbstractModel.EventListener>
		extends Listenable<TEventsListener> implements Resumable {

	Service service;

	public AbstractModel(Service service) {
		this.service = service;
	}

	public interface EventListener extends Listenable.EventListener {
		void onModelDataChanged(AbstractModel abstractModel);
	}

	public interface ModelListener extends Resumable {
		void setModel(AbstractModel abstractModel);
	}
}
