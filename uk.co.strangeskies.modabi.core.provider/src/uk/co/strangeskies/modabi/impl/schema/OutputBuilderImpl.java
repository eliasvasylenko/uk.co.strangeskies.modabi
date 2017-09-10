package uk.co.strangeskies.modabi.impl.schema;

import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.OutputBuilder;
import uk.co.strangeskies.modabi.schema.expression.Expressions;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public class OutputBuilderImpl<E extends NodeBuilder<?>>
    extends IOBuilderImpl<ChildBindingPointBuilder<E>> implements OutputBuilder<E> {
  public OutputBuilderImpl(Function<ValueExpression, ChildBindingPointBuilder<E>> endExpression) {
    super(endExpression);
  }

  @Override
  public ValueExpression source() {
    return Expressions.parameter(0);
  }
}
