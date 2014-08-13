package uk.co.strangeskies.modabi.model.building.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.model.nodes.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaException;

public class OverrideMerge<S extends SchemaNode<S, ?>, C> {
	private final S node;
	private final C configurator;
	private final Collection<? extends S> overriddenNodes;

	public OverrideMerge(S node, C configurator,
			Function<C, Collection<? extends S>> overriddenNodesFunction) {
		this.node = node;
		this.configurator = configurator;
		this.overriddenNodes = overriddenNodesFunction.apply(configurator);
	}

	public S node() {
		return node;
	}

	public C configurator() {
		return configurator;
	}

	public <T> T getValue(Function<S, T> valueFunction) {
		return getValue(valueFunction, (v, o) -> true);
	}

	public <T> T getValue(Function<S, T> valueFunction,
			BiPredicate<T, T> validateOverride) {
		T value = valueFunction.apply(node);

		@SuppressWarnings("unchecked")
		Collection<T> values = overriddenNodes.stream()
				.map(n -> valueFunction.apply((S) n.effective()))
				.filter(Objects::nonNull).collect(Collectors.toSet());

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
