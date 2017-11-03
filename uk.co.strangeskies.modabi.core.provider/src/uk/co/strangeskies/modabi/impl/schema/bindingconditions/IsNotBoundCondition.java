package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

public class IsNotBoundCondition<T> extends BindingConditionImpl<T> {
  private final ChildBindingPoint<?> target;

  public IsNotBoundCondition(BindingConditionPrototype prototype, ChildBindingPoint<?> target) {
    super(prototype);
    this.target = target;
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    throw new UnsupportedOperationException(); // TODO
  }
}
