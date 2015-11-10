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
package uk.co.strangeskies.modabi.processing;

import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.modabi.SchemaException;

public class BindingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final BindingState state;

	private final Collection<? extends Exception> multiCause;

	public BindingException(String message, BindingState state,
			Collection<? extends Exception> cause) {
		super(message + getBindingStateString(state), cause.iterator().next());

		multiCause = cause;
		this.state = state;
	}

	public BindingException(String message, BindingState state, Exception cause) {
		super(message + getBindingStateString(state), cause);

		multiCause = Arrays.asList(cause);
		this.state = state;
	}

	public BindingException(String message, BindingState state) {
		super(message + getBindingStateString(state));

		multiCause = null;
		this.state = state;
	}

	public Collection<? extends Exception> getMultipleCauses() {
		return multiCause;
	}

	private static String getBindingStateString(BindingState state) {
		/*
		 * binding target may be proxied, and so throw an exception...
		 */
		String bindingTarget;
		try {
			bindingTarget = state.bindingTarget().toString();
		} catch (Exception e) {
			bindingTarget = "Unknown";
		}

		return state.bindingNodeStack().isEmpty() ? " at root"
				: " at node '" + state.bindingNode().getName()
						+ "' with binding target object '" + bindingTarget + "'";
	}

	public BindingState getState() {
		return state;
	}
}
