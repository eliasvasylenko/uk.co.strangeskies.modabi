/*
 * Copyright (C) 2016 Elias N Vasylenko <eliasvasylenko@gmail.com>
 *
 * This file is part of uk.co.strangeskies.modabi.core.provider.
 *
 * uk.co.strangeskies.modabi.core.provider is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * uk.co.strangeskies.modabi.core.provider is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with uk.co.strangeskies.modabi.core.provider.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.co.strangeskies.modabi.impl.processing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.BindingException;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public abstract class InputNodeBinder<T extends InputNode.Effective<?, ?>>
		extends ChildNodeBinder<T> {
	public InputNodeBinder(BindingContextImpl context, T node) {
		super(context, node);
	}

	protected Object invokeInMethod(Object... parameters) {
		TypedObject<?> target = getContext().bindingTarget();

		TypedObject<?> result;

		if (!"null".equals(getNode().getInMethodName())) {
			try {
				result = newTypedObject(getNode().getPostInputType(),
						((Method) getNode().getInMethod()).invoke(target.getObject(),
								parameters));
			} catch (IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | SecurityException e) {
				throw new BindingException("Unable to call method '"
						+ getNode().getInMethod() + "' with parameters '"
						+ Arrays.toString(parameters) + "' at node '" + getNode() + "'",
						getContext(), e);
			}

			if (getNode().isInMethodChained()) {
				setContext(getContext().withReplacementBindingTarget(result));
				target = result;
			}
		} else {
			result = null;
		}

		return target;
	}

	@SuppressWarnings("unchecked")
	private <U> TypedObject<U> newTypedObject(TypeToken<U> type, Object object) {
		return new TypedObject<>(type, (U) object);
	}
}
