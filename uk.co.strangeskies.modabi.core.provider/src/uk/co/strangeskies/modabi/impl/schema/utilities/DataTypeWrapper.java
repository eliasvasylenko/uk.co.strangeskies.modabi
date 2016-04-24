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

public class DataTypeWrapper<T>
		extends BindingNodeWrapper<T, DataType.Effective<? super T>, DataType<T>, DataType.Effective<T>>
		implements DataType.Effective<T> {
	public DataTypeWrapper(DataNode.Effective<T> component) {
		super(component);
	}

	@SuppressWarnings("unchecked")
	@Override
	public DataNode.Effective<? super T> getComponent() {
		return (DataNode.Effective<? super T>) super.getComponent();
	}

	@Override
	public DataType.Effective<? super T> baseType() {
		return getComponent().type();
	}

	@Override
	public Boolean isPrivate() {
		return getBase() == null ? null : getBase().isPrivate();
	}
}
