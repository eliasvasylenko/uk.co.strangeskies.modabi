package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.binding.BindingContext;

/**
 * A {@link BindingCondition binding condition} is associated with a
 * {@link ChildBindingPoint binding point}, and specifies rules for determining
 * whether items may be bound to that point during some processing operation.
 * <p>
 * Upon reaching the associated binding point during some process, it is
 * evaluated for the current {@link BindingContext processing state}.
 * 
 * @author Elias N Vasylenko
 */
public interface BindingCondition<T> {
  /**
   * @return The prototype from which this condition was compiled
   */
  BindingConditionPrototype getPrototype();
  
  BindingConditionEvaluation<T> forState(BindingContext state);
}
