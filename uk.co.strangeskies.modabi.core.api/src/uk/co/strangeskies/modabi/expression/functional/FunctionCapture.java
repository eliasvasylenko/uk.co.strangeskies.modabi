package uk.co.strangeskies.modabi.expression.functional;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface FunctionCapture<T, U> {
  FunctionImplementation<U> capture(T capture);

  BoundSet getBounds();

  TypeToken<T> getCaptureType();

  TypeToken<U> getFunctionType();

  TypeToken<U> getResolvedFunctionType();
}
