package uk.co.strangeskies.modabi.expression;

import java.util.function.Function;

import uk.co.strangeskies.reflection.token.TypeToken;

/**
 * @author Elias N Vasylenko
 */
public interface FunctionalExpressionCompiler {
  /**
   * @param implementationType
   *          a functional interface to be implemented according to the expression
   * @param expression
   *          the expression describing the desired behavior
   * @return an implementation of the interface of the given type, implemented
   *         according to the given expression
   */
  <T> T compile(Expression expression, TypeToken<T> implementationType);

  <T, C> Function<C, T> compile(
      Expression expression,
      TypeToken<T> implementationType,
      TypeToken<C> captureScope);
}
