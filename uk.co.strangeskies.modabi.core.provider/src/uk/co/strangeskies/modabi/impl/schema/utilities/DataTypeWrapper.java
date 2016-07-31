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

import uk.co.strangeskies.modabi.schema.DataNode;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;

public class DataTypeWrapper<T> extends BindingNodeWrapper<T, DataType<? super T>, DataType<T>> implements DataType<T> {
	public DataTypeWrapper(DataNode<T> component) {
		super(component);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataNode<? super T> getComponent() {
		return (DataNode<? super T>) super.getComponent();
	}

	@Override
	public DataType<? super T> baseType() {
		return getComponent().type();
	}

	@Override
	public DataTypeConfigurator<T> configurator() {
		return null;
	}

	@Override
	public Boolean isPrivate() {
		return null;
	}
}
