package uk.co.strangeskies.modabi.impl.schema.utilities;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.empty;
import static java.util.stream.Stream.of;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;

/**
 * A class for building override rules to determine the value of a property of a
 * schema node. A "property", in this context, may include for example the
 * {@link S#name() name} of a node or the {@link BindingNode#dataType() type} of
 * a binding node.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the value of the property to override
 * @param <N>
 *          the type of the node
 */
public class OverrideBuilder<T, S> {
	private final boolean concrete;

	private final S node;

	private final Function<? super S, ? extends T> valueFunction;

	private final BiFunction<? super T, ? super T, ? extends T> mergeOverride;

	private final Set<T> inheritedValues;
	private final T override;

	public OverrideBuilder(Collection<? extends S> nodes, Function<? super S, ? extends T> valueFunction, T override) {
		this.node = nodes;

		this.valueFunction = valueFunction;

		mergeOverride = validateOverrideFunction(Objects::equals);

		inheritedValues = nodes
				.stream()
				.map(valueFunction::apply)
				.flatMap(value -> value != null ? of(value) : empty())
				.collect(toSet());

		if (override == null && inheritedValues.size() == 1) {
			override = inheritedValues.iterator().next();
		}

		this.override = override;
	}

	private OverrideBuilder(OverrideBuilder<T, S> from, BiFunction<? super T, ? super T, ? extends T> mergeOverride,
			T override) {
		node = from.node;

		concrete = from.concrete;

		valueFunction = from.valueFunction;

		inheritedValues = from.inheritedValues;

		this.mergeOverride = mergeOverride;
		this.override = override;
	}

	/**
	 * @return all unique values inherited from overridden and base nodes, or
	 *         given directly from their configurators
	 */
	public Set<T> getValues() {
		return inheritedValues;
	}

	@SuppressWarnings("unchecked")
	public Class<S> getNodeClass() {
		return (Class<S>) (Class<?>) node.getThisType().getRawType();
	}

	public OverrideBuilder<T, S> orDefault(T value) {
		if (concrete) {
			return or(value);
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S> orMerged(Function<? super Collection<T>, ? extends T> merge) {
		if (!inheritedValues.isEmpty() && !isOverridden()) {
			T merged = merge.apply(inheritedValues);

			if (merged == null) {
				throw new ModabiException(
						t -> t.cannotMergeIncompatibleProperties(node, valueFunction::apply, getNodeClass(), inheritedValues));
			}

			return or(merged);
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, S> orMerged(BinaryOperator<T> merge) {
		return orMerged(s -> s.stream().reduce(merge).orElseThrow(() -> new ModabiException(
				t -> t.cannotMergeIncompatibleProperties(node, valueFunction::apply, getNodeClass(), inheritedValues))));
	}

	/**
	 * The override validation predicate takes two parameters, the first is a
	 * potential overriding value, and the second is a value it must override. The
	 * predicate passes if the override relationship cannot be satisfied.
	 * 
	 * <p>
	 * If the predicate does not pass then an exception will be thrown
	 * automatically.
	 * 
	 * @param mergeOverride
	 *          the override merge function
	 * @return a new builder instance incorporating the given override behavior
	 */
	public OverrideBuilder<T, S> validateOverride(BiPredicate<? super T, ? super T> validation) {
		return mergeOverride(validateOverrideFunction(validation));
	}

	private BiFunction<? super T, ? super T, ? extends T> validateOverrideFunction(
			BiPredicate<? super T, ? super T> validation) {
		return (a, b) -> {
			if (!validation.test(a, b)) {
				throw new ModabiException(
						t -> t.cannotOverrideIncompatibleProperty(node, valueFunction::apply, getNodeClass(), b, a));
			}

			return a;
		};
	}

	/**
	 * The override merge function takes two parameters, the first is a potential
	 * overriding value, and the second is a value it must override. The function
	 * returns an overriding value which satisfies this relationship, or throws an
	 * exception if the override relationship cannot be satisfied.
	 * 
	 * @param mergeOverride
	 *          the override merge function
	 * @return a new builder instance incorporating the given override behavior
	 */
	public OverrideBuilder<T, S> mergeOverride(BiFunction<? super T, ? super T, ? extends T> mergeOverride) {
		return new OverrideBuilder<>(this, mergeOverride, override);
	}

	public OverrideBuilder<T, S> or(T value) {
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

		if (value == null && concrete) {
			if (inheritedValues.isEmpty()) {
				throw new ModabiException(t -> t.mustProvideValueForNonAbstract(node, valueFunction::apply, getNodeClass()));
			} else {
				throw new ModabiException(
						t -> t.mustOverrideIncompatibleProperties(node, valueFunction::apply, getNodeClass(), inheritedValues));
			}
		}

		return value;
	}

	private boolean isOverridden() {
		return override != null;
	}
}
