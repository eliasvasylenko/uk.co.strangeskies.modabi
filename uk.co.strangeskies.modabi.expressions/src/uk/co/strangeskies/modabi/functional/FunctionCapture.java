package uk.co.strangeskies.modabi.functional;

import uk.co.strangeskies.reflection.token.TypeToken;

public interface FunctionCapture<T, U> {
  FunctionImplementation<U> capture(T capture);

  TypeToken<T> getCaptureType();

  TypeToken<U> getFunctionType();

  TypeToken<U> getResolvedFunctionType();
}
