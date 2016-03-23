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
package uk.co.strangeskies.modabi.processing;

import java.util.Arrays;
import java.util.Collection;

import uk.co.strangeskies.modabi.SchemaException;

public class ProcessingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final ProcessingContext state;

	private final Collection<? extends Exception> multiCause;

	public ProcessingException(String message, ProcessingContext state, Collection<? extends Exception> cause) {
		super(message + getBindingStateString(state), cause.iterator().next());

		multiCause = cause;
		this.state = state;
	}

	public ProcessingException(String message, ProcessingContext state, Exception cause) {
		super(message + getBindingStateString(state), cause);

		multiCause = Arrays.asList(cause);
		this.state = state;
	}

	public ProcessingException(String message, ProcessingContext state) {
		super(message + getBindingStateString(state));

		multiCause = null;
		this.state = state;
	}

	public Collection<? extends Exception> getMultipleCauses() {
		return multiCause;
	}

	private static String getBindingStateString(ProcessingContext state) {
		/*
		 * binding target may be proxied, and so throw an exception...
		 */
		String bindingTarget;
		try {
			bindingTarget = state.getBindingObject().toString();
		} catch (Exception e) {
			bindingTarget = "Unknown";
		}

		return getNodeContext(state) + " with binding object '" + bindingTarget + "'";
	}

	private static String getNodeContext(ProcessingContext state) {
		String nodeContext;

		if (state.getBindingNodeStack().isEmpty()) {
			nodeContext = " at root";
		} else {
			nodeContext = " at node '" + state.getBindingNode().getName() + "'";

			if (state.getBindingNodeStack().size() > 1) {
				nodeContext = nodeContext + " at node '"
						+ (state.getBindingNodeStack().get(state.getBindingNodeStack().size() - 2)).getName() + "'";
			}
		}

		return nodeContext;
	}

	public ProcessingContext getState() {
		return state;
	}
}
