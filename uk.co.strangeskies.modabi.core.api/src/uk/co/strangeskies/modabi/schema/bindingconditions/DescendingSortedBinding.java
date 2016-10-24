package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingCondition;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class DescendingSortedBinding<T extends Comparable<? super T>> extends SortedBinding<T> {
	static final DescendingSortedBinding<?> INSTANCE = new DescendingSortedBinding<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> descending() {
		return (BindingCondition<T>) INSTANCE;
	}

	protected DescendingSortedBinding() {
		super((a, b) -> b.compareTo(a));
	}
}
