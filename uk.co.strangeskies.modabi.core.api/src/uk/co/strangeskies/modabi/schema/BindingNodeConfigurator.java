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

import uk.co.strangeskies.modabi.QualifiedName;
import uk.co.strangeskies.modabi.processing.InputBindingStrategy;
import uk.co.strangeskies.modabi.processing.OutputBindingStrategy;
import uk.co.strangeskies.reflection.AnnotatedTypes;
import uk.co.strangeskies.reflection.TypeToken;

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, ?, ?>, T> {
	BindingNodeConfigurator<?, ?, ? extends T> dataType(String dataType);

	<V extends T> BindingNodeConfigurator<?, ?, V> dataType(TypeToken<? extends V> dataType);

	default <V extends T> BindingNodeConfigurator<?, ?, V> dataType(Class<V> dataClass) {
		return dataType(TypeToken.over(dataClass));
	}

	@SuppressWarnings("unchecked")
	default BindingNodeConfigurator<?, ?, ? extends T> dataType(AnnotatedType dataType) {
		return dataType((TypeToken<? extends T>) TypeToken.over(dataType));
	}

	default BindingNodeConfigurator<?, ?, ? extends T> dataType(Type dataType) {
		return dataType(AnnotatedTypes.over(dataType));
	}

	S bindingStrategy(InputBindingStrategy strategy);

	S bindingType(String bindingType);

	S bindingType(TypeToken<?> bindingType);

	default S bindingType(AnnotatedType bindingType) {
		return bindingType(TypeToken.over(bindingType));
	}

	default S bindingType(Type bindingType) {
		return bindingType(TypeToken.over(AnnotatedTypes.over(bindingType)));
	}

	S unbindingStrategy(OutputBindingStrategy strategy);

	S unbindingFactoryType(String factoryType);

	S unbindingFactoryType(TypeToken<?> factoryType);

	default S unbindingFactoryType(AnnotatedType factoryType) {
		return unbindingFactoryType(TypeToken.over(factoryType));
	}

	default S unbindingFactoryType(Type factoryType) {
		return unbindingFactoryType(TypeToken.over(AnnotatedTypes.over(factoryType)));
	}

	S unbindingType(String unbindingType);

	S unbindingType(TypeToken<?> unbindingType);

	default S unbindingType(AnnotatedType unbindingType) {
		return unbindingType(TypeToken.over(unbindingType));
	}

	default S unbindingType(Type bindingType) {
		return unbindingType(TypeToken.over(AnnotatedTypes.over(bindingType)));
	}

	S unbindingMethod(String unbindingMethod);

	S unbindingMethodUnchecked(boolean unchecked);

	S providedUnbindingMethodParameters(List<QualifiedName> parameterNames);

	default S providedUnbindingMethodParameters(QualifiedName... parameterNames) {
		return providedUnbindingMethodParameters(Arrays.asList(parameterNames));
	}

	S providedUnbindingMethodParameters(String... parameterNames);
}
