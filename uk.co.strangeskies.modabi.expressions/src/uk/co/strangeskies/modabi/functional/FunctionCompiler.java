package uk.co.strangeskies.modabi.functional;

import static uk.co.strangeskies.reflection.token.TypeToken.forClass;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.modabi.expression.Preprocessor;
import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * This interface specifies a compilation strategy from an {@link Expression} to
 * a functional interface which implementations must conform to.
 * <p>
 * The supplier capture type should always a proper type, and implementations
 * should throw if this is not the case.
 * <p>
 * If the capture type is generic and parameterized with unbounded wild-cards
 * the wildcards should be replaced with the type variables themselves. If any
 * of the wildcards are bounded, they should all be replaced with their
 * captures. This ensures type safety for any instance of the capture type which
 * is signature compatible.
 * <p>
 * The implementation type should be a {@link FunctionalInterface functional
 * interface}. If it is a parameterized type, it may contain inference
 * variables. The inference behavior in this case should be roughly equivalent
 * to the inference of the exact type of a lambda expression which is part of a
 * compound expression as per the Java language specification.
 * 
 * @author Elias N Vasylenko
 */
public interface FunctionCompiler {
  /**
   * @param implementationType
   *          a functional interface to be implemented according to the expression
   * @param expression
   *          the expression describing the desired behavior
   * @return an implementation of the interface of the given type, implemented
   *         according to the given expression
   */
  default <T> FunctionImplementation<T> compile(
      Expression expression,
      TypeToken<T> implementationType) {
    return compile(expression, implementationType, forClass(void.class)).capture(null);
  }

  default <T, C> FunctionCapture<C, T> compile(
      Expression expression,
      TypeToken<T> implementationType,
      TypeToken<C> captureScope) {
    return compile(expression, s -> s, implementationType, captureScope);
  }

  <T, C> FunctionCapture<C, T> compile(
      Expression expression,
      Preprocessor preprocessor,
      TypeToken<T> implementationType,
      TypeToken<C> captureScope);
}
