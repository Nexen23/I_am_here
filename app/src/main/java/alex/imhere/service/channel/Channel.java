package alex.imhere.service.channel;

import android.support.annotation.NonNull;

import alex.imhere.exception.BroadcastChannelException;
import alex.imhere.util.AbstractResumable;

public abstract class Channel extends AbstractResumable {
	private String name;
	protected Channel.EventListener listener;

	public void setListener(@NonNull Channel.EventListener listener) {
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

	public abstract void connect() throws BroadcastChannelException;
	public abstract void disconnect();

	public interface EventListener {
		void onConnect(String channel, String greeting);
		void onDisconnect(String channel, String reason);
		void onReconnect(String channel, String reason);
		void onMessageRecieve(String channel, String message, String timetoken);
		void onErrorOccur(String channel, String error);
	}
}
