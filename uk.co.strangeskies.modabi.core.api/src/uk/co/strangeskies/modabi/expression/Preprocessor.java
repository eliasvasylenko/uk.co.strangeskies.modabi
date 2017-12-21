package uk.co.strangeskies.modabi.expression;

import java.util.List;

/**
 * This interface can be implemented to define preprocessor behavior for
 * expression compilation, acting as a simple macro preprocessor.
 * <p>
 * All unqualified variables and function invocations are matched against
 * {@link #process(String)} and {@link #process(String, List)} respectively,
 * which may optionally expand them to complex expressions. The preprocessor is
 * then applied recursively to the results of these expansions.
 * 
 * @author Elias N Vasylenko
 */
public interface Preprocessor {
  /**
   * Optionally expand the given unqualified variable to an expression.
   * 
   * @param name
   *          the name of the unqualified variable
   * @return the expression to expand the variable to, or null to leave the
   *         variable unmodified
   */
  Expression process(String name);

  /**
   * Optionally expand the given unqualified function invocation to an expression.
   * 
   * @param name
   *          the name of the unqualified function
   * @param arguments
   *          the invocation arguments
   * @return the expression to expand the invocation to, or null to leave the
   *         invocation unmodified
   */
  Expression process(String name, List<Expression> arguments);
}
