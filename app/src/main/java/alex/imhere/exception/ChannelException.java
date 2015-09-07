package alex.imhere.exception;

public class ChannelException extends Exception {
	public ChannelException() {
		super();
	}

	public ChannelException(String detailMessage) {
		super(detailMessage);
	}

	public ChannelException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ChannelException(Throwable throwable) {
		super(throwable);
	}
}
