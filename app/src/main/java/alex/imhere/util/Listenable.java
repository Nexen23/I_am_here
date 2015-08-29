package alex.imhere.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;
import java.util.WeakHashMap;

public abstract class Listenable<TEventListener extends Listenable.EventListener> {
	private WeakHashMap<TEventListener, Void> listeners = new WeakHashMap<>();

	public void addListener(@Nullable final TEventListener listener) {
		listeners.put(listener, null);
	}

	public void removeListener(@Nullable final TEventListener listener) {
		listeners.remove(listener);
	}

	public void clearListeners() {
		listeners.clear();
	}

	public void forEachListener(@NonNull final ListenerRunnable<TEventListener> listenerRunnable) {
		for (TEventListener listener : listeners.keySet()) {
			listenerRunnable.runWith(listener);
		}
	}

	public Set<TEventListener> getListenersSet() {
		return listeners.keySet();
	}

	public interface EventListener {
	}

	static public abstract class ListenerRunnable<TEventListener> implements Runnable {
		TEventListener listener;

		public void setListener(TEventListener listener) {
			this.listener = listener;
		}

		public TEventListener getListener() {
			return listener;
		}

		private void runWith(TEventListener listener) {
			setListener(listener);
			run();
		}
	}
}