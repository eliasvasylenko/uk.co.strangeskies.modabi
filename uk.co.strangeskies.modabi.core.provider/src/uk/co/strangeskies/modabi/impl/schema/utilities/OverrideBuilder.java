package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.impl.schema.SchemaNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;

public class OverrideBuilder<T, S extends SchemaNodeConfigurator<S, N>, N extends SchemaNode<N>> {
	private final SchemaNodeConfiguratorImpl<S, N> configurator;

	private final Function<N, T> valueFunction;
	private final Function<S, T> givenValueFunction;

	private final Set<T> values;

	private final BiPredicate<? super T, ? super T> validation;
	private T override;

	public OverrideBuilder(SchemaNodeConfiguratorImpl<S, N> configurator, Function<N, T> valueFunction) {
		this(configurator, valueFunction, null);
	}

	public OverrideBuilder(SchemaNodeConfiguratorImpl<S, N> configurator, Function<N, T> valueFunction,
			Function<S, T> givenValueFunction) {
		this.configurator = configurator;
		this.valueFunction = valueFunction;
		this.givenValueFunction = givenValueFunction;

		values = configurator.getOverridenValues(n -> {
			T value = valueFunction.apply(n);

			if (value != null) {
				return Arrays.asList(value);
			} else if (givenValueFunction == null) {
				return Collections.<T> emptyList();
			} else {
				List<S> values = new ArrayList<>();

				@SuppressWarnings("unchecked")
				S c = (S) n.configurator();
				values.add(c);

				Set<SchemaNode<?>> contains = new HashSet<>();
				contains.add(n);

				for (int i = 0; i < values.size(); i++) {
					for (N nn : values.get(i).getOverriddenNodes()) {
						@SuppressWarnings("unchecked")
						S nnc = (S) nn.configurator();

						if (contains.add(nn)) {
							values.add(nnc);
						}
					}
				}

				return values.stream().map(givenValueFunction::apply).collect(Collectors.toList());
			}
		}).stream().flatMap(Collection::stream).collect(Collectors.toSet());

		validation = null;
		override = givenValueFunction.apply(configurator.getThis());
	}

	private OverrideBuilder(OverrideBuilder<T, S, N> from, BiPredicate<? super T, ? super T> validation, T override) {
		configurator = from.configurator;

		valueFunction = from.valueFunction;
		givenValueFunction = from.givenValueFunction;

		values = from.values;

		this.validation = validation;
		this.override = override;
	}

	public OverrideBuilder<T, S, N> orDefault(T value) {
		if (givenValueFunction == null
				|| (configurator.getConcrete() == null || configurator.getConcrete()) && !isOverridden()) {
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
							t -> t.cannotMergeIncompatibleProperties(valueFunction::apply, getNodeClass(), values));
				}

				return merged;
			});
		} else {
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	private Class<N> getNodeClass() {
		return (Class<N>) configurator.getNodeType().getRawType();
	}

	public OverrideBuilder<T, S, N> orMerged(BinaryOperator<T> merge) {
		return orMerged(s -> s.stream().reduce(merge).get());
	}

	public OverrideBuilder<T, S, N> or(T value) {
		return or(() -> value);
	}

	public OverrideBuilder<T, S, N> or(Supplier<T> supplier) {
		if (override == null) {
			return new OverrideBuilder<>(this, validation, supplier.get());
		} else {
			return this;
		}
	}

	public T tryGet() {
		if (isOverridden() && validation != null) {
			for (T value : values) {
				if (!validation.test(override, value)) {
					throw new ModabiException(
							t -> t.cannotOverrideIncompatibleProperty(valueFunction::apply, getNodeClass(), value, override), null);
				}
			}
		}

		return override;
	}

	public T get() {
		T value = tryGet();

		if (value == null && givenValueFunction != null
				&& (configurator.getConcrete() == null || configurator.getConcrete()))
			throw new ModabiException(t -> t.mustProvideValueForNonAbstract(valueFunction::apply, getNodeClass()));

		return value;
	}

	private boolean isOverridden() {
		boolean overridden = override != null;

		if (!overridden) {
			if (values.size() == 1) {
				override = values.iterator().next();
			} else if (values.size() > 1) {
				throw new ModabiException(
						t -> t.mustOverrideIncompatibleProperties(valueFunction::apply, getNodeClass(), values));
			}
		}

		return overridden;
	}

	public OverrideBuilder<T, S, N> validate(BiPredicate<? super T, ? super T> validation) {
		BiPredicate<? super T, ? super T> newValidation = validation;

		if (this.validation != null) {
			newValidation = (a, b) -> validation.test(a, b) && this.validation.test(a, b);
		}

		return new OverrideBuilder<>(this, newValidation, override);
	}
}
