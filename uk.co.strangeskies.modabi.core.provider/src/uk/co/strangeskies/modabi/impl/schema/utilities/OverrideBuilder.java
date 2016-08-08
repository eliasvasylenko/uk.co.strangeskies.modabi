package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;

public class OverrideBuilder<T, S extends SchemaNodeConfigurator<?, ?>, I extends SchemaNodeConfigurator<? extends S, ? extends N>, N extends SchemaNode<?>> {
	private final I configurator;

	private final Function<? super N, ? extends T> valueFunction;
	private final Function<? super S, ? extends T> givenValueFunction;

	private final Set<T> values;

	private final BiFunction<? super T, ? super T, ? extends T> mergeOverride;
	private T override;

	public OverrideBuilder(I configurator, Function<? super I, ? extends Collection<? extends N>> overridden,
			Function<? super N, ? extends T> valueFunction) {
		this(configurator, overridden, valueFunction, null);
	}

	public OverrideBuilder(I configurator, Function<? super I, ? extends Collection<? extends N>> overridden,
			Function<? super N, ? extends T> valueFunction, Function<? super S, ? extends T> givenValueFunction) {
		this.configurator = configurator;
		this.valueFunction = valueFunction;
		this.givenValueFunction = givenValueFunction;

		values = overridden.apply(configurator).stream().map(n -> {
			T value = valueFunction.apply(n);

			if (value != null) {
				return Stream.of(value);
			} else if (givenValueFunction == null) {
				return Stream.<T> empty();
			} else {
				List<S> values = new ArrayList<>();

				@SuppressWarnings("unchecked")
				S c = (S) n.configurator();
				values.add(c);

				Set<SchemaNode<?>> contains = new HashSet<>();
				contains.add(n);

				for (int i = 0; i < values.size(); i++) {
					@SuppressWarnings("unchecked")
					I nci = (I) values.get(i);

					for (N nn : overridden.apply(nci)) {
						@SuppressWarnings("unchecked")
						S nnc = (S) nn.configurator();

						if (contains.add(nn)) {
							values.add(nnc);
						}
					}
				}

				return values.stream().map(givenValueFunction::apply).filter(Objects::nonNull);
			}
		}).flatMap(Function.identity()).collect(Collectors.toSet());

		mergeOverride = null;
		override = givenValueFunction.apply(configurator.getThis());
	}

	private OverrideBuilder(OverrideBuilder<T, S, I, N> from, BiFunction<? super T, ? super T, ? extends T> validation,
			T override) {
		configurator = from.configurator;

		valueFunction = from.valueFunction;
		givenValueFunction = from.givenValueFunction;

		values = from.values;

		this.mergeOverride = validation;
		this.override = override;
	}

	public OverrideBuilder<T, S, I, N> orDefault(T value) {
		if (givenValueFunction == null
				|| (configurator.getConcrete() == null || configurator.getConcrete()) && !isOverridden()) {
			return or(() -> value);
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S, I, N> orMerged(Function<? super Collection<T>, ? extends T> merge) {
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

	public OverrideBuilder<T, S, I, N> orMerged(BinaryOperator<T> merge) {
		return orMerged(s -> s.stream().reduce(merge).get());
	}

	public OverrideBuilder<T, S, I, N> or(T value) {
		return or(() -> value);
	}

	public OverrideBuilder<T, S, I, N> or(Supplier<T> supplier) {
		if (override == null) {
			return new OverrideBuilder<>(this, mergeOverride, supplier.get());
		} else {
			return this;
		}
	}

	public T tryGet() {
		if (isOverridden() && mergeOverride != null) {
			for (T value : values) {
				override = mergeOverride.apply(override, value);
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

	public OverrideBuilder<T, S, I, N> validateOverride(BiPredicate<? super T, ? super T> validation) {
		BiFunction<? super T, ? super T, ? extends T> newValidation = (a, b) -> {
			if (!validation.test(a, b)) {
				throw new ModabiException(
						t -> t.cannotOverrideIncompatibleProperty(valueFunction::apply, getNodeClass(), b, a));
			}

			return a;
		};

		return mergeOverride(newValidation);
	}

	public OverrideBuilder<T, S, I, N> mergeOverride(BiFunction<? super T, ? super T, ? extends T> mergeOverride) {
		BiFunction<? super T, ? super T, ? extends T> newOverride = mergeOverride;

		if (this.mergeOverride != null) {
			newOverride = (a, b) -> mergeOverride.apply(this.mergeOverride.apply(a, b), b);
		}

		return new OverrideBuilder<>(this, newOverride, override);
	}
}
