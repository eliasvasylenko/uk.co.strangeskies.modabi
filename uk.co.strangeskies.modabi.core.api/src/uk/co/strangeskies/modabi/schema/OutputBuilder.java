package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface OutputBuilder extends IOBuilder {
  ValueExpression source();

  void expression(ValueExpression outputExpression);

  ValueExpression getExpression();
}
