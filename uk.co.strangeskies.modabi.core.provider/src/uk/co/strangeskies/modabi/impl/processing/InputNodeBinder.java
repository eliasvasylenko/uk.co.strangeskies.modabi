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

import static uk.co.strangeskies.modabi.schema.InputNode.noInMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

import uk.co.strangeskies.modabi.processing.ProcessingContext;
import uk.co.strangeskies.modabi.processing.ProcessingException;
import uk.co.strangeskies.modabi.schema.InputNode;
import uk.co.strangeskies.reflection.TypeToken;
import uk.co.strangeskies.reflection.TypedObject;

public abstract class InputNodeBinder<T extends InputNode<?>> extends ChildNodeBinder<T> {
	public InputNodeBinder(ProcessingContext context, T node) {
		super(context, node);
	}

	protected Object invokeInMethod(Object... parameters) {
		TypedObject<?> target = getContext().getBindingObject();

		if (!noInMethod().equals(getNode().inMethod())) {
			TypedObject<?> result;

			try {
				TypeToken<?> postInputType = getNode().postInputType();
				if (postInputType == null) {
					if (getNode().inMethodChained()) {
						postInputType = getNode().inMethod().withTargetType(target.getType()).getReturnType();
					} else {
						postInputType = target.getType();
					}
				}

				result = TypedObject.castInto(postInputType,
						((Method) getNode().inMethod().getExecutable()).invoke(target.getObject(), parameters));
			} catch (Exception e) {
				throw new ProcessingException(t -> t.cannotInvoke(getNode().inMethod().getExecutable(),
						getContext().getBindingObject().getType(), getNode(), Arrays.asList(parameters)), getContext(), e);
			}

			if (getNode().inMethodChained()) {
				setContext(getContext().withReplacementBindingObject(result));
				target = result;
			}
		}

		return target;
	}
}
