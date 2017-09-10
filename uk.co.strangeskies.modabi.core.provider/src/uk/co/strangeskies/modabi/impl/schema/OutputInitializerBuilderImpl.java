package uk.co.strangeskies.modabi.impl.schema;

import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.NodeBuilder;
import uk.co.strangeskies.modabi.schema.OutputInitializerBuilder;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public class OutputInitializerBuilderImpl<E> extends IOBuilderImpl<NodeBuilder<E>>
    implements OutputInitializerBuilder<E> {
  public OutputInitializerBuilderImpl(Function<ValueExpression, NodeBuilder<E>> endExpression) {
    super(endExpression);
  }

  @Override
  public ValueExpression parent() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValueExpression outputObject() {
    // TODO Auto-generated method stub
    return null;
  }
}
