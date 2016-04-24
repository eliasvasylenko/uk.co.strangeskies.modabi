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

import uk.co.strangeskies.modabi.SchemaException;
import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;

public final class DataNodeWrapper<T>
		extends BindingChildNodeWrapper<T, DataNode.Effective<? super T>, DataNode<T>, DataNode.Effective<T>>
		implements DataNode.Effective<T> {
	private final DataType.Effective<? super T> type;

	protected DataNodeWrapper(DataType.Effective<T> component) {
		super(component);
		type = component;
	}

	protected DataNodeWrapper(DataType.Effective<? super T> component, DataNode.Effective<? super T> base) {
		super(component, base);
		type = component;

		String message = "Cannot override '" + base.name() + "' with '" + component.name() + "'";

		for (Object providedValue : base.providedValues())
			if (base.providedValues() != null
					&& !TypeToken.over(component.getDataType().getType()).isAssignableFrom(providedValue.getClass()))
				throw new SchemaException(message);

		DataType.Effective<? super T> check = component;
		while (!check.equals(base.type())) {
			check = check.baseType();
			if (check == null)
				throw new SchemaException(message);
		}
	}

	protected DataNodeWrapper(DataNode.Effective<T> node) {
		super(node, node);
		type = node.type();
	}

	public static <T> DataNodeWrapper<T> wrapType(DataType.Effective<T> component) {
		return new DataNodeWrapper<>(component);
	}

	/*
	 * TODO this isn't really type safe when not just inferred...
	 */
	public static <T> DataNodeWrapper<T> wrapNodeWithOverrideType(DataType.Effective<? super T> override,
			DataNode.Effective<? super T> node) {
		return new DataNodeWrapper<>(override, node);
	}

	public static <T> DataNodeWrapper<T> wrapNode(DataNode.Effective<T> node) {
		return new DataNodeWrapper<>(node);
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
	public DataType.Effective<? super T> type() {
		return type;
	}

	@Override
	public SchemaNode.Effective<?, ?> parent() {
		return getBase() == null ? null : getBase().parent();
	}
}
