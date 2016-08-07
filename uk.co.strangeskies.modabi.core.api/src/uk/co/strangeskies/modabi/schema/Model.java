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

import java.util.List;

import uk.co.strangeskies.modabi.NodeProcessor;
import uk.co.strangeskies.reflection.TypeParameter;
import uk.co.strangeskies.reflection.TypeToken;

public interface Model<T> extends BindingNode<T, Model<T>>, RootNode<T, Model<T>> {
	@Override
	default void process(NodeProcessor context) {
		context.accept(this);
	}

	List<Model<? super T>> baseModel();

	@Override
	default List<Model<? super T>> base() {
		return baseModel();
	}

	@Override
	default TypeToken<Model<T>> getThisType() {
		return new TypeToken<Model<T>>() {}.withTypeArgument(new TypeParameter<T>() {}, dataType());
	}

	@Override
	ModelConfigurator<T> configurator();
}
