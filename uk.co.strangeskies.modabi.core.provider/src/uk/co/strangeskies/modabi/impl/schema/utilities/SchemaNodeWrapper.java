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

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;

import uk.co.strangeskies.modabi.ModabiException;
import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.BindingPoint;
import uk.co.strangeskies.modabi.schema.ChildBindingPoint;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.modabi.schema.SchemaNodeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.Types;

public class SchemaNodeWrapper implements SchemaNode {
	private final SchemaNode component;
	private final SchemaNode base;

	public SchemaNodeWrapper(SchemaNode component) {
		this.component = component;
		base = null;
	}

	@SuppressWarnings("unchecked")
	public SchemaNodeWrapper(SchemaNode base, SchemaNode component) {
		this.component = component;
		this.base = base;

		try {
			if (base.dataType() != null) {
				dataType = (TypeToken<T>) component.dataType().withLooseCompatibilityTo(base.dataType()).infer();
			} else {
				dataType = (TypeToken<T>) component.dataType().infer();
			}
		} catch (Exception e) {
			throw getOverrideException(SchemaNode::dataType, base.dataType(), component.dataType(), e);
		}

		testOverrideEqual(SchemaNode::inputBindingStrategy);
		testOverrideEqual(SchemaNode::outputBindingStrategy);

		testOverride(SchemaNode::inputBindingType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverride(SchemaNode::outputBindingType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverride(SchemaNode::outputBindingFactoryType,
				(baseValue, overrideValue) -> Types.isAssignable(overrideValue.getType(), baseValue.getType()));

		testOverrideEqual(SchemaNode::outputBindingMethod);

		testOverrideEqual(SchemaNode::providedOutputBindingMethodParameters);

		testOverride(SchemaNode::children, (baseValue, overrideValue) -> overrideValue.containsAll(baseValue));
	}

	protected <P> void testOverrideEqual(Function<? super SchemaNode, P> property) {
		testOverride(property, (a, b) -> a.equals(b));
	}

	protected <P> void testOverride(Function<? super SchemaNode, P> property, BiPredicate<? super P, ? super P> test) {
		P baseValue = property.apply(base);

		if (baseValue != null) {
			@SuppressWarnings("unchecked")
			P overrideValue = property.apply(component);

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

	protected <P> ModabiException getOverrideException(Function<? super SchemaNode, ? extends P> property, P baseValue,
			P overrideValue, Exception cause) {
		return new ModabiException(
				t -> t.cannotOverrideIncompatibleProperty(base, property::apply, SchemaNode.class, baseValue, overrideValue),
				cause);
	}

	public SchemaNode getComponent() {
		return component;
	}

	public SchemaNode getBase() {
		return base;
	}

	@Override
	public String toString() {
		return component.toString();
	}

	@Override
	public Schema schema() {
		return component.schema();
	}

	@Override
	public SchemaNodeConfigurator configurator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<SchemaNode> baseNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BindingPoint<?> parentBindingPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ChildBindingPoint<?>> childBindingPoints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof SchemaNodeWrapper))
			return false;

		SchemaNodeWrapper that = (SchemaNodeWrapper) obj;

		return Objects.equals(component, that.component) && Objects.equals(base, that.base);
	}

	@Override
	public int hashCode() {
		return Objects.hash(component) ^ Objects.hash(base);
	}
}
