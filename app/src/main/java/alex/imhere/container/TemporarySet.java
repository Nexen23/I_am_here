package alex.imhere.container;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import alex.imhere.util.WeakListenable;
import alex.imhere.util.Resumable;
import alex.imhere.util.time.TimeUtils;

public class TemporarySet<TItem> extends WeakListenable<TemporarySet.EventListener> implements Resumable {
	protected final SortedSet<TemporaryElement<TItem>> sortedElementsSet = new TreeSet<>();
	protected final List<TItem> list = new ArrayList<>();

	protected final Timer timer = new Timer();
	protected TimerTask timerTask = null;
	protected TemporaryElement<TItem> nextElementToDie = null;

	boolean isResumed = false;

	public TemporarySet() {
		notifier = new TemporarySet.EventListener() {
			@Override
			public void onCleared() {
				for (TemporarySet.EventListener listener : getListenersSet()) {
					listener.onCleared();
				}
			}

			@Override
			public void onAdded(Object item) {
				for (TemporarySet.EventListener listener : getListenersSet()) {
					listener.onAdded(item);
				}
			}

			@Override
			public void onRemoved(Object item) {
				for (TemporarySet.EventListener listener : getListenersSet()) {
					listener.onRemoved(item);
				}
			}
		};
	}

	public boolean add(TItem object, DateTime deathTime) {
		TemporaryElement<TItem> element = new TemporaryElement<>(object, deathTime);
		return _add(element);
	}

	public boolean remove(TItem object) {
		TemporaryElement<TItem> element = new TemporaryElement<>(object);
		return _remove(element);
	}

	public boolean killNextElementToDie() {
		return _remove(nextElementToDie);
	}

	public int size() {
		return sortedElementsSet.size();
	}

	public TItem getNextElementToDie() {
		return nextElementToDie.object;
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

		notifier.onCleared();
	}


	private synchronized boolean _add(TemporaryElement<TItem> insertingElement) {
		boolean wasInserted = _insertElementUnique(insertingElement);

		if (wasInserted) {
			if (nextElementToDie != null &&
					nextElementToDie.deathTime.isAfter(insertingElement.deathTime)) {
				cancelNextDeath();
			}

			if (nextElementToDie == null) {
				openNextDeath();
			}

			notifier.onAdded(insertingElement.object);
		}

		return wasInserted;
	}

	private synchronized boolean _remove(TemporaryElement<TItem> deletingElement) {
		boolean wasDeleted = _deleteElementByObject(deletingElement);

		if (wasDeleted) {
			if (nextElementToDie.equals(deletingElement)) {
				cancelNextDeath();
				openNextDeath();
			}

			notifier.onRemoved(deletingElement.object);
		}

		return wasDeleted;
	}

	private synchronized void openNextDeath() {
		cancelNextDeath();
		if (sortedElementsSet.size() != 0) {
			nextElementToDie = sortedElementsSet.first();
			timerTask = new TimerTask() {
				@Override
				public void run() {
					killNextElementToDie();
				}
			};

			DateTime now = new DateTime();
			Duration duration = TimeUtils.GetNonNegativeDuration(now, nextElementToDie.deathTime);

			timer.schedule(timerTask, duration.getMillis());
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

	@Override
	public void resume() {
		isResumed = true;
		openNextDeath();
	}

	@Override
	public void pause() {
		cancelNextDeath();
		isResumed = false;
	}

	@Override
	public boolean isResumed() {
		return isResumed;
	}

	public interface EventListener extends WeakListenable.EventListener {
		void onCleared();
		void onAdded(Object item);
		void onRemoved(Object item);
	}
}