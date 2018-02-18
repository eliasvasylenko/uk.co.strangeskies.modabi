package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.expression.Expression;
import uk.co.strangeskies.reflection.token.TypeToken;

public interface BindingFunction {
  TypeToken<?> getTypeBefore();

  TypeToken<?> getTypeAfter();

  /**
   * @return the expression from which this function was compiled
   */
  Expression getExpression();

  void apply(BindingContext context);
}
