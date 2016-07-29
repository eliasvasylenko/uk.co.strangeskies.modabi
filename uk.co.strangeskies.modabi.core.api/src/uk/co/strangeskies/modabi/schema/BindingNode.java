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

import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.reflection.Invokable;
import uk.co.strangeskies.reflection.TypeToken;

public interface BindingNode<T, S extends BindingNode<T, S>> extends SchemaNode<S> {
	Invokable<?, ?> outputBindingMethod();

	List<DataNode<?>> providedOutputBindingMethodParameters();

	List<? extends BindingNode<? super T, ?>> base();

	TypeToken<T> dataType();

	InputBindingStrategy inputBindingStrategy();

	TypeToken<?> inputBindingType();

	OutputBindingStrategy outputBindingStrategy();

	TypeToken<?> outputBindingType();

	Boolean outputBindingMethodUnchecked();

	TypeToken<?> outputBindingFactoryType();

	@Override
	BindingNodeConfigurator<?, S, T> configurator();
}
