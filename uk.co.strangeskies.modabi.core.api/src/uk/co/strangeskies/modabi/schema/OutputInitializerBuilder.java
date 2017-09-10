package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface OutputInitializerBuilder<E> extends IOBuilder<NodeBuilder<E>> {
  ValueExpression parent();

  ValueExpression outputObject();
}
