package uk.co.strangeskies.modabi.schema.impl.bindingconstraints;

import uk.co.strangeskies.modabi.schema.BindingProcedure;
import uk.co.strangeskies.modabi.schema.BindingConstraint;
import uk.co.strangeskies.modabi.schema.BindingContext;
import uk.co.strangeskies.modabi.schema.BindingProcess;
import uk.co.strangeskies.modabi.schema.Child;

public class IsBoundConstraint<T> implements BindingProcedure<T> {
  private final Child<?> target;

  public IsBoundConstraint(Child<?> target) {
    this.target = target;
  }

  @Override
  public BindingProcess<T> procedeWithState(BindingContext state) {
    throw new UnsupportedOperationException(); // TODO
  }

  @Override
  public BindingConstraint getConstraint() {
    throw new UnsupportedOperationException(); // TODO
  }
}
