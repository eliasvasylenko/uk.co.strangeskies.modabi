package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.schema.BindingCondition;
import uk.co.strangeskies.modabi.schema.BindingConditionEvaluation;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class Synchronous<T> implements BindingCondition<T> {
	/**
	 * @return a binding condition which only allows processing to proceed once
	 *         the previous item has completed
	 */
	public static <T> BindingCondition<T> synchronous() {
		return new Synchronous<>();
	}

	protected Synchronous() {}

	@Override
	public BindingConditionEvaluation<T> forState(ProcessingContext state) {
		throw new UnsupportedOperationException();
	}
}
