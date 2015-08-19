package alex.imhere.fragment.view;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class UiRunnable implements Runnable {
	private Handler uiHandler;
	private Runnable innerRunnable;

	public UiRunnable(@NonNull Handler uiHandler, @Nullable Runnable innerRunnable) {
		this.uiHandler = uiHandler;
		this.innerRunnable = innerRunnable;
	}

	@Override
	public void run() {
		if (uiHandler != null && innerRunnable != null) {
			uiHandler.post(innerRunnable);
		}
	}
}
