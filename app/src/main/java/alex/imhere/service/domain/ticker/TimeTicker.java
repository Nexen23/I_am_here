package alex.imhere.service.domain.ticker;

import android.os.Handler;
import android.os.Looper;

import java.util.Timer;
import java.util.TimerTask;

import alex.imhere.util.WeakListenable;

public class TimeTicker extends WeakListenable<TimeTicker.EventListener> {
	private static final long TICKING_PERIOD_MS_DEFAULT = 1000;
	private static final boolean DO_INSTANT_TICK_ON_START_DEFAULT = true;
	long tickingPeriodMs;
	boolean doInstantTickOnStart;

	final Handler uiHandler = new Handler(Looper.getMainLooper());
	final Timer tickingTimer = new Timer();
	TimerTask tickingTask;

	public TimeTicker() {
		this(DO_INSTANT_TICK_ON_START_DEFAULT);
	}

	public TimeTicker(boolean doInstantTickOnStart) {
		this.doInstantTickOnStart = doInstantTickOnStart;
		setTickingPeriodMs(TICKING_PERIOD_MS_DEFAULT);
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
						forEachListener(new ListenerExecutor<TimeTicker.EventListener>() {
							@Override
							public void run() {
								getListener().onSecondTick();
							}
						});
					}
				});
			}
		};

		long delay = (doInstantTickOnStart) ? 0 : tickingPeriodMs;
		tickingTimer.scheduleAtFixedRate(tickingTask, delay, tickingPeriodMs);
	}

	public synchronized void stop() {
		if (tickingTask != null) {
			tickingTask.cancel();
		}
		tickingTask = null;
		tickingTimer.purge();
	}

	public interface EventListener extends WeakListenable.EventListener {
		void onSecondTick();
	}

	public interface Owner {
		TimeTicker getTimeTicker();
	}
}
