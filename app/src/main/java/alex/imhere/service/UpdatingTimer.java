package alex.imhere.service;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class UpdatingTimer {
	private static final long UPDATING_PERIOD_MS_DEFAULT = 500;
	// TODO: 27.08.2015 should I place it to Model? Or to ServiceManager.GlobalTimer? Or model listen to GlobalTimer!
	WeakReference<TimerListener> listenerRef;
	Handler uiHandler = new Handler(Looper.getMainLooper());

	long updatingPeriodMs;
	final Timer timer = new Timer();
	TimerTask updateTask;

	public UpdatingTimer() {
		this(null, UPDATING_PERIOD_MS_DEFAULT);
	}

	public UpdatingTimer(@Nullable TimerListener listener) {
		this(listener, UPDATING_PERIOD_MS_DEFAULT);
	}

	public UpdatingTimer(@Nullable TimerListener listener, final long updatingPeriodMs) {
		setListener(listener);
		setUpdatingPeriodMs(updatingPeriodMs);
	}

	public void setListener(@Nullable TimerListener listener) {
		this.listenerRef = new WeakReference<>(listener);
	}

	public void setUpdatingPeriodMs(final long updatingPeriodMs) {
		this.updatingPeriodMs = updatingPeriodMs;
	}

	public synchronized void start() {
		if (updateTask != null) {
			stop();
		}
		updateTask = new TimerTask() {
			@Override
			public void run() {
				uiHandler.post(new Runnable() {
					@Override
					public void run() {
						TimerListener listener = listenerRef.get();
						if (listener != null) {
							listener.onTimerTick();
						} else {
							stop();
						}
					}
				});
			}
		};
		timer.scheduleAtFixedRate(updateTask, 0, updatingPeriodMs);
	}

	public synchronized void stop() {
		updateTask.cancel();
		updateTask = null;
		timer.purge();
	}

	public interface TimerListener {
		void onTimerTick();
	}
}
