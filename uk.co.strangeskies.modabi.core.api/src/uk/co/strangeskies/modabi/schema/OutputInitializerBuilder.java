package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface OutputInitializerBuilder extends IOBuilder {
  ValueExpression parent();

  ValueExpression outputObject();

  void expression(ValueExpression expression);
}
