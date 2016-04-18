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

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public abstract class InputNodeBinder<T extends InputNode.Effective<?, ?>> extends ChildNodeBinder<T> {
	public InputNodeBinder(ProcessingContext context, T node) {
		super(context, node);
	}

	protected Object invokeInMethod(Object... parameters) {
		TypedObject<?> target = getContext().getBindingObject();

		if (!"null".equals(getNode().getInMethodName())) {
			TypedObject<?> result;

			try {
				TypeToken<?> postInputType = getNode().getPostInputType();
				if (postInputType == null) {
					if (getNode().isInMethodChained()) {
						postInputType = getNode().getInMethod().withTargetType(target.getType()).getReturnType();
					} else {
						postInputType = target.getType();
					}
				}

				result = TypedObject.castInto(postInputType,
						((Method) getNode().getInMethod().getExecutable()).invoke(target.getObject(), parameters));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
				throw new ProcessingException(
						"Unable to call method '" + getNode().getInMethod() + "' with parameters '" + Arrays.toString(parameters),
						getContext(), e);
			}

			if (getNode().isInMethodChained()) {
				setContext(getContext().withReplacementBindingObject(result));
				target = result;
			}
		}

		return target;
	}
}
