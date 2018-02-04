package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

public class IsBoundCondition<T> implements BindingCondition<T> {
  private final ChildBindingPoint<?> target;

  public IsBoundCondition(ChildBindingPoint<?> target) {
    this.target = target;
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public BindingConditionPrototype getPrototype() {
    throw new UnsupportedOperationException(); // TODO
  }
}
