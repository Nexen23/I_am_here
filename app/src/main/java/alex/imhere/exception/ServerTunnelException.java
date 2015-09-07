package alex.imhere.exception;

public class ServerTunnelException extends Exception {
	public ServerTunnelException() {
		super();
	}

	public ServerTunnelException(String detailMessage) {
		super(detailMessage);
	}

	public ServerTunnelException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ServerTunnelException(Throwable throwable) {
		super(throwable);
	}
}
