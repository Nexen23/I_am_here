package alex.imhere.service.domain.channel;

import android.support.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ChannelException;
import alex.imhere.exception.ServerTunnelException;
import alex.imhere.service.domain.parser.JsonParser;
import alex.imhere.util.AbstractResumable;
import alex.imhere.util.Resumable;

public class ServerChannel extends AbstractResumable {
	Logger l = LoggerFactory.getLogger(ServerChannel.class);

	JsonParser jsonParser;
	Channel serverChannel;

	ServerChannel.EventListener listener;
	private final Channel.EventListener listenerAdapter = new Channel.EventListener() {
		@Override
		public void onConnect(String channel, String greeting) {

		}

		@Override
		public void onDisconnect(String channel, String reason) {
			if (listener != null && isResumed()) {
				listener.onDisconnect(reason);
			}
		}

		@Override
		public void onReconnect(String channel, String reason) {

		}

		@Override
		public void onMessageRecieve(String channel, String message, String timetoken) {
			if (listener != null && isResumed()) {
				ServerChannel.this.onMessageRecieve(message, timetoken);
			}
		}

		@Override
		public void onErrorOccur(String channel, String error) {
			l.warn(String.format("%s : [error] %s", channel, error));
			if (listener != null && isResumed()) {
				ServerChannel.this.disconnect();
			}
		}
	};

	public ServerChannel(Channel serverChannel, JsonParser jsonParser) {
		this.serverChannel = serverChannel;
		this.jsonParser = jsonParser;
	}

	public final void setListener(@NonNull ServerChannel.EventListener listener) {
		this.listener = listener;
	}

	public final void clearListener() {
		listener = null;
	}

	@Override
	public void pause() {
		super.pause();
		serverChannel.pause();
	}

	@Override
	public void resume() {
		super.resume();
		serverChannel.resume();
	}

	public final void connect() throws ServerTunnelException {
		try {
			serverChannel.setListener(listenerAdapter);
			serverChannel.connect();
		} catch (ChannelException e) {
			e.printStackTrace();
			serverChannel.clearListener();
			throw new ServerTunnelException(e);
		}
	}
	public final void disconnect() {
		serverChannel.disconnect();
		serverChannel.clearListener();
	}

	public void onMessageRecieve(String userJson, String timetoken) {
		DyingUser dyingUser = jsonParser.fromJson(userJson, DyingUser.class);
		if (dyingUser != null) {
			if (dyingUser.isAlive()) {
				listener.onUserLogin(dyingUser);
			} else {
				listener.onUserLogout(dyingUser);
			}
		}
	}

	public interface EventListener {
		void onDisconnect(String reason);
		void onUserLogin(DyingUser dyingUser);
		void onUserLogout(DyingUser dyingUser);
	}
}
