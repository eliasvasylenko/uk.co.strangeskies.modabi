package uk.co.strangeskies.modabi.schema.impl.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.Child;

public class IsBoundCondition<T> implements BindingCondition<T> {
  private final Child<?> target;

  public IsBoundCondition(Child<?> target) {
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
