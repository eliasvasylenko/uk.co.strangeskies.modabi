package uk.co.strangeskies.modabi.impl.schema;

import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.InputBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.expression.Expressions;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;
import uk.co.strangeskies.modabi.schema.expression.VariableExpression;

public class InputBuilderImpl<E extends NodeBuilder<?>>
    extends IOBuilderImpl<ChildBindingPointBuilder<E>> implements InputBuilder<E> {
  public InputBuilderImpl(Function<ValueExpression, ChildBindingPointBuilder<E>> endExpression) {
    super(endExpression);
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
}
