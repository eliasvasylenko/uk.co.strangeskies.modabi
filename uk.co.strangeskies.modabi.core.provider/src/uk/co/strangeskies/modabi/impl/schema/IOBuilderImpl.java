package uk.co.strangeskies.modabi.impl.schema;

import static uk.co.strangeskies.modabi.schema.expression.Expressions.receiver;

import java.util.function.Function;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.IOBuilder;
import uk.co.strangeskies.modabi.schema.expression.ValueExpression;

public class IOBuilderImpl<T> implements IOBuilder<T> {
  private final Function<ValueExpression, T> endExpression;

  public IOBuilderImpl(Function<ValueExpression, T> endExpression) {
    this.endExpression = endExpression;
  }

  @Override
  public ValueExpression none() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValueExpression provide() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValueExpression context() {
    return receiver().getField("context"); // ProcessingContextImpl
  }

  @Override
  public ValueExpression binding(String string) {
    return context().invoke("bindings"); // TODO
  }

  @Override
  public ValueExpression binding(QualifiedName bindingPoint) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ValueExpression binding(ChildBindingPoint<?> bindingPoint) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public T expression(ValueExpression expression) {
    return endExpression.apply(expression);
  }
}
