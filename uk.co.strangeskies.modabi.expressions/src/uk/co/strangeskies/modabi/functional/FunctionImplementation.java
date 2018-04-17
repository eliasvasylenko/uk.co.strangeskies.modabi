package uk.co.strangeskies.modabi.functional;

import uk.co.strangeskies.reflection.BoundSet;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface FunctionImplementation<T> {
  T getInstance();

  BoundSet getBounds();

  TypeToken<T> getResolvedFunctionType();

  TypeToken<T> getFunctionType();
}
