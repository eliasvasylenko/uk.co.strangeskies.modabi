package uk.co.strangeskies.modabi.expression;

public interface Scope<T> {
  Capture capture(T scopeInstance);
}
