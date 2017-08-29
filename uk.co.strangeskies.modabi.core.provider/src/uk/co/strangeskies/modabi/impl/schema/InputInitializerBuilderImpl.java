package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.schema.InputInitializerBuilder;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface InputInitializerBuilderImpl extends IOBuilderImpl, InputInitializerBuilder {
  @Override
  default ValueExpression none() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  default ValueExpression parent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  default void expression(ValueExpression expression) {
    // TODO Auto-generated method stub

  }
}
