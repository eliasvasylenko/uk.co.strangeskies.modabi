package uk.co.strangeskies.modabi.schema;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.bindingconditions.And;
import uk.co.strangeskies.modabi.schema.bindingconditions.Or;

/**
 * A {@link BindingCondition binding condition} is associated with a
 * {@link ChildBindingPoint binding point}, and specifies rules for determining
 * whether items may be bound to that point during some processing operation.
 * <p>
 * Upon reaching the associated binding point during some process, it is
 * evaluated for the current {@link ProcessingContext processing state}.
 * 
 * 
 * 
 * 
 * 
 * TODO EqualTo, GreaterThan, LessThan, GreaterThanOrEqualTo, LessThanOrEqualTo
 * 
 * maybe those contained in a ForEach type class which takes a predicate
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Elias N Vasylenko
 */
public interface BindingCondition<T> {
	BindingConditionEvaluation<T> forState(ProcessingContext state);

	default BindingCondition<T> or(BindingCondition<? super T> condition) {
		return Or.or(this, condition);
	}

	default BindingCondition<T> and(BindingCondition<? super T> condition) {
		return And.and(this, condition);
	}
}
