package uk.co.strangeskies.modabi.schema.processing.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;

public class BindingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final BindingState state;

	public BindingException(String message, BindingState state, Exception cause) {
		super(
				message + " @ " + getBindingNodeStackString(state.bindingNodeStack()),
				cause);

		this.state = state;
	}

	public BindingException(String message, BindingState state) {
		super(message + " @ " + getBindingNodeStackString(state.bindingNodeStack()));

		this.state = state;
	}

	public BindingState getState() {
		return state;
	}

	private static String getBindingNodeStackString(
			List<SchemaNode.Effective<?, ?>> stack) {
		stack = new ArrayList<>(stack);
		Collections.reverse(stack);

		return "[ "
				+ stack.stream().map(SchemaNode::getName).map(Objects::toString)
						.collect(Collectors.joining(" < ")) + " ]";
	}
}
