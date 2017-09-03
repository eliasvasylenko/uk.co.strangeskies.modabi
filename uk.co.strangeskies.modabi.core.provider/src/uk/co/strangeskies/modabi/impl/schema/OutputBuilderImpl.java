package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.schema.OutputBuilder;
import uk.co.strangeskies.modabi.schema.expression.Expressions;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public class OutputBuilderImpl implements IOBuilderImpl, OutputBuilder {
  @Override
  public ValueExpression none() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValueExpression source() {
    return Expressions.parameter(0);
  }

  @Override
  public void expression(ValueExpression outputExpression) {
    // TODO Auto-generated method stub

  }

  @Override
  public ValueExpression getExpression() {
    // TODO Auto-generated method stub
    return null;
  }
}
