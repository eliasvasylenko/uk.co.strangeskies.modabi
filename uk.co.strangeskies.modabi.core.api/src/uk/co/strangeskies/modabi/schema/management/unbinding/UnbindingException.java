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
package uk.co.strangeskies.modabi.schema.management.unbinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public class UnbindingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final UnbindingState state;

	private final Collection<? extends Exception> multiCause;

	public UnbindingException(String message, UnbindingState state,
			Collection<? extends Exception> cause) {
		super(message + " @ "
				+ getUnbindingNodeStackString(state.bindingNodeStack()), cause
				.iterator().next());

		multiCause = cause;
		this.state = state;
	}

	public UnbindingException(String message, UnbindingState state,
			Exception cause) {
		super(message + " @ "
				+ getUnbindingNodeStackString(state.bindingNodeStack()), cause);

		multiCause = Arrays.asList(cause);
		this.state = state;
	}

	public UnbindingException(String message, UnbindingState state) {
		super(message + " @ "
				+ getUnbindingNodeStackString(state.bindingNodeStack()));
		multiCause = null;
		this.state = state;
	}

	public Collection<? extends Exception> getMultipleCauses() {
		return multiCause;
	}

	private static String getUnbindingNodeStackString(
			List<SchemaNode.Effective<?, ?>> stack) {
		stack = new ArrayList<>(stack);
		Collections.reverse(stack);

		return "[ "
				+ stack.stream().map(SchemaNode::getName).map(Objects::toString)
						.collect(Collectors.joining(" < ")) + " ]";
	}

	public UnbindingState getState() {
		return state;
	}
}
