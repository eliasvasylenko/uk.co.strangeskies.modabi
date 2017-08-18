package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface OutputConfigurator<T> extends IOConfigurator {
  <U> ValueExpression<U> source();

  /**
   * Create an expression over a number of iterable items. The returned expression
   * conceptually represents the assigned variable in a for each loop over an
   * iterable.
   * 
   * <p>
   * In the case that a result of invocation of this method is passed to
   * {@link #expression(ValueExpression)}, the output items will be iterated over
   * accordingly.
   * 
   * <p>
   * Expressions returned from this method may then be mentioned by expressions
   * passed to this method, creating nested iterations. Conceptually these will
   * correspond to nested for loops in the output logic.
   * 
   * @param values
   *          an expression over an iterable object instance
   * @return an inner-loop expression over the items of an iterable object
   *         instance
   */
  <U> ValueExpression<U> iterate(ValueExpression<? extends Iterable<U>> values);

  void expression(ValueExpression<? extends T> outputExpression);

  ValueExpression<? extends T> getExpression();
}
