package uk.co.strangeskies.modabi.schema.bindingconditions;

import static uk.co.strangeskies.mathematics.Range.between;

import uk.co.strangeskies.modabi.schema.BindingCondition;

/**
 * A simple rule for binding points which are required to always be processed.
 * 
 * @author Elias N Vasylenko
 */
public class Required<T> extends Occurrences<T> {
	static final Required<?> INSTANCE = new Required<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> required() {
		return (BindingCondition<T>) Required.INSTANCE;
	}

	protected Required() {
		super(between(1, 1));
	}
}
