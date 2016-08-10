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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;

/**
 * A class for specification of the override rules for determining the exact
 * value of a property of a schema node.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 * @param <S>
 * @param <I>
 * @param <N>
 */
public class OverrideBuilder<T, S extends SchemaNodeConfigurator<?, ?>, I extends SchemaNodeConfigurator<? extends S, ? extends N>, N extends SchemaNode<?>> {
	private final I configurator;

	private final Function<? super N, ? extends T> valueFunction;
	private final Function<? super S, ? extends T> givenValueFunction;

	private final BiFunction<? super T, ? super T, ? extends T> mergeOverride;

	private final Set<T> inheritedValues;
	private final T override;

	public OverrideBuilder(I configurator, Function<? super I, ? extends Collection<? extends N>> overridden,
			Function<? super N, ? extends T> valueFunction) {
		this(configurator, overridden, valueFunction, null);
	}

	public OverrideBuilder(I configurator, Function<? super I, ? extends Collection<? extends N>> overridden,
			Function<? super N, ? extends T> valueFunction, Function<? super S, ? extends T> givenValueFunction) {
		this.configurator = configurator;
		this.valueFunction = valueFunction;
		this.givenValueFunction = givenValueFunction;

		mergeOverride = validateOverrideFunction(Objects::equals);

		inheritedValues = overridden.apply(configurator).stream().map(n -> {
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

		T override = givenValueFunction.apply(configurator.getThis());

		if (override == null && inheritedValues.size() == 1) {
			override = inheritedValues.iterator().next();
		}

		this.override = override;
	}

	private OverrideBuilder(OverrideBuilder<T, S, I, N> from, BiFunction<? super T, ? super T, ? extends T> validation,
			T override) {
		configurator = from.configurator;

		valueFunction = from.valueFunction;
		givenValueFunction = from.givenValueFunction;

		inheritedValues = from.inheritedValues;

		this.mergeOverride = validation;
		this.override = override;
	}

	/**
	 * @return all unique values inherited from overridden and base nodes, or
	 *         given directly from their configurators
	 */
	public Set<T> getValues() {
		return inheritedValues;
	}

	public OverrideBuilder<T, S, I, N> orDefault(T value) {
		if (givenValueFunction == null
				|| (configurator.getConcrete() == null || configurator.getConcrete()) && !isOverridden()) {
			return or(value);
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S, I, N> orMerged(Function<? super Collection<T>, ? extends T> merge) {
		if (!inheritedValues.isEmpty()) {
			T merged = merge.apply(inheritedValues);

			if (merged == null) {
				throw new ModabiException(
						t -> t.cannotMergeIncompatibleProperties(valueFunction::apply, getNodeClass(), inheritedValues));
			}

			return or(merged);
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S, I, N> orMerged(BinaryOperator<T> merge) {
		return orMerged(s -> s.stream().filter(Objects::nonNull).reduce(merge).orElseThrow(
				() -> new ModabiException(t -> t.mustProvideValueForNonAbstract(valueFunction::apply, getNodeClass()))));
	}

	public OverrideBuilder<T, S, I, N> validateOverride(BiPredicate<? super T, ? super T> validation) {
		return mergeOverride(validateOverrideFunction(validation));
	}

	private BiFunction<? super T, ? super T, ? extends T> validateOverrideFunction(
			BiPredicate<? super T, ? super T> validation) {
		return (a, b) -> {
			if (!validation.test(a, b)) {
				throw new ModabiException(
						t -> t.cannotOverrideIncompatibleProperty(valueFunction::apply, getNodeClass(), b, a));
			}

			return a;
		};
	}

	public OverrideBuilder<T, S, I, N> mergeOverride(BiFunction<? super T, ? super T, ? extends T> mergeOverride) {
		return new OverrideBuilder<>(this, mergeOverride, override);
	}

	public OverrideBuilder<T, S, I, N> or(T value) {
		return isOverridden() ? this : new OverrideBuilder<>(this, mergeOverride, value);
	}

	public T tryGet() {
		T value = override;

		if (isOverridden() && mergeOverride != null) {
			for (T inheritedValue : inheritedValues) {
				value = mergeOverride.apply(override, inheritedValue);
			}
		}

		return value;
	}

	public T get() {
		T value = tryGet();

		if (value == null && (configurator.getConcrete() == null || configurator.getConcrete())
				&& givenValueFunction != null) {
			if (inheritedValues.isEmpty()) {
				throw new ModabiException(t -> t.mustProvideValueForNonAbstract(valueFunction::apply, getNodeClass()));
			} else {
				throw new ModabiException(
						t -> t.mustOverrideIncompatibleProperties(valueFunction::apply, getNodeClass(), inheritedValues));
			}
		}

		return value;
	}

	private boolean isOverridden() {
		return override != null;
	}

	@SuppressWarnings("unchecked")
	private Class<N> getNodeClass() {
		return (Class<N>) configurator.getNodeType().getRawType();
	}
}
