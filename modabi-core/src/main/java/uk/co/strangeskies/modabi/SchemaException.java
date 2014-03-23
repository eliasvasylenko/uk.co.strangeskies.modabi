package uk.co.strangeskies.modabi;

public class SchemaException extends RuntimeException {
	public SchemaException() {
	}

	public SchemaException(String cause) {
		super(cause);
	}

	public SchemaException(Throwable e) {
		super(e);
	}

	public SchemaException(String cause, Throwable e) {
		super(cause, e);
	}

	private static final long serialVersionUID = 1L;
}
