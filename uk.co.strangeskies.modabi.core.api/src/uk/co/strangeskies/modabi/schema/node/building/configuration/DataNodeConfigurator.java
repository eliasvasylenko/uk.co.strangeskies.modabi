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
package uk.co.strangeskies.modabi.schema.node.building.configuration;

import uk.co.strangeskies.modabi.io.DataSource;
import uk.co.strangeskies.modabi.schema.management.ValueResolution;
import uk.co.strangeskies.modabi.schema.node.DataNode;
import uk.co.strangeskies.modabi.schema.node.DataNode.Format;
import uk.co.strangeskies.modabi.schema.node.type.DataBindingType;
import uk.co.strangeskies.reflection.TypeToken;

public interface DataNodeConfigurator<T> extends
		BindingChildNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>, T>,
		SchemaNodeConfigurator<DataNodeConfigurator<T>, DataNode<T>> {
	public <U extends T> DataNodeConfigurator<U> type(
			DataBindingType<? super U> type);

	@SuppressWarnings("unchecked")
	@Override
	default public <U extends T> DataNodeConfigurator<U> dataClass(
			Class<U> dataClass) {
		return (DataNodeConfigurator<U>) BindingChildNodeConfigurator.super
				.dataClass(dataClass);
	}

	@Override
	public <U extends T> DataNodeConfigurator<U> dataType(TypeToken<U> dataClass);

	public DataNodeConfigurator<T> provideValue(DataSource dataSource);

	public DataNodeConfigurator<T> valueResolution(ValueResolution valueResolution);

	public DataNodeConfigurator<T> optional(boolean optional);

	public DataNodeConfigurator<T> nullIfOmitted(boolean nullIfOmitted);

	public DataNodeConfigurator<T> format(Format format);
}
