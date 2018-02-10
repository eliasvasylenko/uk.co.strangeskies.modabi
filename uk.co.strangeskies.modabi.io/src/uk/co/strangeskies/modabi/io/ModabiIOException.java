package uk.co.strangeskies.modabi.io;

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import uk.co.strangeskies.modabi.ModabiException;

public class ModabiIOException extends ModabiException {
  private static final long serialVersionUID = 1L;

  public static final ModabiIOExceptionProperties MESSAGES = getDefaultProperties(
      ModabiIOExceptionProperties.class);

  public ModabiIOException(String message) {
    super(message);
  }

  public ModabiIOException(String message, Throwable cause) {
    super(message, cause);
  }
}
