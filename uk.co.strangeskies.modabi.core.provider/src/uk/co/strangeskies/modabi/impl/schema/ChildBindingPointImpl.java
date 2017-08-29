package uk.co.strangeskies.modabi.impl.schema;

import java.util.Objects;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointImpl<T> implements ChildBindingPoint<T> {
  private final QualifiedName name;
  private final boolean ordered;
  private final BindingCondition<? super T> condition;

  @SuppressWarnings("unchecked")
  protected ChildBindingPointImpl(ChildBindingPointBuilderImpl<T, ?> configurator) {
    name = configurator
        .overrideChildren(ChildBindingPoint::name, ChildBindingPointBuilder::getName)
        .validateOverride(Objects::equals)
        .get();

    ordered = configurator
        .overrideChildren(ChildBindingPoint::ordered, ChildBindingPointBuilder::getOrdered)
        .validateOverride((a, b) -> a || !b)
        .get();

    BindingCondition<?> condition = configurator
        .<BindingCondition<?>>overrideChildren(
            ChildBindingPoint::bindingCondition,
            ChildBindingPointBuilder::getBindingCondition)
        .get();
    this.condition = (BindingCondition<? super T>) condition;
  }

  @Override
  public QualifiedName name() {
    return name;
  }

  @Override
  public boolean ordered() {
    return ordered;
  }

  @Override
  public BindingCondition<? super T> bindingCondition() {
    return condition;
  }

  @Override
  public TypeToken<T> dataType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Model<? super T> model() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Node<T> override() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Node<?> parent() {
    // TODO Auto-generated method stub
    return null;
  }
}
