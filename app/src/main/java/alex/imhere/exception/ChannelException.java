package alex.imhere.exception;

public class ChannelException extends Exception {
	static public final String
			CONNECT_ERROR = "cannot connect";

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

	@Override
	public String getMessage() {
		return String.format("[%s]", super.getMessage());
	}
}
