package alex.imhere.container;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

class TemporaryElement<T> implements Comparable {
	protected T object = null;
	protected DateTime deathTime;

	public TemporaryElement(@NonNull T object, @NonNull DateTime deathTime) {
		this.deathTime = deathTime;
		this.object = object;
	}

	public TemporaryElement(@NonNull T object) {
		this(object, new DateTime(0));
	}

	@Override
	public boolean equals(@NonNull Object o) {
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
	public int compareTo(@NonNull Object another) {
		TemporaryElement a = this,
				b = (TemporaryElement) another;

		int datesComparisionResult = a.deathTime.compareTo(b.deathTime);
		int objectsComparisionResult = a.hashCode() - b.hashCode();
		return (datesComparisionResult != 0) ? datesComparisionResult : objectsComparisionResult;
	}
}
