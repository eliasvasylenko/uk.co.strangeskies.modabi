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
import uk.co.strangeskies.reflection.TypeLiteral;
import uk.co.strangeskies.utilities.PropertySet;

public interface BindingNode<T, S extends BindingNode<T, S, E>, E extends BindingNode.Effective<T, S, E>>
		extends SchemaNode<S, E> {
	interface Effective<T, S extends BindingNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingNode<T, S, E>, SchemaNode.Effective<S, E> {
		Executable getUnbindingMethod();

		List<DataNode.Effective<?>> getProvidedUnbindingMethodParameters();

		@Override
		default PropertySet<E> effectivePropertySet() {
			return SchemaNode.Effective.super.effectivePropertySet()
					.add(BindingNode.Effective::getUnbindingMethod)
					.add(BindingNode.Effective::getProvidedUnbindingMethodParameters);
		}
	}

	@Override
	default PropertySet<S> propertySet() {
		return SchemaNode.super.propertySet().add(BindingNode::getDataType)
				.add(BindingNode::isAbstract).add(BindingNode::getBindingStrategy)
				.add(BindingNode::getBindingType)
				.add(BindingNode::getUnbindingStrategy)
				.add(BindingNode::getUnbindingType)
				.add(BindingNode::getUnbindingMethodName)
				.add(BindingNode::getUnbindingFactoryType)
				.add(BindingNode::getProvidedUnbindingMethodParameterNames);
	}

	TypeLiteral<? extends T> getDataType();

	BindingStrategy getBindingStrategy();

	Type getBindingType();

	UnbindingStrategy getUnbindingStrategy();

	Type getUnbindingType();

	String getUnbindingMethodName();

	Type getUnbindingFactoryType();

	List<QualifiedName> getProvidedUnbindingMethodParameterNames();
}
