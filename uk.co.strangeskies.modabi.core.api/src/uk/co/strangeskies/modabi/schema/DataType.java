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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public interface DataType<T> extends BindingNode<T, DataType<T>, DataType.Effective<T>> {
	interface Effective<T> extends DataType<T>, BindingNode.Effective<T, DataType<T>, Effective<T>> {
		@Override
		default void process(NodeProcessor context) {
			context.accept(this);
		}

		@Override
		DataType.Effective<? super T> baseType();

		@Override
		default List<DataType.Effective<? super T>> base() {
			List<DataType.Effective<? super T>> base = new ArrayList<>();

			DataType.Effective<? super T> baseComponent = this;
			do {
				base.add(baseComponent);
				baseComponent = baseComponent.baseType();
			} while (baseComponent != null);

			return base;
		}

	}

	Boolean isPrivate();

	DataType<? super T> baseType();

	@Override
	default List<? extends DataType<? super T>> base() {
		return Arrays.asList(baseType());
	}

	@Override
	default TypeToken<DataType<T>> getThisType() {
		return new TypeToken<DataType<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, getDataType());
	}
}
