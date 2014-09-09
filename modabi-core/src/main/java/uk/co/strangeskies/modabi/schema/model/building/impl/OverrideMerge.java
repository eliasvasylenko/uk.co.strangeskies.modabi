package uk.co.strangeskies.modabi.schema.model.building.impl;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.building.configurators.impl.SchemaNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;

public class OverrideMerge<S extends SchemaNode<S, ?>, C extends SchemaNodeConfiguratorImpl<?, ? extends S, ?, ?>> {
	private final S node;
	private final C configurator;

	public OverrideMerge(S node, C configurator) {
		this.node = node;
		this.configurator = configurator;
	}

	public S node() {
		return node;
	}

	public C configurator() {
		return configurator;
	}

	public <T> T getValue(Function<S, T> valueFunction) {
		return getValue(valueFunction, Objects::equals);
	}

	public <T> T getValue(Function<S, T> valueFunction,
			BiPredicate<T, T> validateOverride) {
		return getValueWithOverride(
				node == null ? null : valueFunction.apply(node), valueFunction,
				validateOverride);
	}

	public <T> T getValueWithOverride(T valueOverride,
			Function<S, T> valueFunction) {
		return getValueWithOverride(valueOverride, valueFunction, (v, o) -> true);
	}

	public <T> T getValueWithOverride(T valueOverride,
			Function<S, T> valueFunction, BiPredicate<T, T> validateOverride) {
		@SuppressWarnings("unchecked")
		Collection<T> values = configurator.getOverriddenNodes().stream()
				.map(n -> valueFunction.apply((S) n.effective()))
				.filter(Objects::nonNull).collect(Collectors.toSet());

		if (values.isEmpty())
			return valueOverride;
		else if (values.size() == 1) {
			T overriddenValue = values.iterator().next();
			if (valueOverride != null)
				if (!validateOverride.test(valueOverride, overriddenValue))
					throw new SchemaException("Cannot override property ["
							+ overriddenValue + "] with [" + valueOverride + "]");
				else
					return valueOverride;
			return overriddenValue;
		} else if (valueOverride == null
				|| !values.stream().allMatch(
						v -> validateOverride.test(valueOverride, v)))
			throw new SchemaException("Cannot override properties [" + values
					+ "] with [" + valueOverride + "]");
		return valueOverride;
	}
}
