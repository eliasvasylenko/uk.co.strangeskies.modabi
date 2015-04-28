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

import uk.co.strangeskies.utilities.PropertySet;

public interface InputNode<S extends InputNode<S, E>, E extends InputNode.Effective<S, E>>
		extends ChildNode<S, E> {
	interface Effective<S extends InputNode<S, E>, E extends Effective<S, E>>
			extends InputNode<S, E>, ChildNode.Effective<S, E> {
		Executable getInMethod();

		@SuppressWarnings("rawtypes")
		static final PropertySet<InputNode.Effective> PROPERTY_SET = new PropertySet<>(
				InputNode.Effective.class).add(InputNode.PROPERTY_SET)
				.add(ChildNode.Effective.PROPERTY_SET)
				.add(InputNode.Effective::getInMethod);

		@Override
		default PropertySet<? super E> effectivePropertySet() {
			return PROPERTY_SET;
		}
	}

	@SuppressWarnings("rawtypes")
	static final PropertySet<InputNode> PROPERTY_SET = new PropertySet<>(
			InputNode.class).add(ChildNode.PROPERTY_SET)
			.add(InputNode::getInMethodName).add(InputNode::isInMethodChained)
			.add(InputNode::isInMethodUnchecked).add(InputNode::isInMethodCast);

	@Override
	default PropertySet<? super S> propertySet() {
		return PROPERTY_SET;
	}

	Boolean isInMethodUnchecked();

	String getInMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean isInMethodChained();

	Boolean isInMethodCast();
}
