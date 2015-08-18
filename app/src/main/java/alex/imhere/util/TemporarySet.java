package alex.imhere.util;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.Timer;

public class TemporarySet<T extends Object> {
	// TODO: 18.08.2015 make sure it's thread-safe implementation
	protected SortedSet<TemporaryElement<T>> sortedElementsSet;
	protected ArrayList<T> list;
	protected Timer timer = new Timer();

	public boolean add(T object, DateTime deathTime) {
		TemporaryElement<T> element = new TemporaryElement<>(object, deathTime);
		return _add(element);
	}

	public boolean remove(T object) {
		TemporaryElement<T> element = new TemporaryElement<>(object);
		return _remove(element);
	}

	public final List<T> asReadonlyList() {
		return Collections.unmodifiableList(list);
	}

	private synchronized boolean _add(TemporaryElement<T> element) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasAdded = sortedElementsSet.add(element);

		if (wasAdded) {
			list.add(0, element.object);
		}

		return wasAdded;
	}

	private synchronized boolean _remove(TemporaryElement<T> element) {
		// TODO: 18.08.2015 implement unique add to List
		boolean wasRemoved = sortedElementsSet.remove(element);

		if (wasRemoved) {
			list.remove(element.object);
		}

		return wasRemoved;
	}
}
