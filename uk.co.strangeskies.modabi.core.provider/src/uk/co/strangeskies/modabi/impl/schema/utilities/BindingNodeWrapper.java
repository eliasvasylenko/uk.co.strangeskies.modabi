/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import uk.co.strangeskies.modabi.Abstractness;
import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.modabi.schema.BindingNode;
import uk.co.strangeskies.modabi.schema.ChildNode;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeWrapper<T, B extends BindingNode.Effective<? super T, ?, ?>, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		implements BindingNode.Effective<T, S, E> {
	private final BindingNode.Effective<?, ?, ?> component;
	private final B base;

	private final TypeToken<T> dataType;

	public BindingNodeWrapper(BindingNode.Effective<T, ?, ?> component) {
		this.component = component;
		base = null;
		dataType = component.dataType();
	}

	@SuppressWarnings("unchecked")
	public BindingNodeWrapper(B base, BindingNode.Effective<?, ?, ?> component) {
		this.component = component;
		this.base = base;

		try {
			if (base.dataType() != null) {
				dataType = (TypeToken<T>) component.dataType().withLooseCompatibilityTo(base.dataType()).infer();
			} else {
				dataType = (TypeToken<T>) component.dataType().infer();
			}
		} catch (Exception e) {
			throw getOverrideException(BindingNode::dataType, base.dataType(), component.dataType(), e);
		}

		testOverrideEqual(BindingNode::bindingStrategy);
		testOverrideEqual(BindingNode::unbindingStrategy);

		testOverride(BindingNode::bindingType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverride(BindingNode::unbindingType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverride(BindingNode::unbindingFactoryType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverrideEqual(BindingNode::unbindingMethodName);

		testOverrideEqual(BindingNode::providedUnbindingMethodParameterNames);

		testOverride(BindingNode::children, (baseValue, overrideValue) -> overrideValue.containsAll(baseValue));
	}

	protected <P> void testOverrideEqual(Function<? super B, P> property) {
		testOverride(property, (a, b) -> a.equals(b));
	}

	protected <P> void testOverride(Function<? super B, P> property, BiPredicate<? super P, ? super P> test) {
		P baseValue = property.apply(base);

		if (baseValue != null) {
			@SuppressWarnings("unchecked")
			P overrideValue = property.apply((B) component);

			boolean success = false;
			Exception cause = null;
			try {
				success = test.test(baseValue, overrideValue);
			} catch (Exception e) {
				cause = e;
			}

			if (!success) {
				throw getOverrideException(property::apply, baseValue, overrideValue, cause);
			}
		}
	}

	protected <P> ModabiException getOverrideException(Function<? super B, ? extends P> property, P baseValue,
			P overrideValue, Exception cause) {
		return new ModabiException(
				t -> t.cannotOverrideIncompatibleProperty(property::apply, base, baseValue, overrideValue), cause);
	}

	public BindingNode.Effective<?, ?, ?> getComponent() {
		return component;
	}

	public B getBase() {
		return base;
	}

	@Override
	public Abstractness abstractness() {
		return component.abstractness();
	}

	@Override
	public final TypeToken<T> dataType() {
		return dataType;
	}

	@Override
	public final InputBindingStrategy bindingStrategy() {
		return component.bindingStrategy();
	}

	@Override
	public final TypeToken<?> bindingType() {
		return component.bindingType();
	}

	@Override
	public final OutputBindingStrategy unbindingStrategy() {
		return component.unbindingStrategy();
	}

	@Override
	public final TypeToken<?> unbindingType() {
		return component.unbindingType();
	}

	@Override
	public final String unbindingMethodName() {
		return component.unbindingMethodName();
	}

	@Override
	public Boolean unbindingMethodUnchecked() {
		return component.unbindingMethodUnchecked();
	}

	@Override
	public final Invokable<?, ?> unbindingMethod() {
		return component.unbindingMethod();
	}

	@Override
	public final TypeToken<?> unbindingFactoryType() {
		return component.unbindingFactoryType();
	}

	@Override
	public final QualifiedName name() {
		return component.name();
	}

	@Override
	public final List<ChildNode.Effective<?, ?>> children() {
		return component.children();
	}

	@Override
	public final List<QualifiedName> providedUnbindingMethodParameterNames() {
		return component.providedUnbindingMethodParameterNames();
	}

	@Override
	public final List<DataNode.Effective<?>> providedUnbindingMethodParameters() {
		return component.providedUnbindingMethodParameters();
	}

	@SuppressWarnings("unchecked")
	@Override
	public S source() {
		return (S) this;
	}

	@Override
	public String toString() {
		return name().toString();
	}

	@Override
	public BindingNode.Effective<?, ?, ?> root() {
		return component.root();
	}

	@Override
	public Schema schema() {
		return component.schema();
	}
}
