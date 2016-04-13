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
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.reflection.TypeToken;

public interface ComplexNodeConfigurator<T>
		extends BindingChildNodeConfigurator<ComplexNodeConfigurator<T>, ComplexNode<T>, T> {
	default <V extends T> ComplexNodeConfigurator<V> model(Model<? super V> baseModel) {
		return model(Arrays.asList(baseModel));
	}

	default <V extends T> ComplexNodeConfigurator<V> model(@SuppressWarnings("unchecked") Model<? super V>... baseModel) {
		return model(Arrays.asList(baseModel));
	}

	<V extends T> ComplexNodeConfigurator<V> model(List<? extends Model<? super V>> baseModel);

	@SuppressWarnings("unchecked")
	@Override
	default <V extends T> ComplexNodeConfigurator<V> dataType(Class<V> dataType) {
		return (ComplexNodeConfigurator<V>) BindingChildNodeConfigurator.super.dataType(dataType);
	}

	@SuppressWarnings("unchecked")
	@Override
	default ComplexNodeConfigurator<? extends T> dataType(AnnotatedType dataType) {
		return (ComplexNodeConfigurator<? extends T>) BindingChildNodeConfigurator.super.dataType(dataType);
	}

	@SuppressWarnings("unchecked")
	@Override
	default ComplexNodeConfigurator<? extends T> dataType(Type dataType) {
		return (ComplexNodeConfigurator<? extends T>) BindingChildNodeConfigurator.super.dataType(dataType);
	}

	@Override
	<V extends T> ComplexNodeConfigurator<V> dataType(TypeToken<? extends V> dataClass);

	ComplexNodeConfigurator<T> inline(boolean inline);
}
