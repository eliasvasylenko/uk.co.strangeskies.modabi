package uk.co.strangeskies.modabi.schema.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.model.nodes.SchemaNode;

public class BindingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final List<SchemaNode.Effective<?, ?>> bindingNodeStack;

	public BindingException(String message,
			List<SchemaNode.Effective<?, ?>> bindingNodeStack, Exception cause) {
		super(message
				+ " @ [ "
				+ bindingNodeStack.stream().map(SchemaNode::getName)
						.map(Objects::toString).collect(Collectors.joining(" < ")) + " ]",
				cause);

		this.bindingNodeStack = Collections.unmodifiableList(new ArrayList<>(
				bindingNodeStack));
	}

	public BindingException(String message,
			List<SchemaNode.Effective<?, ?>> bindingNodeStack) {
		super(message
				+ " @ [ "
				+ bindingNodeStack.stream().map(SchemaNode::getName)
						.map(Objects::toString).collect(Collectors.joining(" < ")) + " ]");

		this.bindingNodeStack = Collections.unmodifiableList(new ArrayList<>(
				bindingNodeStack));
	}

	public List<SchemaNode.Effective<?, ?>> getBindingNodeStack() {
		return bindingNodeStack;
	}
}
