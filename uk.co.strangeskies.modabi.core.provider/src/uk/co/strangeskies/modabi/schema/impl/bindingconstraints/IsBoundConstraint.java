package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingConstraintSpecification;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.Child;

public class IsBoundConstraint<T> implements BindingConstraint<T> {
  private final Child<?> target;

  public IsBoundConstraint(Child<?> target) {
    this.target = target;
  }

  @Override
  public BindingProcedure<T> procedeWithState(BindingContext state) {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public BindingConstraintSpecification getSpecification() {
    throw new UnsupportedOperationException(); // TODO
  }
}
