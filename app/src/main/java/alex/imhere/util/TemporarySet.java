package alex.imhere.util;

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

public class TemporarySet<T> extends ListObservable {
	// TODO: 18.08.2015 make sure it's thread-safe implementation
	protected SortedSet<TemporaryElement<T>> sortedElementsSet = new TreeSet<>();
	protected List<T> list = new ArrayList<>();

	protected final Timer timer = new Timer();
	protected TimerTask timerTask = null;
	protected TemporaryElement<T> nextElementToDie = null;

	public boolean add(T object, LocalDateTime deathTime) {
		TemporaryElement<T> element = new TemporaryElement<>(object, deathTime);
		return _add(element);
	}

	public boolean remove(T object) {
		TemporaryElement<T> element = new TemporaryElement<>(object);
		return _remove(element);
	}

	public void clear() {
		_clear();
	}

	public final List<T> asReadonlyList() {
		return Collections.unmodifiableList(list);
	}

	private synchronized void _clear() {
		cancelNextDeath();
		list.clear();
		sortedElementsSet.clear();

		notifyCollectionChanged(Notification.CLEAR, null);
	}


	private synchronized boolean _add(TemporaryElement<T> isertingElement) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasInserted = _insertElementUnique(isertingElement);

		if (wasInserted) {
			if (nextElementToDie != null &&
					nextElementToDie.deathTime.isAfter(isertingElement.deathTime)) {
				cancelNextDeath();
			}

			if (nextElementToDie == null) {
				openNextDeath();
			}

			notifyCollectionChanged(Notification.ADD, isertingElement.object);
		}

		return wasInserted;
	}

	private synchronized boolean _remove(TemporaryElement<T> deletingElement) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasDeleted = _deleteElementByObject(deletingElement);

		if (wasDeleted) {
			if (nextElementToDie.equals(deletingElement)) {
				cancelNextDeath();
				openNextDeath();
			}

			notifyCollectionChanged(Notification.REMOVE, deletingElement.object);
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

	private synchronized Iterator<TemporaryElement<T>> findElement(TemporaryElement<T> searchingElement) {
		Iterator<TemporaryElement<T>> resultIterator = null;
		for (Iterator<TemporaryElement<T>> iterator = sortedElementsSet.iterator(); iterator.hasNext() && resultIterator == null;) {
			if (iterator.next().equals(searchingElement)) {
				resultIterator = iterator;
			}
		}
		return resultIterator;
	}

	private synchronized boolean _insertElementUnique(TemporaryElement<T> element) {
		boolean wasInserted = false;

		Iterator<TemporaryElement<T>> iterator = findElement(element);
		if (iterator == null) {
			wasInserted = true;
			sortedElementsSet.add(element);
			list.add(element.object);
		}

		return wasInserted;
	}

	private synchronized boolean _deleteElementByObject(TemporaryElement<T> element) {
		boolean wasDeleted = false;

		Iterator<TemporaryElement<T>> iterator = findElement(element);
		if (iterator != null) {
			wasDeleted = true;
			iterator.remove();
			list.remove(element.object);
		}

		return wasDeleted;
	}
}
