package uk.co.strangeskies.modabi.schema;

import java.util.List;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public interface ChildBindingPoint<T> extends BindingPoint<T> {
	boolean extensible();

	TypeToken<?> preInputType();

	TypeToken<?> postInputType();

	BindingCondition<? super T> bindingCondition();

	@Override
	TypeToken<ChildBindingPoint<T>> getThisType();

	List<T> providedValues();

	default List<TypedObject<T>> typedProvidedValues() {
		return providedValues().stream().map(dataType()::typedObject).collect(Collectors.toList());
	}

	default T providedValue() {
		if (providedValues() == null || providedValues().isEmpty())
			return null;

		if (providedValues().size() > 1)
			throw new ModabiException(t -> t.cannotProvideSingleValue(name(), providedValues().size()));

		return providedValues().get(0);
	}

	default TypedObject<T> typedProvidedValue() {
		return dataType().typedObject(providedValue());
	}

	default boolean isValueProvided() {
		return providedValues() != null;
	}
}
