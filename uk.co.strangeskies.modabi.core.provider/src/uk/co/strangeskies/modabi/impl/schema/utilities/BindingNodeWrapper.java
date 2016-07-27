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
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public abstract class BindingNodeWrapper<T, B extends BindingNode<? super T, B>, S extends BindingNode<T, S>>
		implements BindingNode<T, S> {
	private final BindingNode<?, ?> component;
	private final B base;

	private final TypeToken<T> dataType;

	public BindingNodeWrapper(BindingNode<T, ?> component) {
		this.component = component;
		base = null;
		dataType = component.dataType();
	}

	@SuppressWarnings("unchecked")
	public BindingNodeWrapper(B base, BindingNode<?, ?> component) {
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

		testOverrideEqual(BindingNode::inputBindingStrategy);
		testOverrideEqual(BindingNode::outputBindingStrategy);

		testOverride(BindingNode::inputBindingType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverride(BindingNode::outputBindingType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverride(BindingNode::outputBindingFactoryType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverrideEqual(BindingNode::unbindingMethodName);

		testOverrideEqual(BindingNode::providedOutputBindingMethodParameterNames);

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

	public BindingNode<?, ?> getComponent() {
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
	public final InputBindingStrategy inputBindingStrategy() {
		return component.inputBindingStrategy();
	}

	@Override
	public final TypeToken<?> inputBindingType() {
		return component.inputBindingType();
	}

	@Override
	public final OutputBindingStrategy outputBindingStrategy() {
		return component.outputBindingStrategy();
	}

	@Override
	public final TypeToken<?> outputBindingType() {
		return component.outputBindingType();
	}

	@Override
	public final String unbindingMethodName() {
		return component.unbindingMethodName();
	}

	@Override
	public Boolean outputBindingMethodUnchecked() {
		return component.outputBindingMethodUnchecked();
	}

	@Override
	public final Invokable<?, ?> outputBindingMethod() {
		return component.outputBindingMethod();
	}

	@Override
	public final TypeToken<?> outputBindingFactoryType() {
		return component.outputBindingFactoryType();
	}

	@Override
	public final QualifiedName name() {
		return component.name();
	}

	@Override
	public final List<ChildNode<?>> children() {
		return component.children();
	}

	@Override
	public final List<QualifiedName> providedOutputBindingMethodParameterNames() {
		return component.providedOutputBindingMethodParameterNames();
	}

	@Override
	public final List<DataNode<?>> providedOutputBindingMethodParameters() {
		return component.providedOutputBindingMethodParameters();
	}

	@Override
	public String toString() {
		return name().toString();
	}

	@Override
	public BindingNode<?, ?> root() {
		return component.root();
	}

	@Override
	public Schema schema() {
		return component.schema();
	}

	@Override
	public SchemaNodeConfigurator<?, S> configurator() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
