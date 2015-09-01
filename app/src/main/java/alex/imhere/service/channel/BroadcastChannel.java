package alex.imhere.service.channel;

import android.support.annotation.NonNull;

import alex.imhere.util.AbstractResumable;

public abstract class BroadcastChannel extends AbstractResumable {
	private String name;
	protected BroadcastChannel.EventListener listener;

	public void setListener(@NonNull BroadcastChannel.EventListener listener) {
		this.listener = listener;
	}

	public EventListener getListener() {
		return listener;
	}

	public void clearListener() {
		listener = null;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract void connect() throws BroadcastChannel.Exception;
	public abstract void disconnect();

	public interface EventListener {
		void onConnect(String channel, String greeting);
		void onDisconnect(String channel, String reason);
		void onReconnect(String channel, String reason);
		void onMessageRecieve(String channel, String message, String timetoken);
		void onErrorOccur(String channel, String error);
	}

	static public class Exception extends java.lang.Exception {
		public Exception() {
			super();
		}

		public Exception(String detailMessage) {
			super(detailMessage);
		}

		public Exception(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public Exception(Throwable throwable) {
			super(throwable);
		}
	}
}
