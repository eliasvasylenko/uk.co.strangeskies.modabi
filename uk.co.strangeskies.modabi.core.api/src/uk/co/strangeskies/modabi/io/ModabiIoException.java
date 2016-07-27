package uk.co.strangeskies.modabi.io;

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import java.util.function.Function;

import uk.co.strangeskies.text.properties.Localized;
import uk.co.strangeskies.text.properties.LocalizedRuntimeException;

public class ModabiIoException extends LocalizedRuntimeException {
	private static final long serialVersionUID = 1L;

	public ModabiIoException(Localized<String> message) {
		super(message);
	}

	public ModabiIoException(Localized<String> message, Throwable cause) {
		super(message, cause);
	}

	public ModabiIoException(Function<ModabiIoProperties, Localized<String>> messageFunction) {
		this(messageFunction.apply(getDefaultProperties(ModabiIoProperties.class)));
	}

	public ModabiIoException(Function<ModabiIoProperties, Localized<String>> messageFunction, Throwable cause) {
		this(messageFunction.apply(getDefaultProperties(ModabiIoProperties.class)), cause);
	}
}
