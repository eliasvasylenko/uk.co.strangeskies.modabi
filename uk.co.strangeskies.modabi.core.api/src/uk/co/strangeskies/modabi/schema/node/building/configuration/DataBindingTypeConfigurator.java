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

import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.node.DataBindingType;
import uk.co.strangeskies.modabi.schema.node.building.ChildBuilder;
import uk.co.strangeskies.reflection.TypeToken;

public interface DataBindingTypeConfigurator<T>
		extends
		BindingNodeConfigurator<DataBindingTypeConfigurator<T>, DataBindingType<T>, T> {
	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getName()}.
	 * @return
	 */
	@Override
	DataBindingTypeConfigurator<T> isAbstract(boolean hidden);

	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getName()}.
	 * @return
	 */
	DataBindingTypeConfigurator<T> isPrivate(boolean hidden);

	/**
	 * @param name
	 *          The value to be returned by {@link DataBindingType#getBaseType()}.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	default <U extends T> DataBindingTypeConfigurator<U> dataClass(
			Class<U> dataClass) {
		return (DataBindingTypeConfigurator<U>) BindingNodeConfigurator.super
				.dataClass(dataClass);
	}

	@Override
	<U extends T> DataBindingTypeConfigurator<U> dataType(TypeToken<U> dataClass);

	<U extends T> DataBindingTypeConfigurator<U> baseType(
			DataBindingType<? super U> baseType);

	@Override
	default public DataBindingTypeConfigurator<T> addChild(
			Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> propertyConfiguration) {
		propertyConfiguration.apply(addChild()).create();
		return this;
	}

	@Override
	ChildBuilder addChild();
}