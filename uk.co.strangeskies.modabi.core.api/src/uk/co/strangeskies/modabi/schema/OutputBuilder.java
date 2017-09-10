package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface OutputBuilder<E extends NodeBuilder<?>>
    extends IOBuilder<ChildBindingPointBuilder<E>> {
  ValueExpression source();
}
