package uk.co.strangeskies.modabi.impl.schema;

import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.InputInitializerBuilder;
import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public class InputInitializerBuilderImpl<E> extends IOBuilderImpl<NodeBuilder<E>>
    implements InputInitializerBuilder<E> {
  public InputInitializerBuilderImpl(Function<ValueExpression, NodeBuilder<E>> endExpression) {
    super(endExpression);
  }

  @Override
  public ValueExpression parent() {
    // TODO Auto-generated method stub
    return null;
  }
}
