package uk.co.strangeskies.modabi.schema.bindingconditions;

import static uk.co.strangeskies.mathematics.Range.between;

import uk.co.strangeskies.modabi.schema.BindingCondition;

public class Optional<T> extends Occurrences<T> {
	static final Optional<?> INSTANCE = new Optional<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> optional() {
		return (BindingCondition<T>) Optional.INSTANCE;
	}

	protected Optional() {
		super(between(0, 1));
	}
}
