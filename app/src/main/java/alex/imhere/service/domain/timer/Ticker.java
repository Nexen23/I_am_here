package alex.imhere.service.domain.timer;

import android.os.Handler;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

import alex.imhere.util.Listenable;

public class Ticker extends Listenable<Ticker.EventListener> {
	private static final long TICKING_PERIOD_MS_DEFAULT = 100;
	long tickingPeriodMs;

	Handler uiHandler = new Handler(Looper.getMainLooper());
	final Timer tickingTimer = new Timer();
	TimerTask tickingTask;

	public Ticker() {
		this(TICKING_PERIOD_MS_DEFAULT);
	}

	public Ticker(final long tickingPeriodMs) {
		setTickingPeriodMs(tickingPeriodMs);
	}

	public void setTickingPeriodMs(final long tickingPeriodMs) {
		this.tickingPeriodMs = tickingPeriodMs;
	}

	public synchronized void start() {
		if (tickingTask != null) {
			stop();
		}

		tickingTask = new TimerTask() {
			@Override
			public void run() {
				uiHandler.post(new Runnable() {
					@Override
					public void run() {
						forEachListener(new ListenerExecutor<Ticker.EventListener>() {
							@Override
							public void run() {
								getListener().onTick();
							}
						});
					}
				});
			}
		};
		tickingTimer.scheduleAtFixedRate(tickingTask, 0, tickingPeriodMs);
	}

	public synchronized void stop() {
		if (tickingTask != null) {
			tickingTask.cancel();
		}
		tickingTask = null;
		tickingTimer.purge();
	}

	public interface EventListener extends Listenable.EventListener {
		void onTick();
	}
}
