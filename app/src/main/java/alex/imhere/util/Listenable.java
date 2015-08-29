package alex.imhere.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.WeakHashMap;

public class Listenable<TEventListener extends Listenable.EventListener> {
	WeakHashMap<TEventListener, Void> listeners = new WeakHashMap<>();

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

	public interface EventListener {
	}

	static public abstract class ListenerRunnable<TEventListener> implements Runnable {
		TEventListener listener;

		public void setListener(TEventListener listener) {
			this.listener = listener;
		}

		private void runWith(TEventListener listener) {
			setListener(listener);
			run();
		}
	}
}