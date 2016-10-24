package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingCondition;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class AscendingSortCondition<T extends Comparable<? super T>> extends SortCondition<T> {
	static final AscendingSortCondition<?> INSTANCE = new AscendingSortCondition<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> ascending() {
		return (BindingCondition<T>) INSTANCE;
	}

	protected AscendingSortCondition() {
		super(Comparable::compareTo);
	}
}
