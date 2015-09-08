package alex.imhere.util.wrapper;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

public class WeakUiRunnable implements Runnable {
	final WeakReference<Runnable> taskRef;
	final Handler uiHandler = new Handler(Looper.getMainLooper());

	public WeakUiRunnable(@Nullable Runnable task) {
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
