package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.schema.InputBuilder;
import uk.co.strangeskies.modabi.schema.expression.Expressions;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.modabi.schema.expression.VariableExpression;

public class InputBuilderImpl implements IOBuilderImpl, InputBuilder {
  @Override
  public ValueExpression none() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValueExpression result() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public VariableExpression target() {
    return Expressions.parameter(0);
  }

  @Override
  public void expression(ValueExpression inputExpression) {
    // TODO Auto-generated method stub

  }

  @Override
  public ValueExpression getExpression() {
    // TODO Auto-generated method stub
    return null;
  }
}
