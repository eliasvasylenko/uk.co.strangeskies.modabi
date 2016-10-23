package uk.co.strangeskies.modabi.schema.bindingconditions;

import uk.co.strangeskies.modabi.schema.BindingCondition;

/**
 * A rule to specify that a binding point must be processed a number of times
 * within a given range.
 * 
 * @author Elias N Vasylenko
 */
public class Ascending<T extends Comparable<? super T>> extends Sorted<T> {
	static final Ascending<?> INSTANCE = new Ascending<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> ascending() {
		return (BindingCondition<T>) INSTANCE;
	}

	protected Ascending() {
		super(Comparable::compareTo);
	}
}
