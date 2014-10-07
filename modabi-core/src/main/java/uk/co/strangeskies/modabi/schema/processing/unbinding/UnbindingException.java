package uk.co.strangeskies.modabi.schema.processing.unbinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import uk.co.strangeskies.modabi.schema.SchemaException;
import uk.co.strangeskies.modabi.schema.node.SchemaNode;
import uk.co.strangeskies.modabi.schema.processing.unbinding.impl.UnbindingContext;

public class UnbindingException extends SchemaException {
	private static final long serialVersionUID = 1L;

	private final UnbindingContext context;

	private final Collection<? extends Exception> multiCause;

	public UnbindingException(String message, UnbindingContext context,
			Collection<? extends Exception> cause) {
		super(message + " @ "
				+ getUnbindingNodeStackString(context.unbindingNodeStack()), cause
				.iterator().next());

		multiCause = cause;

		this.context = context;
	}

	public UnbindingException(String message, UnbindingContext context,
			Exception cause) {
		super(message + " @ "
				+ getUnbindingNodeStackString(context.unbindingNodeStack()), cause);

		multiCause = Arrays.asList(cause);

		this.context = context;
	}

	public UnbindingException(String message, UnbindingContext context) {
		super(message + " @ "
				+ getUnbindingNodeStackString(context.unbindingNodeStack()));
		multiCause = null;
		this.context = context;
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

	public UnbindingContext getContext() {
		return context;
	}
}
