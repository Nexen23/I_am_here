package alex.imhere.util;

import android.support.annotation.NonNull;

import org.joda.time.LocalDateTime;

class TemporaryElement<T> implements Comparable {
	protected T object = null;
	protected LocalDateTime deathTime;

	public TemporaryElement(@NonNull T object, @NonNull LocalDateTime deathTime) {
		this.deathTime = deathTime;
		this.object = object;
	}

	public TemporaryElement(@NonNull T object) {
		this(object, new LocalDateTime(0));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TemporaryElement<?> that = (TemporaryElement<?>) o;

		return object.equals(that.object);
	}

	@Override
	public int hashCode() {
		return object.hashCode();
	}

	@Override
	public int compareTo(Object another) {
		TemporaryElement a = this,
				b = (TemporaryElement) another;

		int datesComparisionResult = a.deathTime.compareTo(b.deathTime);
		int objectsComparisionResult = a.hashCode() - b.hashCode();
		return (datesComparisionResult != 0) ? datesComparisionResult : objectsComparisionResult;
	}
}
