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

public class UnbindingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final UnbindingState state;

	private final Collection<? extends Exception> multiCause;

	public UnbindingException(String message, UnbindingState state,
			Collection<? extends Exception> cause) {
		super(message + getUnbindingStateString(state), cause.iterator().next());

		multiCause = cause;
		this.state = state;
	}

	public UnbindingException(String message, UnbindingState state,
			Exception cause) {
		super(message + getUnbindingStateString(state), cause);

		multiCause = Arrays.asList(cause);
		this.state = state;
	}

	public UnbindingException(String message, UnbindingState state) {
		super(message + getUnbindingStateString(state));
		multiCause = null;
		this.state = state;
	}

	public Collection<? extends Exception> getMultipleCauses() {
		return multiCause;
	}

	private static String getUnbindingStateString(UnbindingState state) {
		return getNodeContext(state) + " from unbinding source object '"
				+ state.unbindingSource() + "'";
	}

	private static String getNodeContext(UnbindingState state) {
		String nodeContext;

		if (state.unbindingNodeStack().isEmpty()) {
			nodeContext = " at root";
		} else {
			nodeContext = " at node '" + state.unbindingNode().getName() + "'";

			if (state.unbindingNodeStack().size() > 1) {
				nodeContext = nodeContext + " at node '" + (state.unbindingNodeStack()
						.get(state.unbindingNodeStack().size() - 2)).getName() + "'";
			}
		}

		return nodeContext;
	}

	public UnbindingState getState() {
		return state;
	}
}
