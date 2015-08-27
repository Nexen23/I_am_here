package alex.imhere.view;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class UpdatingTimer {
	private static final long UPDATING_PERIOD_MS_DEFAULT = 500;
	// TODO: 27.08.2015 should I place it to Model?
	private TimerListener listener;
	private Handler uiHandler;

	private final long updatingPeriodMs;
	private final Timer timer = new Timer();
	private final TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			uiHandler.post( new Runnable() {
				@Override
				public void run() {
					listener.onTimerTick();
				}
			});
		}
	};

	public UpdatingTimer(Handler uiHandler, TimerListener listener) {
		this(uiHandler, listener, UPDATING_PERIOD_MS_DEFAULT);
	}

	public UpdatingTimer(Handler uiHandler, TimerListener listener, long updatingPeriodMs) {
		this.uiHandler = uiHandler;
		this.listener = listener;
		this.updatingPeriodMs = updatingPeriodMs;
	}

	public synchronized UpdatingTimer start() {
		timer.scheduleAtFixedRate(updateTask, 0, updatingPeriodMs);
		return this;
	}

	public synchronized UpdatingTimer stop() {
		updateTask.cancel();
		timer.purge();
		return this;
	}

	public interface TimerListener {
		void onTimerTick();
	}
}
