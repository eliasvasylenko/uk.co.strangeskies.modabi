package uk.co.strangeskies.modabi.schema.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;

public class UnbindingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final List<SchemaNode.Effective<?, ?>> bindingNodeStack;

	public UnbindingException(String message,
			List<SchemaNode.Effective<?, ?>> stack, Exception cause) {
		super(message + " @ " + getUnbindingNodeStackString(stack), cause);

		bindingNodeStack = Collections.unmodifiableList(new ArrayList<>(stack));
	}

	private static String getUnbindingNodeStackString(
			List<SchemaNode.Effective<?, ?>> stack) {
		stack = new ArrayList<>(stack);
		Collections.reverse(stack);

		return "[ "
				+ stack.stream().map(SchemaNode::getName).map(Objects::toString)
						.collect(Collectors.joining(" < ")) + " ]";
	}

	public UnbindingException(String message,
			List<SchemaNode.Effective<?, ?>> bindingNodeStack) {
		super(message + " @ " + getUnbindingNodeStackString(bindingNodeStack));

		this.bindingNodeStack = Collections.unmodifiableList(new ArrayList<>(
				bindingNodeStack));
	}

	public List<SchemaNode.Effective<?, ?>> getBindingNodeStack() {
		return bindingNodeStack;
	}
}