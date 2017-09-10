package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface InputInitializerBuilder<E> extends IOBuilder<NodeBuilder<E>> {
  ValueExpression parent();
}
