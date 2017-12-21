package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import uk.co.strangeskies.modabi.binding.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;
import uk.co.strangeskies.modabi.schema.BindingConditionVisitor;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;

public class IsNotBoundCondition<T> implements BindingCondition<T> {
  private final ChildBindingPoint<?> target;

  public IsNotBoundCondition(ChildBindingPoint<?> target) {
    this.target = target;
  }

  @Override
  public BindingConditionEvaluation<T> forState(BindingContext state) {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public void accept(BindingConditionVisitor visitor) {
    visitor.isNotBound();
  }
}
