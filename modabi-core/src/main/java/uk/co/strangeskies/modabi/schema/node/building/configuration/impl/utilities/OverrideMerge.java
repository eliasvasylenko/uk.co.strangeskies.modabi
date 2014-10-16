package uk.co.strangeskies.modabi.schema.node.building.configuration.impl.utilities;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.node.building.configuration.impl.SchemaNodeConfiguratorImpl;

public class OverrideMerge<S extends SchemaNode<? extends S, ?>, C extends SchemaNodeConfiguratorImpl<?, ? extends S>> {
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
		return getValue(valueFunction, (T) null);
	}

	public <T> T getValue(Function<S, T> valueFunction, T defaultValue) {
		return getValue(valueFunction, Objects::equals, defaultValue);
	}

	public <T> T getValue(Function<S, T> valueFunction,
			BiPredicate<? super T, ? super T> validateOverride, T defaultValue) {
		return checkResult(
				getValueWithOverride(node == null ? null : valueFunction.apply(node),
						defaultValue, valueFunction, validateOverride),
				valueFunction.toString());
	}

	public <T> T tryGetValue(Function<S, T> valueFunction) {
		return tryGetValue(valueFunction, Objects::equals);
	}

	public <T> T tryGetValue(Function<S, T> valueFunction,
			BiPredicate<? super T, ? super T> validateOverride) {
		return getValueWithOverride(
				node == null ? null : valueFunction.apply(node), null, valueFunction,
				validateOverride);
	}

	private <T> T checkResult(T value, String valueName) {
		if (value == null && (node.isAbstract() == null || !node.isAbstract()))
			throw new SchemaException("No value '" + valueName
					+ "' available for non-abstract node '" + node.getName() + "'.");
		return value;
	}

	public <T> T getValueWithOverride(T valueOverride,
			Function<S, T> valueFunction) {
		return getValueWithOverride(valueOverride, valueFunction, Objects::equals);
	}

	public <T> T getValueWithOverride(T valueOverride,
			Function<S, T> valueFunction,
			BiPredicate<? super T, ? super T> validateOverride) {
		return getValueWithOverride(valueOverride, null, valueFunction,
				validateOverride);
	}

	private <T> T getValueWithOverride(T valueOverride, T defaultValue,
			Function<S, T> valueFunction,
			BiPredicate<? super T, ? super T> validateOverride) {
		@SuppressWarnings("unchecked")
		Collection<T> values = configurator.getOverriddenNodes().stream()
				.map(n -> valueFunction.apply((S) n.effective()))
				.filter(Objects::nonNull).collect(Collectors.toSet());

		T value = valueOverride;

		if (values.size() == 1) {
			T overriddenValue = values.iterator().next();
			if (valueOverride != null)
				if (!validateOverride.test(valueOverride, overriddenValue))
					throw new SchemaException("Cannot override property ["
							+ overriddenValue + "] with [" + valueOverride + "]");
				else
					value = valueOverride;
			else
				value = overriddenValue;
		} else if (!values.isEmpty()
				&& (valueOverride == null || !values.stream().allMatch(
						v -> validateOverride.test(valueOverride, v))))
			throw new SchemaException("Cannot override properties [" + values
					+ "] with [" + valueOverride + "]");

		if (value == null
				&& (node == null || (node.isAbstract() == null || !node.isAbstract())))
			value = defaultValue;

		return value;
	}
}
