package uk.co.strangeskies.modabi.io;

import uk.co.strangeskies.utilities.text.LocalizedString;

public class IllegalStructureException extends ModabiIoException {
	private static final long serialVersionUID = 1L;

	public IllegalStructureException(LocalizedString message, Throwable cause) {
		super(message, cause);
	}

	public IllegalStructureException(LocalizedString message) {
		super(message);
	}
}
