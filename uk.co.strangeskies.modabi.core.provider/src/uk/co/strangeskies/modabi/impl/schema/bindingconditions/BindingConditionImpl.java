package uk.co.strangeskies.modabi.impl.schema.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionPrototype;
import uk.co.strangeskies.modabi.schema.BindingConditionVisitor;

/**
 * A simple rule for binding points which are required to never be processed.
 * 
 * @author Elias N Vasylenko
 */
public abstract class BindingConditionImpl<T> implements BindingCondition<T> {
  private final BindingConditionPrototype prototype;

  public BindingConditionImpl(BindingConditionPrototype prototype) {
    this.prototype = prototype;
  }

  @Override
  public void accept(BindingConditionVisitor visitor) {
    prototype.accept(visitor);
  }
}
