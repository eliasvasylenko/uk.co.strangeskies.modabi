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

import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.impl.schema.DataNodeConfiguratorImpl;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataNodeConfigurator;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.SchemaNode;
import uk.co.strangeskies.reflection.TypeToken;

public final class DataNodeWrapper<T> extends BindingChildNodeWrapper<T, DataNode<? super T>, DataNode<T>>
		implements DataNode<T> {
	private final DataType<? super T> type;

	protected DataNodeWrapper(DataType<T> component) {
		super(component);
		type = component;
	}

	protected DataNodeWrapper(DataNode<? super T> base, DataType<? super T> component) {
		super(base, component);
		type = component;

		for (Object providedValue : base.providedValues())
			if (base.providedValues() != null
					&& !TypeToken.over(component.dataType().getType()).isAssignableFrom(providedValue.getClass()))
				throw this.<Object>getOverrideException(n -> n.providedValues(), base.providedValues(), component.dataType(),
						null);

		if (!component.base().containsAll(base.base()))
			throw this.<Object>getOverrideException(DataNode::type, base.base(), component.base(), null);
	}

	protected DataNodeWrapper(DataNode<T> node) {
		super(node, node);
		type = node.type();
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataNodeConfigurator<T> configurator() {
		DataNodeConfigurator<T> baseConfigurator;
		if (getBase() != null) {
			baseConfigurator = (DataNodeConfigurator<T>) getBase().configurator();
		} else {
			baseConfigurator = new DataNodeConfiguratorImpl<>((SchemaNodeConfigurationContext) null);
		}

		return baseConfigurator.type(type);
	}

	public static <T> DataNodeWrapper<T> wrapType(DataType<T> component) {
		return new DataNodeWrapper<>(component);
	}

	public static <T> DataNodeWrapper<? extends T> wrapNodeWithOverrideType(DataNode<T> node, DataType<?> override) {
		/*
		 * This cast isn't strictly going to be valid according to the exact erased
		 * type, but the runtime checks in the constructor should ensure the types
		 * do fit the bounds
		 */
		@SuppressWarnings("unchecked")
		DataType<? super T> castOverride = (DataType<? super T>) override;
		return new DataNodeWrapper<>(node, castOverride);
	}

	public static <T> DataNodeWrapper<T> wrapNode(DataNode<T> node) {
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
	public DataType<? super T> type() {
		return type;
	}

	@Override
	public SchemaNode<?> parent() {
		return getBase() == null ? null : getBase().parent();
	}
}
