package uk.co.strangeskies.modabi.model.building.impl;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;

public class OverrideMerge<E> {
	private final E node;
	private final Collection<? extends E> overriddenNodes;

	public OverrideMerge(E node, Collection<? extends E> overriddenNodes) {
		this.node = node;
		this.overriddenNodes = overriddenNodes;
	}

	public <T> T getValue(Function<E, T> valueFunction) {
		return getValue(valueFunction, (v, o) -> true);
	}

	public <T> T getValue(Function<E, T> valueFunction,
			BiPredicate<T, T> validateOverride) {
		T value = valueFunction.apply(node);

		Collection<T> values = overriddenNodes.stream()
				.map(n -> valueFunction.apply(n)).filter(v -> v != null)
				.collect(Collectors.toSet());

		if (values.isEmpty())
			return value;
		else if (values.size() == 1) {
			T overriddenValue = values.iterator().next();
			if (value != null)
				if (!validateOverride.test(value, overriddenValue))
					throw new SchemaException("Cannot override property ["
							+ overriddenValue + "] with [" + value + "]");
				else
					return value;
			return overriddenValue;
		} else if (value == null
				|| !values.stream().allMatch(v -> validateOverride.test(value, v)))
			throw new SchemaException("value: " + value);
		return value;
	}
}
