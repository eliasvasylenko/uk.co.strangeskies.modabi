package uk.co.strangeskies.modabi.impl.schema;

import static uk.co.strangeskies.modabi.schema.expression.Expressions.receiver;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.IOBuilder;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public interface IOBuilderImpl extends IOBuilder {
  @Override
  default ValueExpression provide() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  default ValueExpression context() {
    return receiver(IOInterface.class).getField("context");
  }

  @Override
  default ValueExpression binding(String string) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  default ValueExpression binding(QualifiedName bindingPoint) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  default ValueExpression binding(ChildBindingPoint<?> bindingPoint) {
    // TODO Auto-generated method stub
    return null;
  }
}
