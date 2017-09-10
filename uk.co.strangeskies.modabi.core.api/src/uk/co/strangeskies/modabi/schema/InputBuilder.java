package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.modabi.schema.expression.VariableExpression;

public interface InputBuilder<E extends NodeBuilder<?>>
    extends IOBuilder<ChildBindingPointBuilder<E>> {
  ValueExpression result();

  VariableExpression target();
}
