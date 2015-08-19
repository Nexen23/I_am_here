package alex.imhere.fragment.view;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class UpdatingViewTimer {
	private AbstractView view;
	private Handler uiHandler;

	private final long updatingPeriodMs = 500;
	private final Timer timer = new Timer();
	private final TimerTask updateTask = new TimerTask() {
		@Override
		public void run() {
			uiHandler.post( new Runnable() {
				@Override
				public void run() {
					view.onDataUpdate();
				}
			});
		}
	};

	public UpdatingViewTimer(Handler uiHandler, AbstractView view) {
		this.uiHandler = uiHandler;
		this.view = view;
	}

	public synchronized void start() {
		timer.scheduleAtFixedRate(updateTask, 0, updatingPeriodMs);
	}

	public synchronized void stop() {
		updateTask.cancel();
		timer.purge();
	}
}
