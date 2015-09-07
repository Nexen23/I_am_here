package alex.imhere.service.channel;

import android.support.annotation.NonNull;

import alex.imhere.exception.ChannelException;
import alex.imhere.util.AbstractResumable;

public abstract class Channel extends AbstractResumable {
	private String name;
	protected Channel.EventListener listener;

	public final void setListener(@NonNull Channel.EventListener listener) {
		this.listener = listener;
	}

	public final void clearListener() {
		listener = null;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final String getName() {
		return name;
	}

	public abstract void connect() throws ChannelException;
	public abstract void disconnect();

	public interface EventListener {
		void onConnect(String channel, String greeting);
		void onDisconnect(String channel, String reason);
		void onReconnect(String channel, String reason);
		void onMessageRecieve(String channel, String message, String timetoken);
		void onErrorOccur(String channel, String error);
	}
}
