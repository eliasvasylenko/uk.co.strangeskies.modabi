package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.impl.schema.SchemaNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;

public class OverrideBuilder<T, S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N>> {
	private final S configurator;

	private Function<N, T> valueFunction;
	private BiPredicate<? super T, ? super T> validation;

	private T override;
	private Set<T> values;

	public OverrideBuilder(SchemaNodeConfiguratorImpl<S, N> configurator, Function<N, T> valueFunction) {
		this.configurator = configurator.getThis();
		this.valueFunction = valueFunction;

		values = configurator.getOverridenValues(valueFunction);
	}

	private OverrideBuilder(S configurator, OverrideBuilder<T, S, N> from) {
		this.configurator = configurator;
		override = from.override;
		values = from.values;
	}

	public OverrideBuilder<T, S, N> orDefault(T value) {
		return orDefault(value, Abstractness.UNINFERRED);
	}

	public OverrideBuilder<T, S, N> orDefault(T value, Abstractness defaultIfAtMost) {
		if (configurator.node() == null
				|| (configurator.node().abstractness() == null || configurator.node().abstractness().isAtMost(defaultIfAtMost))
						&& !isOverridden()) {
			return or(() -> value);
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S, N> orMerged(Function<? super Collection<T>, ? extends T> merge) {
		if (values != null && !values.isEmpty()) {
			return or(() -> {
				T merged = merge.apply(values);

				if (merged == null) {
					throw new ModabiException(
							t -> t.cannotMergeIncompatibleProperties(valueFunction::apply, configurator.node(), values));
				}

				return merged;
			});
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S, N> orMerged(BinaryOperator<T> merge) {
		return orMerged(s -> s.stream().reduce(merge).get());
	}

	public OverrideBuilder<T, S, N> or(Supplier<T> supplier) {
		if (override == null) {
			OverrideBuilder<T, S, N> optional = new OverrideBuilder<>(configurator, this);
			optional.override = supplier.get();

			return optional;
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S, N> or() {
		return or(() -> valueFunction.apply(configurator.node()));
	}

	public T tryGet() {
		if (isOverridden() && validation != null) {
			for (T value : values) {
				if (!validation.test(override, value)) {
					throw new ModabiException(
							t -> t.cannotOverrideIncompatibleProperty(valueFunction::apply, configurator.node(), value, override),
							null);
				}
			}
		}

		return override;
	}

	public T get() {
		override = tryGet();

		if (override == null && configurator.node() != null && (configurator.node().abstractness() == null
				|| configurator.node().abstractness().isAtMost(Abstractness.UNINFERRED)))
			throw new ModabiException(t -> t.mustProvideValueForNonAbstract(valueFunction::apply, configurator.node()));

		return override;
	}

	private boolean isOverridden() {
		boolean overridden = override != null;

		if (!overridden) {
			if (values.size() == 1) {
				override = values.iterator().next();
			} else if (values.size() > 1) {
				throw new ModabiException(
						t -> t.mustOverrideIncompatibleProperties(valueFunction::apply, configurator.node(), values));
			}
		}

		return overridden;
	}

	public OverrideBuilder<T, S, N> validate(BiPredicate<? super T, ? super T> validation) {
		OverrideBuilder<T, S, N> optional = new OverrideBuilder<>(configurator, this);
		if (optional.validation == null) {
			optional.validation = validation;
		} else {
			optional.validation = (a, b) -> validation.test(a, b) && optional.validation.test(a, b);
		}

		return optional;
	}
}
