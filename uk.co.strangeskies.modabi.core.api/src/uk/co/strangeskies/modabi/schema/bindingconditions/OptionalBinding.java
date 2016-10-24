package uk.co.strangeskies.modabi.schema.bindingconditions;

import static uk.co.strangeskies.mathematics.Range.between;

import uk.co.strangeskies.modabi.schema.BindingCondition;

public class OptionalBinding<T> extends RequiredBindingOccurrences<T> {
	static final OptionalBinding<?> INSTANCE = new OptionalBinding<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> optional() {
		return (BindingCondition<T>) OptionalBinding.INSTANCE;
	}

	protected OptionalBinding() {
		super(between(0, 1));
	}
}
