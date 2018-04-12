package uk.co.strangeskies.modabi.schema;

/**
 * A {@link BindingConstraint binding condition} is associated with a
 * {@link Child binding point}, and specifies rules for determining whether
 * items may be bound to that point during some processing operation.
 * <p>
 * Upon reaching the associated binding point during some process, it is
 * evaluated for the current {@link BindingContext processing state}.
 * 
 * @author Elias N Vasylenko
 */
public interface BindingConstraint<T> {
  /**
   * @return The prototype from which this constraint was compiled
   */
  BindingConstraintSpecification getSpecification();

  BindingProcedure<T> procedeWithState(BindingContext state);
}
