package alex.imhere.util;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

class TemporaryElement<T> {
	protected T object = null;
	protected DateTime deathTime;

	public TemporaryElement(@NonNull T object, @NonNull DateTime deathTime) {
		this.deathTime = deathTime;
		this.object = object;
	}

	protected TemporaryElement(@NonNull T object) {
		this(object, new DateTime(0));
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
}
