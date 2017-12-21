package uk.co.strangeskies.modabi.expression;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface CaptureFunction<T, U> {
  U capture(T capture);

  TypeToken<U> getExactType();
}
