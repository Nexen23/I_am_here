package alex.imhere.activity.model;

import alex.imhere.util.Listenable;
import alex.imhere.util.ListeningController;

public abstract class BaseModel<TEventsListener extends BaseModel.EventListener> extends Listenable<TEventsListener> {

	public interface EventListener extends Listenable.EventListener {
	}

	public interface ModelListener extends ListeningController {
		void setModel(BaseModel baseModel);
	}
}
