package uk.co.strangeskies.modabi.io;

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

public class ModabiIOException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public static final ModabiIOExceptionMessages MESSAGES = getDefaultProperties(
      ModabiIOExceptionMessages.class);

  public ModabiIOException(String message) {
    super(message);
  }

  public ModabiIOException(String message, Throwable cause) {
    super(message, cause);
  }
}
