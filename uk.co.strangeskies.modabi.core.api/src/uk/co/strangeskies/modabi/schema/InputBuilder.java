package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.modabi.schema.expression.VariableExpression;

public interface InputBuilder extends IOBuilder {
  ValueExpression result();

  VariableExpression target();

  void expression(ValueExpression inputExpression);

  ValueExpression getExpression();
}
