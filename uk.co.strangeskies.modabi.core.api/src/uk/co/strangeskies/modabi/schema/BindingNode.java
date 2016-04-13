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

import java.lang.reflect.Executable;
import java.util.List;

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.BindingStrategy;
import uk.co.strangeskies.modabi.processing.UnbindingStrategy;
import uk.co.strangeskies.reflection.TypeToken;

public interface BindingNode<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNode<S, E> {
	interface Effective<T, S extends BindingNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingNode<T, S, E>, SchemaNode.Effective<S, E> {
		Executable getUnbindingMethod();

		List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters();

		@Override
		List<? extends BindingNode.Effective<? super T, ?, ?>> base();
	}

	List<? extends BindingNode<? super T, ?, ?>> base();

	default String getDataTypeString() {
		return getDataType() == null ? null : getDataType().toString(schema().getImports());
	}

	TypeToken<T> getDataType();

	BindingStrategy getBindingStrategy();

	default String getBindingTypeString() {
		return getBindingType() == null ? null : getBindingType().toString(schema().getImports());
	}

	TypeToken<?> getBindingType();

	UnbindingStrategy getUnbindingStrategy();

	default String getUnbindingTypeString() {
		return getUnbindingType() == null ? null : getUnbindingType().toString(schema().getImports());
	}

	TypeToken<?> getUnbindingType();

	String getUnbindingMethodName();

	Boolean isUnbindingMethodUnchecked();

	default String getUnbindingFactoryTypeString() {
		return getUnbindingFactoryType() == null ? null : getUnbindingFactoryType().toString(schema().getImports());
	}

	TypeToken<?> getUnbindingFactoryType();

	List<QualifiedName> getProvidedUnbindingMethodParameterNames();
}
