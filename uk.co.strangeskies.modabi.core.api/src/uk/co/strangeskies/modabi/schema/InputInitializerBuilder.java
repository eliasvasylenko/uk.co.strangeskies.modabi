package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface InputInitializerBuilder extends IOBuilder {
  ValueExpression parent();

  void expression(ValueExpression expression);
}
