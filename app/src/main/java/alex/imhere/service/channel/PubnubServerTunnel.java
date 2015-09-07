package alex.imhere.service.channel;

import javax.inject.Inject;

import alex.imhere.entity.DyingUser;
import alex.imhere.exception.ServerTunnelException;
import alex.imhere.service.parser.JsonParser;

public class PubnubServerTunnel extends ServerTunnel {
	@Inject
	public PubnubServerTunnel(Channel serverChannel, JsonParser jsonParser) {
		super(serverChannel, jsonParser);
	}

	@Override
	public void onMessageRecieve(String message, String timetoken) {
		DyingUser dyingUser = jsonParser.fromJson(message, DyingUser.class);
		if (dyingUser != null) {
			if (dyingUser.isAlive()) {
				listener.onUserLogin(dyingUser);
			} else {
				listener.onUserLogout(dyingUser);
			}
		}
	}
}
