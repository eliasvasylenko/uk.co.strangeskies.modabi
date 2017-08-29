package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface OutputInitializerBuilder<T> extends IOBuilder {
  ValueExpression parent();

  ValueExpression outputObject();

  void expression(ValueExpression expression);
}
