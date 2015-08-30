package alex.imhere.container;

import org.joda.time.Duration;
import org.joda.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import alex.imhere.util.listening.Listenable;

public class TemporarySet<TItem> extends Listenable<TemporarySet.EventListener> {
	// TODO: 18.08.2015 make sure it's thread-safe implementation
	protected SortedSet<TemporaryElement<TItem>> sortedElementsSet = new TreeSet<>();
	protected List<TItem> list = new ArrayList<>();

	protected final Timer timer = new Timer();
	protected TimerTask timerTask = null;
	protected TemporaryElement<TItem> nextElementToDie = null;

	public TemporarySet() {
		notifier = new TemporarySet.EventListener() {
			@Override
			public void onClear() {
				for (TemporarySet.EventListener listener : getListenersSet()) {
					listener.onClear();
				}
			}

			@Override
			public void onAdd(Object item) {
				for (TemporarySet.EventListener listener : getListenersSet()) {
					listener.onAdd(item);
				}
			}

			@Override
			public void onRemove(Object item) {
				for (TemporarySet.EventListener listener : getListenersSet()) {
					listener.onRemove(item);
				}
			}
		};
	}

	public boolean add(TItem object, LocalDateTime deathTime) {
		TemporaryElement<TItem> element = new TemporaryElement<>(object, deathTime);
		return _add(element);
	}

	public boolean remove(TItem object) {
		TemporaryElement<TItem> element = new TemporaryElement<>(object);
		return _remove(element);
	}

	public void clear() {
		_clear();
	}

	public final List<TItem> asReadonlyList() {
		return Collections.unmodifiableList(list);
	}

	private synchronized void _clear() {
		cancelNextDeath();
		list.clear();
		sortedElementsSet.clear();

		notifier.onClear();
	}


	private synchronized boolean _add(TemporaryElement<TItem> insertingElement) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasInserted = _insertElementUnique(insertingElement);

		if (wasInserted) {
			if (nextElementToDie != null &&
					nextElementToDie.deathTime.isAfter(insertingElement.deathTime)) {
				cancelNextDeath();
			}

			if (nextElementToDie == null) {
				openNextDeath();
			}

			notifier.onAdd(insertingElement.object);
		}

		return wasInserted;
	}

	private synchronized boolean _remove(TemporaryElement<TItem> deletingElement) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasDeleted = _deleteElementByObject(deletingElement);

		if (wasDeleted) {
			if (nextElementToDie.equals(deletingElement)) {
				cancelNextDeath();
				openNextDeath();
			}

			notifier.onRemove(deletingElement.object);
		}

		return wasDeleted;
	}

	private synchronized void openNextDeath() {
		if (sortedElementsSet.size() != 0) {
			nextElementToDie = sortedElementsSet.first();
			timerTask = new TimerTask() {
				@Override
				public void run() {
					_remove(nextElementToDie);
				}
			};


			LocalDateTime now = new LocalDateTime();
			Duration duration = new Duration(now.toDateTime(), nextElementToDie.deathTime.toDateTime());
			long lifetimeMillis = duration.getMillis();
			long delay = Math.max(0, lifetimeMillis);

			timer.schedule(timerTask, delay);
		}
	}

	private synchronized void cancelNextDeath() {
		if (timerTask != null) {
			timerTask.cancel();
		}
		timer.purge();
		nextElementToDie = null;
		timerTask = null;
	}

	private synchronized Iterator<TemporaryElement<TItem>> findElement(TemporaryElement<TItem> searchingElement) {
		Iterator<TemporaryElement<TItem>> resultIterator = null;
		for (Iterator<TemporaryElement<TItem>> iterator = sortedElementsSet.iterator(); iterator.hasNext() && resultIterator == null;) {
			if (iterator.next().equals(searchingElement)) {
				resultIterator = iterator;
			}
		}
		return resultIterator;
	}

	private synchronized boolean _insertElementUnique(TemporaryElement<TItem> element) {
		boolean wasInserted = false;

		Iterator<TemporaryElement<TItem>> iterator = findElement(element);
		if (iterator == null) {
			wasInserted = true;
			sortedElementsSet.add(element);
			list.add(element.object);
		}

		return wasInserted;
	}

	private synchronized boolean _deleteElementByObject(TemporaryElement<TItem> element) {
		boolean wasDeleted = false;

		Iterator<TemporaryElement<TItem>> iterator = findElement(element);
		if (iterator != null) {
			wasDeleted = true;
			iterator.remove();
			list.remove(element.object);
		}

		return wasDeleted;
	}

	interface EventListener extends Listenable.EventListener {
		void onClear();
		void onAdd(Object item);
		void onRemove(Object item);
	}
}