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
package uk.co.strangeskies.modabi.schema.node;

import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;

import uk.co.strangeskies.modabi.namespace.QualifiedName;
import uk.co.strangeskies.modabi.schema.management.binding.BindingStrategy;
import uk.co.strangeskies.modabi.schema.management.unbinding.UnbindingStrategy;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.utilities.PropertySet;

public interface BindingNode<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNode<S, E> {
	interface Effective<T, S extends BindingNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingNode<T, S, E>, SchemaNode.Effective<S, E> {
		Executable getUnbindingMethod();

		List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters();

		@SuppressWarnings("rawtypes")
		static final PropertySet<BindingNode.Effective> PROPERTY_SET = new PropertySet<>(
				BindingNode.Effective.class).add(BindingNode.PROPERTY_SET)
				.add(SchemaNode.Effective.PROPERTY_SET)
				.add(BindingNode.Effective::getUnbindingMethod)
				.add(BindingNode.Effective::getProvidedUnbindingMethodParameters);

		@Override
		default PropertySet<? super E> effectivePropertySet() {
			return PROPERTY_SET;
		}

		TypeToken<T> inferExactDataType();
	}

	@SuppressWarnings("rawtypes")
	static final PropertySet<BindingNode> PROPERTY_SET = new PropertySet<>(
			BindingNode.class).add(SchemaNode.PROPERTY_SET)
			.add(BindingNode::getDataType).add(BindingNode::isAbstract)
			.add(BindingNode::getBindingStrategy).add(BindingNode::getBindingType)
			.add(BindingNode::getUnbindingStrategy)
			.add(BindingNode::getUnbindingType)
			.add(BindingNode::isUnbindingMethodUnchecked)
			.add(BindingNode::getUnbindingMethodName)
			.add(BindingNode::getUnbindingFactoryType)
			.add(BindingNode::getProvidedUnbindingMethodParameterNames);

	@Override
	default PropertySet<? super S> propertySet() {
		return PROPERTY_SET;
	}

	TypeToken<T> getDataType();

	BindingStrategy getBindingStrategy();

	Type getBindingType();

	UnbindingStrategy getUnbindingStrategy();

	Type getUnbindingType();

	String getUnbindingMethodName();

	Boolean isUnbindingMethodUnchecked();

	Type getUnbindingFactoryType();

	List<QualifiedName> getProvidedUnbindingMethodParameterNames();
}
