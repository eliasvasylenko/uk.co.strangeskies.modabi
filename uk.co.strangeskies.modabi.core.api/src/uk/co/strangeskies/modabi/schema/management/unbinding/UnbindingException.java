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