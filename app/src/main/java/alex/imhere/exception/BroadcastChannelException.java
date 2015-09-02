package alex.imhere.exception;

public class BroadcastChannelException extends Exception {
	public BroadcastChannelException() {
		super();
	}

	public BroadcastChannelException(String detailMessage) {
		super(detailMessage);
	}

	public BroadcastChannelException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public BroadcastChannelException(Throwable throwable) {
		super(throwable);
	}
}
