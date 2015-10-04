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
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import uk.co.strangeskies.modabi.schema.building.ChildBuilder;
import uk.co.strangeskies.reflection.TypeToken;

public interface ModelConfigurator<T>
		extends AbstractComplexNodeConfigurator<ModelConfigurator<T>, Model<T>, T> {
	@Override
	default <V extends T> ModelConfigurator<V> baseModel(
			Model<? super V> baseModel) {
		return baseModel(Arrays.asList(baseModel));
	}

	@Override
	default <V extends T> ModelConfigurator<V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return baseModel(Arrays.asList(baseModel));
	}

	@Override
	<V extends T> ModelConfigurator<V> baseModel(
			List<? extends Model<? super V>> baseModel);

	@SuppressWarnings("unchecked")
	@Override
	default <V extends T> ModelConfigurator<V> dataType(Class<V> dataType) {
		return (ModelConfigurator<V>) AbstractComplexNodeConfigurator.super.dataType(
				dataType);
	}

	@Override
	<V extends T> ModelConfigurator<V> dataType(
			TypeToken<? extends V> bindingClass);

	@SuppressWarnings("unchecked")
	@Override
	default ModelConfigurator<? extends T> dataType(AnnotatedType dataType) {
		return (ModelConfigurator<? extends T>) AbstractComplexNodeConfigurator.super.dataType(
				dataType);
	}

	@SuppressWarnings("unchecked")
	@Override
	default ModelConfigurator<? extends T> dataType(Type dataType) {
		return (ModelConfigurator<? extends T>) AbstractComplexNodeConfigurator.super.dataType(
				dataType);
	}

	@Override
	default public ModelConfigurator<T> addChild(
			Function<ChildBuilder, SchemaNodeConfigurator<?, ?>> propertyConfiguration) {
		propertyConfiguration.apply(addChild()).create();
		return this;
	}
}
