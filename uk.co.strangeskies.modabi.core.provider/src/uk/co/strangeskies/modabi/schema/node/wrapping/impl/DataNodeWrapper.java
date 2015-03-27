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
package uk.co.strangeskies.modabi.schema.node.wrapping.impl;

import java.util.List;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.reflection.TypeLiteral;

public final class DataNodeWrapper<T>
		extends
		BindingChildNodeWrapper<T, DataBindingType.Effective<T>, DataNode.Effective<? super T>, DataNode<T>, DataNode.Effective<T>>
		implements DataNode.Effective<T> {
	public DataNodeWrapper(DataBindingType.Effective<T> component) {
		super(component);
	}

	public DataNodeWrapper(DataBindingType.Effective<T> component,
			DataNode.Effective<? super T> base) {
		super(component, base);

		String message = "Cannot override '" + base.getName() + "' with '"
				+ component.getName() + "'.";

		for (Object providedValue : base.providedValues())
			if (base.providedValues() != null
					&& !TypeLiteral.from(component.getDataType().getType())
							.isAssignableFrom(providedValue.getClass()))
				throw new SchemaException(message);

		DataBindingType.Effective<? super T> check = component;
		while (!check.equals(base.type())) {
			check = (DataBindingType.Effective<? super T>) check.baseType();
			if (check == null)
				throw new SchemaException(message);
		}
	}

	@Override
	public DataSource providedValueBuffer() {
		return getBase() == null ? null : getBase().providedValueBuffer();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> providedValues() {
		return getBase() == null ? null : (List<T>) getBase().providedValues();
	}

	@Override
	public ValueResolution valueResolution() {
		return getBase() == null ? null : getBase().valueResolution();
	}

	@Override
	public DataNode.Format format() {
		return getBase() == null ? null : getBase().format();
	}

	@Override
	public DataBindingType.Effective<T> type() {
		return getComponent();
	}

	@Override
	public Boolean optional() {
		return getBase() == null ? null : getBase().optional();
	}

	@Override
	public Boolean nullIfOmitted() {
		return getBase() == null ? null : getBase().nullIfOmitted();
	}
}