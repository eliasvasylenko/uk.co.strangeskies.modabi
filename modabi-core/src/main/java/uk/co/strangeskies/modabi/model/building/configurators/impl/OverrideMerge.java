package uk.co.strangeskies.modabi.model.building.configurators.impl;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;

public class OverrideMerge<E, C> {
	private final E node;
	private final C configurator;
	private final Function<C, Collection<? extends E>> overriddenNodesFunction;

	public OverrideMerge(E node, C configurator,
			Function<C, Collection<? extends E>> overriddenNodesFunction) {
		this.node = node;
		this.configurator = configurator;
		this.overriddenNodesFunction = overriddenNodesFunction;
	}

	public static <E, C extends SchemaNodeConfiguratorImpl<?, ? extends E, ?, ?>> OverrideMerge<E, C> with(
			E node, C configurator) {
		return new OverrideMerge<E, C>(node, configurator,
				c -> c.getOverriddenNodes());
	}

	public E node() {
		return node;
	}

	public C configurator() {
		return configurator;
	}

	public <T> T getValue(Function<E, T> valueFunction) {
		return getValue(valueFunction, (v, o) -> true);
	}

	public <T> T getValue(Function<E, T> valueFunction,
			BiPredicate<T, T> validateOverride) {
		T value = valueFunction.apply(node);

		Collection<T> values = overriddenNodesFunction.apply(configurator).stream()
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
