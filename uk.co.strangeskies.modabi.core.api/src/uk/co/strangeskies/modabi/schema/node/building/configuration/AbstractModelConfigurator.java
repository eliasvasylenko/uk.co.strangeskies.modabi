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

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.schema.node.AbstractComplexNode;
import uk.co.strangeskies.modabi.schema.node.Model;
import uk.co.strangeskies.reflection.TypeToken;

public interface AbstractModelConfigurator<S extends AbstractModelConfigurator<S, N, T>, N extends AbstractComplexNode<T, ?, ?>, T>
		extends BindingNodeConfigurator<S, N, T>, SchemaNodeConfigurator<S, N> {
	default <V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return this.<V> baseModel(Arrays.asList(baseModel));
	}

	<V extends T> AbstractModelConfigurator<?, ?, V> baseModel(
			List<? extends Model<? super V>> baseModel);

	@Override
	default <V extends T> AbstractModelConfigurator<?, ?, V> dataType(
			Class<V> dataType) {
		return (AbstractModelConfigurator<?, ?, V>) BindingNodeConfigurator.super
				.dataType(dataType);
	}

	@Override
	default AbstractModelConfigurator<?, ?, ? extends T> dataType(
			AnnotatedType dataType) {
		return (AbstractModelConfigurator<?, ?, ? extends T>) BindingNodeConfigurator.super
				.dataType(dataType);
	}

	@Override
	default AbstractModelConfigurator<?, ?, ? extends T> dataType(Type dataType) {
		return (AbstractModelConfigurator<?, ?, ? extends T>) BindingNodeConfigurator.super
				.dataType(dataType);
	}

	@Override
	<V extends T> AbstractModelConfigurator<?, ?, V> dataType(
			TypeToken<? extends V> dataClass);
}
