package uk.co.strangeskies.modabi.schema.impl;

import java.util.Objects;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPointBuilder;
import uk.co.strangeskies.modabi.schema.Model;
import uk.co.strangeskies.modabi.schema.Node;
import uk.co.strangeskies.reflection.token.TypeToken;

public class ChildBindingPointImpl<T> implements ChildBindingPoint<T> {
  private final QualifiedName name;

  private final TypeToken<T> type;
  private final Model<? super T> model;

  private final boolean ordered;
  private final BindingCondition<T> condition;

  @SuppressWarnings("unchecked")
  protected ChildBindingPointImpl(ChildBindingPointBuilderImpl<?> configurator) {
    name = configurator
        .overrideChildren(ChildBindingPoint::name, ChildBindingPointBuilder::getName)
        .validateOverride(Objects::equals)
        .or(() -> configurator.getModel().get().name())
        .get();

    ordered = configurator
        .overrideChildren(ChildBindingPoint::ordered, ChildBindingPointBuilder::getOrdered)
        .validateOverride((a, b) -> a || !b)
        .or(false)
        .get();

    type = null; // TODO

    model = configurator
        .overrideChildren(ChildBindingPoint::model, ChildBindingPointBuilder::getModel)
        .validateOverride((a, b) -> a.equals(b) || a.baseModels().anyMatch(b::equals))
        .tryGet()
        .map(m -> (Model<? super T>) m)
        .orElse(null);

    BindingConditionPrototype condition = configurator
        .overrideChildren(
            b -> b.bindingCondition()::accept,
            ChildBindingPointBuilder::getBindingCondition)
        .orMerged(c -> BindingConditionPrototype.allOf(c))
        .or(v -> v.required())
        .get();
    this.condition = new BindingConditionFactory<>(type).create(condition);
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
  public BindingCondition<T> bindingCondition() {
    return condition;
  }

  @Override
  public TypeToken<T> dataType() {
    return type;
  }

  @Override
  public Model<? super T> model() {
    return model;
  }

  @Override
  public Node override() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Node parent() {
    // TODO Auto-generated method stub
    return null;
  }
}
