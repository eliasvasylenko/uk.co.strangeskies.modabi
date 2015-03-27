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

import java.lang.reflect.Method;

import uk.co.strangeskies.mathematics.Range;
import uk.co.strangeskies.utilities.PropertySet;

public interface BindingChildNode<T, S extends BindingChildNode<T, S, E>, E extends BindingChildNode.Effective<T, S, E>>
		extends BindingNode<T, S, E>, InputNode<S, E> {
	interface Effective<T, S extends BindingChildNode<T, S, E>, E extends Effective<T, S, E>>
			extends BindingChildNode<T, S, E>, BindingNode.Effective<T, S, E>,
			InputNode.Effective<S, E> {
		Method getOutMethod();

		@Override
		default PropertySet<E> effectivePropertySet() {
			return BindingNode.Effective.super.effectivePropertySet()
					.add(BindingChildNode.Effective::getOutMethod)
					.add(InputNode.Effective::getInMethod);
		}
	}

	@Override
	default PropertySet<S> propertySet() {
		return BindingNode.super.propertySet()
				.add(BindingChildNode::getOutMethodName)
				.add(BindingChildNode::isOutMethodIterable)
				.add(BindingChildNode::occurrences).add(BindingChildNode::isOrdered)
				.add(BindingChildNode::isExtensible).add(InputNode::getInMethodName)
				.add(InputNode::isInMethodChained).add(InputNode::isInMethodCast);
	}

	String getOutMethodName();

	/**
	 * If this method returns true, the return value of any invocation of the
	 * inMethod will replace the build class of any
	 *
	 * @return
	 */
	Boolean isOutMethodIterable();

	/**
	 * Default behaviour is as if 1..1.
	 *
	 * @return
	 */
	Range<Integer> occurrences();

	/**
	 * Default behaviour is as if true. If unordered, may input concurrently, and
	 * semantics of updating existing binding are more flexible. Also note that
	 * unordered nodes may bind and unbind with less memory-efficiency...
	 *
	 * @return
	 */
	Boolean isOrdered();

	Boolean isExtensible();
}
