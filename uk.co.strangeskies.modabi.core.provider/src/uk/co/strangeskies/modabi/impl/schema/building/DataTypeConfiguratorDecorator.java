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
package uk.co.strangeskies.modabi.impl.schema.building;

import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;
import uk.co.strangeskies.reflection.TypeToken;

public class DataTypeConfiguratorDecorator<T> extends
		BindingNodeConfiguratorDecorator<DataTypeConfigurator<T>, DataType<T>, T> implements DataTypeConfigurator<T> {
	public DataTypeConfiguratorDecorator(DataTypeConfigurator<T> component) {
		super(component);
	}

	@Override
	public DataType<T> create() {
		return getComponent().create();
	}

	@Override
	public DataTypeConfigurator<T> isPrivate(boolean hidden) {
		setComponent(getComponent().isPrivate(hidden));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <U extends T> DataTypeConfigurator<U> baseType(DataType<? super U> baseType) {
		setComponent((DataTypeConfigurator<T>) getComponent().baseType(baseType));
		return (DataTypeConfigurator<U>) this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V extends T> DataTypeConfigurator<V> dataType(TypeToken<? extends V> bindingClass) {
		return (DataTypeConfigurator<V>) super.dataType(bindingClass);
	}

	@Override
	public String toString() {
		return getComponent().toString();
	}
}
