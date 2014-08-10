package uk.co.strangeskies.modabi.model.building.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class OverrideMerge<E extends SchemaNode<? extends E>, C> {
	private final E node;
	private final C configurator;
	private final Collection<? extends E> overriddenNodes;

	public OverrideMerge(E node, C configurator,
			Function<C, Collection<? extends E>> overriddenNodesFunction) {
		this.node = node;
		this.configurator = configurator;
		this.overriddenNodes = overriddenNodesFunction.apply(configurator);
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

		Collection<T> values = overriddenNodes.stream()
				.map(n -> valueFunction.apply(n.effective())).filter(Objects::nonNull)
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
