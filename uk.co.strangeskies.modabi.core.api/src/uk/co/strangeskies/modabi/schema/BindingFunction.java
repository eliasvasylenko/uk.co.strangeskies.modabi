package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.expression.Expression;

public interface BindingFunction {
  /**
   * @return the expression from which this function was compiled
   */
  Expression getExpression();

  void apply(BindingContext context);
}
