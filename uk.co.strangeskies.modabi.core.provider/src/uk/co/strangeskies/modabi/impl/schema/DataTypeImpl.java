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
package uk.co.strangeskies.modabi.impl.schema;

import uk.co.strangeskies.modabi.Schema;
import uk.co.strangeskies.modabi.schema.DataType;
import uk.co.strangeskies.modabi.schema.DataTypeConfigurator;

public class DataTypeImpl<T> extends BindingNodeImpl<T, DataType<T>> implements DataType<T> {
	private final Boolean isPrivate;

	private final DataType<? super T> baseType;

	private final Schema schema;

	public DataTypeImpl(DataTypeConfiguratorImpl<T> configurator) {
		super(configurator);

		isPrivate = configurator.getPrivate() != null && configurator.getPrivate();

		baseType = configurator.getBaseType();

		schema = configurator.getSchema();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DataTypeConfigurator<T> configurator() {
		return (DataTypeConfigurator<T>) super.configurator();
	}

	@Override
	public Boolean isPrivate() {
		return isPrivate;
	}

	@Override
	public DataType<? super T> baseType() {
		return baseType;
	}

	@Override
	public DataType<T> root() {
		return this;
	}

	@Override
	public Schema schema() {
		return schema;
	}
}
