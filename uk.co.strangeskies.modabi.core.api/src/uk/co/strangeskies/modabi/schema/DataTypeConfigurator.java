/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
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
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.reflection.TypeToken;

public interface DataTypeConfigurator<T> extends BindingNodeConfigurator<DataTypeConfigurator<T>, DataType<T>, T>,
		RootNodeConfigurator<DataTypeConfigurator<T>, DataType<T>, T> {
	/**
	 * @param dataType
	 *          The value to be returned by {@link DataType#dataType()}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	default <U extends T> DataTypeConfigurator<U> dataType(Class<U> dataType) {
		Type type = dataType;
		return (DataTypeConfigurator<U>) RootNodeConfigurator.super.dataType(type);
	}

	@Override
	<U extends T> DataTypeConfigurator<U> dataType(TypeToken<? extends U> dataClass);

	@SuppressWarnings("unchecked")
	@Override
	default DataTypeConfigurator<? extends T> dataType(AnnotatedType dataType) {
		return (DataTypeConfigurator<? extends T>) RootNodeConfigurator.super.dataType(dataType);
	}

	@SuppressWarnings("unchecked")
	@Override
	default DataTypeConfigurator<? extends T> dataType(Type dataType) {
		return (DataTypeConfigurator<? extends T>) RootNodeConfigurator.super.dataType(dataType);
	}

	<U extends T> DataTypeConfigurator<U> baseType(DataType<? super U> baseType);

	@Override
	default public DataTypeConfigurator<T> addChild(
			Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> propertyConfiguration) {
		RootNodeConfigurator.super.addChild(propertyConfiguration);
		return this;
	}

	@Override
	ChildBuilder addChild();

	@Override
	default TypeToken<DataType<T>> getNodeType() {
		return new TypeToken<DataType<T>>() {};
	}
}
