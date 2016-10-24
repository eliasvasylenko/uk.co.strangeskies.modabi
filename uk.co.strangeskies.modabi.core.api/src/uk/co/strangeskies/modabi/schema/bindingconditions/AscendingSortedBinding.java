package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingCondition;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class AscendingSortedBinding<T extends Comparable<? super T>> extends SortedBinding<T> {
	static final AscendingSortedBinding<?> INSTANCE = new AscendingSortedBinding<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> ascending() {
		return (BindingCondition<T>) INSTANCE;
	}

	protected AscendingSortedBinding() {
		super(Comparable::compareTo);
	}
}
