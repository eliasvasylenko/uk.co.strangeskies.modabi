package uk.co.strangeskies.modabi.io;

public class IOException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public IOException() {
	}

	public IOException(String message, Throwable cause) {
		super(message, cause);
	}

	public IOException(String message) {
		super(message);
	}

	public IOException(Throwable cause) {
		super(cause);
	}
}
