package uk.co.strangeskies.modabi.expression;

import static uk.co.strangeskies.text.properties.PropertyLoader.getDefaultProperties;

import uk.co.strangeskies.modabi.ModabiException;

public class ExpressionException extends ModabiException {
  private static final long serialVersionUID = 1L;

  public static final ExpressionExceptionProperties MESSAGES = getDefaultProperties(
      ExpressionExceptionProperties.class);

  public ExpressionException(String message) {
    super(message);
  }

  public ExpressionException(String message, Throwable cause) {
    super(message, cause);
  }
}
