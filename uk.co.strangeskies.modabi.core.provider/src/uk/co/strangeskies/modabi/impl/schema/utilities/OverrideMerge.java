/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.schema.utilities;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.impl.schema.SchemaNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.schema.SchemaNode;

public class OverrideMerge<S extends SchemaNode<? extends S, ?>, C extends SchemaNodeConfiguratorImpl<?, ? extends S>> {
	public class OverrideOptional<T> {
		private Function<S, T> valueFunction;
		private BiPredicate<? super T, ? super T> validation;

		private T override;
		private Set<T> values;

		private OverrideOptional(Function<S, T> valueFunction) {
			this.valueFunction = valueFunction;

			values = getOverridenValues(valueFunction);
		}

		private OverrideOptional(OverrideOptional<T> from) {
			override = from.override;
			values = from.values;
		}

		public OverrideOptional<T> orDefault(T value) {
			if (node == null || (node.isAbstract() == null || !node.isAbstract())
					&& !isOverridden()) {
				return or(() -> value);
			} else {
				return this;
			}
		}

		public OverrideOptional<T> orMerged(
				Function<? super Collection<T>, ? extends T> merge) {
			if (values != null && !values.isEmpty()) {
				return or(() -> merge.apply(values));
			} else {
				return this;
			}
		}

		public OverrideOptional<T> orMerged(BinaryOperator<T> merge) {
			return orMerged(s -> s.stream().reduce(merge).get());
		}

		private OverrideOptional<T> or(Supplier<T> supplier) {
			if (override == null) {
				OverrideOptional<T> optional = new OverrideOptional<>(this);
				optional.override = supplier.get();

				return optional;
			} else {
				return this;
			}
		}

		private OverrideOptional<T> or() {
			return or(() -> valueFunction.apply(node));
		}

		public T tryGet() {
			if (isOverridden() && validation != null) {
				for (T value : values) {
					if (!validation.test(override, value)) {
						throw new SchemaException("Cannot override incompatible property '"
								+ value + "' with '" + override + "'");
					}
				}
			}

			return override;
		}

		public T get() {
			override = tryGet();

			if (override == null && node != null
					&& (node.isAbstract() == null || !node.isAbstract()))
				throw new SchemaException("No value '" + valueFunction
						+ "' available for non-abstract node '" + node.getName() + "'");

			return override;
		}

		private boolean isOverridden() {
			boolean overridden = override != null;

			if (!overridden) {
				if (values.size() == 1) {
					override = values.iterator().next();
				} else if (values.size() > 1) {
					throw new SchemaException(
							"No override provided for incompatible properties '" + values
									+ "'");
				}
			}

			return overridden;
		}

		public OverrideOptional<T> validate(
				BiPredicate<? super T, ? super T> validation) {
			OverrideOptional<T> optional = new OverrideOptional<>(this);
			if (optional.validation == null) {
				optional.validation = validation;
			} else {
				optional.validation = (a, b) -> validation.test(a, b)
						&& optional.validation.test(a, b);
			}

			return optional;
		}
	}

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

	@SuppressWarnings("unchecked")
	public <T> Set<T> getOverridenValues(Function<S, T> valueFunction) {
		return configurator.getOverriddenNodes().stream()
				.map(n -> valueFunction.apply((S) n.effective()))
				.filter(Objects::nonNull).collect(Collectors.toSet());
	}

	public <T> OverrideOptional<T> getOverride(Function<S, T> valueFunction) {
		return new OverrideOptional<>(valueFunction).or();
	}

	public <T> OverrideOptional<T> getOverride(Function<S, T> valueFunction,
			T override) {
		return new OverrideOptional<>(valueFunction).or(() -> override);
	}
}
