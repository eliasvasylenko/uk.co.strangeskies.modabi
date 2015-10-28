/*
 * Copyright (C) 2015 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.api.
 *
 * uk.co.strangeskies.modabi.core.api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.api.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.schema;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

import uk.co.strangeskies.modabi.ValueResolution;
import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.DataNode.Format;
import uk.co.strangeskies.reflection.TypeToken;

public interface DataNodeConfigurator<T> extends
		BindingChildNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, T>,
		SchemaNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>> {
	public <U extends T> DataNodeConfigurator<U> type(DataType<U> type);

	@SuppressWarnings("unchecked")
	@Override
	default public <U extends T> DataNodeConfigurator<U> dataType(
			Class<U> dataClass) {
		Type type = dataClass;
		return (DataNodeConfigurator<U>) BindingChildNodeConfigurator.super.dataType(
				type);
	}

	@Override
	public <V extends T> DataNodeConfigurator<V> dataType(
			TypeToken<? extends V> dataType);

	@SuppressWarnings("unchecked")
	@Override
	default public DataNodeConfigurator<? extends T> dataType(
			AnnotatedType dataType) {
		return (DataNodeConfigurator<? extends T>) BindingChildNodeConfigurator.super.dataType(
				dataType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public default DataNodeConfigurator<? extends T> dataType(Type dataType) {
		return (DataNodeConfigurator<? extends T>) BindingChildNodeConfigurator.super.dataType(
				dataType);
	}

	public DataNodeConfigurator<T> provideValue(DataSource dataSource);

	public DataNodeConfigurator<T> valueResolution(
			ValueResolution valueResolution);

	public DataNodeConfigurator<T> nullIfOmitted(boolean nullIfOmitted);

	public DataNodeConfigurator<T> format(Format format);
}
