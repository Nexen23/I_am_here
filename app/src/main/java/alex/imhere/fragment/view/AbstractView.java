package alex.imhere.fragment.view;

import android.support.annotation.NonNull;

import alex.imhere.activity.model.AbstractModel;

public interface AbstractView {
	void setModel(AbstractModel abstractModel);
	void onDataUpdate(final int notification, @NonNull final Object data);
}
