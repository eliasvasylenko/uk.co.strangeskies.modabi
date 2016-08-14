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
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;

/**
 * A class for building override rules to determine the value of a property of a
 * schema node. A "property", in this context, may include for example the
 * {@link SchemaNode#name() name} of a node or the {@link BindingNode#dataType()
 * type} of a binding node.
 * 
 * @author Elias N Vasylenko
 *
 * @param <T>
 *          the type of the value of the property to override
 * @param <C>
 *          the type of the configurator
 * @param <N>
 *          the type of the node
 */
public class OverrideBuilder<T, C extends SchemaNodeConfigurator<?, ?>, N extends SchemaNode<?>> {
	private final C configurator;
	private final N node;

	private final Function<? super N, ? extends T> valueFunction;
	private final Function<? super C, ? extends T> givenValueFunction;

	private final BiFunction<? super T, ? super T, ? extends T> mergeOverride;

	private final Set<T> inheritedValues;
	private final T override;

	public OverrideBuilder(C configurator, N node, Function<? super C, ? extends Collection<? extends N>> overridden,
			Function<? super N, ? extends T> valueFunction) {
		this(configurator, node, overridden, valueFunction, null);
	}

	public OverrideBuilder(C configurator, N node, Function<? super C, ? extends Collection<? extends N>> overridden,
			Function<? super N, ? extends T> valueFunction, Function<? super C, ? extends T> givenValueFunction) {
		this.configurator = configurator;
		this.node = node;

		this.valueFunction = valueFunction;
		this.givenValueFunction = givenValueFunction;

		mergeOverride = validateOverrideFunction(Objects::equals);

		inheritedValues = overridden.apply(configurator).stream().map(n -> {
			T value = valueFunction.apply(n);

			if (value != null) {
				return Stream.of(value);
			} else if (givenValueFunction == null) {
				return Stream.<T>empty();
			} else {
				List<C> values = new ArrayList<>();

				@SuppressWarnings("unchecked")
				C c = (C) n.configurator();
				values.add(c);

				Set<SchemaNode<?>> contains = new HashSet<>();
				contains.add(n);

				for (int i = 0; i < values.size(); i++) {
					C nci = values.get(i);

					for (N nn : overridden.apply(nci)) {
						@SuppressWarnings("unchecked")
						C nnc = (C) nn.configurator();

						if (contains.add(nn)) {
							values.add(nnc);
						}
					}
				}

				return values.stream().map(i -> givenValueFunction.apply(i)).filter(Objects::nonNull);
			}
		}).flatMap(Function.identity()).collect(Collectors.toSet());

		T override = givenValueFunction.apply(configurator);

		if (override == null && inheritedValues.size() == 1) {
			override = inheritedValues.iterator().next();
		}

		this.override = override;
	}

	private OverrideBuilder(OverrideBuilder<T, C, N> from, BiFunction<? super T, ? super T, ? extends T> validation,
			T override) {
		configurator = from.configurator;
		node = from.node;

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

	public OverrideBuilder<T, C, N> orDefault(T value) {
		if (givenValueFunction == null
				|| (configurator.getConcrete() == null || configurator.getConcrete()) && !isOverridden()) {
			return or(value);
		} else {
			return this;
		}
	}

	public OverrideBuilder<T, C, N> orMerged(Function<? super Collection<T>, ? extends T> merge) {
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

	public OverrideBuilder<T, C, N> orMerged(BinaryOperator<T> merge) {
		return orMerged(s -> s.stream().reduce(merge).orElseThrow(() -> new ModabiException(
				t -> t.cannotMergeIncompatibleProperties(node, valueFunction::apply, getNodeClass(), inheritedValues))));
	}

	public OverrideBuilder<T, C, N> validateOverride(BiPredicate<? super T, ? super T> validation) {
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

	public OverrideBuilder<T, C, N> mergeOverride(BiFunction<? super T, ? super T, ? extends T> mergeOverride) {
		return new OverrideBuilder<>(this, mergeOverride, override);
	}

	public OverrideBuilder<T, C, N> or(T value) {
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

	@SuppressWarnings("unchecked")
	private Class<N> getNodeClass() {
		return (Class<N>) configurator.getNodeType().getRawType();
	}
}