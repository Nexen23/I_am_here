package alex.imhere.util;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

public class UiRunnable implements Runnable {
	WeakReference<Runnable> taskRef;
	Handler uiHandler = new Handler(Looper.getMainLooper());

	public UiRunnable(@Nullable Runnable task) {
		this.taskRef = new WeakReference<>(task);
	}

	@Override
	public void run() {
		Runnable task = taskRef.get();
		if (task != null) {
			uiHandler.post(task);
		}
	}

	public void stopRunning() {
		Runnable task = taskRef.get();
		if (task != null) {
			uiHandler.removeCallbacks(task);
		}
	}
}
