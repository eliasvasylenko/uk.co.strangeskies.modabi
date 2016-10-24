package uk.co.strangeskies.modabi.schema.bindingconditions;

import static uk.co.strangeskies.mathematics.Range.between;

import uk.co.strangeskies.modabi.schema.BindingCondition;

public class OptionalCondition<T> extends OccurrencesCondition<T> {
	static final OptionalCondition<?> INSTANCE = new OptionalCondition<>();

	@SuppressWarnings("unchecked")
	public static <T> BindingCondition<T> optional() {
		return (BindingCondition<T>) OptionalCondition.INSTANCE;
	}

	protected OptionalCondition() {
		super(between(0, 1));
	}
}
