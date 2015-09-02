package alex.imhere.exception;

public class ApiException extends Exception {
	public ApiException() {
		super();
	}

	public ApiException(String detailMessage) {
		super(detailMessage);
	}

	public ApiException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public ApiException(Throwable throwable) {
		super(throwable);
	}
}
