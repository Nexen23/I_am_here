package alex.imhere.exception;

public class ApiException extends Exception {
	static public final String
			LOGIN_ERROR = "cannot login",
			TIMEOUT_ERROR = "server doesn\'t response",
			GET_USERS_ERROR = "cannot get online users",
			GET_DATE_ERROR = "cannot get now date";

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
