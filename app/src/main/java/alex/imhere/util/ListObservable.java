package alex.imhere.util;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Observable;

public class ListObservable<T> extends Observable {
	public enum Notification {
		UNIVERSAL,

		CLEAR,

		ADD,
		REMOVE
	};

	protected void notifyCollectionChanged(@NonNull Notification notification, @Nullable T data) {
		setChanged();
		notifyObservers(new NotificationData(notification, data));
	}

	public class NotificationData {
		public Notification notification;
		public T data;

		public NotificationData(Notification notification, T data) {
			this.notification = notification;
			this.data = data;
		}
	}
}
