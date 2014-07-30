package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class OverrideMerge<E extends SchemaNode<? extends E>, C extends SchemaNodeConfiguratorImpl<?, ? extends E, ?, ?>> {
	private final E node;
	private final C configurator;

	public OverrideMerge(E node, C configurator) {
		this.node = node;
		this.configurator = configurator;
	}

	public C configurator() {
		return null;
	}

	public <T> T getValue(Function<E, T> valueFunction) {
		return getValue(valueFunction, (v, o) -> true);
	}

	public <T> T getValue(Function<E, T> valueFunction,
			BiPredicate<T, T> validateOverride) {
		T value = valueFunction.apply(node.effective());

		Collection<T> values = configurator.getOverriddenNodes().stream()
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
