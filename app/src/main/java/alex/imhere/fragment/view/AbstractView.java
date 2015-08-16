package alex.imhere.fragment.view;

import alex.imhere.activity.model.AbstractModel;

public interface AbstractView {
	void setModel(AbstractModel model);
	void onDataUpdate();
}
