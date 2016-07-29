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

public interface BindingNodeConfigurator<S extends BindingNodeConfigurator<S, N, T>, N extends BindingNode<T, N>, T>
		extends SchemaNodeConfigurator<S, N> {
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

	TypeToken<T> getDataType();

	S inputBindingStrategy(InputBindingStrategy strategy);

	InputBindingStrategy getInputBindingStrategy();

	S inputBindingType(String bindingType);

	S inputBindingType(TypeToken<?> bindingType);

	default S inputBindingType(AnnotatedType bindingType) {
		return inputBindingType(TypeToken.over(bindingType));
	}

	default S inputBindingType(Type bindingType) {
		return inputBindingType(TypeToken.over(AnnotatedTypes.over(bindingType)));
	}

	TypeToken<?> getInputBindingType();

	S outputBindingStrategy(OutputBindingStrategy strategy);

	OutputBindingStrategy getOutputBindingStrategy();

	S outputBindingFactoryType(String factoryType);

	S outputBindingFactoryType(TypeToken<?> factoryType);

	default S outputBindingFactoryType(AnnotatedType factoryType) {
		return outputBindingFactoryType(TypeToken.over(factoryType));
	}

	default S outputBindingFactoryType(Type factoryType) {
		return outputBindingFactoryType(TypeToken.over(AnnotatedTypes.over(factoryType)));
	}

	TypeToken<?> getOutputBindingFactoryType();

	S outputBindingType(String unbindingType);

	S outputBindingType(TypeToken<?> unbindingType);

	default S outputBindingType(AnnotatedType unbindingType) {
		return outputBindingType(TypeToken.over(unbindingType));
	}

	default S outputBindingType(Type bindingType) {
		return outputBindingType(TypeToken.over(AnnotatedTypes.over(bindingType)));
	}

	TypeToken<?> getOutputBindingType();

	S outputBindingMethod(String unbindingMethod);

	String getOutputBindingMethod();

	S outputBindingMethodUnchecked(boolean unchecked);

	Boolean getOutputBindingMethodUnchecked();

	S providedOutputBindingMethodParameters(List<QualifiedName> parameterNames);

	default S providedOutputBindingMethodParameters(QualifiedName... parameterNames) {
		return providedOutputBindingMethodParameters(Arrays.asList(parameterNames));
	}

	/*
	 * TODO Replace this with a model based on (forgotten the term I made up for
	 * it ...) static references where the reference target node is resolved at
	 * schema loading time, rather than schema application time like normal
	 * references. "modabi.namespace:this" can be a special reference id for the
	 * direct parent node.
	 * 
	 * It may also be possible to solve certain other issues in this manner.
	 * 
	 */
	S providedOutputBindingMethodParameters(String... parameterNames);

	List<QualifiedName> getProvidedOutputBindingMethodParameters();
}
