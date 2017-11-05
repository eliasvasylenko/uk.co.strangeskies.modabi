package uk.co.strangeskies.modabi.expression;

public interface CaptureFunction<T, U> {
  U capture(T capture);
}
