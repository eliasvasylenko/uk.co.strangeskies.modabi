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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.modabi.schema.node.BindingNode;
import uk.co.strangeskies.reflection.TypeToken;

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, ?, ?>, T>
		extends SchemaNodeConfigurator<S, N> {
	<V extends T> BindingNodeConfigurator<?, ?, V> dataType(TypeToken<? extends V> dataType);

	@SuppressWarnings("unchecked")
	default BindingNodeConfigurator<?, ?, ?> dataType(Type dataType) {
		return dataType((TypeToken<? extends T>) TypeToken.over(dataType));
	}

	default <V extends T> BindingNodeConfigurator<?, ?, V> dataClass(
			Class<V> dataClass) {
		return (BindingNodeConfigurator<?, ?, V>) dataType(TypeToken
				.over(dataClass));
	}

	S bindingStrategy(BindingStrategy strategy);

	S bindingType(TypeToken<?> bindingType);

	default S bindingType(Type bindingType) {
		return bindingType(TypeToken.over(bindingType));
	}

	S unbindingStrategy(UnbindingStrategy strategy);

	S unbindingFactoryType(TypeToken<?> factoryType);

	default S unbindingFactoryType(Type factoryType) {
		return unbindingFactoryType(TypeToken.over(factoryType));
	}

	S unbindingType(TypeToken<?> unbindingType);

	default S unbindingType(Type unbindingType) {
		return unbindingType(TypeToken.over(unbindingType));
	}

	S unbindingMethod(String unbindingMethod);

	S unbindingMethodUnchecked(boolean unchecked);

	S providedUnbindingMethodParameters(List<QualifiedName> parameterNames);

	default S providedUnbindingMethodParameters(QualifiedName... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames));
	}

	S providedUnbindingMethodParameters(String... parameterNames);
}
